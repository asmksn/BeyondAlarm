package com.kibria.dynamicalarm;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class Alarm {
  public int id, hour = 7, minute = 0, days = 0, kind = 0, volume = 80;
  public long at = 0, snoozeAt = 0;
  public boolean enabled = true, vibrate = true;
  public String title = "",
      target = "",
      body = "",
      subject = "",
      mime = "",
      targetName = "",
      tone = "",
      quote = "";
  public final ArrayList<Attachment> attachments = new ArrayList<>();

  public static class Attachment {
    public String uri, name, mime;

    public Attachment(String uri, String name, String mime) {
      this.uri = uri;
      this.name = name;
      this.mime = mime;
    }
  }

  public static final String[] KINDS = {
    "Just ring",
    "Call someone",
    "Send a message",
    "Send an email",
    "Open a document",
    "Get directions",
    "Open an app",
    "Open a website",
    "Morning weather"
  };
  public static final String[] ACTIONS = {
    "I'm up",
    "Open dialer",
    "Compose message",
    "Compose email",
    "View document",
    "Get directions",
    "Open app",
    "Open website",
    "See today's weather"
  };

  public JSONObject json() {
    JSONObject j = new JSONObject();
    try {
      j.put("id", id)
          .put("hour", hour)
          .put("minute", minute)
          .put("days", days)
          .put("kind", kind)
          .put("volume", volume)
          .put("at", at)
          .put("snoozeAt", snoozeAt)
          .put("enabled", enabled)
          .put("vibrate", vibrate)
          .put("title", title)
          .put("target", target)
          .put("body", body)
          .put("subject", subject)
          .put("mime", mime)
          .put("targetName", targetName)
          .put("tone", tone)
          .put("quote", quote);
      JSONArray files = new JSONArray();
      for (Attachment f : attachments)
        files.put(new JSONObject().put("uri", f.uri).put("name", f.name).put("mime", f.mime));
      j.put("attachments", files);
    } catch (Exception ignored) {
    }
    return j;
  }

  public static Alarm from(JSONObject j) {
    Alarm a = new Alarm();
    a.id = j.optInt("id");
    a.hour = j.optInt("hour", 7);
    a.minute = j.optInt("minute");
    a.days = j.optInt("days");
    a.kind = j.optInt("kind");
    a.volume = j.optInt("volume", 80);
    a.at = j.optLong("at");
    a.snoozeAt = j.optLong("snoozeAt");
    a.enabled = j.optBoolean("enabled", true);
    a.vibrate = j.optBoolean("vibrate", true);
    a.title = j.optString("title");
    a.target = j.optString("target");
    a.body = j.optString("body");
    a.subject = j.optString("subject");
    a.mime = j.optString("mime");
    a.targetName = j.optString("targetName");
    a.tone = j.optString("tone");
    a.quote = j.optString("quote");
    JSONArray files = j.optJSONArray("attachments");
    if (files != null)
      for (int n = 0; n < files.length(); n++) {
        JSONObject f = files.optJSONObject(n);
        if (f != null && !f.optString("uri").isEmpty())
          a.attachments.add(
              new Attachment(
                  f.optString("uri"),
                  f.optString("name", "Attachment"),
                  f.optString("mime", "application/octet-stream")));
      }
    return a;
  }
}
