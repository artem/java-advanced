package info.kgeorgiy.ja.labazov.walk;

import info.kgeorgiy.java.advanced.base.BaseTester;

public class CustomTester extends BaseTester {

  public static void main(String[] args) {
    new CustomTester()
        .add("Custom", CustomTest.class)
        .add("Walk", WalkTest.class)
        .add("RecursiveWalk", RecursiveWalkTest.class)
        .add("AdvancedWalk", (tester, cut) -> {
            tester.test("Walk", cut.replace(".RecursiveWalk", ".Walk"));
            return tester.test("RecursiveWalk", cut);
        })
        .run(args);
  }
}
