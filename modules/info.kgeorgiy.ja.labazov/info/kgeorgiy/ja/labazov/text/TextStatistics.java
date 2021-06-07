package info.kgeorgiy.ja.labazov.text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class TextStatistics {
    private final Locale textLocale;
    private final StringStatistics sentences;
    private final StringStatistics words;
    private final NumberStatistics numbers;
    private final NumberStatistics currency;
    private final DateStatistics dates;

    private final NumberFormat currencyFormatter;

    public TextStatistics(Locale textLocale) {
        this.textLocale = textLocale;
        sentences = new StringStatistics(textLocale);
        words = new StringStatistics(textLocale);
        numbers = new NumberStatistics(textLocale);
        currency = new NumberStatistics(textLocale);
        dates = new DateStatistics(textLocale);

        currencyFormatter = NumberFormat.getCurrencyInstance(textLocale);
    }

    public static void printAt(int pos, String source) { //todo
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(source);
        int end = boundary.following(pos);
        int start = boundary.previous();
        System.out.println(source.substring(start,end));
    }

    private boolean acceptCurrency(final String text, final ParsePosition position) { //TODO + perf

        final Number money = currencyFormatter.parse(text, position);
        if (money != null) {
            currency.accept(money.doubleValue());
            return true;
        }
        return false;
    }

    public void accept(final String text) {
        collectSentenceStats(text);
        collectTokenStats(text);
    }

    private void collectSentenceStats(final String source) {
        final BreakIterator boundary = BreakIterator.getSentenceInstance(textLocale);
        boundary.setText(source);

        int start = boundary.first();
        for (int end = boundary.next();
             end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            sentences.accept(source.substring(start, end));
        }
    }

    private void collectTokenStats(final String source) {
        final BreakIterator boundary = BreakIterator.getWordInstance(textLocale);
        boundary.setText(source);

        int start = boundary.first();
        for (int end = boundary.next();
             end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            final ParsePosition pos = new ParsePosition(start); //todo object creation

            final boolean tryParseExtra = dates.accept(source, pos) || acceptCurrency(source, pos) || numbers.accept(source, pos);

            if (tryParseExtra) {
                boundary.following(pos.getIndex());
                boundary.previous();
                continue;
            } else if (StringStatistics.isWord(source, start, end)) {
                words.accept(source.substring(start, end));
            }
        }
    }

    private static void exportStats(final TextStatistics stats, final Path outDir, final Locale locale) throws IOException {
        try (final BufferedWriter out = Files.newBufferedWriter(outDir)) {
            out.write("Сводная статистика");
            out.newLine();
            out.write(String.format("\tЧисло предложений: %d%n", stats.sentences.getAmount()));
            out.write(String.format("\tЧисло слов: %d%n", stats.words.getAmount()));
            out.write(String.format("\tЧисло чисел: %d%n", stats.numbers.getAmount()));
            out.write(String.format("\tЧисло сумм: %d%n", stats.currency.getAmount()));
            out.write(String.format("\tЧисло дат: %d%n", stats.dates.getAmount()));

            out.write("Статистика по предложениям");
            out.newLine();
            long am = stats.sentences.getAmount();
            out.write(String.format("\tЧисло предложений: %d (%d различных)%n", am, stats.sentences.getDistinct()));
            if (am != 0) {
                out.write(String.format("\tМинимальное предложение: \"%s\"%n", stats.sentences.getMin()));
                out.write(String.format("\tМаксимальное предложение: \"%s\"%n", stats.sentences.getMax()));
                out.write(String.format("\tМинимальная длина предложения: %d (\"%s\")%n", stats.sentences.getShortest().length(), stats.sentences.getShortest()));
                out.write(String.format("\tМаксимальная длина предложения: %d (\"%s\")%n", stats.sentences.getLongest().length(), stats.sentences.getLongest()));
                out.write(String.format("\tСредняя длина предложения: \"%f\"%n", stats.sentences.getAverage()));
            }

            out.write("Статистика по словам");
            out.newLine();
            am = stats.words.getAmount();
            out.write(String.format("\tЧисло слов: %d (%d различных)%n", am, stats.words.getDistinct()));
            if (am != 0) {
                out.write(String.format("\tМинимальное слово: \"%s\"%n", stats.words.getMin()));
                out.write(String.format("\tМаксимальное слово: \"%s\"%n", stats.words.getMax()));
                out.write(String.format("\tМинимальная длина слова: %d (\"%s\")%n", stats.words.getShortest().length(), stats.words.getShortest()));
                out.write(String.format("\tМаксимальная длина слова: %d (\"%s\")%n", stats.words.getLongest().length(), stats.words.getLongest()));
                out.write(String.format("\tСредняя длина слова: \"%f\"%n", stats.words.getAverage()));
            }

            out.write("Статистика по числам");
            out.newLine();
            am = stats.numbers.getAmount();
            out.write(String.format("\tЧисло чисел: %d (%d различных)%n", am, stats.numbers.getDistinct()));
            if (am != 0) {
                out.write(String.format("\tМинимальное число: \"%f\"%n", stats.numbers.getMin()));
                out.write(String.format("\tМаксимальное число: \"%f\"%n", stats.numbers.getMax()));
                out.write(String.format("\tСреднее число: \"%f\"%n", stats.numbers.getAverage()));
            }

            out.write("Статистика по суммам денег"); //todo format
            out.newLine();
            am = stats.currency.getAmount();
            out.write(String.format("\tЧисло сумм: %d (%d различных)%n", am, stats.currency.getDistinct()));
            if (am != 0) {
                final NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
                out.write(String.format("\tМинимальная сумма: \"%s\"%n", nf.format(stats.currency.getMin())));
                out.write(String.format("\tМаксимальная сумма: \"%s\"%n", nf.format(stats.currency.getMax())));
                out.write(String.format("\tСредняя сумма: \"%s\"%n", nf.format(stats.currency.getAverage())));
            }

            out.write("Статистика по датам");
            out.newLine();
            am = stats.dates.getAmount();
            out.write(String.format("\tЧисло дат: %d (%d различных)%n", am, stats.dates.getDistinct()));
            if (am != 0) {
                final DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, locale);
                out.write(String.format("\tМинимальная дата: \"%s\"%n", df.format(stats.dates.getMinDate())));
                out.write(String.format("\tМаксимальная дата: \"%s\"%n", df.format(stats.dates.getMaxDate())));
                out.write(String.format("\tСредняя дата: \"%s\"%n", df.format(stats.dates.getAverageDate())));
            }
        }
    }

    public static void main(String[] args) {
        args = new String[] {"ru-RU", "ru-RU", "tolstoy.txt", "output.txt"};
        final Locale inLocale = Locale.forLanguageTag(args[0]);
        final String content;
        try {
            content = Files.readString(Path.of(args[2]));
            final TextStatistics statistics = new TextStatistics(inLocale);
            statistics.accept(content);

            final Locale outLocale = Locale.forLanguageTag(args[1]);
            try {
                exportStats(statistics, Path.of(args[3]), outLocale);
            } catch (final IOException e) {
                //todo
            }
        } catch (final IOException e) {
            e.printStackTrace(); //todo
        }
    }


}
