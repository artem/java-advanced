package info.kgeorgiy.ja.labazov.text;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

public class NumberStatistics extends UnitStatistics {
    private final NumberFormat formatter;
    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
    private final HashSet<Double> distinct = new HashSet<>();

    public NumberStatistics(final Locale locale) {
        super(locale);
        formatter = NumberFormat.getInstance(locale);
    }

    @Override
    public boolean accept(String str) {
        return accept(str, 0);
    }

    public boolean accept(final String str, final int pos) {
        return accept(str, new ParsePosition(pos));
    }

    public boolean accept(final String str, final ParsePosition pos) {
        final Number number = formatter.parse(str, pos);
        if (number != null) {
            accept(number.doubleValue());
            return true;
        }
        return false;
    }

    public void accept(final double value) {
        stats.accept(value);
        distinct.add(value);
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

    public double getMin() {
        return stats.getMin();
    }

    public double getMax() {
        return stats.getMax();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberStatistics that = (NumberStatistics) o;
        return formatter.equals(that.formatter) && stats.equals(that.stats) && distinct.equals(that.distinct);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatter, stats, distinct);
    }
}
