package info.kgeorgiy.ja.labazov.walk;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for hard version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-walk">Walk</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecursiveWalkTest extends WalkTest {
    @Test @Override
    public void test46_filesAndDirs() throws IOException {
        final Map<String, String> entries = randomFiles(10, 100);
        final Set<String> dirs = randomDirs(5).keySet();
        test(Stream.concat(entries.keySet().stream(), dirs.stream()).collect(Collectors.toSet()), entries);
    }

    @Test
    public void test70_singleRecursion() throws IOException {
        final Path root = getTestDir();
        test(List.of(root.toString()), randomDirs(3, 4, 100, root));
    }

    @Test
    public void test80_doubleRecursion() throws IOException {
        final Path root = getTestDir();
        final Path dir1 = root.resolve(randomFileName());
        final Path dir2 = root.resolve(randomFileName());
        final String from = dir1.toString();
        final String to = dir2.resolve("..").resolve(dir1.getFileName()).toString();

        final Map<String, String> files = randomDirs(3, 4, 100, dir1);
        files.putAll(files.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().replace(from, to), Map.Entry::getValue)));
        files.putAll(randomDirs(3, 4, 100, dir2));

        test(Arrays.asList(from, dir2.toString(), to), files);
    }

    private Map<String, String> randomDirs(final int n, final int d, final int maxL, final Path dir) throws IOException {
        final Map<String, String> result = randomFiles(random.nextInt(n + 1), maxL, dir);
        if (d > 0) {
            for (int i = random.nextInt(n + 1); i < n; i++) {
                result.putAll(randomDirs(n, d - 1, maxL, dir.resolve(randomFileName())));
            }
        }
        return result;
    }
}
