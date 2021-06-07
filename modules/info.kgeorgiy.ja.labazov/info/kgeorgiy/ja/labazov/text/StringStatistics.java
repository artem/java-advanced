package info.kgeorgiy.ja.labazov.text;

import java.text.Collator;
import java.util.*;

public class StringStatistics extends UnitStatistics {
    private final IntSummaryStatistics stats = new IntSummaryStatistics();
    private final TreeSet<String> distinct;
    private final TreeSet<String> length;

    public StringStatistics(final Locale locale) {
        super(locale);
        final Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.PRIMARY);
        distinct = new TreeSet<>(collator); //todo optimize memory
        length = new TreeSet<>(Comparator.comparingInt(String::length));
    }

    @Override
    public boolean accept(final String str) {
        stats.accept(str.length());
        distinct.add(str);
        length.add(str);
        return true;
    }

    @Override
    public long getAmount() {
        return stats.getCount();
    }

    @Override
    public long getDistinct() {
        return distinct.size();
    }

    @Override
    public double getAverage() {
        return stats.getAverage();
    }

    public String getMin() {
        return distinct.first();
    }

    public String getMax() {
        return distinct.last();
    }

    public String getShortest() {
        return length.first();
    }

    public String getLongest() {
        return length.last();
    }

    public static boolean isWord(final String str, final int startPos, final int endPos) {
        for (int p = startPos; p < endPos; p++) {
            if (Character.isLetter(str.codePointAt(p)))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringStatistics that = (StringStatistics) o;
        return stats.equals(that.stats) && distinct.equals(that.distinct) && length.equals(that.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stats, distinct, length);
    }
}
