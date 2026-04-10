package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.StatsInterface;
import java.util.Locale;

public class Stats implements StatsInterface {
    private double sum = 0;
    private double sumOfSquares = 0;
    private int count = 0;

    @Override
    public void addNumber(double number) {
        if (count == 10) {
            sum = 0;
            sumOfSquares = 0;
            count = 0;
        }

        sum += number;
        sumOfSquares += number * number;
        count++;
    }

    @Override
    public double getAverage() {
        return (count == 0) ? 0.0 : sum / count;
    }

    @Override
    public double getStandardDeviation() {
        if (count == 0) return 0.0;
        double mean = getAverage();
        double variance = (sumOfSquares / count) - (mean * mean);
        return Math.sqrt(Math.max(0, variance));
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public String getFormattedStatistics() {
        return String.format(Locale.US, "%2d %.3f %.3f", count, getAverage(), getStandardDeviation());
    }
}