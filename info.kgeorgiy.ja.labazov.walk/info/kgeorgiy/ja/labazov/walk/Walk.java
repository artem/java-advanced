package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class Walk {
    private static class WalkVisitor extends SimpleFileVisitor<Path> {
        private final BufferedWriter out;

        private WalkVisitor(BufferedWriter out) {
            this.out = out;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(file);
            Objects.requireNonNull(attrs);

            long hash = hashFile(file);
            commitFileHash(file.toString(), hash);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            Objects.requireNonNull(file);
            commitFileHash(file.toString(), 0);
            return FileVisitResult.CONTINUE;
        }

        public void commitFileHash(String path, long hash) throws IOException {
            out.write(String.format("%016x %s", hash, path));
            out.newLine();
        }

        private static long hashFile(Path path) {
            long h = 0;
            final int bits = 64;
            final long mask = -(1L << (bits - bits / 8)); // first bits/8 MSBs

            try (InputStream in = Files.newInputStream(path)) {
                int c;
                while ((c = in.read()) >= 0) {
                    h = (h << bits / 8) + c;
                    long high = h & mask;
                    if (high != 0) {
                        h ^= high >> (bits * 3 / 4);
                        h &= ~high;
                    }
                }
            } catch (IOException e) {
                h = 0;
            }

            return h;
        }
    }

    private static void processInputFile(final Path inputFilename, final Path outputFilename) {
        try (BufferedReader reader = Files.newBufferedReader(inputFilename)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilename)) {
                WalkVisitor walkVisitor = new WalkVisitor(writer);
                String curEntry;
                while ((curEntry = reader.readLine()) != null) {
                    try {
                        Path path = Path.of(curEntry);
                        Files.walkFileTree(path, walkVisitor);
                    } catch (InvalidPathException e) {
                        walkVisitor.commitFileHash(curEntry, 0);
                    }
                }
            } catch (IOException e) {
                System.err.println("I/O error with output file: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("I/O error with input file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java Walk <input> <output>");
            return;
        }

        try {
            Path input = Path.of(args[0]);
            Path output = Path.of(args[1]);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            processInputFile(input, output);
        } catch (InvalidPathException e) {
            System.err.println("Invalid filename specified: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Unable to create parent dir for output: " + e.getMessage());
        }
    }
}
