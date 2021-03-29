package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.BaseImplementorTest;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import org.junit.Assert;

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
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class Implementor implements JarImpler {
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

    static String getSimpleImplName(final Class<?> token) { //todo perm
        return token.getSimpleName() + "Impl";
    }

    private static String getImplName(final Class<?> token) {
        return token.getPackageName() + "." + getSimpleImplName(token);
    }

    private static Path getFile(final Path root, final Class<?> clazz, String suffix) {
        return root.resolve(getImplName(clazz).replace(".", File.separator) + suffix).toAbsolutePath();
    }

    private static String getClassPath() {
        try {
            return Path.of(BaseImplementorTest.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static void compileFiles(final Path root, final String file) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assert.assertNotNull("Could not find java compiler, include tools.jar to classpath", compiler);
        final String classpath = root + File.pathSeparator + getClassPath();
        final String[] args = new String[]{file, "-cp", classpath};
        final int exitCode = compiler.run(null, null, null, args);
        Assert.assertEquals("Compiler exit code", 0, exitCode);
    }

    private static void compile(final Path root, final Class<?> clazz) {
        compileFiles(root, getFile(root, clazz, ".java").toString());
    }

    private static void clean(final Path root) throws IOException {
        if (Files.exists(root)) {
            Files.walkFileTree(root, DELETE_VISITOR);
        }
    }

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

    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory("JarImplementor-");
        } catch (IOException e) {
            throw new ImplerException("Unable to create temp directory", e);
        }

        try {
            implement(token, tmpDir);
            compile(tmpDir, token);

            try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile))) {
                writer.putNextEntry(new ZipEntry(getImplName(token).replace(".", File.separator) + ".class")); //TODO copy-pasta
                Files.copy(getFile(tmpDir, token, ".class"), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to JAR file", e);
            }
        } finally {
            try {
                clean(tmpDir);
            } catch (IOException e) {
                e.printStackTrace(); //todo impler
            }
        }
    }

}
