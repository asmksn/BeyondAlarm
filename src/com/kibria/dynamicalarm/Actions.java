package com.kibria.dynamicalarm;

import android.app.*;
import android.content.*;
import android.net.Uri;

final class Actions {
  static void open(Activity c, Alarm a, Runnable done) {
    if (a.kind == 8) {
      Weather.show(c, a.target, done);
      return;
    }
    try {
      Intent i;
      switch (a.kind) {
        case 1:
          i = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", a.target, null));
          break;
        case 2:
          i =
              new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", a.target, null))
                  .putExtra("sms_body", a.body);
          break;
        case 3:
          i = EmailIntents.chooser(c, a);
          break;
        case 4:
          i =
              new Intent(Intent.ACTION_VIEW)
                  .setDataAndType(Uri.parse(a.target), a.mime)
                  .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          i.setClipData(ClipData.newRawUri("Document", Uri.parse(a.target)));
          break;
        case 5:
          i =
              new Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(
                      "https://www.google.com/maps/dir/?api=1&destination="
                          + Uri.encode(a.target)));
          break;
        case 6:
          i = c.getPackageManager().getLaunchIntentForPackage(a.target);
          if (i == null) throw new ActivityNotFoundException();
          break;
        case 7:
          i = new Intent(Intent.ACTION_VIEW, Uri.parse(a.target));
          break;
        default:
          done.run();
          return;
      }
      c.startActivity(i);
      done.run();
    } catch (Exception e) {
      new AlertDialog.Builder(c)
          .setTitle("Couldn't open this action")
          .setMessage(
              a.kind == 4
                  ? "The file may have moved or its access expired. Check that a compatible viewer"
                      + " is installed, then select the file again in Edit alarm."
                  : "A compatible app may be missing or no longer available. Your alarm has been"
                      + " dismissed; you can update its action in Edit alarm.")
          .setPositiveButton("Close", (d, w) -> done.run())
          .setOnCancelListener(d -> done.run())
          .show();
    }
  }
}
