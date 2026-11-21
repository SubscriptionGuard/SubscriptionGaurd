package com.tfmhntspn.subguard.utils;

public class SplitCalculator {

    // Splits an amount evenly and distributes leftover cents
    public static long[] calculateEqualSplit(long totalCents, int peopleCount) {

        if (totalCents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        if (peopleCount <= 0) {
            throw new IllegalArgumentException("Must have at least one person");
        }

        long[] shares = new long[peopleCount];

        long baseAmount = totalCents / peopleCount;
        long remainder = totalCents % peopleCount;

        for (int i = 0; i < peopleCount; i++) {
            shares[i] = baseAmount;

            if (i < remainder) {
                shares[i]++;
            }
        }

        return shares;
    }

    // Checks whether custom shares add up to the full bill
    public static boolean validateCustomSplit(long totalCents, long[] shares) {

        if (totalCents < 0 || shares == null || shares.length == 0) {
            return false;
        }

        long sum = 0;

        for (long share : shares) {
            if (share < 0 || share > totalCents - sum) {
                return false;
            }
            sum += share;
        }

        return sum == totalCents;
    }
}
