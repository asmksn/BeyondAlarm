package com.kibria.dynamicalarm;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

final class Ui {
  static final int BG = 0xfff5f4ef,
      INK = 0xff162d38,
      MUTED = 0xff60727b,
      TEAL = 0xff087e8b,
      PALE = 0xffe2f0ed;

  static int dp(Context c, float n) {
    return (int) (n * c.getResources().getDisplayMetrics().density + .5f);
  }

  static GradientDrawable bg(int color, int radius) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(radius);
    return d;
  }

  static LinearLayout column(Context c) {
    LinearLayout l = new LinearLayout(c);
    l.setOrientation(1);
    return l;
  }

  static LinearLayout page(Activity a) {
    ScrollView s = new ScrollView(a);
    s.setFillViewport(true);
    s.setBackgroundColor(BG);
    LinearLayout l = column(a);
    l.setPadding(dp(a, 24), dp(a, 24), dp(a, 24), dp(a, 32));
    s.addView(l);
    a.setContentView(s);
    safeInsets(s);
    return l;
  }

  static void safeInsets(View view) {
    if (android.os.Build.VERSION.SDK_INT < 35) return;
    final int left = view.getPaddingLeft(), top = view.getPaddingTop();
    final int right = view.getPaddingRight(), bottom = view.getPaddingBottom();
    view.setOnApplyWindowInsetsListener((v, insets) -> {
      android.graphics.Insets i = insets.getInsets(WindowInsets.Type.systemBars()
          | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
      v.setPadding(left + i.left, top + i.top, right + i.right, bottom + i.bottom);
      return WindowInsets.CONSUMED;
    });
    view.requestApplyInsets();
  }

  static TextView text(Context c, String value, int size, int color) {
    TextView t = new TextView(c);
    t.setText(value);
    t.setTextSize(size);
    t.setTextColor(color);
    t.setPadding(0, dp(c, 6), 0, dp(c, 6));
    return t;
  }

  static TextView title(Context c, String value, int size) {
    TextView t = text(c, value, size, INK);
    t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    return t;
  }

  static void gap(LinearLayout l, int n) {
    View v = new View(l.getContext());
    l.addView(v, new LinearLayout.LayoutParams(1, dp(l.getContext(), n)));
  }

  static LinearLayout card(LinearLayout parent, int color) {
    Context c = parent.getContext();
    LinearLayout l = column(c);
    l.setPadding(dp(c, 20), dp(c, 16), dp(c, 20), dp(c, 16));
    l.setBackground(bg(color, dp(c, 22)));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.bottomMargin = dp(c, 14);
    parent.addView(l, p);
    return l;
  }

  static Button button(LinearLayout l, String label, boolean primary, Runnable action) {
    Context c = l.getContext();
    Button b = new Button(c);
    b.setText(label);
    b.setAllCaps(false);
    b.setTextSize(16);
    b.setTextColor(primary ? 0xffffffff : TEAL);
    b.setBackground(bg(primary ? TEAL : PALE, dp(c, 16)));
    b.setPadding(dp(c, 14), dp(c, 10), dp(c, 14), dp(c, 10));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.topMargin = dp(c, 8);
    p.bottomMargin = dp(c, 4);
    b.setMinHeight(dp(c, 52));
    l.addView(b, p);
    b.setOnClickListener(v -> action.run());
    return b;
  }

  static EditText field(LinearLayout l, String label, String value, int input) {
    Context c = l.getContext();
    l.addView(text(c, label, 13, MUTED));
    EditText e = new EditText(c);
    e.setText(value);
    e.setTextColor(INK);
    e.setTextSize(17);
    e.setInputType(input);
    e.setPadding(dp(c, 12), dp(c, 12), dp(c, 12), dp(c, 12));
    e.setBackground(bg(0xffffffff, dp(c, 10)));
    l.addView(e, new LinearLayout.LayoutParams(-1, -2));
    return e;
  }

  static void error(Context c, String text) {
    new AlertDialog.Builder(c)
        .setTitle("Please check")
        .setMessage(text)
        .setPositiveButton("OK", null)
        .show();
  }
}
