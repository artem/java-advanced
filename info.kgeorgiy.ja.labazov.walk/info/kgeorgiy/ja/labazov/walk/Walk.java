package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class Walk {
    private static void commitFileHash(String path, long hash, BufferedWriter out) throws IOException {
        out.write(String.format("%016x", hash));
        out.write(' ');
        out.write(path);
        out.newLine();
    }

    private static long hashFile(Path path) {
        long h = 0;
        final int bits = 64;
        final long mask = -(1L << (bits - bits / 8)); // first bits/8 MSBs

        try (InputStream in = Files.newInputStream(path)) {
            int c;
            while ((c = in.read()) >= 0) {
                h = (h << bits/8) + c;
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

    private static void processPathString(String pathStr, BufferedWriter out) throws IOException {
        try {
            Path path = Path.of(pathStr);
            processPath(path, out);
        } catch (InvalidPathException e) {
            commitFileHash(pathStr, 0, out);
        }
    }

    private static void processPath(Path path, BufferedWriter out) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry: stream) {
                    processPath(entry, out);
                }
            } catch (IOException e) {
                // TODO exceptions
            }
        } else {
            long hash = hashFile(path);
            commitFileHash(path.toString(), hash, out);
        }
    }

    private static void processInputFile(final Path inputFilename, final Path outputFilename) {
        try (BufferedReader reader = Files.newBufferedReader(inputFilename)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilename)) {
                String curEntry;
                while ((curEntry = reader.readLine()) != null) {
                    processPathString(curEntry, writer);
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
            System.err.println("Unable to create output dir: " + e.getMessage());
        }
    }
}
