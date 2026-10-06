package com.kibria.dynamicalarm.test;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.accessibility.AccessibilityNodeInfo;
import com.kibria.dynamicalarm.*;
import java.util.*;

public class AttachmentSmokeTest extends Instrumentation {
  @Override public void onCreate(Bundle b){super.onCreate(b);start();}
  private boolean contains(AccessibilityNodeInfo n,String text){if(n==null)return false;if(n.getText()!=null&&n.getText().toString().contains(text))return true;for(int i=0;i<n.getChildCount();i++)if(contains(n.getChild(i),text))return true;return false;}
  @Override public void onStart(){Bundle result=new Bundle();ArrayList<Uri> created=new ArrayList<>();try{
    android.content.Context context=getTargetContext();Alarm alarm=new Alarm();alarm.kind=3;alarm.target="example@example.com";alarm.subject="Attachment test";
    for(int i=0;i<2;i++){
      ContentValues values=new ContentValues();values.put(MediaStore.Downloads.DISPLAY_NAME,"DynamicAlarm-test-"+i+".txt");values.put(MediaStore.Downloads.MIME_TYPE,"text/plain");
      Uri uri=context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,values);created.add(uri);
      try(java.io.OutputStream out=context.getContentResolver().openOutputStream(uri)){out.write("attachment".getBytes(java.nio.charset.StandardCharsets.UTF_8));}
      alarm.attachments.add(new Alarm.Attachment(uri.toString(),"test-"+i+".txt","text/plain"));
    }
    Intent chooser=EmailIntents.chooser(context,alarm);Intent send=chooser.getParcelableExtra(Intent.EXTRA_INTENT);
    if(send==null||!"com.kibria.dynamicalarm.test".equals(send.getPackage()))throw new AssertionError("Email-only chooser did not discover the fixture email app");
    context.startActivity(send.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    boolean readable=false;long until=SystemClock.elapsedRealtime()+10000;
    while(!readable&&SystemClock.elapsedRealtime()<until){readable=contains(getUiAutomation().getRootInActiveWindow(),"Attachments readable: 2 files, 20 bytes");if(!readable)SystemClock.sleep(250);}
    if(!readable)throw new AssertionError("External recipient could not read both attachments");
    result.putString("stream","Attachment runtime checks passed: email-only chooser, two real document URIs, read grants across app UIDs, recipient details. No mail sent.\n");finish(Activity.RESULT_OK,result);
  }catch(Throwable e){result.putString("stream","FAILED: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}finally{for(Uri uri:created)getTargetContext().getContentResolver().delete(uri,null,null);}}
}
