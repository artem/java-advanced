package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * This class represents meta information, required to generate
 * a class' implementation.
 *
 * @author Artem Labazov
 */
class ClassImplMeta {
    private static final String TABULATION = "    ";
    private final Class<?> implement;
    private final boolean parentInterface;
    private final Collection<Signature> abstractMethods;
    private final Collection<ConstructorSig> constructorsList;
    private final String name;

    /**
     * Constructs meta information for the implementation of a given class
     * @param token Source class to be implemented.
     * @throws ImplerException Class cannot be implemented.
     */
    public ClassImplMeta(Class<?> token) throws ImplerException {
        implement = token;
        name = Implementor.getSimpleImplName(token);

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

    /**
     * Produces class' <code>.java</code> implementation
     * @return Plain text implementation
     */
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
            sb.append(TABULATION + "@Override" + '\n' + TABULATION)
                    .append(sig.toString())
                    .append(" {" + "\n" + TABULATION + TABULATION + "return ")
                    .append(sig.getDefaultRet())
                    .append(";\n" + TABULATION + "}\n\n");
        }

        sb.append('}');

        return sb.toString();
    }
}
