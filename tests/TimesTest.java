import com.kibria.dynamicalarm.Times;
import java.time.*;
import java.util.*;

public class TimesTest {
  static int count = 0;

  static void check(String expected, long actual) {
    count++;
    String got =
        Instant.ofEpochMilli(actual).atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
    if (!expected.equals(got)) throw new AssertionError("Expected " + expected + ", got " + got);
  }

  static long at(String date) {
    return LocalDateTime.parse(date).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"));
    check("2026-10-05T07:00", Times.next(7, 0, 0, at("2026-10-05T06:59:59")));
    check("2026-10-06T07:00", Times.next(7, 0, 0, at("2026-10-05T07:00:00")));
    check("2026-10-12T07:00", Times.next(7, 0, 1, at("2026-10-05T07:00:01")));
    check("2026-10-11T07:00", Times.next(7, 0, 64, at("2026-10-05T07:00:00")));
    check("2026-10-06T07:00", Times.next(7, 0, 31, at("2026-10-05T08:00:00")));
    check("2026-10-12T07:00", Times.next(7, 0, 31, at("2026-10-09T08:00:00")));
    check("2027-01-01T00:00", Times.next(0, 0, 127, at("2026-12-31T23:59:59")));
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    check("2026-03-08T07:00", Times.next(7, 0, 127, at("2026-03-07T08:00:00")));
    check("2026-11-01T07:00", Times.next(7, 0, 127, at("2026-10-31T08:00:00")));
    check("2026-03-08T03:30", Times.next(2, 30, 127, at("2026-03-07T08:00:00")));
    System.out.println(count + " scheduling tests passed.");
  }
}
