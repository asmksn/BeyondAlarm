package com.kibria.dynamicalarm.test;
import android.app.*;
import android.content.*;
import android.os.*;
import com.kibria.dynamicalarm.*;

public class StoreScreenshots extends Instrumentation {
  public void onCreate(Bundle b){ super.onCreate(b); start(); }
  private void capture(String name) throws Exception {
    SystemClock.sleep(1800);
    try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),name))){
      getUiAutomation().takeScreenshot().compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
    }
  }
  public void onStart(){Bundle result=new Bundle(); try {
    Context c=getTargetContext(); c.getSharedPreferences("alarms",0).edit().remove("notice").commit();
    Alarm morning=new Alarm(); morning.id=81001; morning.title="A fresh start"; morning.hour=7; morning.days=127; morning.quote="Success comes to those who have the willpower to win over their snooze buttons."; morning.at=Times.next(morning.hour,morning.minute,morning.days,System.currentTimeMillis()); Store.put(c,morning);
    Alarm travel=new Alarm(); travel.id=81002; travel.title="Time to head out"; travel.hour=9; travel.days=31; travel.kind=5; travel.target="23.8103,90.4125"; travel.targetName="Dhaka"; travel.at=Times.next(travel.hour,travel.minute,travel.days,System.currentTimeMillis()); Store.put(c,travel);
    Activity home=startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); capture("store-home.png");
    Activity ring=startActivitySync(new Intent(c,RingActivity.class).putExtra("id",morning.id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); capture("store-quote.png"); runOnMainSync(()->ring.finish());
    Activity privacy=startActivitySync(new Intent(c,PrivacyActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); capture("store-privacy.png");runOnMainSync(()->privacy.finish());
    Scheduler.cancel(c,morning.id);Scheduler.cancel(c,travel.id);Store.remove(c,morning.id);Store.remove(c,travel.id);runOnMainSync(()->home.finish());
    result.putString("stream","Store screenshots captured; runtime checks passed.\n");finish(Activity.RESULT_OK,result);
  }catch(Throwable e){result.putString("stream","FAILED: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}


