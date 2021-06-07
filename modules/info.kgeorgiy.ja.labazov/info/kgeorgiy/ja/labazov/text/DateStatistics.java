package info.kgeorgiy.ja.labazov.text;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateStatistics extends NumberStatistics {
    public DateStatistics(final Locale locale) {
        super(locale);
    }

    @Override
    public boolean accept(final String str, final ParsePosition pos) {
        final List<Integer> modes = List.of(DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT);

        for (int mode : modes) {
            final DateFormat formatter = DateFormat.getDateInstance(mode, locale);
            final Date date = formatter.parse(str, pos);
            if (date != null) {
                accept(date);
                return true;
            }
        }
        return false;
    }

    public void accept(final Date date) {
        super.accept(date.getTime());
    }

    public Date getAverageDate() {
        return new Date((long) super.getAverage());
    }
}
