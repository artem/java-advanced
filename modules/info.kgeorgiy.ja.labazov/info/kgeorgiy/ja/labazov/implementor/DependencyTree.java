package info.kgeorgiy.ja.labazov.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DependencyTree {
    private final List<Node> kek = new ArrayList<>(); //todo rename
    private final Set<Signature> requiredMethods = new HashSet<>();
    private final Set<ConstructorSig> requiredConstructors = new HashSet<>();
    private final String root;

    public DependencyTree(Class<?> token, String root) {
        this.root = root;
        while (token != null) {
            kek.add(new Node(token));
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
        final Set<Signature> implementedMetPool = new HashSet<>();
        final Set<ConstructorSig> implementedConstrPool = new HashSet<>();

        for (Node n : kek) {
            Method[] methods = n.token.getDeclaredMethods();
            Constructor<?>[] constructors = n.token.getDeclaredConstructors();

            for (int i = 0; i <= n.interfaces.length; i++) {
                for (Method m : methods) {
                    if (!Modifier.isAbstract(m.getModifiers())) {
                        implementedMetPool.add(new Signature(m));
                    }

                    if (!Modifier.isPrivate(m.getModifiers())) {
                        requiredMethods.add(new Signature(m));
                    }
                }
                for (Constructor<?> c : constructors) {
                    if (!Modifier.isAbstract(c.getModifiers())) {
                        implementedConstrPool.add(new ConstructorSig(c, root));
                    }

                    if (!Modifier.isPrivate(c.getModifiers())) {
                        requiredConstructors.add(new ConstructorSig(c, root));
                    }
                }
                if (i < n.interfaces.length) {
                    methods = n.interfaces[i].getDeclaredMethods();
                    constructors = n.interfaces[i].getDeclaredConstructors();
                }
            }
        }

        requiredMethods.removeAll(implementedMetPool);
        requiredConstructors.removeAll(implementedConstrPool);
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
