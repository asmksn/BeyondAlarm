package com.kibria.dynamicalarm;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PrivacyActivity extends Activity {
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    LinearLayout page = Ui.page(this);
    Ui.button(page, "Back", false, () -> finish());
    page.addView(Ui.title(this, "Privacy policy", 28));
    try (InputStream in = getAssets().open("privacy-policy.txt")) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096]; int n;
      while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
      page.addView(Ui.text(this, out.toString("UTF-8"), 16, Ui.INK));
    } catch (Exception e) {
      page.addView(Ui.text(this, "Contact: asmksn@gmail.com", 16, Ui.INK));
    }
  }
}
