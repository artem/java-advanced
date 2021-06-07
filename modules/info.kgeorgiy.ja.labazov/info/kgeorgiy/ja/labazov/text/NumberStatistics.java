package info.kgeorgiy.ja.labazov.text;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.Locale;

public class NumberStatistics extends UnitStatistics {
    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
    private final HashSet<Double> distinct = new HashSet<>();

    public NumberStatistics(final Locale locale) {
        super(locale);
    }

    @Override
    public boolean accept(String str) {
        return accept(str, 0);
    }

    public boolean accept(final String str, final int pos) {
        return accept(str, new ParsePosition(pos));
    }

    public boolean accept(final String str, final ParsePosition pos) {
        final NumberFormat formatter = NumberFormat.getInstance(locale);
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
}
