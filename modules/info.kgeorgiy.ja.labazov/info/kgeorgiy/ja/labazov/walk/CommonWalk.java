package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class CommonWalk {
    protected abstract WalkVisitor getVisitor(BufferedWriter out);

    protected static boolean invalidArguments(String[] args, String className) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java " + className + " <input> <output>");
            return true;
        }
        return false;
    }

    private void processFiles(final Path inputFilename, final Path outputFilename) {
        try (BufferedReader reader = Files.newBufferedReader(inputFilename)) {
            try {
                Path parent = outputFilename.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (IOException e) {
                System.err.println("Unable to create parent dir for output: " + e.getMessage());
                return;
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputFilename)) {
                WalkVisitor walkVisitor = getVisitor(writer);
                processData(reader, walkVisitor);
            } catch (IOException e) {
                System.err.println("Unable to open output file: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Unable to open input file: " + e.getMessage());
        }
    }

    private void processData(BufferedReader reader, WalkVisitor visitor) {
        String curEntry;
        try {
            while ((curEntry = reader.readLine()) != null) {
                try {
                    try {
                        Path path = Path.of(curEntry);
                        Files.walkFileTree(path, visitor);
                    } catch (InvalidPathException e) {
                        visitor.commitFileHash(curEntry, 0);
                    }
                } catch (IOException e) {
                    System.err.println("I/O error with output file: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error with input file: " + e.getMessage());
        }
    }

    public void run(String in, String out) {
        try {
            Path input = Path.of(in);
            try {
                Path output = Path.of(out);

                processFiles(input, output);
            } catch (InvalidPathException e) {
                System.err.printf("Invalid output filename specified: %s: %s%n", out, e.getMessage());
            }
        } catch (InvalidPathException e) {
            System.err.printf("Invalid input filename specified: %s: %s%n", in, e.getMessage());
        }
    }
}
