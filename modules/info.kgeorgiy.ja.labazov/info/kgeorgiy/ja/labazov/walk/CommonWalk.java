package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class CommonWalk {
    protected abstract WalkVisitor getVisitor(BufferedWriter out);

    private void processFiles(final Path inputFilename, final Path outputFilename) {
        try (final BufferedReader reader = Files.newBufferedReader(inputFilename)) {
            try {
                final Path parent = outputFilename.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (final IOException e) {
                System.err.println("Unable to create parent dir for output: " + e.getMessage());
            }

            try (final BufferedWriter writer = Files.newBufferedWriter(outputFilename)) {
                processData(reader, getVisitor(writer));
            } catch (final IOException e) {
                System.err.println("Unable to open output file: " + e.getMessage());
            }
        } catch (final IOException e) {
            System.err.println("Unable to open input file: " + e.getMessage());
        }
    }

    private void processData(final BufferedReader reader, final WalkVisitor visitor) {
        try {
            String curEntry;

            while ((curEntry = reader.readLine()) != null) {
                try {
                    try {
                        final Path path = Path.of(curEntry);
                        Files.walkFileTree(path, visitor);
                    } catch (final InvalidPathException e) {
                        visitor.commitFileHash(curEntry, 0);
                    }
                } catch (final IOException e) {
                    System.err.println("I/O error with output file: " + e.getMessage());
                }
            }
        } catch (final IOException e) {
            System.err.println("I/O error with input file: " + e.getMessage());
        }
    }

    private Path stringToPath(final String str, final String type) {
        try {
            return Path.of(str);
        } catch (final InvalidPathException e) {
            System.err.printf("Invalid %s filename: '%s' (%s)%n", type, str, e.getMessage());
            throw e;
        }
    }

    public void run(final String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java <input> <output>");
            return;
        }
        final String in = args[0];
        final String out = args[1];

        Path input;
        Path output;
        try {
            input = stringToPath(in, "input");
            output = stringToPath(out, "output");
        } catch (InvalidPathException e) {
            return;
        }

        processFiles(input, output);
    }
}
