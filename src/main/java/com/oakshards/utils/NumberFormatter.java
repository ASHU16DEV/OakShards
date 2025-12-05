package com.oakshards.utils;

import java.text.DecimalFormat;

public class NumberFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat SHORT_FORMAT = new DecimalFormat("#.##");

    public static String format(long number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatShort(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return SHORT_FORMAT.format(number / 1000.0) + "k";
        } else if (number < 1000000000) {
            return SHORT_FORMAT.format(number / 1000000.0) + "M";
        } else {
            return SHORT_FORMAT.format(number / 1000000000.0) + "B";
        }
    }

    public static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return String.format("%02d:%02d", minutes, secs);
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
    }

    public static String formatTimeFormatted(int seconds) {
        if (seconds < 60) {
            return String.format("%02d", seconds);
        } else {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    public static boolean isValidNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static long parseNumber(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
