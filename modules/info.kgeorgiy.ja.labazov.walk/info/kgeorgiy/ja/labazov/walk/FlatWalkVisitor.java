package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FlatWalkVisitor extends WalkVisitor {
    public FlatWalkVisitor(BufferedWriter out) {
        super(out);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return visitFileFailed(dir, null);
    }
}
