package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class CommonWalk {
    protected abstract WalkVisitor getVisitor(BufferedWriter out);

    private void processInputFile(final Path inputFilename, final Path outputFilename) {
        try (BufferedReader reader = Files.newBufferedReader(inputFilename)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilename)) {
                WalkVisitor walkVisitor = getVisitor(writer);
                String curEntry;
                while ((curEntry = reader.readLine()) != null) {
                    try {
                        Path path = Path.of(curEntry);
                        Files.walkFileTree(path, walkVisitor);
                    } catch (InvalidPathException e) {
                        walkVisitor.commitFileHash(curEntry, 0);
                    }
                }
            } catch (IOException | SecurityException e) {
                System.err.println("I/O error with output file: " + e.getMessage());
            }
        } catch (IOException | SecurityException e) {
            System.err.println("I/O error with input file: " + e.getMessage());
        }
    }

    public void run(String in, String out) {
        try {
            Path input = Path.of(in);
            Path output = Path.of(out);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            processInputFile(input, output);
        } catch (InvalidPathException e) {
            System.err.println("Invalid filename specified: " + e.getMessage());
        } catch (IOException | SecurityException e) {
            System.err.println("Unable to create parent dir for output: " + e.getMessage());
        }
    }
}
