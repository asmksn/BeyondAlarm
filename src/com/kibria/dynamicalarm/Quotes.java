package com.kibria.dynamicalarm;

public final class Quotes {

  public static class Category {
    public final String name;
    public final String[] quotes;

    public Category(String name, String[] quotes) {
      this.name = name;
      this.quotes = quotes;
    }
  }

  public static final Category[] CATEGORIES = new Category[] {
    new Category(
        "Islamic",
        new String[] {
          "\"Prayer is better than sleep.\" (Fajr adhan, Abu Dawud 500)",
          "\"The two rak'ahs of Fajr are better than the world and all it contains.\" (Muslim 725)",
          "\"Whoever prays Fajr is under the protection of Allah.\" (Muslim 657)",
          "\"Whoever prays the two cool prayers (Fajr and Asr) will enter Paradise.\" (Bukhari 574, Muslim 635)",
          "\"Indeed, the recitation of dawn is ever witnessed.\" (Quran 17:78)",
          "\"When he wakes and remembers Allah, one knot is undone. When he makes wudu, another. When he prays, the last.\" (Bukhari 1142, abridged)",
          "\"Who is calling upon Me that I may answer him?\" (Allah's call in the last third of the night, Bukhari 1145, Muslim 758)",
          "\"Establish prayer for My remembrance.\" (Quran 20:14)",
          "\"Indeed, prayer prohibits immorality and wrongdoing.\" (Quran 29:45)",
          "\"The most beloved deed to Allah is prayer at its proper time.\" (Bukhari 527, Muslim 85)",
          "\"Indeed, prayer has been decreed upon the believers a decree of specified times.\" (Quran 4:103)",
          "\"By time. Indeed, mankind is in loss, except those who believe and do righteous deeds.\" (Quran 103:1–3, abridged)",
          "\"Two blessings many people lose: health and free time.\" (Bukhari 6412)"
        }),
    new Category(
        "Morning motivation",
        new String[] {
          "\"Success comes to those who have the willpower to win over their snooze buttons.\"",
          "\"The secret of getting ahead is getting started.\"",
          "\"Rise up, start fresh, see the bright opportunity in each new day.\"",
          "\"Your future is created by what you do today, not tomorrow.\"",
          "\"Energy flows where attention goes. Win the morning, win the day.\"",
          "\"Today's effort is tomorrow's victory. Make every hour count.\""
        }),
    new Category(
        "A fresh start",
        new String[] {
          "\"A new day is a new chance. Begin with one good step.\"",
          "\"Every morning we are born again. What we do today matters most.\"",
          "\"No matter how difficult yesterday was, today is a clean page.\"",
          "\"Mistakes belong to the past. Today is full of possibilities.\"",
          "\"It is never too late to be what you might have been.\""
        }),
    new Category(
        "Small steps",
        new String[] {
          "\"Small steps, taken every day, lead to meaningful change.\"",
          "\"Big journeys begin with tiny, consistent strides.\"",
          "\"Focus on making today just 1% better than yesterday.\"",
          "\"Great things are not done by impulse, but by a series of small things brought together.\"",
          "\"Patience and daily persistence overcome the tallest mountains.\""
        }),
    new Category(
        "Time for yourself",
        new String[] {
          "\"Make a little time today for the person you want to become.\"",
          "\"Self-care is not selfish; it is essential to show up at your best.\"",
          "\"Breathe, pause, and give yourself space before the day rushes in.\"",
          "\"Invest in your own peace of mind first.\"",
          "\"Guard your morning quiet time; it sets the tone for your day.\""
        }),
    new Category(
        "Gratitude",
        new String[] {
          "\"Start your morning with a grateful heart.\"",
          "\"Gratitude turns what we have into enough.\"",
          "\"When you appreciate the good in your life, the good increases.\"",
          "\"Be thankful for another sunrise, another breath, another opportunity.\"",
          "\"Acknowledging the good you already have is the foundation for all abundance.\""
        }),
    new Category(
        "Focus",
        new String[] {
          "\"Give this moment your attention. Your next step matters.\"",
          "\"Starve your distractions, feed your focus.\"",
          "\"Do not look at how far you have left to go; focus on the next step.\"",
          "\"Simplicity and focus are the keys to extraordinary results.\"",
          "\"Where your focus goes, your energy flows.\""
        })
  };

  public static String[] categoryNames() {
    String[] names = new String[CATEGORIES.length + 1];
    for (int i = 0; i < CATEGORIES.length; i++) {
      names[i] = CATEGORIES[i].name;
    }
    names[CATEGORIES.length] = "Clear quote";
    return names;
  }

  public static final String[] LABELS = categoryNames();

  public static final String[] TEXT = new String[] {
    CATEGORIES[0].quotes[0],
    CATEGORIES[1].quotes[0],
    CATEGORIES[2].quotes[0],
    CATEGORIES[3].quotes[0],
    CATEGORIES[4].quotes[0],
    CATEGORIES[5].quotes[0],
    CATEGORIES[6].quotes[0],
    ""
  };
}
