package info.kgeorgiy.ja.labazov.walk;

import info.kgeorgiy.java.advanced.base.BaseTester;

public class CustomTester extends BaseTester {

  public static void main(String[] args) {
    new CustomTester()
        .add("Custom", CustomTest.class)
        .add("Walk", OldWalkTest.class)
        .add("Recursive", OldRecursiveWalkTest.class)
        .run(args);
  }
}
