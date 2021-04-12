package info.kgeorgiy.ja.labazov.implementor;

import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class generates method and constructor dependency tree
 * of a given class
 *
 * @author Artem Labazov
 */
public class DependencyTree {
    /**
     * Class dependency tree split into layers.
     */
    private final List<Node> layers = new ArrayList<>();

    /**
     * Set of the methods to be generated.
     */
    private final Set<Signature> requiredMethods = new HashSet<>();

    /**
     * Set of the constructors to be generated.
     */
    private final Set<ConstructorSig> requiredConstructors = new HashSet<>();

    /**
     * Name of the implementation class.
     */
    private final String root;

    /**
     * Constructs a tree with dependent classes.
     * @param token Target class to find dependencies for.
     * @param root Target class name.
     */
    public DependencyTree(Class<?> token, String root) {
        this.root = root;
        while (token != null) {
            layers.add(new Node(token));
            token = token.getSuperclass();
        }
    }

    /**
     * Returns methods to be implemented. Requires {@link #build()} to be ran first
     * @return Set of methods to be implemented
     */
    public Set<Signature> getRequiredMethods() {
        return requiredMethods;
    }

    /**
     * Returns constructors to be implemented. Requires {@link #build()} to be ran first
     * @return Set of constructors to be implemented
     */
    public Set<ConstructorSig> getRequiredConstructors() {
        return requiredConstructors;
    }

    /**
     * Fills class dependency tree with methods and constructors
     * that are required to be implemented.
     */
    public void build() {
        final Node first = layers.get(0);
        if (!first.token.isInterface()) {
            final Constructor<?>[] constructors = first.token.getDeclaredConstructors();

            for (Constructor<?> c : constructors) {
                if (!Modifier.isPrivate(c.getModifiers())) {
                    requiredConstructors.add(new ConstructorSig(c, root));
                }
            }
        } else {
            requiredConstructors.add(new ConstructorSig(root));
        }

        final Set<Signature> implementedMetPool = new HashSet<>();

        for (int j = layers.size() - 1; j >= 0; j--) {
            final Node n = layers.get(j);
            Method[] methods = n.token.getDeclaredMethods();

            for (int i = 0; i <= n.interfaces.length; i++) {
                for (Method m : methods) {
                    if (!Modifier.isAbstract(m.getModifiers())) {
                        implementedMetPool.add(new Signature(m));
                    }

                    if (!Modifier.isPrivate(m.getModifiers())) {
                        requiredMethods.add(new Signature(m));
                    }
                }

                if (i < n.interfaces.length) {
                    methods = n.interfaces[i].getMethods();
                }
            }
            requiredMethods.removeAll(implementedMetPool);
            implementedMetPool.clear();
        }
    }

    /**
     * Dependency tree node.
     */
    private static class Node {
        private final Class<?>[] interfaces;
        private final Class<?> token;

        /**
         * Constructs a new dependency tree node.
         * @param token Class token
         */
        private Node(Class<?> token) {
            this.token = token;
            interfaces = token.getInterfaces();
        }
    }
}
