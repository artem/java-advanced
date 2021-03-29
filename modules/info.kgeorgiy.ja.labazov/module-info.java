module info.kgeorgiy.ja.labazov {
  requires transitive quickcheck;

  requires transitive info.kgeorgiy.java.advanced.base;
  requires transitive info.kgeorgiy.java.advanced.student;
  requires transitive info.kgeorgiy.java.advanced.implementor;
    requires java.compiler;

    exports info.kgeorgiy.ja.labazov.walk;
  exports info.kgeorgiy.ja.labazov.arrayset;
  exports info.kgeorgiy.ja.labazov.student;
  exports info.kgeorgiy.ja.labazov.implementor;

  opens info.kgeorgiy.ja.labazov.walk to junit;
  opens info.kgeorgiy.ja.labazov.arrayset to junit;
  opens info.kgeorgiy.ja.labazov.student to junit;
  opens info.kgeorgiy.ja.labazov.implementor to junit;
}