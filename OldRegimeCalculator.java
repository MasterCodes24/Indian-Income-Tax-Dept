// File: com/taxsystem/calculations/OldRegimeCalculator.java
package com.taxsystem.calculations;

import com.taxsystem.models.Deductions;
import com.taxsystem.models.Taxpayer;
import com.taxsystem.models.TaxResult;

/**
 * Implements Old Regime (Income Tax Act 1961) slabs.
 * These slabs are age-dependent (Normal / Senior / Super Senior Citizen).
 *
 * Normal Citizen (age < 60):
 * ₹0 - ₹2.5L : Nil
 * ₹2.5L - ₹5L : 5%
 * ₹5L - ₹10L : 20%
 * Above ₹10L  : 30%
 *
 * Senior Citizen (age 60-79): Basic exemption ₹3L
 * Super Senior (age 80+): Basic exemption ₹5L, no 5% slab
 */
public class OldRegimeCalculator {

    public TaxResult calculate(Taxpayer taxpayer, Deductions deductions) {

        // Step 1: Subtract all deductions (THIS IS THE OLD REGIME'S ADVANTAGE)
        double taxableIncome = taxpayer.getGrossIncome() - deductions.total();
        if (taxableIncome < 0) taxableIncome = 0;

        // Step 2: Apply age-specific slabs
        double baseTax = calculateAgeBasedTax(taxableIncome, taxpayer.getAge());

        // Step 3: Apply Section 87A Rebate (Old Regime: only up to ₹5L income)
        if (taxableIncome <= 500000) {
            double rebate = Math.min(baseTax, 12500);
            baseTax = baseTax - rebate;
            if (baseTax < 0) baseTax = 0;
        }

        // Step 4: Surcharge
        double surcharge = calculateSurcharge(taxableIncome, baseTax);

        return new TaxResult("Old Regime (With Deductions)",
                             taxableIncome, baseTax, surcharge,
                             taxpayer.getGrossIncome());
    }

    private double calculateAgeBasedTax(double income, int age) {
        if (age >= 80) return calculateSuperSeniorTax(income);
        if (age >= 60) return calculateSeniorTax(income);
        return calculateNormalTax(income);
    }

    private double calculateNormalTax(double income) {
        double tax = 0;
        if (income <= 250000) return 0;
        if (income > 250000)  tax += Math.min(income - 250000, 250000) * 0.05;
        if (income > 500000)  tax += Math.min(income - 500000, 500000) * 0.20;
        if (income > 1000000) tax += (income - 1000000) * 0.30;
        return tax;
    }

    private double calculateSeniorTax(double income) {
        // Basic exemption limit is ₹3L for seniors
        double tax = 0;
        if (income <= 300000) return 0;
        if (income > 300000)  tax += Math.min(income - 300000, 200000) * 0.05;
        if (income > 500000)  tax += Math.min(income - 500000, 500000) * 0.20;
        if (income > 1000000) tax += (income - 1000000) * 0.30;
        return tax;
    }

    private double calculateSuperSeniorTax(double income) {
        // Basic exemption is ₹5L, no 5% slab
        double tax = 0;
        if (income <= 500000)  return 0;
        if (income > 500000)   tax += Math.min(income - 500000, 500000) * 0.20;
        if (income > 1000000)  tax += (income - 1000000) * 0.30;
        return tax;
    }

    private double calculateSurcharge(double income, double baseTax) {
        if (income <= 5000000)  return 0;
        if (income <= 10000000) return baseTax * 0.10;
        if (income <= 20000000) return baseTax * 0.15;
        if (income <= 50000000) return baseTax * 0.25;
        return baseTax * 0.37;
    }
}