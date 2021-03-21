package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyTree {
    private final List<Node> kek = new ArrayList<>(); //todo rename
    private final Set<Signature> requiredMethods = new HashSet<>();
    //private final Set

    public DependencyTree(Class<?> token) {
        while (token != null) {
            kek.add(new Node(token));
            token = token.getSuperclass();
        }
    }

    public Set<Signature> getRequiredMethods() {
        return requiredMethods;
    }

    public void build() {
        final Set<Signature> implementedPool = new HashSet<>();

        for (Node n : kek) {
            Method[] methods = n.token.getDeclaredMethods();

            for (int i = 0; i <= n.interfaces.length; i++) {
                for (Method m : methods) {
                    if (!Modifier.isAbstract(m.getModifiers())) {
                        implementedPool.add(new Signature(m));
                    }

                    if (!Modifier.isPrivate(m.getModifiers())) {
                        requiredMethods.add(new Signature(m));
                    }
                }
                if (i < n.interfaces.length) {
                    methods = n.interfaces[i].getDeclaredMethods();
                }
            }
        }

        requiredMethods.removeAll(implementedPool);
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
