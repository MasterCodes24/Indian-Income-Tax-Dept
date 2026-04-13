// File: com/taxsystem/calculations/ValidationHelper.java
package com.taxsystem.calculations;

/**
 * SOLVES THE DATA SILO GAP — Input Validation Layer.
 * Validates PAN, Aadhaar, and numeric inputs before any calculation runs.
 * Uses Java Regex (String pattern matching).
 */
public class ValidationHelper {

    // PAN format: 5 uppercase letters, 4 digits, 1 uppercase letter
    // Example: ABCDE1234F
    private static final String PAN_PATTERN = "[A-Z]{5}[0-9]{4}[A-Z]{1}";

    // Aadhaar: exactly 12 digits
    private static final String AADHAAR_PATTERN = "[0-9]{12}";

    // Positive decimal number (allows up to 2 decimal places)
    private static final String INCOME_PATTERN = "[0-9]+(\\.[0-9]{1,2})?";

    public static boolean isValidPAN(String pan) {
        if (pan == null || pan.trim().isEmpty()) return false;
        return pan.trim().toUpperCase().matches(PAN_PATTERN);
    }

    public static boolean isValidAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.trim().isEmpty()) return false;
        // Remove spaces (Aadhaar is often written as XXXX XXXX XXXX)
        String cleaned = aadhaar.replaceAll("\\s", "");
        return cleaned.matches(AADHAAR_PATTERN);
    }

    public static boolean isValidIncome(String income) {
        if (income == null || income.trim().isEmpty()) return false;
        return income.trim().matches(INCOME_PATTERN);
    }

    public static boolean isValidAge(String age) {
        try {
            int a = Integer.parseInt(age.trim());
            return (a >= 18 && a <= 120);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Converts a validated income string to double safely
    public static double parseIncome(String income) {
        return Double.parseDouble(income.trim());
    }
}
