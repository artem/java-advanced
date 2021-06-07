package info.kgeorgiy.ja.labazov.text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.BreakIterator;
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

    public TextStatistics(Locale textLocale) {
        this.textLocale = textLocale;
        sentences = new StringStatistics(textLocale);
        words = new StringStatistics(textLocale);
        numbers = new NumberStatistics(textLocale);
        currency = new NumberStatistics(textLocale);
        dates = new DateStatistics(textLocale);
    }

    public static void printAt(int pos, String source) { //todo
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(source);
        int end = boundary.following(pos);
        int start = boundary.previous();
        System.out.println(source.substring(start,end));
    }

    private boolean acceptCurrency(final String text, final ParsePosition position) { //TODO
        final NumberFormat formatter = NumberFormat.getCurrencyInstance(textLocale);
        final Number money = formatter.parse(text, position);
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

            if (!tryParseExtra && StringStatistics.isWord(source, start, end)) {
                words.accept(source.substring(start, end));
            }
        }
    }

    private static void exportStats(final TextStatistics stats, final Path outDir, final Locale locale) throws IOException {
        final BufferedWriter out = Files.newBufferedWriter(outDir);

        out.write("Сводная статистика");
        out.newLine();
        out.write(String.format("\tЧисло предложений: %d", stats.sentences.getAmount()));
        out.write(String.format("\tЧисло слов: %d", stats.words.getAmount()));
        out.write(String.format("\tЧисло чисел: %d", stats.numbers.getAmount()));
        out.write(String.format("\tЧисло сумм: %d", stats.currency.getAmount()));
        out.write(String.format("\tЧисло дат: %d", stats.dates.getAmount()));

        out.write("Статистика по предложениям");
        out.newLine();
        out.write(String.format("\tЧисло предложений: %1$d (%1$d различных)", stats.sentences.getAmount()));
        out.write(String.format("\tМинимальное предложение: \"%s\"", stats.sentences.getMin()));
        out.write(String.format("\tМаксимальное предложение: \"%s\"", stats.sentences.getMax()));
        out.write(String.format("\tМинимальная длина предложения: %d (\"%s\")", stats.sentences.getShortest().length(), stats.sentences.getShortest()));
        out.write(String.format("\tМаксимальная длина предложения: %d (\"%s\")", stats.sentences.getLongest().length(), stats.sentences.getLongest()));
        out.write(String.format("\tСредняя длина предложения: \"%f\"", stats.sentences.getAverage()));

        out.write("Статистика по словам");
        out.newLine();
        out.write(String.format("\tЧисло слов: %1$d (%1$d различных)", stats.words.getAmount()));
        out.write(String.format("\tМинимальное слово: \"%s\"", stats.words.getMin()));
        out.write(String.format("\tМаксимальное слово: \"%s\"", stats.words.getMax()));
        out.write(String.format("\tМинимальная длина слова: %d (\"%s\")", stats.words.getShortest().length(), stats.words.getShortest()));
        out.write(String.format("\tМаксимальная длина слова: %d (\"%s\")", stats.words.getLongest().length(), stats.words.getLongest()));
        out.write(String.format("\tСредняя длина слова: \"%f\"", stats.words.getAverage()));

        out.write("Статистика по числам");
        out.newLine();
        out.write(String.format("\tЧисло чисел: %1$d (%1$d различных)", stats.numbers.getAmount()));
        out.write(String.format("\tМинимальное число: \"%f\"", stats.numbers.getMin()));
        out.write(String.format("\tМаксимальное число: \"%f\"", stats.numbers.getMax()));
        out.write(String.format("\tСреднее число: \"%f\"", stats.numbers.getAverage()));

        out.write("Статистика по суммам денег");
        out.newLine();
        out.write(String.format("\tЧисло сумм: %1$d (%1$d различных)", stats.currency.getAmount())); //todo format
        out.write(String.format("\tМинимальная сумма: \"%f\"", stats.currency.getMin()));
        out.write(String.format("\tМаксимальная сумма: \"%f\"", stats.currency.getMax()));
        out.write(String.format("\tСредняя сумма: \"%f\"", stats.currency.getAverage()));

        out.write("Статистика по датам");
        out.newLine();
        out.write(String.format("\tЧисло дат: %1$d (%1$d различных)", stats.dates.getAmount()));
        out.write(String.format("\tМинимальная дата: \"%s\"", stats.dates.getMin()));
        out.write(String.format("\tМаксимальная дата: \"%s\"", stats.dates.getMax()));
        out.write(String.format("\tСредняя дата: \"%s\"", stats.dates.getAverageDate()));
    }

    public static void main(String[] args) {
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
