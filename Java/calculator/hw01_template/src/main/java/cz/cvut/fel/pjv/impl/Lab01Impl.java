package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.Lab01;
import java.util.Locale;
import java.util.Scanner;

public class Lab01Impl implements Lab01 {

    @Override
    public void homework() {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);

        System.out.println("Vyber operaci (1-soucet, 2-rozdil, 3-soucin, 4-podil):");

        if (!scanner.hasNextInt()) {
            scanner.close();
            return;
        }

        int operation = scanner.nextInt();

        if (operation < 1 || operation > 4) {
            System.out.println("Chybna volba!");
            scanner.close();
            return;
        }

        double op1 = 0, op2 = 0;
        String opSymbol = "";

        switch (operation) {
            case 1:
                System.out.println("Zadej scitanec: ");
                op1 = scanner.nextDouble();
                System.out.println("Zadej scitanec: ");
                op2 = scanner.nextDouble();
                opSymbol = "+";
                break;
            case 2:
                System.out.println("Zadej mensenec: ");
                op1 = scanner.nextDouble();
                System.out.println("Zadej mensitel: ");
                op2 = scanner.nextDouble();
                opSymbol = "-";
                break;
            case 3:
                System.out.println("Zadej cinitel: ");
                op1 = scanner.nextDouble();
                System.out.println("Zadej cinitel: ");
                op2 = scanner.nextDouble();
                opSymbol = "*";
                break;
            case 4:
                System.out.println("Zadej delenec: ");
                op1 = scanner.nextDouble();
                System.out.println("Zadej delitel: ");
                op2 = scanner.nextDouble();
                if (op2 == 0) {
                    System.out.println("Pokus o deleni nulou!");
                    scanner.close();
                    return;
                }
                opSymbol = "/";
                break;
        }

        System.out.println("Zadej pocet desetinnych mist: ");
        if (!scanner.hasNextInt()) {
            scanner.close();
            return;
        }
        int decimals = scanner.nextInt();

        if (decimals < 0) {
            System.out.println("Chyba - musi byt zadane kladne cislo!");
            scanner.close();
            return;
        }

        double result = 0;
        switch (operation) {
            case 1: result = op1 + op2; break;
            case 2: result = op1 - op2; break;
            case 3: result = op1 * op2; break;
            case 4: result = op1 / op2; break;
        }

        String format = "%." + decimals + "f %s %." + decimals + "f = %." + decimals + "f%n";
        System.out.printf(Locale.US, format, op1, opSymbol, op2, result);

        scanner.close();
    }
}