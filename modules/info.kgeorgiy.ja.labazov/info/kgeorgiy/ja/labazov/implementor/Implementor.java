package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Implementor implements Impler {
    public static void main(String[] args) {
        new Implementor().implement(Impler.class, null);
    }

    @Override
    public void implement(Class<?> token, Path root) {
        final ClassImplMeta kek = new ClassImplMeta(token);
        System.err.println(kek.toString());
    }

    private static class ClassImplMeta {
        private final Class<?> implement;
        private final List<Signature> abstractMethods = new ArrayList<>();
        private final String name;
        private final Class<?>[] ifaces;
        static final String TABULATION = "    ";

        public ClassImplMeta(Class<?> token) {
            implement = token;
            name = token.getSimpleName() + "Impl";
            if (!token.isInterface()) {
                throw new UnsupportedOperationException("abstract class is not supported");
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

            sb.append("package ").append(implement.getPackageName()).append('\n');
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
            private final Object defaultRet;

            private Signature(Method method) {
                name = method.getName();
                returnType = method.getReturnType();
                arguments = method.getParameterTypes();
                throwTypes = method.getExceptionTypes();
                defaultRet = method.getDefaultValue();
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder(returnType.toString());
                sb.append(' ').append(name).append('(');
                if (arguments.length != 0) {
                    sb.append(arguments[0].getCanonicalName()).append(" arg0");
                    for (int i = 1; i < arguments.length; i++) {
                        sb.append(", ").append(arguments[i].getCanonicalName()).append(" args").append(i);
                    }
                }
                sb.append(')');

                if (throwTypes.length != 0) {
                    sb.append(' ').append(throwTypes[0]);
                    for (int i = 1; i < throwTypes.length; i++) {
                        sb.append(", ").append(throwTypes[i]);
                    }
                }

                return sb.toString();
            }
        }
    }
}
