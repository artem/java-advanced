package info.kgeorgiy.ja.labazov.text;

import java.text.ParsePosition;
import java.util.Locale;

public abstract class UnitStatistics {
    protected final Locale locale;

    protected UnitStatistics(Locale locale) {
        this.locale = locale;
    }

//    public void accept(final String str, final int startPos, final int endPos) {
//        accept(str.substring(startPos, endPos));
//    }

    public abstract boolean accept(final String str);

    public abstract long getAmount();

    public abstract long getDistinct();

    public abstract double getAverage();
}
