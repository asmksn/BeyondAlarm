package com.kibria.dynamicalarm;

import android.content.*;

public class AlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    Alarm a = Store.get(c, i.getIntExtra("id", -1));
    if (a == null || (!a.enabled && a.snoozeAt == 0)) return;
    long due = a.snoozeAt > 0 ? a.snoozeAt : a.at;
    if (due > System.currentTimeMillis() + 1500) return;
    a.snoozeAt = 0;
    if (a.days == 0) a.enabled = false;
    else a.at = Times.next(a.hour, a.minute, a.days, System.currentTimeMillis());
    Store.put(c, a);
    try {
      Scheduler.schedule(c, a);
    } catch (SecurityException ignored) {
    }
    c.startForegroundService(new Intent(c, AlarmService.class).putExtra("id", a.id));
  }
}
