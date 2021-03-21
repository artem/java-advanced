package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
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
        Path target = root.resolve(token.getName().replace('.', '/') + "Impl.java").toAbsolutePath();

        try {
            Files.createDirectories(target.getParent());
        } catch (IOException ignored) {
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
        private final List<ConstructorSig> constructorsList = new ArrayList<>();
        private final String name;
        private static final String TABULATION = "    ";

        public ClassImplMeta(Class<?> token) throws ImplerException {
            implement = token;
            name = token.getSimpleName() + "Impl";
            parentInterface = token.isInterface();
            if (Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
                throw new ImplerException("Cannot extend final class");
            }

            DependencyTree dt = new DependencyTree(token);
            dt.build();
            abstractMethods = dt.getRequiredMethods();

            Constructor<?>[] constructors = token.getConstructors();
            for (Constructor<?> c : constructors) {
                constructorsList.add(new ConstructorSig(c, name));
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
            sb.append(implement.getSimpleName());

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

            return sb.toString();
        }



        private static class ConstructorSig {
            private final String name;
            private final Class<?>[] arguments;
            private final Class<?>[] throwTypes;

            private ConstructorSig(Constructor<?> method, String name) {
                this.name = name;
                arguments = method.getParameterTypes();
                throwTypes = method.getExceptionTypes();
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("public ");
                sb.append(' ').append(name).append('(');
                if (arguments.length != 0) {
                    sb.append(arguments[0].getCanonicalName()).append(" arg0");
                    for (int i = 1; i < arguments.length; i++) {
                        sb.append(", ").append(arguments[i].getCanonicalName()).append(" arg").append(i);
                    }
                }
                sb.append(')');

                if (throwTypes.length != 0) {
                    sb.append(" throws ").append(throwTypes[0].getCanonicalName());
                    for (int i = 1; i < throwTypes.length; i++) {
                        sb.append(", ").append(throwTypes[i].getCanonicalName());
                    }
                }

                return sb.toString();
            }
        }
    }
}
