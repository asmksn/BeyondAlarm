package com.kibria.dynamicalarm;

import java.time.*;

public final class Times {
  // Bits 0..6 are Monday..Sunday. Missing DST times shift forward; overlaps use the first
  // occurrence.
  public static long next(int hour, int minute, int days, long now) {
    ZoneId zone = ZoneId.systemDefault();
    LocalDate date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
    for (int i = 0; i < 8; i++) {
      LocalDate candidate = date.plusDays(i);
      int bit = candidate.getDayOfWeek().getValue() - 1;
      long at = candidate.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli();
      if (at > now && (days == 0 || (days & (1 << bit)) != 0)) return at;
    }
    throw new IllegalStateException("No next occurrence");
  }
}
