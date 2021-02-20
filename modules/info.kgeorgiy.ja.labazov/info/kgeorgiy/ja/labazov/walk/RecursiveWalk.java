package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;

public class RecursiveWalk extends CommonWalk {
    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java RecursiveWalk <input> <output>");
            return;
        }

        new RecursiveWalk().run(args[0], args[1]);
    }

    @Override
    protected WalkVisitor getVisitor(BufferedWriter out) {
        return new WalkVisitor(out);
    }
}
