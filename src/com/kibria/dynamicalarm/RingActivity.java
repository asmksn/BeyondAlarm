package com.kibria.dynamicalarm;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

public class RingActivity extends Activity {
  private Alarm alarm;
  private boolean performing = false;
  private final BroadcastReceiver closed =
      new BroadcastReceiver() {
        public void onReceive(Context c, Intent i) {
          if (alarm != null && i.getIntExtra("id", -1) == alarm.id && !performing) finish();
        }
      };

  @Override
  public void onCreate(Bundle b) {
    super.onCreate(b);
    getWindow()
        .addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    if (Build.VERSION.SDK_INT >= 33)
      registerReceiver(
          closed,
          new IntentFilter("com.kibria.dynamicalarm.CLOSED"),
          Context.RECEIVER_NOT_EXPORTED);
    else registerReceiver(closed, new IntentFilter("com.kibria.dynamicalarm.CLOSED"));
    render();
    if (b != null && b.getBoolean("performing") && alarm != null && alarm.kind == 8) {
      performing = true;
      Weather.show(this, alarm.target, () -> finish());
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putBoolean("performing", performing);
    super.onSaveInstanceState(state);
  }

  @Override
  protected void onNewIntent(Intent i) {
    super.onNewIntent(i);
    setIntent(i);
    performing = false;
    render();
  }

  private void render() {
    alarm = Store.get(this, getIntent().getIntExtra("id", -1));
    if (alarm == null) {
      finish();
      return;
    }
    LinearLayout l = Ui.page(this);
    Ui.gap(l, 44);
    l.addView(Ui.text(this, "IT'S TIME", 14, Ui.TEAL));
    l.addView(
        Ui.title(
            this,
            android.text.format.DateFormat.getTimeFormat(this).format(new java.util.Date()),
            64));
    l.addView(Ui.title(this, alarm.title, 30));
    Ui.gap(l, 22);
    LinearLayout card = Ui.card(l, 0xffffffff);
    card.addView(Ui.text(this, Alarm.KINDS[alarm.kind], 14, Ui.TEAL));
    String detail = alarm.targetName.isEmpty() ? alarm.target : alarm.targetName;
    if (alarm.kind == 0) detail = alarm.quote;
    if (!detail.isEmpty()) card.addView(Ui.title(this, detail, 22));
    if (alarm.kind == 3 && !alarm.attachments.isEmpty())
      card.addView(Ui.text(this, alarm.attachments.size() + " attachment(s) ready", 15, Ui.MUTED));
    if (!alarm.body.isEmpty()) card.addView(Ui.text(this, alarm.body, 16, Ui.MUTED));
    Ui.gap(l, 24);
    Ui.button(l, Alarm.ACTIONS[alarm.kind], true, () -> unlockAndAct());
    Ui.button(l, "Snooze · 5 minutes", false, () -> stop("snooze"));
    Ui.button(l, "Dismiss", false, () -> stop("dismiss"));
    l.addView(Ui.text(this, "Your next step, right on time.", 14, Ui.MUTED));
  }

  private void unlockAndAct() {
    if (alarm.kind == 0) {
      stop("dismiss");
      return;
    }
    KeyguardManager km = getSystemService(KeyguardManager.class);
    if (Build.VERSION.SDK_INT >= 26 && km.isKeyguardLocked())
      km.requestDismissKeyguard(
          this,
          new KeyguardManager.KeyguardDismissCallback() {
            @Override
            public void onDismissSucceeded() {
              act();
            }
          });
    else act();
  }

  private void act() {
    performing = true;
    startService(
        new Intent(this, AlarmService.class).setAction("dismiss").putExtra("id", alarm.id));
    Actions.open(this, alarm, () -> finish());
  }

  private void stop(String action) {
    startService(new Intent(this, AlarmService.class).setAction(action).putExtra("id", alarm.id));
    finish();
  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(closed);
    super.onDestroy();
  }
}
