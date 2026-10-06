package com.kibria.dynamicalarm;

import android.app.*;
import android.content.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import java.util.*;

public class AlarmService extends Service {
  public static final String CHANNEL = "ring_v1";
  private final LinkedHashMap<Integer, Alarm> active = new LinkedHashMap<>();
  private final Handler handler = new Handler();
  private final HashMap<Integer, Runnable> timeouts = new HashMap<>();
  private MediaPlayer player;
  private Vibrator vibrator;
  private PowerManager.WakeLock lock;
  public static int sounding = -1;
  public static final Set<Integer> ringing = new HashSet<>();

  public static void channels(Context c) {
    NotificationManager nm = c.getSystemService(NotificationManager.class);
    NotificationChannel channel =
        new NotificationChannel(CHANNEL, "Ringing alarms", NotificationManager.IMPORTANCE_HIGH);
    channel.setDescription("Alarm controls and lock-screen alerts");
    channel.setSound(null, null);
    channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    nm.createNotificationChannel(channel);
    nm.createNotificationChannel(
        new NotificationChannel("missed", "Missed alarms", NotificationManager.IMPORTANCE_DEFAULT));
  }

  @Override
  public void onCreate() {
    super.onCreate();
    channels(this);
    lock =
        getSystemService(PowerManager.class)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DynamicAlarm:ring");
    lock.acquire(11 * 60 * 1000L);
    vibrator = getSystemService(Vibrator.class);
  }

  private PendingIntent control(int id, String action) {
    return PendingIntent.getService(
        this,
        id,
        new Intent(this, AlarmService.class).setAction(action).putExtra("id", id),
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
  }

  private Notification notification(Alarm a) {
    Intent open =
        new Intent(this, RingActivity.class)
            .putExtra("id", a.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pi =
        PendingIntent.getActivity(
            this, a.id, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    return new Notification.Builder(this, CHANNEL)
        .setSmallIcon(com.kibria.dynamicalarm.R.drawable.ic_notification)
        .setContentTitle(a.title)
        .setContentText(Alarm.KINDS[a.kind] + " · Tap to open")
        .setCategory(Notification.CATEGORY_ALARM)
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setOngoing(true)
        .setContentIntent(pi)
        .setFullScreenIntent(pi, true)
        .addAction(
            new Notification.Action.Builder(null, "Snooze 5 min", control(a.id, "snooze")).build())
        .addAction(
            new Notification.Action.Builder(null, "Dismiss", control(a.id, "dismiss")).build())
        .build();
  }

  @Override
  public int onStartCommand(Intent i, int flags, int startId) {
    if (i == null) {
      stopSelf();
      return START_NOT_STICKY;
    }
    int id = i.getIntExtra("id", -1);
    String action = i.getAction();
    if ("dismiss".equals(action) || "snooze".equals(action)) {
      if ("snooze".equals(action)) {
        Alarm a = Store.get(this, id);
        if (a != null) {
          a.snoozeAt = System.currentTimeMillis() + 5 * 60 * 1000;
          try {
            Scheduler.schedule(this, a);
            Store.put(this, a);
          } catch (SecurityException e) {
            android.widget.Toast.makeText(this, "Snooze unavailable. Enable Alarms & reminders.", 1)
                .show();
            return START_NOT_STICKY;
          }
        }
      }
      finishAlarm(id);
      return START_NOT_STICKY;
    }
    Alarm a = Store.get(this, id);
    if (a == null) {
      if (active.isEmpty()) stopSelf();
      return START_NOT_STICKY;
    }
    boolean wasEmpty = active.isEmpty();
    active.put(id, a);
    ringing.add(id);
    if (lock.isHeld()) lock.release();
    lock.acquire(11 * 60 * 1000L);
    startForeground(
        active.keySet().iterator().next(), notification(active.values().iterator().next()));
    getSystemService(NotificationManager.class).notify(id, notification(a));
    if (wasEmpty) play(a);
    Runnable old = timeouts.remove(id);
    if (old != null) handler.removeCallbacks(old);
    Runnable timeout =
        () -> {
          getSystemService(NotificationManager.class)
              .notify(
                  id + 100000,
                  new Notification.Builder(this, "missed")
                      .setSmallIcon(R.drawable.ic_notification)
                      .setContentTitle("Missed: " + a.title)
                      .setContentText("Alarm stopped after 10 minutes.")
                      .setAutoCancel(true)
                      .setContentIntent(
                          PendingIntent.getActivity(
                              this,
                              id,
                              new Intent(this, MainActivity.class),
                              PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                      .build());
          finishAlarm(id);
        };
    timeouts.put(id, timeout);
    handler.postDelayed(timeout, 10 * 60 * 1000);
    return START_NOT_STICKY;
  }

  private void play(Alarm a) {
    stopSound();
    sounding = a.id;
    try {
      Uri uri =
          a.tone.isEmpty()
              ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
              : Uri.parse(a.tone);
      startPlayer(uri, a.volume);
    } catch (Exception e) {
      try {
        startPlayer(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), a.volume);
      } catch (Exception ignored) {
      }
    }
    if (a.vibrate && vibrator != null)
      vibrator.vibrate(
          VibrationEffect.createWaveform(new long[] {0, 500, 400, 500, 1200}, 0),
          new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
  }

  private void startPlayer(Uri uri, int volume) throws Exception {
    if (player != null) player.release();
    player = new MediaPlayer();
    player.setAudioAttributes(
        new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build());
    player.setDataSource(this, uri);
    player.setLooping(true);
    player.setVolume(volume / 100f, volume / 100f);
    player.prepare();
    player.start();
  }

  private void stopSound() {
    if (player != null) {
      player.release();
      player = null;
    }
    if (vibrator != null) vibrator.cancel();
    sounding = -1;
  }

  private void finishAlarm(int id) {
    active.remove(id);
    ringing.remove(id);
    Runnable r = timeouts.remove(id);
    if (r != null) handler.removeCallbacks(r);
    getSystemService(NotificationManager.class).cancel(id);
    sendBroadcast(
        new Intent("com.kibria.dynamicalarm.CLOSED")
            .setPackage(getPackageName())
            .putExtra("id", id));
    if (active.isEmpty()) {
      stopForeground(STOP_FOREGROUND_REMOVE);
      stopSelf();
    } else {
      Alarm next = active.values().iterator().next();
      startForeground(next.id, notification(next));
      if (sounding == id) play(next);
    }
  }

  @Override
  public void onDestroy() {
    ringing.clear();
    handler.removeCallbacksAndMessages(null);
    stopSound();
    if (lock != null && lock.isHeld()) lock.release();
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent i) {
    return null;
  }
}
