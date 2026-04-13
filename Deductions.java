// File: com/taxsystem/models/Deductions.java
package com.taxsystem.models;

/**
 * Updated Deductions model.
 * Now respects residential status before allowing HRA.
 *
 * KEY RULE: HRA is blocked for NR taxpayers.
 * This is enforced here at the model level so neither
 * calculator needs to check it separately.
 */
public class Deductions {

    private double section80C;
    private double section80D;
    private double hra;
    private double nps;
    private double standardDeduction;

    // Whether HRA is legally applicable for this taxpayer
    private boolean hraApplicable;

    /**
     * @param taxpayer         Pass the Taxpayer so we can check residential status
     * @param section80C       PPF, ELSS, LIC premium etc. (max ₹1.5L)
     * @param section80D       Medical insurance premium
     * @param hra              HRA exemption amount
     * @param nps              NPS contribution u/s 80CCD(1B) (max ₹50K)
     */
    public Deductions(Taxpayer taxpayer,
                      double section80C, double section80D,
                      double hra, double nps) {

        this.hraApplicable    = taxpayer.isHRAApplicable();
        this.section80C       = Math.min(section80C, 150000);
        this.section80D       = section80D;
        this.nps              = Math.min(nps, 50000);
        this.standardDeduction = 50000;

        // HRA is only allowed if residential status permits it
        if (this.hraApplicable) {
            this.hra = hra;
        } else {
            this.hra = 0; // NR — HRA blocked
        }
    }

    /**
     * Total deductible amount under Old Regime.
     */
    public double total() {
        return standardDeduction + section80C + section80D + hra + nps;
    }

    // ── Getters ───────────────────────────────────────────────
    public double getSection80C()        { return section80C; }
    public double getSection80D()        { return section80D; }
    public double getHra()               { return hra; }
    public double getNps()               { return nps; }
    public double getStandardDeduction() { return standardDeduction; }
    public boolean isHraApplicable()     { return hraApplicable; }
}