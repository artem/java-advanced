package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;

public class Walk extends CommonWalk {
    private static final CommonWalk walker = new Walk();

    public static void main(String[] args) {
        if (invalidArguments(args, "Walk")) {
            return;
        }

        walker.run(args[0], args[1]);
    }

    @Override
    protected WalkVisitor getVisitor(BufferedWriter out) {
        return new FlatWalkVisitor(out);
    }
}
