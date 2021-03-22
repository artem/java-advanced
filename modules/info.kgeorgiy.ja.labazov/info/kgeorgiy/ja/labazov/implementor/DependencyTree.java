package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyTree {
    private final List<Node> layers = new ArrayList<>();
    private final Set<Signature> requiredMethods = new HashSet<>();
    private final Set<ConstructorSig> requiredConstructors = new HashSet<>();
    private final String root;

    public DependencyTree(Class<?> token, String root) {
        this.root = root;
        while (token != null) {
            layers.add(new Node(token));
            token = token.getSuperclass();
        }
    }

    public Set<Signature> getRequiredMethods() {
        return requiredMethods;
    }

    public Set<ConstructorSig> getRequiredConstructors() {
        return requiredConstructors;
    }

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

    private static class Node {
        private final Class<?>[] interfaces;
        private final Class<?> token;

        private Node(Class<?> token) {
            this.token = token;
            interfaces = token.getInterfaces();
        }
    }
}
