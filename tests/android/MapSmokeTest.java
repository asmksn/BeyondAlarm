package com.kibria.dynamicalarm.test;
import android.app.*;
import android.content.*;
import android.os.*;
import android.webkit.WebView;
import com.kibria.dynamicalarm.MapActivity;
import java.util.concurrent.*;

public class MapSmokeTest extends Instrumentation {
  private boolean has(android.view.accessibility.AccessibilityNodeInfo node,String text){if(node==null)return false;if(node.getText()!=null&&node.getText().toString().contains(text))return true;for(int i=0;i<node.getChildCount();i++)if(has(node.getChild(i),text))return true;return false;}
  private boolean click(android.view.accessibility.AccessibilityNodeInfo node,String text){
    if(node==null)return false;
    if(node.getText()!=null&&node.getText().toString().contains(text)){
      android.graphics.Rect bounds=new android.graphics.Rect();node.getBoundsInScreen(bounds);
      long t=SystemClock.uptimeMillis();
      android.view.MotionEvent down=android.view.MotionEvent.obtain(t,t,0,bounds.centerX(),bounds.centerY(),0);
      android.view.MotionEvent up=android.view.MotionEvent.obtain(t,t+80,1,bounds.centerX(),bounds.centerY(),0);
      boolean accepted=getUiAutomation().injectInputEvent(down,true);getUiAutomation().injectInputEvent(up,true);down.recycle();up.recycle();return accepted;
    }
    for(int i=0;i<node.getChildCount();i++)if(click(node.getChild(i),text))return true;
    return false;
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);start();}
  @Override public void onStart(){Bundle result=new Bundle();try{
    Activity activity=startActivitySync(new Intent(getTargetContext(),MapActivity.class).putExtra("destination","").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    SystemClock.sleep(5000);
    CountDownLatch latch=new CountDownLatch(1);String[] info={""};
    runOnMainSync(()->{WebView map=activity.findViewById(MapActivity.MAP_VIEW);map.evaluateJavascript("typeof L === 'object' && document.querySelectorAll('.leaflet-tile-loaded').length > 0",value->{info[0]=value;latch.countDown();});});
    latch.await(8,TimeUnit.SECONDS);if(!"true".equals(info[0]))throw new AssertionError("Map library or tiles failed to load: "+info[0]);
    runOnMainSync(()->{((android.widget.EditText)activity.findViewById(MapActivity.SEARCH_FIELD)).setText("Dhaka");activity.findViewById(MapActivity.SEARCH_BUTTON).performClick();});
    long until=SystemClock.elapsedRealtime()+18000;boolean found=false;
    while(!found&&SystemClock.elapsedRealtime()<until){found=has(getUiAutomation().getRootInActiveWindow(),"Choose a place");if(!found)SystemClock.sleep(250);}
    if(!found)throw new AssertionError("Live place search did not return a result chooser");
    boolean selected=false; long selectUntil=SystemClock.elapsedRealtime()+5000; while(!selected && SystemClock.elapsedRealtime()<selectUntil){selected=click(getUiAutomation().getRootInActiveWindow(),"Dhaka");if(!selected)SystemClock.sleep(250);} if(!selected){android.graphics.Bitmap failure=getUiAutomation().takeScreenshot();try(java.io.FileOutputStream f=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),"map-failure.png"))){failure.compress(android.graphics.Bitmap.CompressFormat.PNG,100,f);}throw new AssertionError("Search result could not be selected");}
    SystemClock.sleep(2500);final boolean[] ready={false};runOnMainSync(()->ready[0]=activity.findViewById(MapActivity.USE_BUTTON).isEnabled());if(!ready[0])throw new AssertionError("Map selection did not reach native UI");
    android.graphics.Bitmap screenshot=getUiAutomation().takeScreenshot();
    try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(getTargetContext().getExternalFilesDir(null),"map-v1.2.png"))){screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}
    result.putString("stream","Map runtime checks passed: bundled map library, live tiles, place search, result selection, native confirmation.\n");finish(Activity.RESULT_OK,result);
  }catch(Throwable e){result.putString("stream",android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}




