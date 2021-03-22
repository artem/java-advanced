package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class Implementor implements Impler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <class name>");
            return;
        }

        try {
            new Implementor().implement(ClassLoader.getSystemClassLoader().loadClass(args[0]), Path.of("."));
        } catch (ClassNotFoundException e) {
            System.err.println("Target class not found");
        } catch (ImplerException e) {
            System.err.println("Failed to generate implementation");
            e.printStackTrace();
        }
    }

    private static String getImplName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        final ClassImplMeta classImplMeta = new ClassImplMeta(token);
        Path target = root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(getImplName(token) + "java");

        if (target.getParent() != null) {
            try {
                Files.createDirectories(target.getParent());
            } catch (IOException ignored) {
            }
        }

        try (BufferedWriter out = Files.newBufferedWriter(target)) {
            out.write(classImplMeta.toString());
        } catch (IOException e) {
            throw new ImplerException("IO error");
        }
    }

    private static class ClassImplMeta {
        private static final String TABULATION = "    ";
        private final Class<?> implement;
        private final boolean parentInterface;
        private final Collection<Signature> abstractMethods;
        private final Collection<ConstructorSig> constructorsList;
        private final String name;

        public ClassImplMeta(Class<?> token) throws ImplerException {
            implement = token;
            name = getImplName(token);

            parentInterface = token.isInterface();
            if (Modifier.isFinal(token.getModifiers()) || token == Enum.class || Modifier.isPrivate(token.getModifiers())) {
                throw new ImplerException("Target class is final or inaccessible");
            }

            DependencyTree dt = new DependencyTree(token, name);
            dt.build();
            abstractMethods = dt.getRequiredMethods();
            constructorsList = dt.getRequiredConstructors();
            if (constructorsList.isEmpty()) {
                throw new ImplerException("No accessible constructors in parent class");
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("package ").append(implement.getPackageName()).append(";\n");
            sb.append("public class ").append(name);

            if (parentInterface) {
                sb.append(" implements ");
            } else {
                sb.append(" extends ");
            }
            sb.append(implement.getCanonicalName());

            sb.append(" {\n");

            for (ConstructorSig sig : constructorsList) {
                sb.append(TABULATION);
                sb.append(sig.toString()).append(" {\n" + TABULATION);
                sb.append(TABULATION);
                sb.append(TABULATION).append("super(");
                final Class<?>[] args = sig.getArguments();
                if (args.length != 0) {
                    sb.append("arg0");
                    for (int i = 1; i < args.length; i++) {
                        sb.append(", arg").append(i);
                    }
                }
                sb.append(");\n" + TABULATION + "}");
                sb.append('\n');
            }
            for (Signature sig : abstractMethods) {
                sb.append(TABULATION);
                sb.append(sig.toString()).append(" {\n" + TABULATION);
                sb.append(TABULATION);
                sb.append(TABULATION).append("return ").append(sig.getDefaultRet()).append(";\n" + TABULATION + "}");
                sb.append('\n');
            }

            sb.append('}');

            return sb.toString();
        }
    }
}
