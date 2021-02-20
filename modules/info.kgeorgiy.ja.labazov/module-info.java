module info.kgeorgiy.ja.labazov {
  requires transitive quickcheck;

  requires transitive info.kgeorgiy.java.advanced.base;

  exports info.kgeorgiy.ja.labazov.walk;

  exports info.kgeorgiy.ja.labazov.arrayset;

  opens info.kgeorgiy.ja.labazov.walk to junit;

  opens info.kgeorgiy.ja.labazov.arrayset to junit;
}