package info.kgeorgiy.ja.labazov.text;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class DateStatistics extends NumberStatistics {
    private final List<DateFormat> modes;

    public DateStatistics(final Locale locale) {
        super(locale);
        modes = List.of(DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT).stream()
                .map(mode -> DateFormat.getDateInstance(mode, locale))
                .collect(Collectors.toList());
    }

    @Override
    public boolean accept(final String str, final ParsePosition pos) {
        for (final DateFormat formatter : modes) {
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

    public Date getMinDate() {
        return new Date((long) super.getMin());
    }

    public Date getMaxDate() {
        return new Date((long) super.getMax());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DateStatistics that = (DateStatistics) o;
        return modes.equals(that.modes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), modes);
    }
}
