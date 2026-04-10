package cz.cvut.fel.pjv;

import cz.cvut.fel.pjv.impl.BruteForceAttacker;

public class Main {
    public static void main(String[] args) {
        final String chars = "ABC";
        final String password = "AAAA";

        BruteForceAttacker attacker = new BruteForceAttacker();
        attacker.init(chars.toCharArray(), password);
        attacker.breakPassword(password.length());
        System.out.println(attacker.isOpened());
    }
}
