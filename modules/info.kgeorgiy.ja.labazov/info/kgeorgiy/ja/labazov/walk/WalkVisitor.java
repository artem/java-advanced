package info.kgeorgiy.ja.labazov.walk;

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
    private final byte[] buffer = new byte[4096];
    private final Writer out;

    public WalkVisitor(final Writer out) {
        this.out = out;
    }

    private long hashFile(final Path path) {
        try (final InputStream in = Files.newInputStream(path)) {
            long hash = 0;
            final int bits = 64;
            final long mask = (-1L << (bits - bits / 8)); // first bits/8 MSBs
            int size;

            while ((size = in.read(buffer)) >= 0) {
                for (int i = 0; i < size; i++) {
                    hash = (hash << bits / 8) + (buffer[i] & 0xff);
                    final long high = hash & mask;
                    if (high != 0) {
                        hash ^= high >> (bits * 3 / 4);
                        hash &= ~high;
                    }
                }
            }
            return hash;
        } catch (final IOException e) {
            return 0;
        }
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);

        final long hash = hashFile(file);
        commitFileHash(file.toString(), hash);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
        Objects.requireNonNull(file);
        commitFileHash(file.toString(), 0);
        return FileVisitResult.CONTINUE;
    }

    public void commitFileHash(final String path, final long hash) throws IOException {
        out.write(String.format("%016x %s%n", hash, path));
    }
}
