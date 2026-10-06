package com.kibria.dynamicalarm;

import android.content.*;

public class RestoreReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context c, Intent i) {
    Scheduler.restore(c);
  }
}
