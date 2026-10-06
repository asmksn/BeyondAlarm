package com.kibria.dynamicalarm;

import android.app.*;
import android.net.Uri;
import android.widget.*;
import java.io.*;
import java.net.*;
import org.json.*;

final class Weather {
  private static JSONObject get(String address) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
    connection.setConnectTimeout(12000);
    connection.setReadTimeout(12000);
    try (InputStream in = connection.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] b = new byte[4096];
      int n;
      while ((n = in.read(b)) != -1) out.write(b, 0, n);
      return new JSONObject(out.toString("UTF-8"));
    } finally {
      connection.disconnect();
    }
  }

  static void show(Activity c, String city, Runnable done) {
    LinearLayout l = Ui.page(c);
    l.addView(Ui.text(c, "YOUR MORNING, AT A GLANCE", 12, Ui.TEAL));
    l.addView(Ui.title(c, "Good morning.", 36));
    TextView status = Ui.text(c, "Finding " + city + "…", 17, Ui.MUTED);
    l.addView(status);
    Ui.button(l, "Back to alarms", false, done);
    new Thread(
            () -> {
              try {
                JSONArray places =
                    get("https://geocoding-api.open-meteo.com/v1/search?name="
                            + Uri.encode(city)
                            + "&count=5&language=en&format=json")
                        .optJSONArray("results");
                if (places == null || places.length() == 0) throw new IOException("City not found");
                String[] names = new String[places.length()];
                for (int i = 0; i < names.length; i++) {
                  JSONObject p = places.getJSONObject(i);
                  names[i] =
                      p.getString("name")
                          + ", "
                          + p.optString("admin1")
                          + ", "
                          + p.optString("country");
                }
                c.runOnUiThread(
                    () -> {
                      if (c.isFinishing() || c.isDestroyed()) return;
                      status.setText("Choose your city to see today's forecast.");
                      new AlertDialog.Builder(c)
                          .setTitle("Which " + city + "?")
                          .setItems(
                              names,
                              (d, index) -> {
                                try {
                                  forecast(c, l, status, places.getJSONObject(index), names[index]);
                                } catch (Exception e) {
                                  status.setText("Could not read city details. Please try again.");
                                }
                              })
                          .setNegativeButton("Cancel", (d, w) -> done.run())
                          .setOnCancelListener(d -> done.run())
                          .show();
                    });
              } catch (Exception e) {
                c.runOnUiThread(
                    () -> {
                      if (!c.isDestroyed())
                        status.setText(
                            "Couldn't find your city. Check your connection or edit the alarm to"
                                + " use a city name such as Dhaka. Your alarm still rang"
                                + " normally.");
                    });
              }
            },
            "weather-city")
        .start();
  }

  private static void forecast(
      Activity c, LinearLayout l, TextView status, JSONObject place, String name) throws Exception {
    status.setText("Loading today's forecast for " + name + "…");
    String url =
        "https://api.open-meteo.com/v1/forecast?latitude="
            + place.getDouble("latitude")
            + "&longitude="
            + place.getDouble("longitude")
            + "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=auto&forecast_days=1";
    new Thread(
            () -> {
              try {
                JSONObject data = get(url),
                    now = data.getJSONObject("current"),
                    day = data.getJSONObject("daily");
                double temp = now.getDouble("temperature_2m"),
                    high = day.getJSONArray("temperature_2m_max").getDouble(0),
                    low = day.getJSONArray("temperature_2m_min").getDouble(0);
                int rain = day.getJSONArray("precipitation_probability_max").getInt(0);
                String date = day.getJSONArray("time").getString(0);
                c.runOnUiThread(
                    () -> {
                      if (c.isFinishing() || c.isDestroyed()) return;
                      status.setText(name + "\n" + date + " · Local forecast");
                      LinearLayout card = Ui.card(l, 0xffffffff);
                      card.addView(Ui.title(c, Math.round(temp) + "°C", 60));
                      card.addView(
                          Ui.text(
                              c,
                              "High " + Math.round(high) + "°  /  Low " + Math.round(low) + "°",
                              19,
                              Ui.MUTED));
                      card.addView(Ui.title(c, rain + "% chance of precipitation", 20));
                      card.addView(
                          Ui.text(
                              c,
                              rain >= 40
                                  ? "Take an umbrella before you head out."
                                  : high >= 32
                                      ? "A hot day ahead. Bring water and plan for shade."
                                      : low <= 12
                                          ? "A cool start. You may want an extra layer."
                                          : "A good moment to check your plans for the day.",
                              18,
                              Ui.TEAL));
                      card.addView(
                          Ui.text(
                              c,
                              "Weather data by Open-Meteo.com · CC BY 4.0\n"
                                  + "Place data by GeoNames\n"
                                  + "Forecasts may change.",
                              12,
                              Ui.MUTED));
                    });
              } catch (Exception e) {
                c.runOnUiThread(
                    () -> {
                      if (!c.isDestroyed())
                        status.setText(
                            "Forecast unavailable. Check your connection and try again from your"
                                + " next alarm. Your alarm does not need internet to ring.");
                    });
              }
            },
            "weather-forecast")
        .start();
  }
}
