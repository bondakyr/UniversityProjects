package org.example;

public class Homework1 {

    private int hCounter = 0;

    private static int iCounter = 0;

    public boolean f() {
        return true;
    }

    public static boolean g() {
        return false;
    }
    public int h() {
        hCounter++;
        return hCounter;
    }

    public int i() {
        iCounter++;
        return iCounter;
    }
}
