package com.kibria.dynamicalarm;

import android.content.*;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import java.util.*;

public final class EmailIntents {
  public static Intent message(Alarm alarm) {
    Intent intent;
    if (alarm.attachments.isEmpty()) {
      intent =
          new Intent(
              Intent.ACTION_SENDTO,
              Uri.parse(
                  "mailto:"
                      + Uri.encode(alarm.target)
                      + "?subject="
                      + Uri.encode(alarm.subject)
                      + "&body="
                      + Uri.encode(alarm.body)));
    } else {
      ArrayList<Uri> uris = new ArrayList<>();
      for (Alarm.Attachment file : alarm.attachments) {
        Uri uri = Uri.parse(file.uri);
        if (!"content".equals(uri.getScheme()))
          throw new IllegalArgumentException("Choose the attachment again using the file picker.");
        uris.add(uri);
      }
      intent = new Intent(uris.size() == 1 ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE);
      intent.setType(uris.size() == 1 ? alarm.attachments.get(0).mime : "*/*");
      if (uris.size() == 1) intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
      else intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
      ClipData clips =
          new ClipData("Email attachments", new String[] {"*/*"}, new ClipData.Item(uris.get(0)));
      for (int i = 1; i < uris.size(); i++) clips.addItem(new ClipData.Item(uris.get(i)));
      intent.setClipData(clips);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
    return intent
        .putExtra(Intent.EXTRA_EMAIL, new String[] {alarm.target})
        .putExtra(Intent.EXTRA_SUBJECT, alarm.subject)
        .putExtra(Intent.EXTRA_TEXT, alarm.body);
  }

  public static Intent chooser(Context context, Alarm alarm) {
    Intent send = message(alarm);
    if (alarm.attachments.isEmpty()) return send;
    List<ResolveInfo> handlers =
        context
            .getPackageManager()
            .queryIntentActivities(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")), 0);
    LinkedHashSet<String> packages = new LinkedHashSet<>();
    ArrayList<Intent> choices = new ArrayList<>();
    for (ResolveInfo handler : handlers) {
      String pkg = handler.activityInfo.packageName;
      if (packages.add(pkg)) {
        Intent candidate = new Intent(send).setPackage(pkg);
        if (candidate.resolveActivity(context.getPackageManager()) != null) choices.add(candidate);
      }
    }
    if (choices.isEmpty())
      throw new ActivityNotFoundException(
          "Install an email app that supports attachments, then try again.");
    Intent picker = Intent.createChooser(choices.remove(0), "Send email with attachments");
    picker.putExtra(Intent.EXTRA_INITIAL_INTENTS, choices.toArray(new Intent[0]));
    return picker;
  }
}
