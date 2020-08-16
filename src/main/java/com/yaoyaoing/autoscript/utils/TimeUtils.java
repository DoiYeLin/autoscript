package com.yaoyaoing.autoscript.utils;

public class TimeUtils {
    public static String converMinSecond(int overdueMin, long seconds) {
        seconds = overdueMin * 60 - (seconds / 1000);
        long mintue = seconds / 60;
        long second = seconds % 60;
        String result;
        if (String.valueOf(mintue).length() == 1) {
            result = "0" + mintue;
        } else {
            result = "" + mintue;
        }
        result += ":";
        if (String.valueOf(second).length() == 1) {
            result += "0" + second;
        } else {
            result += "" + second;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(converMinSecond(5,72000));
    }
}
