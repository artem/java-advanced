package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * This class implements {@link JarImpler}.
 * Generates <code>.java</code> implementation of a given class.
 * Packs compiled implementation into a <code>.jar</code> file if needed.
 *
 * @author Artem Labazov
 */
public class Implementor implements JarImpler {
    /**
     * Visitor for deleting temporary files.
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Program's main entry point.
     *
     * @param args Array of program arguments
     *             args[0]: target class name
     *             args[1] (Optional): output .jar file
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: <class name> [output file]");
            return;
        }

        try {
            JarImpler implementor = new Implementor();
            Class<?> token = Class.forName(args[0]);
            if (args.length >= 2) {
                implementor.implementJar(token, Path.of(args[1]));
            } else {
                implementor.implement(token, Path.of("."));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Target class not found");
        } catch (ImplerException e) {
            System.err.println("Failed to generate implementation");
            e.printStackTrace();
        }
    }

    /**
     * Returns simple class name for class implementation.
     *
     * @param token Token of the source class.
     * @return Name of the implementation class.
     */
    static String getSimpleImplName(final Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Returns full class name for class implementation.
     *
     * @param token Token of the source class.
     * @return Name of the implementation class.
     */
    private static String getImplName(final Class<?> token) {
        return token.getPackageName() + "." + getSimpleImplName(token);
    }

    /**
     * Generates file path for a given class token.
     *
     * @param root   Root path to be resolved from.
     * @param clazz  Class token.
     * @param suffix Resulting filename suffix.
     * @return Path of the required file.
     */
    private static Path getFile(final Path root, final Class<?> clazz, String suffix) {
        return root.resolve(getImplName(clazz).replace(".", File.separator) + suffix).toAbsolutePath();
    }

    /**
     * Returns system classpath.
     *
     * @return Classpath as String.
     */
    private static String getClassPath(final Class<?> clazz) {
        try {
            final CodeSource cs = clazz.getProtectionDomain().getCodeSource();
            if (cs != null) {
                return Path.of(cs.getLocation().toURI()).toString();
            } else {
                return "";
            }
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compiles implementation of a class with a given token
     *
     * @param root  Output root path
     * @param clazz Source class token
     */
    private static void compile(final Path root, final Class<?> clazz) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler, include tools.jar to classpath");
        }
        String classpath = root + File.pathSeparator + System.getProperty("java.class.path");
        final String tokenClasspath = getClassPath(clazz);
        final List<String> args = new ArrayList<>();
        if (clazz.getModule().isNamed() && tokenClasspath.isEmpty()) {
            args.add("--patch-module");
            args.add(clazz.getModule().getName() + "=" + root);
        } else {
            classpath += File.pathSeparator + tokenClasspath;
        }

        Collections.addAll(args, "-cp", classpath,
                getFile(root, clazz, ".java").toString());

        final int exitCode = compiler.run(null, null, null, args.toArray(new String[0]));
        if (exitCode != 0) {
            throw new ImplerException("Compiler exit code is not 0 but " + exitCode);
        }
    }

    /**
     * Removes directory recursively.
     *
     * @param root Directory to be removed.
     * @throws IOException I/O error has occurred.
     */
    private static void clean(final Path root) throws IOException {
        if (Files.exists(root)) {
            Files.walkFileTree(root, DELETE_VISITOR);
        }
    }

    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * <p>
     * Generated class classes name should be same as classes name of the type token with {@code Impl} suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * {@code root} directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to {@code $root/java/util/ListImpl.java}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        final ClassImplMeta classImplMeta = new ClassImplMeta(token);
        Path target = getFile(root, token, ".java");

        if (target.getParent() != null) {
            try {
                Files.createDirectories(target.getParent());
            } catch (IOException ignored) {
            }
        }

        try (BufferedWriter out = Files.newBufferedWriter(target)) {
            out.write(classImplMeta.toString());
        } catch (IOException e) {
            throw new ImplerException("IO error", e);
        }
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        jarFile = jarFile.toAbsolutePath();
        if (jarFile.getParent() == null) {
            throw new ImplerException("Invalid output file path: " + jarFile);
        }

        try {
            Files.createDirectories(jarFile.getParent());
        } catch (IOException e) {
            System.err.println("Couldn't create directories for output");
        }

        Path tmpDir = jarFile.getParent().resolve("JarImplementor" + new Random().nextInt());
        try {
            Files.createDirectories(tmpDir);
        } catch (IOException e) {
            throw new ImplerException("Unable to create temp directory", e);
        }

        try {
            implement(token, tmpDir);
            compile(tmpDir, token);

            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile))) {
                writer.putNextEntry(new ZipEntry(getImplName(token).replace(".", "/") + ".class"));
                Files.copy(getFile(tmpDir, token, ".class"), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to create a .jar file", e);
            }
        } finally {
            try {
                clean(tmpDir);
            } catch (IOException e) {
                System.err.println("Unable to clean temp directory");
            }
        }
    }

}
