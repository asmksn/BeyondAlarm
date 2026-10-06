package com.kibria.dynamicalarm.test;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import java.util.*;

/** Disposable test-only email destination. It never sends mail or accesses a network. */
public class EmailCaptureActivity extends Activity {
  @Override public void onCreate(Bundle state){super.onCreate(state);String message;
    try{
      ArrayList<Uri> uris=getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
      if(uris==null){uris=new ArrayList<>();Uri one=getIntent().getParcelableExtra(Intent.EXTRA_STREAM);if(one!=null)uris.add(one);}
      int bytes=0;
      for(Uri uri:uris)try(java.io.InputStream in=getContentResolver().openInputStream(uri)){byte[] b=new byte[1024];int n;while((n=in.read(b))!=-1)bytes+=n;}
      message="Attachments readable: "+uris.size()+" files, "+bytes+" bytes\nRecipient: "+Arrays.toString(getIntent().getStringArrayExtra(Intent.EXTRA_EMAIL));
    }catch(Exception e){message="READ FAILED: "+e.toString();}
    TextView view=new TextView(this);view.setText(message);view.setTextSize(24);setContentView(view);
  }
}
