package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Constructor;

public class ConstructorSig {
    private final String name;
    public final Class<?>[] arguments; //todo permissions
    private final Class<?>[] throwTypes;

    public ConstructorSig(Constructor<?> method, String name) {
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
