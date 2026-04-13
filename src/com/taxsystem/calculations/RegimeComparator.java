// File: com/taxsystem/calculations/RegimeComparator.java
package com.taxsystem.calculations;

import com.taxsystem.models.Deductions;
import com.taxsystem.models.Taxpayer;
import com.taxsystem.models.TaxResult;

/**
 * SOLVES THE REGIME PARADOX.
 * Runs both calculators and flags the better option.
 * The UI just calls compare() and displays both TaxResult objects.
 */
public class RegimeComparator {

    private final OldRegimeCalculator oldCalc = new OldRegimeCalculator();
    private final NewRegimeCalculator newCalc = new NewRegimeCalculator();

    /**
     * Returns array of size 2: [0] = Old Regime result, [1] = New Regime result
     * The cheaper one has isRecommended() == true
     */
    public TaxResult[] compare(Taxpayer taxpayer, Deductions deductions) {

        TaxResult oldResult = oldCalc.calculate(taxpayer, deductions);
        TaxResult newResult = newCalc.calculate(taxpayer);

        // Mark the beneficial regime
        if (oldResult.getTotalTax() <= newResult.getTotalTax()) {
            oldResult.setRecommended(true);
            newResult.setRecommended(false);
        } else {
            oldResult.setRecommended(false);
            newResult.setRecommended(true);
        }

        return new TaxResult[] { oldResult, newResult };
    }

    public double getSavings(TaxResult[] results) {
        return Math.abs(
            results[0].getTotalTax() - results[1].getTotalTax()
        );
    }
}