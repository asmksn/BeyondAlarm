package com.kibria.dynamicalarm;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class MapActivity extends Activity {
  public static final int SEARCH_FIELD = 2101,
      SEARCH_BUTTON = 2102,
      MAP_VIEW = 2103,
      USE_BUTTON = 2104;
  private final ExecutorService worker = Executors.newSingleThreadExecutor();
  private WebView map;
  private EditText query;
  private TextView status, chosen;
  private Button search, use;
  private boolean hasPoint, busy, loaded;
  private double latitude, longitude;
  private String label = "Selected map point";
  private static long lastRequest;
  private static final String ORIGIN = "https://appassets.androidplatform.net/";

  @Override
  public void onCreate(Bundle saved) {
    super.onCreate(saved);
    LinearLayout root = Ui.column(this);
    root.setBackgroundColor(Ui.BG);
    root.setPadding(Ui.dp(this, 16), Ui.dp(this, 8), Ui.dp(this, 16), Ui.dp(this, 12));
    setContentView(root);
    Ui.safeInsets(root);
    Ui.button(root, "‹  Back to alarm", false, () -> finish());
    root.addView(Ui.title(this, "Choose a destination", 24));
    query =
        Ui.field(
            root,
            "Search a place or address",
            getIntent().getStringExtra("destination"),
            InputType.TYPE_CLASS_TEXT);
    query.setId(SEARCH_FIELD);
    query.setSingleLine(true);
    query.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
    search = Ui.button(root, "Search map", false, () -> searchPlaces());
    search.setId(SEARCH_BUTTON);
    query.setOnEditorActionListener(
        (v, id, event) -> {
          searchPlaces();
          return true;
        });
    status =
        Ui.text(
            this,
            "Search for a place, or tap the map to drop a pin. Internet required.",
            12,
            Ui.MUTED);
    root.addView(status);
    map = new WebView(this);
    map.setId(MAP_VIEW);
    root.addView(map, new LinearLayout.LayoutParams(-1, 0, 1));
    chosen = Ui.text(this, "No destination selected", 14, Ui.INK);
    chosen.setMaxLines(2);
    chosen.setEllipsize(android.text.TextUtils.TruncateAt.END);
    root.addView(chosen);
    use =
        Ui.button(
            root,
            "Use this destination",
            true,
            () -> {
              if (!hasPoint) return;
              setResult(
                  RESULT_OK,
                  new Intent()
                      .putExtra("destination", coordinates(latitude, longitude))
                      .putExtra("label", label));
              finish();
            });
    use.setId(USE_BUTTON);
    use.setEnabled(false);
    if (saved != null) {
      hasPoint = saved.getBoolean("hasPoint");
      latitude = saved.getDouble("latitude");
      longitude = saved.getDouble("longitude");
      label = saved.getString("label", "Selected map point");
      query.setText(saved.getString("query", ""));
    } else {
      String dest = getIntent().getStringExtra("destination");
      double[] point = parseCoordinates(dest);
      if (point != null) {
        hasPoint = true;
        latitude = point[0];
        longitude = point[1];
        label = getIntent().getStringExtra("label");
        if (label == null || label.isEmpty()) label = "Selected map point";
        query.setText("");
      }
    }
    map.getSettings().setJavaScriptEnabled(true);
    map.getSettings().setAllowFileAccess(false);
    map.getSettings().setAllowContentAccess(false);
    map.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
    map.getSettings()
        .setUserAgentString(
            map.getSettings().getUserAgentString()
                + " DynamicAlarm/1.2 (contact: asmksn@gmail.com)");
    map.addJavascriptInterface(new Bridge(), "AlarmMap");
    map.setWebViewClient(
        new WebViewClient() {
          @Override
          public WebResourceResponse shouldInterceptRequest(
              WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if ("appassets.androidplatform.net".equals(uri.getHost())) {
              String path = uri.getPath();
              if (!Arrays.asList("/map.html", "/leaflet.js", "/leaflet.css").contains(path))
                return blocked();
              try {
                return new WebResourceResponse(
                    path.endsWith(".html")
                        ? "text/html"
                        : path.endsWith(".css") ? "text/css" : "application/javascript",
                    "UTF-8",
                    getAssets().open(path.substring(1)));
              } catch (IOException e) {
                return blocked();
              }
            }
            if ("https".equals(uri.getScheme()) && "tile.openstreetmap.org".equals(uri.getHost()))
              return null;
            return blocked();
          }

          @Override
          public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if ("appassets.androidplatform.net".equals(uri.getHost())) return false;
            if ("https".equals(uri.getScheme()))
              try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
              } catch (ActivityNotFoundException ignored) {
              }
            return true;
          }

          @Override
          public void onPageFinished(WebView view, String url) {
            loaded = true;
            if (hasPoint) showPoint(latitude, longitude, label);
          }
        });
    map.loadUrl(ORIGIN + "map.html");
    if (hasPoint) updateChoice();
  }

  private static WebResourceResponse blocked() {
    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
  }

  public static String coordinates(double lat, double lon) {
    return String.format(Locale.US, "%.6f,%.6f", lat, lon);
  }

  public static double[] parseCoordinates(String input) {
    if (input == null) return null;
    String[] pieces = input.trim().split(",");
    if (pieces.length != 2) return null;
    try {
      double lat = Double.parseDouble(pieces[0].trim()), lon = Double.parseDouble(pieces[1].trim());
      return valid(lat, lon) ? new double[] {lat, lon} : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static boolean valid(double lat, double lon) {
    return Double.isFinite(lat)
        && Double.isFinite(lon)
        && Math.abs(lat) <= 90
        && Math.abs(lon) <= 180;
  }

  private void updateChoice() {
    chosen.setText(label + "\n" + coordinates(latitude, longitude));
    use.setEnabled(true);
  }

  private void showPoint(double lat, double lon, String name) {
    hasPoint = true;
    latitude = lat;
    longitude = lon;
    label = name;
    updateChoice();
    if (loaded)
      map.evaluateJavascript(
          "setPoint(" + lat + "," + lon + "," + JSONObject.quote(name) + ",true)", null);
  }

  private class Bridge {
    @JavascriptInterface
    public void pick(double lat, double lon, String name) {
      if (!valid(lat, lon)) return;
      runOnUiThread(
          () -> {
            if (isDestroyed()) return;
            hasPoint = true;
            latitude = lat;
            longitude = lon;
            label =
                name == null
                    ? "Selected map point"
                    : name.substring(0, Math.min(name.length(), 250));
            updateChoice();
          });
    }

    @JavascriptInterface
    public void tileError() {
      runOnUiThread(
          () -> {
            if (!isDestroyed())
              status.setText(
                  "Some map tiles could not load. Check your connection, or go back and enter an"
                      + " address.");
          });
    }
  }

  private void searchPlaces() {
    String text = query.getText().toString().trim();
    if (text.length() < 2) {
      Ui.error(this, "Enter a place or address to search.");
      return;
    }
    double[] point = parseCoordinates(text);
    if (point != null) {
      showPoint(point[0], point[1], "Selected map point");
      return;
    }
    if (busy) return;
    getSystemService(InputMethodManager.class).hideSoftInputFromWindow(query.getWindowToken(), 0);
    android.content.SharedPreferences cache = getSharedPreferences("map-search", 0);
    String key = text.toLowerCase(Locale.ROOT), cached = cache.getString(key, "");
    if (!cached.isEmpty()
        && System.currentTimeMillis() - cache.getLong(key + ".time", 0) < 7 * 86400000L) {
      try {
        showResults(new JSONArray(cached));
        return;
      } catch (JSONException ignored) {
      }
    }
    if (SystemClock.elapsedRealtime() - lastRequest < 1500) {
      status.setText("Please wait a moment before searching again.");
      return;
    }
    lastRequest = SystemClock.elapsedRealtime();
    busy = true;
    search.setEnabled(false);
    status.setText("Searching for " + text + "…");
    worker.execute(
        () -> {
          HttpURLConnection connection = null;
          try {
            connection =
                (HttpURLConnection)
                    new URL(
                            "https://photon.komoot.io/api/?q="
                                + Uri.encode(text)
                                + "&limit=5&lang=en")
                        .openConnection();
            connection.setRequestProperty(
                "User-Agent", "DynamicAlarm/1.2 (contact: asmksn@gmail.com)");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(12000);
            String response;
            try (InputStream in = connection.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
              byte[] buffer = new byte[4096];
              int n;
              while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                if (out.size() > 1000000) throw new IOException("Response too large");
              }
              response = out.toString("UTF-8");
            }
            JSONArray features = new JSONObject(response).getJSONArray("features");
            if (features.length() > 0)
              cache
                  .edit()
                  .putString(key, features.toString())
                  .putLong(key + ".time", System.currentTimeMillis())
                  .apply();
            runOnUiThread(
                () -> {
                  if (!isDestroyed()) {
                    busy = false;
                    search.setEnabled(true);
                    showResults(features);
                  }
                });
          } catch (Exception error) {
            runOnUiThread(
                () -> {
                  if (!isDestroyed()) {
                    busy = false;
                    search.setEnabled(true);
                    status.setText(
                        "Search is unavailable. Try again, tap a point on the map, or enter the"
                            + " address in your alarm.");
                  }
                });
          } finally {
            if (connection != null) connection.disconnect();
          }
        });
  }

  private void showResults(JSONArray features) {
    if (features.length() == 0) {
      status.setText("No matches. Try a city, landmark, or a more complete address.");
      return;
    }
    try {
      String[] names = new String[features.length()];
      for (int n = 0; n < names.length; n++) {
        JSONObject p = features.getJSONObject(n).getJSONObject("properties");
        LinkedHashSet<String> parts = new LinkedHashSet<>();
        for (String field : new String[] {"name", "street", "city", "state", "country"})
          if (!p.optString(field).isEmpty()) parts.add(p.optString(field));
        names[n] = parts.isEmpty() ? "Search result " + (n + 1) : String.join(", ", parts);
      }
      status.setText(
          "Choose a result, then confirm the pin. Search by Photon; © OpenStreetMap contributors.");
      new AlertDialog.Builder(this)
          .setTitle("Choose a place")
          .setItems(
              names,
              (d, index) -> {
                try {
                  JSONArray coords =
                      features
                          .getJSONObject(index)
                          .getJSONObject("geometry")
                          .getJSONArray("coordinates");
                  double lat = coords.getDouble(1), lon = coords.getDouble(0);
                  if (valid(lat, lon)) showPoint(lat, lon, names[index]);
                } catch (JSONException e) {
                  Ui.error(this, "This result has no valid map point. Try another result.");
                }
              })
          .setNegativeButton("Cancel", null)
          .show();
    } catch (JSONException e) {
      status.setText("Could not read the search results. Try another search.");
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    state.putBoolean("hasPoint", hasPoint);
    state.putDouble("latitude", latitude);
    state.putDouble("longitude", longitude);
    state.putString("label", label);
    state.putString("query", query.getText().toString());
    super.onSaveInstanceState(state);
  }

  @Override
  protected void onDestroy() {
    worker.shutdownNow();
    map.removeJavascriptInterface("AlarmMap");
    map.destroy();
    super.onDestroy();
  }
}
