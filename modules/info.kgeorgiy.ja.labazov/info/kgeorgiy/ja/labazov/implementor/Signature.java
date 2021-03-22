package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class Signature {
    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] arguments;
    private final Class<?>[] throwTypes;
    private final String defaultRet; //TODO fix access

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

    public String getDefaultRet() {
        return defaultRet;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return name.equals(signature.name) && returnType.equals(signature.returnType) && Arrays.equals(arguments, signature.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, returnType);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
