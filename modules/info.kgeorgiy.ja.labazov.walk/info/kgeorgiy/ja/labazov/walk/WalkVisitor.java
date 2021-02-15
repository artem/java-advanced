package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class WalkVisitor extends SimpleFileVisitor<Path> {
    protected final Writer out;

    public WalkVisitor(Writer out) {
        this.out = out;
    }

    private static long hashFile(Path path) {
        long h = 0;
        final int bits = 64;
        final long mask = -(1L << (bits - bits / 8)); // first bits/8 MSBs

        try (InputStream in = Files.newInputStream(path)) {
            int size;
            byte[] buffer = new byte[4096];
            while ((size = in.read(buffer)) >= 0) {
                for (int i = 0; i < size; i++) {
                    h = (h << bits / 8) + (buffer[i] & 0xff);
                    long high = h & mask;
                    if (high != 0) {
                        h ^= high >> (bits * 3 / 4);
                        h &= ~high;
                    }
                }
            }
        } catch (IOException e) {
            h = 0;
        }

        return h;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);

        long hash = WalkVisitor.hashFile(file);
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
        out.write(String.format("%016x %s%n", hash, path));
    }
}
