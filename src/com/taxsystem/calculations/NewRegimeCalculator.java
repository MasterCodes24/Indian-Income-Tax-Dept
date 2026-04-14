// File: com/taxsystem/calculations/NewRegimeCalculator.java
package com.taxsystem.calculations;

import com.taxsystem.models.Taxpayer;
import com.taxsystem.models.TaxResult;

/**
 * ============================================================
 * NEW REGIME TAX CALCULATOR — Finance Act 2025
 * Effective: FY 2025-26 (April 2026 onwards)
 * ============================================================
 *
 * SLAB STRUCTURE (After Standard Deduction):
 * ─────────────────────────────────────────
 * Rs.0          to Rs.4,00,000   →  Nil  (0%)
 * Rs.4,00,001   to Rs.8,00,000   →  5%
 * Rs.8,00,001   to Rs.12,00,000  →  10%
 * Rs.12,00,001  to Rs.16,00,000  →  15%
 * Rs.16,00,001  to Rs.20,00,000  →  20%
 * Rs.20,00,001  to Rs.24,00,000  →  25%
 * Above Rs.24,00,000            →  30%
 *
 * KEY FEATURES:
 * ─────────────────────────────────────────
 * 1. Standard Deduction : Rs.75,000 (for salaried/pensioners)
 * 2. Section 87A Rebate : Full tax rebated if taxable income <= Rs.12L
 * 3. Marginal Relief    : Prevents tax from exceeding income above Rs.12L
 * 4. Surcharge          : Applies on incomes above Rs.50 Lakh
 * 5. Health & Edu Cess  : 4% on (tax + surcharge) — handled in TaxResult
 *
 * NOTE: No deductions (80C, 80D, HRA, NPS etc.) are allowed
 *       under the New Regime. The Deductions object is intentionally
 *       NOT accepted as a parameter in this calculator.
 * ============================================================
 */
public class NewRegimeCalculator {

    // ============================================================
    // CONSTANTS — Change here if Finance Act updates slabs
    // ============================================================

    // Standard deduction for salaried individuals under New Regime
    private static final double STANDARD_DEDUCTION = 75000;

    // Section 87A: Full rebate if taxable income is at or below this
    private static final double REBATE_INCOME_LIMIT = 1200000; // Rs.12,00,000

    // Slab upper boundaries (in ascending order)
    // Index 0 = end of first slab, Index 1 = end of second slab, etc.
    private static final double[] SLAB_LIMITS = {
        400000,   // Rs.4 Lakh
        800000,   // Rs.8 Lakh
        1200000,  // Rs.12 Lakh
        1600000,  // Rs.16 Lakh
        2000000,  // Rs.20 Lakh
        2400000   // Rs.24 Lakh
        // Above Rs.24L = 30% (handled separately in calculateSlabTax)
    };

    // Tax rates corresponding to each slab
    // SLAB_RATES[i] is the rate for income between SLAB_LIMITS[i-1] and SLAB_LIMITS[i]
    private static final double[] SLAB_RATES = {
        0.00,  // 0%  — Rs.0 to Rs.4L
        0.05,  // 5%  — Rs.4L to Rs.8L
        0.10,  // 10% — Rs.8L to Rs.12L
        0.15,  // 15% — Rs.12L to Rs.16L
        0.20,  // 20% — Rs.16L to Rs.20L
        0.25,  // 25% — Rs.20L to Rs.24L
        0.30   // 30% — Above Rs.24L
    };

    // Surcharge thresholds and rates
    private static final double SURCHARGE_LIMIT_1  = 5000000;   // Rs.50 Lakh
    private static final double SURCHARGE_LIMIT_2  = 10000000;  // Rs.1 Crore
    private static final double SURCHARGE_LIMIT_3  = 20000000;  // Rs.2 Crore
    private static final double SURCHARGE_LIMIT_4  = 50000000;  // Rs.5 Crore

    private static final double SURCHARGE_RATE_1   = 0.10; // 10%
    private static final double SURCHARGE_RATE_2   = 0.15; // 15%
    private static final double SURCHARGE_RATE_3   = 0.25; // 25%
    private static final double SURCHARGE_RATE_4   = 0.37; // 37%


    // ============================================================
    // MAIN PUBLIC METHOD — Called by RegimeComparator
    // ============================================================

    /**
     * Calculates complete tax liability under New Regime for FY 2025-26.
     *
     * @param taxpayer  The Taxpayer object with gross income and age
     * @return          A TaxResult object with full tax breakdown
     */
    public TaxResult calculate(Taxpayer taxpayer) {

        double grossIncome = taxpayer.getGrossIncome();

        // ── STEP 1: Apply Standard Deduction ──────────────────────
        // Salaried individuals get Rs.75,000 deducted from gross income.
        // This is the ONLY deduction allowed under New Regime.
        double taxableIncome = grossIncome - STANDARD_DEDUCTION;

        // Taxable income cannot go below zero
        if (taxableIncome < 0) {
            taxableIncome = 0;
        }

        // ── STEP 2: Calculate Raw Slab-wise Tax ───────────────────
        // Calculate tax purely based on slabs, before any rebate.
        double baseTax = calculateSlabTax(taxableIncome);

        // ── STEP 3: Apply Section 87A Rebate + Marginal Relief ────
        // This is the most critical step for FY 2025-26.
        // The new Finance Act raised this rebate limit from Rs.7L to Rs.12L.
        baseTax = applyRebateAndMarginalRelief(taxableIncome, baseTax);

        // ── STEP 4: Calculate Surcharge ───────────────────────────
        // Surcharge applies only for very high incomes (above Rs.50 Lakh).
        // Surcharge itself has marginal relief but that is an advanced topic.
        double surcharge = calculateSurcharge(taxableIncome, baseTax);

        // ── STEP 5: Build and Return TaxResult ────────────────────
        // TaxResult constructor internally calculates 4% Cess and Total Tax.
        return new TaxResult(
            "New Regime (Finance Act 2025)",
            taxableIncome,
            baseTax,
            surcharge,
            grossIncome
        );
    }


    // ============================================================
    // PRIVATE HELPER: SLAB TAX CALCULATION
    // ============================================================

    /**
     * Calculates tax by applying each slab progressively.
     *
     * HOW IT WORKS (Example: income = Rs.10,00,000):
     *
     * Slab 1: Rs.0    to Rs.4L   → taxed portion = Rs.4,00,000 at 0%  = Rs.0
     * Slab 2: Rs.4L   to Rs.8L   → taxed portion = Rs.4,00,000 at 5%  = Rs.20,000
     * Slab 3: Rs.8L   to Rs.10L  → taxed portion = Rs.2,00,000 at 10% = Rs.20,000
     * Income doesn't reach Rs.12L so loop stops here.
     * Total = Rs.40,000
     *
     * @param income  Taxable income after standard deduction
     * @return        Raw tax before rebate, surcharge, or cess
     */
    private double calculateSlabTax(double income) {

        double tax = 0.0;
        double previousLimit = 0.0;

        // Loop through each defined slab
        for (int i = 0; i < SLAB_LIMITS.length; i++) {

            // If income doesn't even reach the start of this slab, stop
            if (income <= previousLimit) {
                break;
            }

            // How much of the income falls within THIS slab?
            // It's either the full slab width, or whatever income remains
            double incomeInThisSlab = Math.min(income, SLAB_LIMITS[i]) - previousLimit;

            // Apply this slab's rate to that portion
            tax += incomeInThisSlab * SLAB_RATES[i];

            // Move the lower boundary up to current slab's upper limit
            previousLimit = SLAB_LIMITS[i];
        }

        // Handle the topmost slab (above Rs.24 Lakh at 30%)
        // This is not in the loop because it has no upper boundary
        if (income > SLAB_LIMITS[SLAB_LIMITS.length - 1]) {
            double incomeAboveTopSlab = income - SLAB_LIMITS[SLAB_LIMITS.length - 1];
            tax += incomeAboveTopSlab * SLAB_RATES[SLAB_RATES.length - 1]; // 30%
        }

        return tax;
    }


    // ============================================================
    // PRIVATE HELPER: SECTION 87A REBATE + MARGINAL RELIEF
    // ============================================================

    /**
     * Applies Section 87A rebate as per Finance Act 2025.
     *
     * THREE SCENARIOS:
     *
     * SCENARIO A — Income <= Rs.12,00,000:
     *   Full tax is rebated. Final tax = Rs.0.
     *   Example: Taxable = Rs.11,00,000 → Tax = Rs.42,500 → After rebate = Rs.0
     *
     * SCENARIO B — Income slightly above Rs.12,00,000 (Marginal Relief zone):
     *   Without relief, earning Rs.1 extra above Rs.12L would suddenly
     *   cost you thousands in tax — that's unfair.
     *   Marginal Relief Rule: Tax payable cannot EXCEED the amount
     *   by which income exceeds Rs.12,00,000.
     *
     *   Example: Taxable = Rs.12,10,000 (Rs.10,000 above Rs.12L)
     *   Raw Tax = Rs.63,000 (approx)
     *   Without relief you'd pay Rs.63,000 on just Rs.10,000 extra — absurd!
     *   With marginal relief → Tax capped at Rs.10,000
     *
     * SCENARIO C — Income well above Rs.12,00,000:
     *   Eventually the raw tax naturally exceeds the excess income.
     *   No relief applies. Full slab tax is charged.
     *   Example: Taxable = Rs.15,00,000 → Full tax applies normally.
     *
     * @param taxableIncome  Income after standard deduction
     * @param baseTax        Raw slab tax before any rebate
     * @return               Adjusted tax after rebate/marginal relief
     */
    private double applyRebateAndMarginalRelief(double taxableIncome,
                                                 double baseTax) {

        // SCENARIO A: Full rebate — income within the Rs.12L limit
        if (taxableIncome <= REBATE_INCOME_LIMIT) {
            return 0.0; // Zero tax. This is legally correct per Finance Act 2025.
        }

        // SCENARIO B & C: Income is above Rs.12L
        // Check if Marginal Relief is applicable
        double excessIncomeAboveLimit = taxableIncome - REBATE_INCOME_LIMIT;

        if (baseTax > excessIncomeAboveLimit) {
            // SCENARIO B: Tax exceeds the excess income
            // Apply marginal relief — cap tax at the excess income amount
            // The taxpayer pays only what they "extra earned" above Rs.12L
            return excessIncomeAboveLimit;
        }

        // SCENARIO C: Tax is less than or equal to excess income
        // Marginal relief doesn't help here — full tax applies
        return baseTax;
    }


    // ============================================================
    // PRIVATE HELPER: SURCHARGE CALCULATION
    // ============================================================

    /**
     * Calculates surcharge on tax for high-income earners.
     *
     * Surcharge is a tax ON the tax (not on income directly).
     * It applies only when taxable income crosses Rs.50 Lakh.
     *
     * SURCHARGE TABLE:
     * Rs.50L  to Rs.1Cr  → 10% of baseTax
     * Rs.1Cr  to Rs.2Cr  → 15% of baseTax
     * Rs.2Cr  to Rs.5Cr  → 25% of baseTax
     * Above Rs.5Cr      → 37% of baseTax
     *
     * NOTE: Under New Regime, surcharge is CAPPED at 25%
     * (The 37% slab was removed for New Regime in Budget 2023).
     * This is a key difference from Old Regime.
     *
     * @param taxableIncome  Income after standard deduction
     * @param baseTax        Tax after rebate (used as surcharge base)
     * @return               Surcharge amount (Rs.0 if income <= Rs.50L)
     */
    private double calculateSurcharge(double taxableIncome, double baseTax) {

        // No surcharge for incomes up to Rs.50 Lakh
        if (taxableIncome <= SURCHARGE_LIMIT_1) {
            return 0.0;
        }

        double surchargeRate;

        if (taxableIncome <= SURCHARGE_LIMIT_2) {
            surchargeRate = SURCHARGE_RATE_1; // 10% for Rs.50L to Rs.1Cr

        } else if (taxableIncome <= SURCHARGE_LIMIT_3) {
            surchargeRate = SURCHARGE_RATE_2; // 15% for Rs.1Cr to Rs.2Cr

        } else if (taxableIncome <= SURCHARGE_LIMIT_4) {
            surchargeRate = SURCHARGE_RATE_3; // 25% for Rs.2Cr to Rs.5Cr

        } else {
            // NEW REGIME SPECIFIC RULE (Budget 2023 amendment):
            // Surcharge is CAPPED at 25% under New Regime.
            // Old Regime allows 37% above Rs.5Cr. New Regime does NOT.
            // This makes New Regime more beneficial for ultra-high earners.
            surchargeRate = SURCHARGE_RATE_3; // 25% cap — NOT 37%
        }

        return baseTax * surchargeRate;
    }


    // ============================================================
    // UTILITY: TAX BREAKDOWN STRING (for debugging/logging)
    // ============================================================

    /**
     * Returns a detailed step-by-step breakdown of the tax calculation.
     * Useful for showing users HOW the tax was computed.
     * Can be called from the UI's "Show Breakdown" button.
     *
     * @param taxpayer  The taxpayer whose breakdown you want
     * @return          Multi-line string with each step explained
     */
    public String getDetailedBreakdown(Taxpayer taxpayer) {

        double grossIncome   = taxpayer.getGrossIncome();
        double taxableIncome = Math.max(grossIncome - STANDARD_DEDUCTION, 0);
        double rawSlabTax    = calculateSlabTax(taxableIncome);
        double finalBaseTax  = applyRebateAndMarginalRelief(taxableIncome, rawSlabTax);
        double surcharge     = calculateSurcharge(taxableIncome, finalBaseTax);
        double cess          = (finalBaseTax + surcharge) * 0.04;
        double totalTax      = finalBaseTax + surcharge + cess;

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(55)).append("\n");
        sb.append("  NEW REGIME — STEP BY STEP BREAKDOWN\n");
        sb.append("=".repeat(55)).append("\n\n");

        sb.append(String.format("  Gross Income            : Rs.%,10.0f%n", grossIncome));
        sb.append(String.format("  Less: Standard Deduction: Rs.%,10.0f%n", STANDARD_DEDUCTION));
        sb.append(String.format("  ──────────────────────────────────%n"));
        sb.append(String.format("  Taxable Income          : Rs.%,10.0f%n%n", taxableIncome));

        sb.append("  SLAB-WISE TAX:\n");
        sb.append(slabBreakdownString(taxableIncome));

        sb.append(String.format("%n  Raw Tax (before rebate) : Rs.%,10.0f%n", rawSlabTax));

        // Explain rebate decision
        if (taxableIncome <= REBATE_INCOME_LIMIT) {
            sb.append(String.format(
                "  Sec 87A Rebate          : Rs.%,10.0f (FULL REBATE — income ≤ Rs.12L)%n",
                rawSlabTax));
        } else {
            double excess = taxableIncome - REBATE_INCOME_LIMIT;
            if (rawSlabTax > excess) {
                sb.append(String.format(
                    "  Marginal Relief Applied : Tax capped at Rs.%,.0f%n", excess));
            } else {
                sb.append("  No rebate/relief        : Income well above Rs.12L\n");
            }
        }

        sb.append(String.format("  ──────────────────────────────────%n"));
        sb.append(String.format("  Tax After Rebate        : Rs.%,10.0f%n", finalBaseTax));
        sb.append(String.format("  Surcharge               : Rs.%,10.0f%n", surcharge));
        sb.append(String.format("  4%% Health & Edu Cess    : Rs.%,10.0f%n", cess));
        sb.append(String.format("  ──────────────────────────────────%n"));
        sb.append(String.format("  TOTAL TAX PAYABLE       : Rs.%,10.0f%n", totalTax));
        sb.append(String.format("  Effective Rate          : %9.2f%%%n",
            grossIncome > 0 ? (totalTax / grossIncome * 100) : 0));
        sb.append("=".repeat(55));

        return sb.toString();
    }

    /**
     * Helper for getDetailedBreakdown().
     * Builds a slab-by-slab tax line for display.
     */
    private String slabBreakdownString(double income) {

        StringBuilder sb = new StringBuilder();
        double previousLimit = 0;
        String[] slabLabels = {
            "  Rs.0      - Rs.4L   (0%) ",
            "  Rs.4L     - Rs.8L   (5%) ",
            "  Rs.8L     - Rs.12L (10%) ",
            "  Rs.12L    - Rs.16L (15%) ",
            "  Rs.16L    - Rs.20L (20%) ",
            "  Rs.20L    - Rs.24L (25%) "
        };

        for (int i = 0; i < SLAB_LIMITS.length; i++) {
            if (income <= previousLimit) break;

            double incomeInSlab = Math.min(income, SLAB_LIMITS[i]) - previousLimit;
            double taxInSlab    = incomeInSlab * SLAB_RATES[i];

            sb.append(String.format("  %s: Rs.%,8.0f × %4.0f%% = Rs.%,8.0f%n",
                slabLabels[i], incomeInSlab, SLAB_RATES[i] * 100, taxInSlab));

            previousLimit = SLAB_LIMITS[i];
        }

        // Top slab
        if (income > SLAB_LIMITS[SLAB_LIMITS.length - 1]) {
            double top = income - SLAB_LIMITS[SLAB_LIMITS.length - 1];
            sb.append(String.format(
                "  Above Rs.24L      (30%) : Rs.%,8.0f × 30%% = Rs.%,8.0f%n",
                top, top * 0.30));
        }

        return sb.toString();
    }
}
