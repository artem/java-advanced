package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

//TODO extension of final classes
public class Implementor implements Impler {
    public static void main(String[] args) throws ImplerException {
        new Implementor().implement(RandomAccess.class, Path.of("."));
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        final ClassImplMeta kek = new ClassImplMeta(token);
        Path target = root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + "Impl.java");

        if (target.getParent() != null) {
            try {
                Files.createDirectories(target.getParent());
            } catch (IOException ignored) {
            }
        }


        try (BufferedWriter out = Files.newBufferedWriter(target)) {
            out.write(kek.toString());
        } catch (IOException e) {
            throw new ImplerException("IO error");
        }
    }

    private static class ClassImplMeta {
        private final Class<?> implement;
        private final boolean parentInterface;
        private final Collection<Signature> abstractMethods;
        private final Collection<ConstructorSig> constructorsList;
        private final String name;
        private static final String TABULATION = "    ";

        public ClassImplMeta(Class<?> token) throws ImplerException {
            implement = token;
            name = token.getSimpleName() + "Impl";
            if (name.equals("CompletionsImpl")) {
                System.err.println(1);
            }
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
                if (sig.arguments.length != 0) {
                    sb.append("arg0");
                    for (int i = 1; i < sig.arguments.length; i++) {
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
                sb.append(TABULATION).append("return ").append(sig.defaultRet).append(";\n" + TABULATION + "}");
                sb.append('\n');
            }

            sb.append('}');

            //System.err.println(sb.toString());

            return sb.toString();
        }
    }
}
