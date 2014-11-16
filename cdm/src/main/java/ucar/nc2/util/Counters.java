package ucar.nc2.util;

import java.util.*;

/**
 * Describe
 *
 * @author caron
 * @since 11/15/2014
 */
public class Counters {
    List<Counter> counters = new ArrayList<>();
    Map<String, Counter> map = new HashMap<>();

    public void add(Counter c) {
      counters.add(c);
      map.put(c.getName(), c);
    }

    public void show (Formatter f) {
      for (Counter c : counters)
        c.show(f);
    }

    public Counter get(String name) {
      return map.get(name);
    }

    public void count(String name, int value) {
      CounterOfInt counter = (CounterOfInt) map.get(name);
      counter.count(value);
    }

    public void countS(String name, String value) {
      CounterOfString counter = (CounterOfString) map.get(name);
      counter.count(value);
    }

    public void addTo(Counters sub) {
      for (Counter subC : sub.counters) {
        Counter all = map.get(subC.getName());
        all.addTo(subC);
      }
    }

    public Counters makeSubCounters() {
      Counters result = new Counters();
      for (Counter c : counters) {
        if (c instanceof CounterOfInt)
          result.add( new CounterOfInt(c.getName()));
        else
          result.add( new CounterOfString(c.getName()));
      }
      return result;
    }

  public static interface Counter {
    public void show(Formatter f);
    public String getName();
    public void addTo(Counter sub);
    public int getUnique();
    public int getTotal();
    public Counter setShowRange(boolean showRange);
  }

  private static abstract class CounterAbstract implements Counter {
    protected String name;
    protected boolean showRange;

    public String getName() {
      return name;
    }

    public Counter setShowRange(boolean showRange) {
      this.showRange = showRange;
      return this;
    }
  }

  // a counter whose keys are ints
  public static class CounterOfInt extends CounterAbstract {
    private Map<Integer, Integer> set = new HashMap<>();

    public CounterOfInt(String name) {
      this.name = name;
    }

    public void reset() {
      set = new HashMap<>();
    }

    public void count(int value) {
      Integer count = set.get(value);
      if (count == null)
        set.put(value, 1);
      else
        set.put(value, count + 1);
    }

    public void addTo(Counter sub) {
      CounterOfInt subi = (CounterOfInt) sub;
      for (Map.Entry<Integer, Integer> entry : subi.set.entrySet()) {
        Integer count = this.set.get(entry.getKey());
        if (count == null)
          count = 0;
        set.put(entry.getKey(), count + entry.getValue());
      }
    }

    @Override
    public int getUnique() {
      return set.size();
    }

    @Override
    public int getTotal() {
      int total = 0;
      for (Map.Entry<Integer, Integer> entry : set.entrySet()) {
        total += entry.getValue();
      }
      return total;
    }

    public void show(Formatter f) {
      f.format("%n%s%n", name);
      java.util.List<Integer> list = new ArrayList<>(set.keySet());
      Collections.sort(list);
      for (int template : list) {
        int count = set.get(template);
        f.format("   %3d: count = %d%n", template, count);
      }
    }
  }

  // a counter whose keys are strings
  public static class CounterOfString extends CounterAbstract {
    private Map<String, Integer> set = new HashMap<>();
    private String range;

    public String getName() {
      return name;
    }

    public CounterOfString(String name) {
      this.name = name;
    }

    public void count(String value) {
      Integer count = set.get(value);
      if (count == null)
        set.put(value, 1);
      else
        set.put(value, count + 1);
    }

    public void addTo(Counter sub) {
      CounterOfString subs = (CounterOfString) sub;
      for (Map.Entry<String, Integer> entry : subs.set.entrySet()) {
        Integer count = this.set.get(entry.getKey());
        if (count == null)
          count = 0;
        set.put(entry.getKey(), count + entry.getValue());
      }
    }

    public void show(Formatter f) {
      f.format("%n%s%n", name);
      java.util.List<String> list = new ArrayList<>(set.keySet());
      Collections.sort(list);
      if (showRange) {
        int n = list.size();
        f.format("   %10s - %10s: count = %d%n", list.get(0), list.get(n-1), getUnique());

      } else {
        for (String key : list) {
          int count = set.get(key);
          f.format("   %10s: count = %d%n", key, count);
        }
      }
    }

    public String showRange() {
      if (range == null) {
        java.util.List<String> list = new ArrayList<>(set.keySet());
        Collections.sort(list);
        int n = list.size();
        Formatter f = new Formatter();
        f.format("%10s - %10s", list.get(0), list.get(n - 1));
        range = f.toString();
      }
      return range;
    }

     @Override
    public int getUnique() {
      return set.size();
    }

    @Override
    public int getTotal() {
      int total = 0;
      for (Map.Entry<String, Integer> entry : set.entrySet()) {
        total += entry.getValue();
      }
      return total;
    }

  }
}
