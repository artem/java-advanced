package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;

public class RecursiveWalk extends CommonWalk {
    private static final CommonWalk walker = new RecursiveWalk();

    public static void main(String[] args) {
        walker.run(args);
    }

    @Override
    protected WalkVisitor getVisitor(BufferedWriter out) {
        return new WalkVisitor(out);
    }
}
