package com.kibria.dynamicalarm;

import android.content.Context;
import java.util.*;
import org.json.*;

public final class Store {
  public static synchronized ArrayList<Alarm> all(Context c) {
    ArrayList<Alarm> result = new ArrayList<>();
    try {
      JSONArray arr = new JSONArray(c.getSharedPreferences("alarms", 0).getString("items", "[]"));
      for (int i = 0; i < arr.length(); i++) result.add(Alarm.from(arr.getJSONObject(i)));
    } catch (Exception ignored) {
    }
    return result;
  }

  public static Alarm get(Context c, int id) {
    for (Alarm a : all(c)) if (a.id == id) return a;
    return null;
  }

  public static synchronized int newId(Context c) {
    int id = c.getSharedPreferences("alarms", 0).getInt("sequence", 0) + 1;
    c.getSharedPreferences("alarms", 0).edit().putInt("sequence", id).commit();
    return id;
  }

  public static synchronized void put(Context c, Alarm a) {
    ArrayList<Alarm> list = all(c);
    list.removeIf(x -> x.id == a.id);
    list.add(a);
    write(c, list);
  }

  public static synchronized void remove(Context c, int id) {
    ArrayList<Alarm> list = all(c);
    list.removeIf(x -> x.id == id);
    write(c, list);
  }

  private static void write(Context c, List<Alarm> list) {
    JSONArray arr = new JSONArray();
    for (Alarm a : list) arr.put(a.json());
    if (!c.getSharedPreferences("alarms", 0).edit().putString("items", arr.toString()).commit())
      throw new IllegalStateException("Could not save alarm");
  }
}
