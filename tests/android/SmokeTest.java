package com.kibria.dynamicalarm.test;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.kibria.dynamicalarm.*;
import java.util.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import org.json.JSONObject;

/** Runs only on the disposable test emulator. It clears the target app's alarms. */
public class SmokeTest extends Instrumentation {
  private int assertions;
  private Context context;

  @Override
  public void onCreate(Bundle b) {
    super.onCreate(b);
    start();
  }

  private void check(boolean value, String message) {
    assertions++;
    if (!value) throw new AssertionError(message);
  }

  private boolean ringing(int id) {
    final boolean[] value = {false};
    runOnMainSync(() -> value[0] = AlarmService.ringing.contains(id));
    return value[0];
  }

  private Alarm alarm(String title, long delay) {
    Alarm a = new Alarm();
    a.id = Store.newId(context);
    a.title = title;
    a.at = System.currentTimeMillis() + delay;
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(a.at);
    a.hour = c.get(Calendar.HOUR_OF_DAY);
    a.minute = c.get(Calendar.MINUTE);
    return a;
  }

  private void waitRing(int id) {
    long deadline = SystemClock.elapsedRealtime() + 15000;
    while (!ringing(id) && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(100);
    check(ringing(id), "Alarm did not start foreground ringing: " + id);
  }

  private void control(int id, String action) {
    context.startService(
        new Intent(context, AlarmService.class).setAction(action).putExtra("id", id));
    SystemClock.sleep(500);
  }

  private boolean hasText(View view, String text) {
    if (view instanceof TextView && ((TextView) view).getText().toString().contains(text))
      return true;
    if (view instanceof ViewGroup) {
      ViewGroup g = (ViewGroup) view;
      for (int i = 0; i < g.getChildCount(); i++) if (hasText(g.getChildAt(i), text)) return true;
    }
    return false;
  }

  private View textView(View view,String text) {
    if(view instanceof TextView && text.equals(((TextView)view).getText().toString()))return view;
    if(view instanceof ViewGroup)for(int n=0;n<((ViewGroup)view).getChildCount();n++){View found=textView(((ViewGroup)view).getChildAt(n),text);if(found!=null)return found;}
    return null;
  }

  private boolean clickNode(AccessibilityNodeInfo node,String text) {
    if(node==null)return false;
    if(text.equalsIgnoreCase(node.getText()==null?"":node.getText().toString())&&node.isClickable())return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    for(int n=0;n<node.getChildCount();n++)if(clickNode(node.getChild(n),text))return true;
    return false;
  }

  private void capture(String name)throws Exception {
    waitForIdleSync();
    Bitmap image=getUiAutomation().takeScreenshot();
    if(image!=null)try(FileOutputStream out=new FileOutputStream(new File(context.getExternalFilesDir(null),name))){image.compress(Bitmap.CompressFormat.PNG,100,out);}
  }

  @Override
  public void onStart() {
    Bundle result = new Bundle();
    try {
      context = getTargetContext();
      for (Alarm a : Store.all(context)) {
        Scheduler.cancel(context, a.id);
        Store.remove(context, a.id);
      }
      Activity main =
          startActivitySync(
              new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      runOnMainSync(
          () ->
              check(
                  hasText(main.getWindow().getDecorView(), "Dynamic Alarm"),
                  "Home screen did not render"));
      check(Scheduler.allowed(context), "Exact alarm permission is required by this test");
      Alarm a = alarm("Round-trip: café বাংলা", 4000);
      a.kind = 3;
      a.target = "demo@example.com";
      a.body = "Line 1\nLine 2 & test";
      Store.put(context, a);
      Alarm loaded = Store.get(context, a.id);
      check(
          loaded.title.equals(a.title)
              && loaded.body.equals(a.body)
              && loaded.target.equals(a.target),
          "Persistence round trip failed");
      Scheduler.schedule(context, a);
      check(
          context.getSystemService(AlarmManager.class).getNextAlarmClock().getTriggerTime() == a.at,
          "OS schedule did not match requested time");
      waitRing(a.id);
      check(!Store.get(context, a.id).enabled, "One-time alarm stayed enabled after firing");
      control(a.id, "snooze");
      check(!ringing(a.id), "Snooze did not stop ringing");
      loaded = Store.get(context, a.id);
      long remaining = loaded.snoozeAt - System.currentTimeMillis();
      check(remaining > 290000 && remaining <= 300000, "Snooze was not five minutes");
      check(
          context.getSystemService(AlarmManager.class).getNextAlarmClock().getTriggerTime()
              == loaded.snoozeAt,
          "Snooze was not scheduled in Android");
      Scheduler.cancel(context, a.id);
      Store.remove(context, a.id);
      Alarm repeat = alarm("Repeat test", 2500);
      repeat.days = 127;
      Store.put(context, repeat);
      Scheduler.schedule(context, repeat);
      waitRing(repeat.id);
      loaded = Store.get(context, repeat.id);
      check(
          loaded.enabled && loaded.at > System.currentTimeMillis(),
          "Repeating alarm did not advance");
      Alarm second = alarm("Overlapping alarm", 2500);
      Store.put(context, second);
      Scheduler.schedule(context, second);
      waitRing(second.id);
      check(ringing(repeat.id), "New alarm lost the earlier alarm");
      control(repeat.id, "dismiss");
      check(!ringing(repeat.id) && ringing(second.id), "Dismiss affected the wrong alarm");
      final int[] sound = {-1};
      runOnMainSync(() -> sound[0] = AlarmService.sounding);
      check(sound[0] == second.id, "Sound did not switch to second alarm");
      control(second.id, "dismiss");
      check(!ringing(second.id), "Dismiss did not stop second alarm");
      Scheduler.cancel(context, repeat.id);
      Store.remove(context, repeat.id);
      Store.remove(context, second.id);
      Alarm missed = alarm("Expired test", -60000);
      Store.put(context, missed);
      Scheduler.restore(context);
      check(!Store.get(context, missed.id).enabled, "Expired one-time alarm was not deactivated");
      Store.remove(context, missed.id);
      runOnMainSync(() -> main.finish());

      Alarm legacy = Alarm.from(new JSONObject("{\"id\":999,\"title\":\"Old alarm\",\"kind\":0}"));
      check(legacy.quote.isEmpty()&&legacy.attachments.isEmpty(),"Old alarms did not migrate with empty new fields");
      check(MapActivity.parseCoordinates("91,0")==null && MapActivity.parseCoordinates("NaN,90")==null,"Invalid map coordinates accepted");
      double[] coords=MapActivity.parseCoordinates("23.810300,90.412500");
      check(coords!=null&&Math.abs(coords[0]-23.8103)<0.000001,"Map destination coordinates did not parse");

      Alarm email=alarm("Attachment persistence",60000);email.kind=3;email.target="example@example.com";email.subject="A & B";email.body="Two files";
      email.attachments.add(new Alarm.Attachment("content://test.documents/one","Plan.pdf","application/pdf"));
      email.attachments.add(new Alarm.Attachment("content://test.documents/two","Image.jpg","image/jpeg"));
      Store.put(context,email);Alarm savedEmail=Store.get(context,email.id);
      check(savedEmail.attachments.size()==2&&savedEmail.attachments.get(0).name.equals("Plan.pdf"),"Attachments did not persist");
      Intent share=EmailIntents.message(savedEmail);
      check(Intent.ACTION_SEND_MULTIPLE.equals(share.getAction()),"Multiple files use wrong email intent");
      check(share.getParcelableArrayListExtra(Intent.EXTRA_STREAM).size()==2&&share.getClipData().getItemCount()==2,"Attachment URI list or clip grants lost");
      check((share.getFlags()&Intent.FLAG_GRANT_READ_URI_PERMISSION)!=0,"Email lacks read URI grant");
      check(share.getStringArrayExtra(Intent.EXTRA_EMAIL)[0].equals(email.target)&&share.getStringExtra(Intent.EXTRA_SUBJECT).equals(email.subject),"Email details lost");
      savedEmail.attachments.remove(1);share=EmailIntents.message(savedEmail);
      check(Intent.ACTION_SEND.equals(share.getAction())&&"application/pdf".equals(share.getType()),"Single attachment MIME/action invalid");
      savedEmail.attachments.clear();check(Intent.ACTION_SENDTO.equals(EmailIntents.message(savedEmail).getAction()),"Plain email no longer uses email composer");Store.remove(context,email.id);

      Alarm quoteAlarm=alarm("Fajr prayer",60000);quoteAlarm.quote=Quotes.TEXT[2];Store.put(context,quoteAlarm);
      check(Store.get(context,quoteAlarm.id).quote.equals("Wake up and break Satan's knots!"),"Fajr quote not preserved");
      Activity ring=startActivitySync(new Intent(context,RingActivity.class).putExtra("id",quoteAlarm.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      final boolean[] visible={false};runOnMainSync(()->visible[0]=hasText(ring.getWindow().getDecorView(),quoteAlarm.quote));check(visible[0],"Quote did not appear on alarm screen");capture("quote-v1.2.png");runOnMainSync(()->ring.finish());
      Scheduler.schedule(context,quoteAlarm);
      Activity home=startActivitySync(new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      runOnMainSync(()->visible[0]=hasText(home.getWindow().getDecorView(),"Your alarm, with a purpose, by Kibria"));check(visible[0],"Requested footer not present");
      capture("home-v1.2.png");
      final View[] delete={null};runOnMainSync(()->delete[0]=textView(home.getWindow().getDecorView(),"Delete alarm"));check(delete[0]!=null,"Home delete button missing");
      runOnMainSync(()->delete[0].performClick());waitForIdleSync();SystemClock.sleep(400);
      boolean confirmed=false;long until=SystemClock.elapsedRealtime()+10000;
      while(!confirmed&&SystemClock.elapsedRealtime()<until){confirmed=clickNode(getUiAutomation().getRootInActiveWindow(),"Delete");if(!confirmed)SystemClock.sleep(250);}
      check(confirmed,"Delete confirmation inaccessible");waitForIdleSync();SystemClock.sleep(400);
      check(Store.get(context,quoteAlarm.id)==null,"Home delete did not remove saved alarm");
      check(context.getSystemService(AlarmManager.class).getNextAlarmClock()==null,"Deleted alarm still scheduled");
      runOnMainSync(()->home.finish());
      result.putString(
          "stream",
          assertions
              + " Android runtime checks passed: launch, persistence, exact trigger, service,"
              + " snooze, repeat, overlap, dismiss, recovery, migration, quotes, home deletion, attachment intents, map coordinates.\n");
      finish(Activity.RESULT_OK, result);
    } catch (Throwable error) {
      result.putString("stream", "FAILED: " + android.util.Log.getStackTraceString(error));
      finish(Activity.RESULT_CANCELED, result);
    }
  }
}
