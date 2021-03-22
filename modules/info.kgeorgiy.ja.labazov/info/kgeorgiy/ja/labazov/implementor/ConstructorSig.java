package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

public class ConstructorSig {
    private static final Class<?>[] DUMMY = new Class<?>[0];
    private final String name;
    private final Class<?>[] arguments;
    private final Class<?>[] throwTypes;

    ConstructorSig(Constructor<?> method, String name) {
        this.name = name;
        arguments = method.getParameterTypes();
        throwTypes = method.getExceptionTypes();
    }

    ConstructorSig(String name) {
        this.name = name;
        this.arguments = DUMMY;
        this.throwTypes = DUMMY;
    }

    public Class<?>[] getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstructorSig that = (ConstructorSig) o;
        return name.equals(that.name) && Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
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
