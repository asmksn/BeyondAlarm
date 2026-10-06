package com.kibria.dynamicalarm.test;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.kibria.dynamicalarm.*;

public class PromoScreenshots extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 private void capture(String name)throws Exception{
  SystemClock.sleep(1300);
  try(java.io.FileOutputStream f=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),name))){getUiAutomation().takeScreenshot().compress(android.graphics.Bitmap.CompressFormat.PNG,100,f);}
 }
 private View find(View v,String text){if(v instanceof TextView && text.equals(((TextView)v).getText().toString()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View r=find(g.getChildAt(i),text);if(r!=null)return r;}}return null;}
 public void onStart(){Bundle result=new Bundle();try{
  Context c=getTargetContext(); Activity a=startActivitySync(new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  Alarm email=new Alarm();email.title="Send the project update";email.hour=9;email.kind=3;email.target="team@example.com";email.subject="Project update";email.body="Hi team, here is the latest update.";
  java.io.File file=new java.io.File(c.getFilesDir(),"Project-notes.txt");try(java.io.FileOutputStream f=new java.io.FileOutputStream(file)){f.write("Demo project notes for the app preview.".getBytes("UTF-8"));}
  email.attachments.add(new Alarm.Attachment(android.net.Uri.fromFile(file).toString(),"Project-notes.txt","text/plain"));
  java.lang.reflect.Method editor=MainActivity.class.getDeclaredMethod("editor",Alarm.class);editor.setAccessible(true);
  runOnMainSync(()->{try{editor.invoke(a,email);}catch(Exception e){throw new RuntimeException(e);}});SystemClock.sleep(700);
  runOnMainSync(()->{View v=find(a.getWindow().getDecorView(),"Subject (optional)");android.graphics.Rect rect=new android.graphics.Rect();v.getDrawingRect(rect);v.requestRectangleOnScreen(rect,true);});
  java.lang.reflect.Field field=MainActivity.class.getDeclaredField("actionFields");field.setAccessible(true);View fields=(View)field.get(a);
  runOnMainSync(()->{ScrollView scroll=(ScrollView)fields.getParent().getParent();scroll.scrollTo(0,fields.getTop()+find(fields,"Subject (optional)").getTop());});capture("promo-email.png");
  runOnMainSync(()->{ScrollView scroll=(ScrollView)fields.getParent().getParent();scroll.scrollTo(0,fields.getTop()-140);});
  runOnMainSync(()->{ViewGroup root=(ViewGroup)fields.getParent();for(int i=0;i<root.getChildCount();i++)if(root.getChildAt(i) instanceof Spinner){root.getChildAt(i).performClick();break;}});capture("promo-actions.png");
  sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);runOnMainSync(()->a.finish());file.delete();
  result.putString("stream","Promo captures ready; runtime checks passed. No message sent.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){result.putString("stream","FAILED: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}
