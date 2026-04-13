// File: com/taxsystem/models/Deductions.java
package com.taxsystem.models;

// Deductions are ONLY relevant for the Old Regime.
// Under the New Regime, this entire object is ignored.
public class Deductions {

    private double section80C;  // Max 1,50,000 (PPF, ELSS, LIC)
    private double section80D;  // Medical insurance
    private double hra;         // House Rent Allowance
    private double nps;         // Section 80CCD(1B), max 50,000
    private double standardDeduction; // Fixed 50,000 for salaried

    public Deductions(double section80C, double section80D,
                      double hra, double nps) {
        // Cap 80C at its legal maximum
        this.section80C = Math.min(section80C, 150000);
        this.section80D = section80D;
        this.hra = hra;
        // Cap NPS deduction at its legal maximum
        this.nps = Math.min(nps, 50000);
        this.standardDeduction = 50000; // Fixed by law for salaried individuals
    }

    // Returns the total deductible amount under Old Regime
    public double total() {
        return standardDeduction + section80C + section80D + hra + nps;
    }

    // Getters
    public double getSection80C() { return section80C; }
    public double getSection80D() { return section80D; }
    public double getHra()        { return hra; }
    public double getNps()        { return nps; }
    public double getStandardDeduction() { return standardDeduction; }
}