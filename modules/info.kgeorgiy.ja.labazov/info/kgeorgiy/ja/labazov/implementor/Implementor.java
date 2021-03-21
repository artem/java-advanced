package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        private final List<Signature> abstractMethods = new ArrayList<>();
        private final String name;
        private final Class<?>[] ifaces;
        static final String TABULATION = "    ";

        public ClassImplMeta(Class<?> token) throws ImplerException {
            implement = token;
            name = token.getSimpleName() + "Impl";
            if (!token.isInterface()) {
                throw new ImplerException("abstract class is not supported");
            }

            Method[] methods = token.getMethods();
            for (Method m : methods) {
                if (Modifier.isAbstract(m.getModifiers())) {
                    abstractMethods.add(new Signature(m));
                }
            }

            ifaces = token.getInterfaces();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("package ").append(implement.getPackageName()).append(";\n");
            sb.append("public class ").append(name);

            //sb.append("extends")
            /*sb.append(" implements").append(ifaces[0].toString());
            for (int i = 1; i < ifaces.length; i++) {
                sb.append(", ").append(ifaces[i].toString());
            }*/
            sb.append(" implements ").append(implement.getSimpleName());

            sb.append(" {\n");
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

        private static class Signature {
            private final String name;
            private final Class<?> returnType;
            private final Class<?>[] arguments;
            private final Class<?>[] throwTypes;
            private final String defaultRet;

            private Signature(Method method) {
                name = method.getName();
                returnType = method.getReturnType();
                arguments = method.getParameterTypes();
                throwTypes = method.getExceptionTypes();
                if (returnType.isPrimitive()) {
                    defaultRet = defaultValue(method);
                } else {
                    defaultRet = String.valueOf(method.getDefaultValue());
                }
                if (defaultRet.equals("")) {
                    System.err.println("kek");
                }
            }

            private String defaultValue(Method method) {
                Object defaultValue = method.getDefaultValue();

                if (defaultValue != null) {
                    return defaultValue.toString();
                }
                if (returnType == boolean.class) {
                    return "false";
                } else if (returnType == void.class) {
                    return "";
                } else {
                    return "0";
                }
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("public ").append(returnType.getCanonicalName());
                sb.append(' ').append(name).append('(');
                if (arguments.length != 0) {
                    sb.append(arguments[0].getCanonicalName()).append(" arg0");
                    for (int i = 1; i < arguments.length; i++) {
                        sb.append(", ").append(arguments[i].getCanonicalName()).append(" args").append(i);
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
