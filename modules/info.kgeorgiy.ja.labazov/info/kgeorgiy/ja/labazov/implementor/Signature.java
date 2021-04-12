package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * This class represents unique method signature
 *
 * @author Artem Labazov
 */
public class Signature {
    /**
     * Name of the method.
     */
    private final String name;

    /**
     * Token of the type which represented method returns.
     */
    private final Class<?> returnType;

    /**
     * Array of the types represented method takes as arguments.
     */
    private final Class<?>[] arguments;

    /**
     * List of the types represented method throws.
     */
    private final Class<?>[] throwTypes;

    /**
     * Default value that represented method should return.
     */
    private final String defaultRet;

    /**
     * Constructs a signature representation
     * @param method Method to be represented
     */
    Signature(Method method) {
        name = method.getName();
        returnType = method.getReturnType();
        arguments = method.getParameterTypes();
        throwTypes = method.getExceptionTypes();

        if (returnType.isPrimitive()) {
            defaultRet = defaultValue(method);
        } else {
            if (method.getDefaultValue() instanceof String) {
                defaultRet = '"' + (String)method.getDefaultValue() + '"';
            } else {
                defaultRet = String.valueOf(method.getDefaultValue());
            }
        }
    }

    /**
     * Returns default return value of the method.
     * @return Method's default value.
     */
    public String getDefaultRet() {
        return defaultRet;
    }

    /**
     * Returns default return value of the method.
     * @param method Method to retrieve return value from.
     * @return String default value representation.
     */
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

    /**
     * Converts method representation into a source code string.
     * @return Source code representation
     */
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

    /**
     * Checks equality of methods.
     * @param o Other object to be compared with
     * @return <code>true</code> if methods are equal, <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return name.equals(signature.name) && returnType.equals(signature.returnType) && Arrays.equals(arguments, signature.arguments);
    }

    /**
     * Generates hashcode.
     * @return Integer hashcode.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(name, returnType);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
