package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.TextIO;
import java.util.Scanner;

public class Lab02 {
    public void main(String[] args) {
        homework();
    }

    public void homework() {
        Scanner scanner = new Scanner(System.in);
        Stats stats = new Stats();
        int lineCounter = 0;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineCounter++;

            if (line.trim().isEmpty()) continue;

            String[] tokens = line.trim().split("\\s+");

            for (String token : tokens) {
                if (TextIO.isDouble(token)) {
                    stats.addNumber(Double.parseDouble(token));
                    if (stats.getCount() == 10) {
                        System.out.println(stats.getFormattedStatistics());
                    }
                } else {
                    System.err.println("A number has not been parsed from line " + lineCounter);
                    break;
                }
            }
        }
        if (stats.getCount() > 1 && stats.getCount() < 10) {
            System.out.println(stats.getFormattedStatistics());
        }

        System.err.println("End of input detected!");
    }
}