package com.fiap.demo.auth.service;

public final class CpfUtils {

    private CpfUtils() {
    }

    public static String normalize(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean isValid(String cpf) {
        String normalized = normalize(cpf);
        if (normalized.length() != 11 || normalized.chars().distinct().count() == 1) {
            return false;
        }

        int d1 = calcDigit(normalized, 9, 10);
        int d2 = calcDigit(normalized, 10, 11);
        return d1 == Character.getNumericValue(normalized.charAt(9))
                && d2 == Character.getNumericValue(normalized.charAt(10));
    }

    private static int calcDigit(String cpf, int length, int weightStart) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            int digit = Character.getNumericValue(cpf.charAt(i));
            sum += digit * (weightStart - i);
        }
        int mod = 11 - (sum % 11);
        return mod > 9 ? 0 : mod;
    }
}
