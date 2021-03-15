package info.kgeorgiy.ja.labazov.student;

import info.kgeorgiy.java.advanced.base.BaseTester;
import info.kgeorgiy.java.advanced.student.GroupQueryTest;
import info.kgeorgiy.java.advanced.student.StudentQueryTest;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class CustomTester extends BaseTester {
    public static void main(final String... args) {
        new CustomTester()
                .add("StudentQuery", OldStudentQueryTest.class)
                .add("GroupQuery", OldGroupQueryTest.class)
                .run(args);
    }
}
