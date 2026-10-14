package com.kibria.dynamicalarm;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class ActionTheme {
  public static final int KIND_RING = 0;
  public static final int KIND_CALL = 1;
  public static final int KIND_MESSAGE = 2;
  public static final int KIND_EMAIL = 3;
  public static final int KIND_DOCUMENT = 4;
  public static final int KIND_DIRECTIONS = 5;
  public static final int KIND_APP = 6;
  public static final int KIND_WEB = 7;
  public static final int KIND_WEATHER = 8;

  // Distinct soft pastel shades for each action card
  public static final int[] SHADES = {
    0xfff1f5f9, // Ring - Cool slate tint
    0xffecfdf5, // Call - Mint emerald tint
    0xfffff7ed, // Message - Warm peach amber tint
    0xfffff1f2, // Email - Soft coral rose tint
    0xfff0fdfa, // Document - Fresh cyan teal tint
    0xfff0f9ff, // Directions - Sky azure tint
    0xfffdf2f8, // App - Radiant magenta pink tint
    0xfff5f3ff, // Website - Soft lavender violet tint
    0xfffefce8  // Weather - Warm morning sunshine tint
  };

  // Border accents for each shade
  public static final int[] BORDERS = {
    0xffcbd5e1, // Ring
    0xffa7f3d0, // Call
    0xfffed7aa, // Message
    0xfffecdd3, // Email
    0xff99f6e4, // Document
    0xffbae6fd, // Directions
    0xfffbcfe8, // App
    0xffddd6fe, // Website
    0xfffef08a  // Weather
  };

  // Vibrant accent colors for badges & icons
  public static final int[] ACCENTS = {
    0xff475569, // Ring - Slate
    0xff059669, // Call - Emerald
    0xffea580c, // Message - Amber/Orange
    0xffff5722, // Email - Bright Coral Orange
    0xff0d9488, // Document - Teal
    0xff0284c7, // Directions - Sky Blue
    0xffdb2777, // App - Hot Pink / Magenta
    0xff7c3aed, // Website - Royal Purple
    0xffd97706  // Weather - Sun Amber
  };

  // Icon resource IDs
  public static final int[] ICONS = {
    R.drawable.ic_action_ring,
    R.drawable.ic_action_call,
    R.drawable.ic_action_message,
    R.drawable.ic_action_email,
    R.drawable.ic_action_document,
    R.drawable.ic_action_directions,
    R.drawable.ic_action_app,
    R.drawable.ic_action_web,
    R.drawable.ic_action_weather
  };

  public static final String[] SUBTITLES = {
    "Plays ringtone and vibrates device",
    "Opens phone dialer with contact",
    "Composes an SMS text draft",
    "Prepares an email with attachments",
    "Opens chosen document or file",
    "Opens map route to destination",
    "Launches chosen installed application",
    "Opens destination web page in browser",
    "Displays local weather forecast"
  };

  public static Drawable getIcon(Context c, int kind, int color) {
    if (kind < 0 || kind >= ICONS.length) kind = 0;
    Drawable d = c.getDrawable(ICONS[kind]).mutate();
    d.setTint(color);
    return d;
  }

  public static GradientDrawable cardBg(int kind, int radiusDp, Context c) {
    if (kind < 0 || kind >= SHADES.length) kind = 0;
    GradientDrawable g = new GradientDrawable();
    g.setColor(SHADES[kind]);
    g.setCornerRadius(Ui.dp(c, radiusDp));
    g.setStroke(Ui.dp(c, 1.5f), BORDERS[kind]);
    return g;
  }

  public static GradientDrawable badgeBg(int kind, Context c) {
    if (kind < 0 || kind >= ACCENTS.length) kind = 0;
    GradientDrawable g = new GradientDrawable();
    g.setColor(ACCENTS[kind]);
    g.setCornerRadius(Ui.dp(c, 19)); // Circular badge for 38dp view
    return g;
  }

  public static BaseAdapter createAdapter(Context context, java.util.function.IntSupplier selectedSupplier) {
    return new BaseAdapter() {
      @Override
      public int getCount() {
        return Alarm.KINDS.length;
      }

      @Override
      public Object getItem(int position) {
        return Alarm.KINDS[position];
      }

      @Override
      public long getItemId(int position) {
        return position;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        return buildRow(context, position, false, position == selectedSupplier.getAsInt());
      }

      @Override
      public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return buildRow(context, position, true, position == selectedSupplier.getAsInt());
      }
    };
  }

  private static View buildRow(Context c, int pos, boolean isDropdown, boolean isSelected) {
    LinearLayout outer = new LinearLayout(c);
    outer.setOrientation(LinearLayout.VERTICAL);
    int padV = isDropdown ? Ui.dp(c, 5) : 0;
    int padH = isDropdown ? Ui.dp(c, 8) : 0;
    outer.setPadding(padH, padV, padH, padV);

    LinearLayout card = new LinearLayout(c);
    card.setOrientation(LinearLayout.HORIZONTAL);
    card.setGravity(Gravity.CENTER_VERTICAL);
    // 999dp corner radius creates an elegant, smooth oval pill shape
    card.setBackground(cardBg(pos, 999, c));
    card.setPadding(Ui.dp(c, 14), Ui.dp(c, 10), Ui.dp(c, 18), Ui.dp(c, 10));

    // Left circular icon badge
    LinearLayout badge = new LinearLayout(c);
    badge.setGravity(Gravity.CENTER);
    badge.setBackground(badgeBg(pos, c));
    int badgeSize = Ui.dp(c, 38);
    badge.setLayoutParams(new LinearLayout.LayoutParams(badgeSize, badgeSize));

    ImageView icon = new ImageView(c);
    icon.setImageDrawable(getIcon(c, pos, 0xffffffff));
    int iconSize = Ui.dp(c, 20);
    badge.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));
    card.addView(badge);

    // Text details (Title + Subtitle)
    LinearLayout textCol = new LinearLayout(c);
    textCol.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
    textLp.leftMargin = Ui.dp(c, 14);
    textLp.rightMargin = Ui.dp(c, 8);
    textCol.setLayoutParams(textLp);

    TextView title = new TextView(c);
    title.setText(Alarm.KINDS[pos]);
    title.setTextColor(Ui.INK);
    title.setTextSize(16);
    title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    textCol.addView(title);

    TextView sub = new TextView(c);
    sub.setText(SUBTITLES[pos]);
    sub.setTextColor(Ui.MUTED);
    sub.setTextSize(12);
    textCol.addView(sub);
    card.addView(textCol);

    if (isDropdown) {
      if (isSelected) {
        TextView check = new TextView(c);
        check.setText("\u2713");
        check.setTextSize(18);
        check.setTextColor(ACCENTS[pos]);
        check.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(check);
      }
    } else {
      // Main screen spinner view: show dropdown indicator
      TextView arrow = new TextView(c);
      arrow.setText("\u25BE");
      arrow.setTextSize(18);
      arrow.setTextColor(ACCENTS[pos]);
      card.addView(arrow);
    }

    outer.addView(card, new LinearLayout.LayoutParams(-1, -2));
    return outer;
  }
}
