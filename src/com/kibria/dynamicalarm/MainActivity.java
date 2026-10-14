package com.kibria.dynamicalarm;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
  private String page = "home";
  private Alarm draft;
  private LinearLayout root, actionFields;
  private EditText title, target, body, subject, quote;
  private LinearLayout attachmentList;
  private Button timeButton, dateButton, selection, toneButton;
  private CheckBox[] days = new CheckBox[7];
  private Switch vibration;
  private SeekBar volume;
  private long chosenDate;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    if (Build.VERSION.SDK_INT >= 33) {
      getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
          android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> {
            if ("home".equals(page)) finish(); else onBackPressed();
          });
    }
    AlarmService.channels(this);
    Scheduler.restore(this);
    if (state != null && state.containsKey("draft")) {
      try {
        draft = Alarm.from(new org.json.JSONObject(state.getString("draft")));
        chosenDate = state.getLong("date");
        editor(draft);
        return;
      } catch (Exception ignored) {
      }
    }
    home();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if ("home".equals(page)) home();
    else if ("setup".equals(page)) setup();
  }

  @Override
  protected void onSaveInstanceState(Bundle b) {
    if ("edit".equals(page) && draft != null) {
      collect();
      b.putString("draft", draft.json().toString());
      b.putLong("date", chosenDate);
    }
    super.onSaveInstanceState(b);
  }

  @Override
  public void onBackPressed() {
    if (!"home".equals(page)) {
      if ("edit".equals(page))
        new AlertDialog.Builder(this)
            .setTitle("Leave without saving?")
            .setMessage("Your changes to this alarm will be discarded.")
            .setNegativeButton("Keep editing", null)
            .setPositiveButton("Discard", (d, w) -> home())
            .show();
      else home();
    } else super.onBackPressed();
  }

  private String time(long at) {
    return android.text.format.DateFormat.getTimeFormat(this).format(new Date(at));
  }

  private String alarmTime(Alarm a) {
    if (a.snoozeAt > 0) return time(a.snoozeAt);
    Calendar c = Calendar.getInstance();
    c.set(Calendar.HOUR_OF_DAY, a.hour);
    c.set(Calendar.MINUTE, a.minute);
    return android.text.format.DateFormat.getTimeFormat(this).format(c.getTime());
  }

  private String date(long at) {
    return new java.text.SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        .format(new Date(at));
  }

  private String repeat(int mask) {
    if (mask == 0) return "Once";
    if (mask == 127) return "Every day";
    StringBuilder s = new StringBuilder();
    String[] names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (int i = 0; i < 7; i++) if ((mask & (1 << i)) != 0) s.append(names[i]).append(" ");
    return s.toString().trim();
  }

  private boolean notifications() {
    return getSystemService(NotificationManager.class).areNotificationsEnabled()
        && getSystemService(NotificationManager.class)
                .getNotificationChannel(AlarmService.CHANNEL)
                .getImportance()
            != NotificationManager.IMPORTANCE_NONE;
  }

  private boolean fullScreen() {
    return Build.VERSION.SDK_INT < 34
        || getSystemService(NotificationManager.class).canUseFullScreenIntent();
  }

  private void home() {
    page = "home";
    draft = null;
    root = Ui.page(this);
    root.addView(Ui.text(this, "A LITTLE REMINDER. A REAL NEXT STEP.", 11, Ui.TEAL));
    root.addView(Ui.title(this, "Beyond Alarm", 34));
    root.addView(Ui.text(this, "Your alarm, with a purpose.", 16, Ui.MUTED));
    Ui.gap(root, 16);
    if (!Scheduler.allowed(this) || !notifications() || !fullScreen()) {
      LinearLayout c = Ui.card(root, 0xffffe8ca);
      c.addView(Ui.title(this, "Finish your alarm setup", 19));
      c.addView(
          Ui.text(
              this,
              "Allow alarms, notifications and full-screen alerts so your phone can reach you on"
                  + " time.",
              14,
              Ui.INK));
      Ui.button(c, "Open setup", false, () -> setup());
    }
    ArrayList<Alarm> alarms = Store.all(this);
    Alarm next = null;
    long minUpcoming = Long.MAX_VALUE;
    for (Alarm a : alarms) {
      long trigger = a.snoozeAt > 0 ? a.snoozeAt : a.at;
      if ((a.enabled || a.snoozeAt > 0)
          && trigger > System.currentTimeMillis()
          && trigger < minUpcoming) {
        minUpcoming = trigger;
        next = a;
      }
    }
    alarms.sort(Comparator.comparingInt((Alarm a) -> a.hour * 60 + a.minute).thenComparingLong(a -> a.at));
    LinearLayout hero = Ui.card(root, Ui.INK);
    hero.addView(
        Ui.text(this, next == null ? "YOUR DAY, WITH INTENTION" : "UP NEXT", 12, 0xff90d6cf));
    hero.addView(
        Ui.text(
            this,
            next == null
                ? "From reminder\nto action."
                : time(next.snoozeAt > 0 ? next.snoozeAt : next.at),
            next == null ? 30 : 48,
            0xffffffff));
    hero.addView(
        Ui.text(
            this,
            next == null
                ? "A call. A place. A plan.\nAttach the next step to your alarm."
                : next.title + "\n" + date(next.snoozeAt > 0 ? next.snoozeAt : next.at),
            16,
            0xffc9dadd));
    Ui.button(
        root,
        "+  Create an alarm",
        true,
        () -> {
          Alarm a = new Alarm();
          Calendar c = Calendar.getInstance();
          c.add(Calendar.MINUTE, 5);
          a.hour = c.get(Calendar.HOUR_OF_DAY);
          a.minute = c.get(Calendar.MINUTE);
          a.at = Times.next(a.hour, a.minute, 0, System.currentTimeMillis());
          chosenDate = a.at;
          editor(a);
        });
    Ui.gap(root, 22);
    root.addView(Ui.title(this, "Your alarms", 22));
    String notice = getSharedPreferences("alarms", 0).getString("notice", "");
    if (!notice.isEmpty()) {
      root.addView(Ui.text(this, notice, 14, Ui.MUTED));
      Ui.button(
          root,
          "Clear notice",
          false,
          () -> {
            getSharedPreferences("alarms", 0).edit().remove("notice").apply();
            home();
          });
    }
    if (alarms.isEmpty())
      root.addView(
          Ui.text(
              this,
              "Start with a morning forecast, a call home, or a document you need to review.",
              16,
              Ui.MUTED));
    for (Alarm a : alarms) {
      LinearLayout card = Ui.card(root, 0xffffffff);
      LinearLayout row = new LinearLayout(this);
      row.setGravity(Gravity.CENTER_VERTICAL);
      TextView clock = Ui.title(this, alarmTime(a), 32);
      row.addView(clock, new LinearLayout.LayoutParams(0, -2, 1));
      Switch on = new Switch(this);
      on.setContentDescription("Enable " + a.title);
      on.setChecked(a.enabled || a.snoozeAt > 0);
      row.addView(on);
      card.addView(row);
      if (!a.title.isEmpty() && !a.title.equals(Alarm.KINDS[a.kind])) {
        card.addView(Ui.title(this, a.title, 19));
      }

      LinearLayout infoCol = new LinearLayout(this);
      infoCol.setOrientation(LinearLayout.VERTICAL);
      infoCol.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 4));

      LinearLayout kindBadge = new LinearLayout(this);
      kindBadge.setOrientation(LinearLayout.HORIZONTAL);
      kindBadge.setGravity(Gravity.CENTER_VERTICAL);
      kindBadge.setBackground(ActionTheme.cardBg(a.kind, 999, this));
      kindBadge.setPadding(Ui.dp(this, 12), Ui.dp(this, 5), Ui.dp(this, 12), Ui.dp(this, 5));

      ImageView bIcon = new ImageView(this);
      bIcon.setImageDrawable(ActionTheme.getIcon(this, a.kind, ActionTheme.ACCENTS[a.kind]));
      kindBadge.addView(bIcon, new LinearLayout.LayoutParams(Ui.dp(this, 16), Ui.dp(this, 16)));

      TextView bText = new TextView(this);
      bText.setText("  " + Alarm.KINDS[a.kind]);
      bText.setTextSize(13);
      bText.setTextColor(ActionTheme.ACCENTS[a.kind]);
      bText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
      kindBadge.addView(bText);

      LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(-2, -2);
      badgeLp.bottomMargin = Ui.dp(this, 6);
      infoCol.addView(kindBadge, badgeLp);

      String sched = repeat(a.days)
          + (a.days == 0 ? " • " + date(a.at) : "")
          + (a.snoozeAt > 0 ? " • Snoozed" : "");
      TextView schedView = new TextView(this);
      schedView.setText(sched);
      schedView.setTextSize(13);
      schedView.setTextColor(a.snoozeAt > 0 ? ActionTheme.ACCENTS[ActionTheme.KIND_MESSAGE] : Ui.MUTED);
      schedView.setPadding(Ui.dp(this, 2), 0, 0, Ui.dp(this, 4));
      infoCol.addView(schedView);

      card.addView(infoCol);

      // Side-by-side Edit and Delete buttons on one line for a shorter, cleaner card
      LinearLayout btnRow = new LinearLayout(this);
      btnRow.setOrientation(LinearLayout.HORIZONTAL);
      btnRow.setGravity(Gravity.CENTER_VERTICAL);
      LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(-1, -2);
      btnRowLp.topMargin = Ui.dp(this, 8);
      btnRowLp.bottomMargin = Ui.dp(this, 2);

      Button editBtn = new Button(this);
      editBtn.setText("Edit alarm");
      editBtn.setAllCaps(false);
      editBtn.setTextSize(15);
      editBtn.setTextColor(Ui.TEAL);
      editBtn.setBackground(Ui.bg(Ui.PALE, Ui.dp(this, 14)));
      editBtn.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8));
      editBtn.setMinHeight(Ui.dp(this, 44));
      editBtn.setOnClickListener(v -> {
        chosenDate = a.at;
        editor(Alarm.from(a.json()));
      });
      LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
      editLp.rightMargin = Ui.dp(this, 6);
      btnRow.addView(editBtn, editLp);

      Button delBtn = new Button(this);
      delBtn.setText("Delete alarm");
      delBtn.setAllCaps(false);
      delBtn.setTextSize(15);
      delBtn.setTextColor(Ui.MUTED);
      delBtn.setBackground(Ui.bg(0xffeef2f5, Ui.dp(this, 14)));
      delBtn.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8));
      delBtn.setMinHeight(Ui.dp(this, 44));
      delBtn.setOnClickListener(v -> deleteAlarm(a));
      LinearLayout.LayoutParams delLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
      delLp.leftMargin = Ui.dp(this, 6);
      btnRow.addView(delBtn, delLp);

      card.addView(btnRow, btnRowLp);
      on.setOnCheckedChangeListener(
          (b, checked) -> {
            if (checked && (!Scheduler.allowed(this) || !notifications())) {
              on.setChecked(false);
              setup();
              return;
            }
            try {
              a.enabled = checked;
              a.snoozeAt = 0;
              if (checked && (a.days != 0 || a.at <= System.currentTimeMillis()))
                a.at = Times.next(a.hour, a.minute, a.days, System.currentTimeMillis());
              Scheduler.schedule(this, a);
              Store.put(this, a);
              if (!checked) stopRinging(a.id);
              home();
            } catch (Exception e) {
              Ui.error(this, e.getMessage());
            }
          });
    }
    Ui.gap(root, 8);
    Ui.button(root, "Setup & test alarm", false, () -> setup());
    Ui.button(root, "Suggestions & feedback", false, () -> feedback());
    root.addView(Ui.text(this, "Beyond Alarm — Your alarm, with a purpose, by Kibria", 12, Ui.MUTED));
  }

  private void setup() {
    page = "setup";
    root = Ui.page(this);
    Ui.button(root, "‹  Back to alarms", false, () -> home());
    root.addView(Ui.title(this, "Ready when\nyou need it.", 32));
    root.addView(
        Ui.text(this, "Complete these checks before relying on your first alarm.", 16, Ui.MUTED));
    permissionCard(
        "1  Exact alarms",
        Scheduler.allowed(this),
        "Lets the alarm ring at your chosen time.",
        () ->
            launchSettings(
                new Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + getPackageName()))));
    permissionCard(
        "2  Notifications",
        notifications(),
        "Shows your alarm and its snooze controls.",
        () -> {
          if (Build.VERSION.SDK_INT >= 33
              && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                  != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 42);
          else
            launchSettings(
                new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
        });
    permissionCard(
        "3  Full-screen alerts",
        fullScreen(),
        "Shows the alarm over the lock screen. While using your phone, Android may show a banner"
            + " instead.",
        () -> {
          if (Build.VERSION.SDK_INT >= 34)
            launchSettings(
                new Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:" + getPackageName())));
        });
    LinearLayout c = Ui.card(root, 0xffffffff);
    c.addView(Ui.title(this, "4  Sound & battery", 19));
    c.addView(
        Ui.text(
            this,
            "Keep your system alarm volume audible. In Do Not Disturb, allow alarms. Check that"
                + " your phone's battery settings allow this app to run. Force stopping the"
                + " app prevents alarms until you reopen it.",
            14,
            Ui.MUTED));
    Ui.button(
        c,
        "Open app settings",
        false,
        () ->
            launchSettings(
                new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()))));
    Ui.button(
        c,
        "Open sound settings",
        false,
        () -> launchSettings(new Intent(Settings.ACTION_SOUND_SETTINGS)));
    Ui.button(
        root,
        "Schedule a test in 15 seconds",
        true,
        () -> {
          if (!Scheduler.allowed(this) || !notifications()) {
            Ui.error(this, "Allow exact alarms and notifications first.");
            return;
          }
          Alarm a = new Alarm();
          a.id = Store.newId(this);
          a.title = "Your Beyond Alarm test";
          a.at = System.currentTimeMillis() + 15000;
          Calendar t = Calendar.getInstance();
          t.setTimeInMillis(a.at);
          a.hour = t.get(Calendar.HOUR_OF_DAY);
          a.minute = t.get(Calendar.MINUTE);
          try {
            Scheduler.schedule(this, a);
            Store.put(this, a);
            Toast.makeText(this, "Test set. Lock your phone to try the alarm screen.", 1).show();
            home();
          } catch (Exception e) {
            Ui.error(this, e.getMessage());
          }
        });
    Ui.button(
        root,
        "Restore saved schedules",
        false,
        () -> {
          Scheduler.restore(this);
          Toast.makeText(this, "Saved schedules checked", 0).show();
          home();
        });
    Ui.button(root, "Privacy & about", false, () -> showPrivacyAboutDialog());
  }

  private void showPrivacyAboutDialog() {
    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    LinearLayout layout = Ui.column(this);
    int padH = Ui.dp(this, 16);
    int padV = Ui.dp(this, 20);
    layout.setPadding(padH, padV, padH, padV);
    scroll.addView(layout);

    layout.addView(Ui.title(this, "Beyond Alarm • 2.0", 22));
    Ui.gap(layout, 12);

    String body =
        "Your alarms stay on this phone. No account, advertising or analytics. Selected"
            + " files use Android's document access permission. Weather sends your"
            + " entered city to Open-Meteo only when you request the forecast. External"
            + " actions open another app, where you confirm calls or sending messages.\n\n"
            + "Beyond Alarm — Your alarm, with a purpose, by Kibria\n\n"
            + "Map searches use Photon and map tiles use OpenStreetMap. These services"
            + " receive the place you search for or view. Email attachments remain on"
            + " your phone until you choose to send them from your email app.\n\n"
            + "Suggestions or questions: asmksn@gmail.com\n\n"
            + "Weather: Open-Meteo (CC BY 4.0) | Places: GeoNames | Maps: © OpenStreetMap contributors (ODbL)";

    TextView tv = Ui.text(this, body, 14, Ui.INK);
    tv.setLineSpacing(Ui.dp(this, 2), 1.25f);
    layout.addView(tv);

    Ui.gap(layout, 18);

    LinearLayout bar = new LinearLayout(this);
    bar.setOrientation(LinearLayout.HORIZONTAL);
    bar.setGravity(Gravity.CENTER);
    bar.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 4));

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(scroll)
        .create();

    TextView opt1 = new TextView(this);
    opt1.setText("Privacy policy");
    opt1.setTextSize(13);
    opt1.setTextColor(Ui.TEAL);
    opt1.setSingleLine(true);
    opt1.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    opt1.setPadding(Ui.dp(this, 2), Ui.dp(this, 6), Ui.dp(this, 2), Ui.dp(this, 6));
    opt1.setOnClickListener(v -> {
      dialog.dismiss();
      startActivity(new Intent(this, PrivacyActivity.class));
    });
    bar.addView(opt1);

    TextView sep1 = new TextView(this);
    sep1.setText(" | ");
    sep1.setTextSize(13);
    sep1.setTextColor(Ui.MUTED);
    sep1.setSingleLine(true);
    bar.addView(sep1);

    TextView opt2 = new TextView(this);
    opt2.setText("Email feedback");
    opt2.setTextSize(13);
    opt2.setTextColor(Ui.TEAL);
    opt2.setSingleLine(true);
    opt2.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    opt2.setPadding(Ui.dp(this, 2), Ui.dp(this, 6), Ui.dp(this, 2), Ui.dp(this, 6));
    opt2.setOnClickListener(v -> {
      dialog.dismiss();
      feedback();
    });
    bar.addView(opt2);

    TextView sep2 = new TextView(this);
    sep2.setText(" | ");
    sep2.setTextSize(13);
    sep2.setTextColor(Ui.MUTED);
    sep2.setSingleLine(true);
    bar.addView(sep2);

    TextView opt3 = new TextView(this);
    opt3.setText("Close");
    opt3.setTextSize(13);
    opt3.setTextColor(Ui.TEAL);
    opt3.setSingleLine(true);
    opt3.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    opt3.setPadding(Ui.dp(this, 2), Ui.dp(this, 6), Ui.dp(this, 2), Ui.dp(this, 6));
    opt3.setOnClickListener(v -> dialog.dismiss());
    bar.addView(opt3);

    layout.addView(bar);

    dialog.show();
  }

  private void permissionCard(String title, boolean ready, String desc, Runnable action) {
    LinearLayout c = Ui.card(root, 0xffffffff);
    c.addView(Ui.title(this, title + "  ·  " + (ready ? "Ready" : "Needed"), 19));
    c.addView(Ui.text(this, desc, 14, Ui.MUTED));
    Ui.button(c, ready ? "Review settings" : "Allow access", false, action);
  }

  private void launchSettings(Intent i) {
    try {
      startActivity(i);
    } catch (ActivityNotFoundException e) {
      startActivity(
          new Intent(
              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              Uri.parse("package:" + getPackageName())));
    }
  }

  @Override
  public void onRequestPermissionsResult(int r, String[] p, int[] results) {
    super.onRequestPermissionsResult(r, p, results);
    if ("setup".equals(page)) setup();
  }

  private void editor(Alarm a) {
    page = "edit";
    draft = a;
    root = Ui.page(this);
    Ui.button(root, "‹  Back", false, () -> onBackPressed());
    root.addView(Ui.title(this, a.id == 0 ? "Make a moment." : "Edit your alarm", 30));
    root.addView(Ui.text(this, "Choose when. Decide what comes next.", 15, Ui.MUTED));
    timeButton =
        Ui.button(
            root,
            String.format(Locale.getDefault(), "%02d:%02d", a.hour, a.minute),
            true,
            () ->
                new TimePickerDialog(
                        this,
                        (v, h, m) -> {
                          draft.hour = h;
                          draft.minute = m;
                          timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                        },
                        draft.hour,
                        draft.minute,
                        android.text.format.DateFormat.is24HourFormat(this))
                    .show());
    timeButton.setTextSize(40);
    dateButton =
        Ui.button(
            root,
            "Date · " + date(chosenDate),
            false,
            () -> {
              Calendar c = Calendar.getInstance();
              c.setTimeInMillis(chosenDate);
              DatePickerDialog d =
                  new DatePickerDialog(
                      this,
                      (v, y, m, day) -> {
                        Calendar pick = Calendar.getInstance();
                        pick.set(y, m, day, 0, 0, 0);
                        pick.set(Calendar.MILLISECOND, 0);
                        chosenDate = pick.getTimeInMillis();
                        dateButton.setText("Date · " + date(chosenDate));
                      },
                      c.get(Calendar.YEAR),
                      c.get(Calendar.MONTH),
                      c.get(Calendar.DATE));
              d.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
              d.show();
            });
    Ui.gap(root, 12);
    title =
        Ui.field(
            root,
            "Alarm name",
            a.title,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    root.addView(Ui.text(this, "Repeat · leave unchecked for a single date", 13, Ui.MUTED));
    String[] names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(0);
    root.addView(row);
    for (int k = 0; k < 7; k++) {
      days[k] = new CheckBox(this);
      days[k].setText(names[k]);
      days[k].setTextSize(11);
      days[k].setButtonDrawable(null);
      days[k].setGravity(Gravity.CENTER);
      days[k].setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
      days[k].setChecked((a.days & (1 << k)) != 0);
      days[k].setTextColor(days[k].isChecked() ? 0xffffffff : Ui.INK);
      days[k].setBackground(Ui.bg(days[k].isChecked() ? Ui.TEAL : Ui.PALE, Ui.dp(this, 8)));
      row.addView(days[k], new LinearLayout.LayoutParams(0, Ui.dp(this, 48), 1));
      days[k].setOnCheckedChangeListener(
          (b, on) -> {
            b.setTextColor(on ? 0xffffffff : Ui.INK);
            b.setBackground(Ui.bg(on ? Ui.TEAL : Ui.PALE, Ui.dp(this, 8)));
          });
    }
    root.addView(
        Ui.text(
            this,
            "Repeating alarms use the weekdays above; the date applies only to a one-time alarm.",
            12,
            Ui.MUTED));
    Ui.gap(root, 12);
    Ui.gap(root, 12);
    root.addView(Ui.title(this, "When it rings…", 23));
    Ui.gap(root, 6);
    Spinner kinds = new Spinner(this);
    kinds.setBackground(null);
    BaseAdapter kindAdapter = ActionTheme.createAdapter(this, () -> draft.kind);
    kinds.setAdapter(kindAdapter);
    kinds.setSelection(a.kind);
    LinearLayout.LayoutParams kindLp = new LinearLayout.LayoutParams(-1, -2);
    kindLp.bottomMargin = Ui.dp(this, 12);
    root.addView(kinds, kindLp);
    actionFields = Ui.column(this);
    root.addView(actionFields);
    fields();
    kinds.setOnItemSelectedListener(
        new android.widget.AdapterView.OnItemSelectedListener() {
          public void onNothingSelected(android.widget.AdapterView<?> p) {}

          public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
            if (pos != draft.kind) {
              draft.kind = pos;
              draft.target = "";
              draft.targetName = "";
              draft.body = "";
              draft.subject = "";
              draft.mime = "";
              kindAdapter.notifyDataSetChanged();
              fields();
            }
          }
        });
    Ui.gap(root, 16);
    root.addView(Ui.title(this, "Sound & feel", 23));
    toneButton =
        Ui.button(
            root,
            a.tone.isEmpty() ? "Tone · Default alarm" : "Tone · Custom",
            false,
            () -> {
              Intent i =
                  new Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                      .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                      .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                      .putExtra(
                          RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                          draft.tone.isEmpty()
                              ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                              : Uri.parse(draft.tone));
              try {
                startActivityForResult(i, 12);
              } catch (Exception e) {
                Ui.error(
                    this,
                    "No tone picker is available on this phone. The default alarm tone will be"
                        + " used.");
              }
            });
    TextView volumeLabel =
        Ui.text(this, "Tone level · " + a.volume + "% of system alarm volume", 13, Ui.MUTED);
    root.addView(volumeLabel);
    volume = new SeekBar(this);
    volume.setMin(10);
    volume.setMax(100);
    volume.setProgress(a.volume);
    root.addView(volume);
    volume.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          public void onStartTrackingTouch(SeekBar s) {}

          public void onStopTrackingTouch(SeekBar s) {}

          public void onProgressChanged(SeekBar s, int p, boolean user) {
            volumeLabel.setText("Tone level · " + p + "% of system alarm volume");
          }
        });
    vibration = new Switch(this);
    vibration.setText("Vibrate");
    vibration.setChecked(a.vibrate);
    root.addView(vibration, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
    Ui.gap(root, 18);
    Ui.button(root, "Save alarm", true, () -> save());
    if (a.id != 0)
      Ui.button(
          root,
          "Delete alarm",
          false,
          () ->
              new AlertDialog.Builder(this)
                  .setTitle("Delete this alarm?")
                  .setMessage(a.title)
                  .setNegativeButton("Keep", null)
                  .setPositiveButton(
                      "Delete",
                      (d, w) -> {
                        Scheduler.cancel(this, a.id);
                        stopRinging(a.id);
                        Store.remove(this, a.id);
                        home();
                      })
                  .show());
  }

  private void fields() {
    actionFields.removeAllViews();
    target = null;
    body = null;
    subject = null;
    quote = null;
    attachmentList = null;
    selection = null;
    int k = draft.kind;
    if (k == 0) {
      Ui.button(
          actionFields,
          "Choose a quote",
          false,
          () -> showQuoteCategories());
      quote =
          Ui.field(
              actionFields,
              "Quote · choose one or write your own (optional)",
              draft.quote,
              InputType.TYPE_CLASS_TEXT
                  | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                  | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
      actionFields.addView(
          Ui.text(this, "Your quote appears when this alarm rings.", 13, Ui.MUTED));
    }
    if (k == 1 || k == 2) {
      target = Ui.field(actionFields, "Phone number", draft.target, InputType.TYPE_CLASS_PHONE);
      Ui.button(actionFields, "Choose from contacts", false, () -> pickContact(false));
    }
    if (k == 3) {
      target =
          Ui.field(
              actionFields,
              "Email address",
              draft.target,
              InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
      Ui.button(actionFields, "Choose from contacts", false, () -> pickContact(true));
      subject =
          Ui.field(
              actionFields,
              "Subject (optional)",
              draft.subject,
              InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }
    if (k == 2 || k == 3)
      body =
          Ui.field(
              actionFields,
              "Message (optional)",
              draft.body,
              InputType.TYPE_CLASS_TEXT
                  | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                  | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    if (k == 3) {
      Ui.button(
          actionFields,
          "Add email attachments",
          false,
          () -> {
            Intent pick =
                new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .setType("*/*")
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    .addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(pick, 15);
          });
      attachmentList = Ui.column(this);
      actionFields.addView(attachmentList);
      renderAttachments();
      actionFields.addView(
          Ui.text(this, "Up to 10 files. Your email app's size limits apply.", 13, Ui.MUTED));
    }
    if (k == 4)
      selection =
          Ui.button(
              actionFields,
              draft.target.isEmpty() ? "Choose a document or image" : draft.targetName,
              false,
              () -> {
                Intent i =
                    new Intent(Intent.ACTION_OPEN_DOCUMENT)
                        .setType("*/*")
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .addFlags(
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(i, 10);
              });
    if (k == 5) {
      target =
          Ui.field(
              actionFields,
              "Destination · address or place name",
              draft.target,
              InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
      selection =
          Ui.button(
              actionFields,
              draft.targetName.isEmpty() ? "Search & choose on map" : "Map · " + draft.targetName,
              false,
              () ->
                  startActivityForResult(
                      new Intent(this, MapActivity.class)
                          .putExtra("destination", target.getText().toString().trim())
                          .putExtra("label", draft.targetName),
                      16));
    }
    if (k == 6)
      selection =
          Ui.button(
              actionFields,
              draft.target.isEmpty() ? "Choose an installed app" : draft.targetName,
              false,
              () -> pickApp());
    if (k == 7)
      target =
          Ui.field(
              actionFields,
              "Website · https://example.com",
              draft.target,
              InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    if (k == 8) {
      target =
          Ui.field(
              actionFields,
              "City · e.g. Dhaka",
              draft.target,
              InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
      actionFields.addView(
          Ui.text(
              this,
              "After you confirm the alarm, choose your city match to load today's weather and a"
                  + " rain suggestion. Uses Open-Meteo; internet required.",
              13,
              Ui.MUTED));
    }
    if (k == 1 || k == 2 || k == 3)
      actionFields.addView(
          Ui.text(
              this,
              "Opens the dialer or composer with your details. You confirm the call or send.",
              13,
              Ui.MUTED));
  }

  private void showQuoteCategories() {
    String[] items = Quotes.categoryNames();
    ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(this, 0, items) {
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView instanceof TextView) {
              tv = (TextView) convertView;
            } else {
              tv = new TextView(getContext());
              tv.setTextSize(16);
              tv.setTextColor(Ui.INK);
              int hPad = Ui.dp(getContext(), 18);
              int vPad = Ui.dp(getContext(), 15);
              tv.setPadding(hPad, vPad, hPad, vPad);
            }
            tv.setText(getItem(position));
            if (position == items.length - 1) {
              tv.setTextColor(0xffc0392b);
              tv.setBackgroundColor(0xfffff0f0);
            } else {
              tv.setTextColor(Ui.INK);
              int bg = (position % 2 == 0) ? 0xffeaf4f3 : 0xfffff7ec;
              tv.setBackgroundColor(bg);
            }
            return tv;
          }
        };

    new AlertDialog.Builder(this)
        .setTitle("Choose a category")
        .setAdapter(
            adapter,
            (d, which) -> {
              if (which >= Quotes.CATEGORIES.length) {
                draft.quote = "";
                if (quote != null) quote.setText("");
              } else {
                showQuotesInCategory(Quotes.CATEGORIES[which]);
              }
            })
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void showQuotesInCategory(Quotes.Category cat) {
    ArrayAdapter<String> adapter =
        new ArrayAdapter<String>(this, 0, cat.quotes) {
          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView instanceof TextView) {
              tv = (TextView) convertView;
            } else {
              tv = new TextView(getContext());
              tv.setTextSize(15);
              tv.setTextColor(Ui.INK);
              tv.setLineSpacing(Ui.dp(getContext(), 2), 1.25f);
              int hPad = Ui.dp(getContext(), 18);
              int vPad = Ui.dp(getContext(), 14);
              tv.setPadding(hPad, vPad, hPad, vPad);
            }
            tv.setText(getItem(position));
            tv.setTextColor(Ui.INK);
            int bg = (position % 2 == 0) ? 0xffeaf4f3 : 0xfffff7ec;
            tv.setBackgroundColor(bg);
            return tv;
          }
        };

    new AlertDialog.Builder(this)
        .setTitle(cat.name)
        .setAdapter(
            adapter,
            (d, which) -> {
              draft.quote = cat.quotes[which];
              if (quote != null) quote.setText(draft.quote);
            })
        .setNeutralButton("‹ Back", (d, which) -> showQuoteCategories())
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void pickContact(boolean email) {
    try {
      startActivityForResult(
          new Intent(
              Intent.ACTION_PICK,
              email
                  ? ContactsContract.CommonDataKinds.Email.CONTENT_URI
                  : ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
          email ? 14 : 13);
    } catch (ActivityNotFoundException e) {
      Ui.error(this, "No contacts picker is available. You can enter the details directly.");
    }
  }

  private void deleteAlarm(Alarm alarm) {
    new AlertDialog.Builder(this)
        .setTitle("Delete this alarm?")
        .setMessage(alarm.title + "\nThis also cancels any pending snooze.")
        .setNegativeButton("Keep", null)
        .setPositiveButton(
            "Delete",
            (d, w) -> {
              Scheduler.cancel(this, alarm.id);
              stopRinging(alarm.id);
              Store.remove(this, alarm.id);
              home();
            })
        .show();
  }

  private void feedback() {
    try {
      startActivity(
          new Intent(
                  Intent.ACTION_SENDTO,
                  Uri.parse("mailto:asmksn@gmail.com?subject=Beyond%20Alarm%20feedback"))
              .putExtra(Intent.EXTRA_EMAIL, new String[] {"asmksn@gmail.com"})
              .putExtra(Intent.EXTRA_SUBJECT, "Beyond Alarm feedback"));
    } catch (ActivityNotFoundException e) {
      new AlertDialog.Builder(this)
          .setTitle("Suggestions & feedback")
          .setMessage("Email your suggestions or questions to asmksn@gmail.com.")
          .setPositiveButton(
              "Copy email",
              (d, w) -> {
                getSystemService(android.content.ClipboardManager.class)
                    .setPrimaryClip(ClipData.newPlainText("Feedback email", "asmksn@gmail.com"));
                Toast.makeText(this, "Email address copied", Toast.LENGTH_SHORT).show();
              })
          .setNegativeButton("Close", null)
          .show();
    }
  }

  private void renderAttachments() {
    if (attachmentList == null) return;
    attachmentList.removeAllViews();
    if (draft.attachments.isEmpty())
      attachmentList.addView(Ui.text(this, "No attachments selected", 13, Ui.MUTED));
    for (Alarm.Attachment file : new ArrayList<>(draft.attachments)) {
      attachmentList.addView(Ui.text(this, file.name, 15, Ui.INK));
      Ui.button(
          attachmentList,
          "Remove " + file.name,
          false,
          () -> {
            draft.attachments.remove(file);
            renderAttachments();
          });
    }
  }

  private void pickApp() {
    List<ResolveInfo> apps =
        getPackageManager()
            .queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0);
    apps.removeIf(r -> r.activityInfo.packageName.equals(getPackageName()));
    apps.sort(
        (a, b) ->
            a.loadLabel(getPackageManager())
                .toString()
                .compareToIgnoreCase(b.loadLabel(getPackageManager()).toString()));
    String[] names = new String[apps.size()];
    for (int j = 0; j < apps.size(); j++)
      names[j] = apps.get(j).loadLabel(getPackageManager()).toString();
    new AlertDialog.Builder(this)
        .setTitle("Choose an app")
        .setItems(
            names,
            (d, which) -> {
              draft.target = apps.get(which).activityInfo.packageName;
              draft.targetName = names[which];
              selection.setText(draft.targetName);
            })
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void collect() {
    draft.title = title.getText().toString().trim();
    if (target != null) {
      String value = target.getText().toString().trim();
      if (draft.kind == 5 && !value.equals(draft.target)) draft.targetName = "";
      draft.target = value;
    }
    if (body != null) draft.body = body.getText().toString();
    if (subject != null) draft.subject = subject.getText().toString();
    if (quote != null) draft.quote = quote.getText().toString().trim();
    draft.days = 0;
    for (int i = 0; i < 7; i++) if (days[i].isChecked()) draft.days |= 1 << i;
    draft.vibrate = vibration.isChecked();
    draft.volume = volume.getProgress();
  }

  private void save() {
    collect();
    if (draft.title.isEmpty()) draft.title = Alarm.KINDS[draft.kind];
    if (draft.kind != 0 && draft.target.isEmpty()) {
      Ui.error(this, "Choose the details for this alarm's action.");
      return;
    }
    if ((draft.kind == 1 || draft.kind == 2) && !draft.target.matches("[+0-9() .-]{3,30}")) {
      Ui.error(this, "Enter a valid phone number.");
      return;
    }
    if (draft.kind == 3 && !android.util.Patterns.EMAIL_ADDRESS.matcher(draft.target).matches()) {
      Ui.error(this, "Enter a valid email address.");
      return;
    }
    if (draft.kind == 7) {
      if (!draft.target.contains("://")) draft.target = "https://" + draft.target;
      Uri u = Uri.parse(draft.target);
      if ((!"https".equals(u.getScheme()) && !"http".equals(u.getScheme()))
          || u.getHost() == null
          || u.getHost().isEmpty()) {
        Ui.error(this, "Enter an http or https website address.");
        return;
      }
    }
    if (!Scheduler.allowed(this) || !notifications()) {
      new AlertDialog.Builder(this)
          .setTitle("Allow alarm access first")
          .setMessage(
              "Your draft is kept while you enable access in Android settings. Return here and save"
                  + " again.")
          .setPositiveButton(
              "Open settings",
              (d, w) -> {
                if (!Scheduler.allowed(this))
                  launchSettings(
                      new Intent(
                          Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                          Uri.parse("package:" + getPackageName())));
                else if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED)
                  requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 42);
                else
                  launchSettings(
                      new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                          .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
              })
          .setNegativeButton("Keep editing", null)
          .show();
      return;
    }
    long now = System.currentTimeMillis();
    if (draft.days == 0) {
      Calendar c = Calendar.getInstance();
      c.setTimeInMillis(chosenDate);
      c.set(Calendar.HOUR_OF_DAY, draft.hour);
      c.set(Calendar.MINUTE, draft.minute);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      draft.at = c.getTimeInMillis();
      if (draft.at <= now) {
        Ui.error(this, "This time has passed. Choose a future date or time.");
        return;
      }
    } else draft.at = Times.next(draft.hour, draft.minute, draft.days, now);
    if (draft.id == 0) draft.id = Store.newId(this);
    draft.enabled = true;
    draft.snoozeAt = 0;
    try {
      Scheduler.schedule(this, draft);
      Store.put(this, draft);
      Toast.makeText(this, "Alarm set for " + date(draft.at) + " at " + time(draft.at), 1).show();
      home();
    } catch (Exception e) {
      Ui.error(this, e.getMessage());
    }
  }

  private void stopRinging(int id) {
    if (AlarmService.ringing.contains(id))
      startService(new Intent(this, AlarmService.class).setAction("dismiss").putExtra("id", id));
  }

  @Override
  protected void onActivityResult(int r, int result, Intent data) {
    super.onActivityResult(r, result, data);
    if (result != RESULT_OK || data == null || draft == null) return;
    if (r == 16) {
      draft.target = data.getStringExtra("destination");
      draft.targetName = data.getStringExtra("label");
      if (target != null) target.setText(draft.target);
      if (selection != null) selection.setText("Map · " + draft.targetName);
      return;
    }
    if (r == 15) {
      ArrayList<Uri> files = new ArrayList<>();
      if (data.getClipData() != null)
        for (int n = 0; n < data.getClipData().getItemCount(); n++)
          files.add(data.getClipData().getItemAt(n).getUri());
      else if (data.getData() != null) files.add(data.getData());
      for (Uri uri : files) {
        if (draft.attachments.size() >= 10) {
          Ui.error(this, "You can attach up to 10 files to an alarm.");
          break;
        }
        boolean exists = false;
        for (Alarm.Attachment f : draft.attachments)
          if (f.uri.equals(uri.toString())) exists = true;
        if (exists) continue;
        try {
          getContentResolver()
              .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
          String name = "Attachment", mime = getContentResolver().getType(uri);
          try (Cursor cursor =
              getContentResolver()
                  .query(uri, new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
          }
          draft.attachments.add(
              new Alarm.Attachment(
                  uri.toString(), name, mime == null ? "application/octet-stream" : mime));
        } catch (Exception e) {
          Ui.error(this, "Cannot keep access to this file. Choose it from local storage instead.");
        }
      }
      renderAttachments();
      return;
    }
    if ((r == 13 || r == 14) && data.getData() != null) {
      String column =
          r == 13
              ? ContactsContract.CommonDataKinds.Phone.NUMBER
              : ContactsContract.CommonDataKinds.Email.ADDRESS;
      try (Cursor c =
          getContentResolver().query(data.getData(), new String[] {column}, null, null, null)) {
        if (c != null && c.moveToFirst()) {
          draft.target = c.getString(0);
          if (target != null) target.setText(draft.target);
        }
      } catch (Exception e) {
        Ui.error(this, "Could not read that contact. Please enter the details directly.");
      }
    } else if (r == 10 && data.getData() != null) {
      Uri u = data.getData();
      try {
        getContentResolver().takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION);
      } catch (SecurityException e) {
        Ui.error(this, "This provider cannot keep file access. Choose a file from local storage.");
        return;
      }
      draft.target = u.toString();
      draft.mime = getContentResolver().getType(u);
      if (draft.mime == null) draft.mime = "application/octet-stream";
      draft.targetName = "Selected document";
      try (Cursor c =
          getContentResolver()
              .query(u, new String[] {OpenableColumns.DISPLAY_NAME}, null, null, null)) {
        if (c != null && c.moveToFirst()) draft.targetName = c.getString(0);
      } catch (Exception ignored) {
      }
      if (selection != null) selection.setText(draft.targetName);
    } else if (r == 12) {
      Uri u = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
      draft.tone = u == null ? "" : u.toString();
      toneButton.setText(draft.tone.isEmpty() ? "Tone · Default alarm" : "Tone · Custom");
    }
  }
}