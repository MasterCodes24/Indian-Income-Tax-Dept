// File: com/taxsystem/models/TaxResult.java
package com.taxsystem.models;

// Holds the final output of one regime's calculation
public class TaxResult {

    private String regimeName;      // "Old Regime" or "New Regime"
    private double taxableIncome;
    private double baseTax;
    private double surcharge;
    private double healthAndEducationCess; // 4% of baseTax + surcharge
    private double totalTax;
    private double effectiveRate;   // totalTax / grossIncome * 100
    private boolean isRecommended;  // true for the cheaper regime

    public TaxResult(String regimeName, double taxableIncome, double baseTax,
                     double surcharge, double grossIncome) {
        this.regimeName = regimeName;
        this.taxableIncome = taxableIncome;
        this.baseTax = baseTax;
        this.surcharge = surcharge;
        this.healthAndEducationCess = (baseTax + surcharge) * 0.04;
        this.totalTax = baseTax + surcharge + healthAndEducationCess;
        this.effectiveRate = (grossIncome > 0) ? (totalTax / grossIncome) * 100 : 0;
    }

    // Getters
    public String getRegimeName()   { return regimeName; }
    public double getTaxableIncome(){ return taxableIncome; }
    public double getBaseTax()      { return baseTax; }
    public double getSurcharge()    { return surcharge; }
    public double getCess()         { return healthAndEducationCess; }
    public double getTotalTax()     { return totalTax; }
    public double getEffectiveRate(){ return effectiveRate; }
    public boolean isRecommended()  { return isRecommended; }
    public void setRecommended(boolean r) { this.isRecommended = r; }

    @Override
    public String toString() {
        return String.format(
            "[%s] Taxable: Rs.%.0f | Base Tax: Rs.%.0f | Cess: Rs.%.0f | " +
            "TOTAL: Rs.%.0f | Effective Rate: %.2f%%",
            regimeName, taxableIncome, baseTax,
            healthAndEducationCess, totalTax, effectiveRate
        );
    }
}
