package com.kibria.dynamicalarm;

import android.app.*;
import android.content.*;
import android.os.Build;

public final class Scheduler {
  public static boolean allowed(Context c) {
    return Build.VERSION.SDK_INT < 31
        || c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
  }

  static PendingIntent pending(Context c, int id) {
    return PendingIntent.getBroadcast(
        c,
        id,
        new Intent(c, AlarmReceiver.class).putExtra("id", id),
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  public static void cancel(Context c, int id) {
    c.getSystemService(AlarmManager.class).cancel(pending(c, id));
  }

  public static void schedule(Context c, Alarm a) {
    cancel(c, a.id);
    if (!a.enabled && a.snoozeAt == 0) return;
    if (!allowed(c)) throw new SecurityException("Allow Alarms & reminders in Setup first.");
    long time = a.snoozeAt > 0 ? a.snoozeAt : a.at;
    PendingIntent show =
        PendingIntent.getActivity(
            c,
            a.id,
            new Intent(c, MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    c.getSystemService(AlarmManager.class)
        .setAlarmClock(new AlarmManager.AlarmClockInfo(time, show), pending(c, a.id));
  }

  public static void restore(Context c) {
    long now = System.currentTimeMillis();
    for (Alarm a : Store.all(c)) {
      if (a.snoozeAt > 0 && a.snoozeAt <= now) a.snoozeAt = 0;
      if (a.enabled && a.days != 0) a.at = Times.next(a.hour, a.minute, a.days, now);
      else if (a.enabled && a.at <= now) {
        a.enabled = false;
        c.getSharedPreferences("alarms", 0)
            .edit()
            .putString(
                "notice",
                "An alarm passed while the phone was off or unavailable. Check your alarms below.")
            .apply();
      }
      Store.put(c, a);
      if (allowed(c)) {
        try {
          schedule(c, a);
        } catch (SecurityException ignored) {
        }
      }
    }
  }
}
