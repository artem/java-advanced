package info.kgeorgiy.ja.labazov.walk;

import java.io.BufferedWriter;

public class RecursiveWalk extends CommonWalk {
    private static final CommonWalk walker = new RecursiveWalk();

    public static void main(String[] args) {
        if (invalidArguments(args, "RecursiveWalk")) {
            return;
        }

        walker.run(args[0], args[1]);
    }

    @Override
    protected WalkVisitor getVisitor(BufferedWriter out) {
        return new WalkVisitor(out);
    }
}
