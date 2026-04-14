// File: com/taxsystem/models/Taxpayer.java
package com.taxsystem.models;

/**
 * Updated Taxpayer model with Residential Status support.
 * Residential Status directly affects:
 * 1. Which income is taxable (global vs India-only)
 * 2. Whether HRA deduction is applicable
 * 3. Whether foreign income needs to be declared
 */
public class Taxpayer {

    // ── Residential Status Constants ──────────────────────────
    public static final String STATUS_RES   = "RES";   // Resident
    public static final String STATUS_RNOR  = "RNOR";  // Resident But Not Ordinarily Resident
    public static final String STATUS_NR    = "NR";    // Non Resident

    // ── Personal Fields ───────────────────────────────────────
    private String name;
    private String pan;
    private String aadhaar;
    private int    age;
    private String taxYear;

    // ── Income Fields ─────────────────────────────────────────
    private double grossIndiaIncome;    // Income earned in India
    private double foreignIncome;       // Income earned abroad (if any)

    // ── Residential Status ────────────────────────────────────
    private String residentialStatus;   // RES / RNOR / NR

    // ── Constructor ───────────────────────────────────────────
    public Taxpayer(String name, String pan, String aadhaar,
                    double grossIndiaIncome, double foreignIncome,
                    int age, String taxYear, String residentialStatus) {

        this.name               = name;
        this.pan                = pan;
        this.aadhaar            = aadhaar;
        this.grossIndiaIncome   = grossIndiaIncome;
        this.foreignIncome      = foreignIncome;
        this.age                = age;
        this.taxYear            = taxYear;
        this.residentialStatus  = residentialStatus;
    }

    /**
     * THE CORE METHOD — Returns the actual taxable gross income
     * based on residential status.
     *
     * RES  → India income + Foreign income (global taxation)
     * RNOR → India income only (foreign income exempt)
     * NR   → India income only (foreign income exempt)
     */
    public double getGrossIncome() {
        if (STATUS_RES.equals(residentialStatus)) {
            return grossIndiaIncome + foreignIncome;
        }
        // RNOR and NR: only India-sourced income is taxable
        return grossIndiaIncome;
    }

    /**
     * Returns true if HRA deduction is applicable for this taxpayer.
     * NR taxpayers typically do not receive HRA in Indian payroll.
     */
    public boolean isHRAApplicable() {
        return !STATUS_NR.equals(residentialStatus);
    }

    /**
     * Returns true if the taxpayer must declare foreign income.
     */
    public boolean mustDeclareForeignIncome() {
        return STATUS_RES.equals(residentialStatus);
    }

    /**
     * Returns a human-readable label for the residential status.
     */
    public String getResidentialStatusLabel() {
        switch (residentialStatus) {
            case STATUS_RES:  return "Resident (RES)";
            case STATUS_RNOR: return "Resident But Not Ordinarily Resident (RNOR)";
            case STATUS_NR:   return "Non Resident (NR)";
            default:          return "Unknown";
        }
    }

    /**
     * Returns a brief description of the tax implication
     * for this taxpayer's residential status.
     */
    public String getResidentialStatusImplication() {
        switch (residentialStatus) {
            case STATUS_RES:
                return "Taxed on GLOBAL income (India + Foreign). "
                     + "All deductions applicable.";
            case STATUS_RNOR:
                return "Taxed on India-sourced income ONLY. "
                     + "Foreign income is exempt. "
                     + "HRA applicable if accommodation is in India.";
            case STATUS_NR:
                return "Taxed on India-sourced income ONLY. "
                     + "Foreign income fully exempt. "
                     + "HRA deduction NOT applicable.";
            default:
                return "Status unknown.";
        }
    }

    // ── Getters ───────────────────────────────────────────────
    public String getName()                { return name; }
    public String getPan()                 { return pan; }
    public String getAadhaar()             { return aadhaar; }
    public double getGrossIndiaIncome()    { return grossIndiaIncome; }
    public double getForeignIncome()       { return foreignIncome; }
    public int    getAge()                 { return age; }
    public String getTaxYear()             { return taxYear; }
    public String getResidentialStatus()   { return residentialStatus; }

    @Override
    public String toString() {
        return String.format("Taxpayer[%s | PAN=%s | Status=%s | Income=Rs.%,.0f]",
            name, pan, residentialStatus, getGrossIncome());
    }
}
