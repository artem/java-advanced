package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;

public class Walk extends CommonWalk {
    private static final CommonWalk walker = new Walk();

    public static void main(String[] args) {
        walker.run(args);
    }

    @Override
    protected WalkVisitor getVisitor(BufferedWriter out) {
        return new FlatWalkVisitor(out);
    }
}
