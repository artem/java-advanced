package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents unique constructor signature
 *
 * @author Artem Labazov
 */
public class ConstructorSig {
    private static final Class<?>[] DUMMY = new Class<?>[0];
    private final String name;
    private final Class<?>[] arguments;
    private final Class<?>[] throwTypes;

    /**
     * Constructs a signature representation
     * @param constructor Constructor to be represented
     * @param name Name of the class the constructor belongs to
     */
    ConstructorSig(Constructor<?> constructor, String name) {
        this.name = name;
        arguments = constructor.getParameterTypes();
        throwTypes = constructor.getExceptionTypes();
    }

    /**
     * Constructs a constructor with empty signature
     * @param name Name of the class the constructor belongs to
     */
    ConstructorSig(String name) {
        this.name = name;
        this.arguments = DUMMY;
        this.throwTypes = DUMMY;
    }

    /**
     * Returns an array of constructor's arguments
     * @return Constructor's arguments
     */
    public Class<?>[] getArguments() {
        return arguments;
    }

    /**
     * Checks equality of constructors.
     * @param o Other object to be compared with
     * @return <code>true</code> if constructors are equal, <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstructorSig that = (ConstructorSig) o;
        return name.equals(that.name) && Arrays.equals(arguments, that.arguments);
    }

    /**
     * Generates hashcode.
     * @return Integer hashcode.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    /**
     * Converts constructor representation into a source code string.
     * @return Source code representation
     */
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
