module info.kgeorgiy.ja.labazov {
    requires transitive quickcheck;

    requires transitive info.kgeorgiy.java.advanced.base;
    requires transitive info.kgeorgiy.java.advanced.student;
    requires transitive info.kgeorgiy.java.advanced.implementor;
    requires transitive info.kgeorgiy.java.advanced.concurrent;
    requires transitive info.kgeorgiy.java.advanced.mapper;
    requires transitive info.kgeorgiy.java.advanced.crawler;
    requires transitive info.kgeorgiy.java.advanced.hello;
    requires java.compiler;

    exports info.kgeorgiy.ja.labazov.walk;
    exports info.kgeorgiy.ja.labazov.arrayset;
    exports info.kgeorgiy.ja.labazov.student;
    exports info.kgeorgiy.ja.labazov.implementor;
    exports info.kgeorgiy.ja.labazov.concurrent;
    exports info.kgeorgiy.ja.labazov.crawler;
    exports info.kgeorgiy.ja.labazov.hello;

    opens info.kgeorgiy.ja.labazov.walk to junit;
    opens info.kgeorgiy.ja.labazov.arrayset to junit;
    opens info.kgeorgiy.ja.labazov.student to junit;
    opens info.kgeorgiy.ja.labazov.implementor to junit;
    opens info.kgeorgiy.ja.labazov.concurrent to junit;
    opens info.kgeorgiy.ja.labazov.crawler to junit;
    opens info.kgeorgiy.ja.labazov.hello to junit;
}