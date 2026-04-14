package com.taxsystem.calculations;

import com.taxsystem.models.Deductions;
import com.taxsystem.models.TaxResult;
import com.taxsystem.models.Taxpayer;

public class RegimeComparator {

    private final OldRegimeCalculator oldCalc = new OldRegimeCalculator();
    private final NewRegimeCalculator newCalc = new NewRegimeCalculator();

    public TaxResult[] compare(Taxpayer taxpayer, Deductions deductions) {

        TaxResult oldResult = oldCalc.calculate(taxpayer, deductions);
        TaxResult newResult = newCalc.calculate(taxpayer);

        if (oldResult.getTotalTax() <= newResult.getTotalTax()) {
            oldResult.setRecommended(true);
            newResult.setRecommended(false);
        } else {
            oldResult.setRecommended(false);
            newResult.setRecommended(true);
        }

        return new TaxResult[]{ oldResult, newResult };
    }

    public double getSavings(TaxResult[] results) {
        return Math.abs(results[0].getTotalTax() - results[1].getTotalTax());
    }

    public String buildResidentialStatusConfirmation(Taxpayer taxpayer) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(65)).append("\n");
        sb.append("  RESIDENTIAL STATUS CONFIRMATION\n");
        sb.append("=".repeat(65)).append("\n");
        sb.append(String.format("  Status Selected : %s%n",
                                taxpayer.getResidentialStatusLabel()));
        sb.append(String.format("  Tax Implication : %s%n",
                                taxpayer.getResidentialStatusImplication()));

        sb.append("\n  INCOME SCOPE:\n");
        sb.append(String.format("  India Income    : Rs.%,.0f%n",
                                taxpayer.getGrossIndiaIncome()));

        if (taxpayer.getForeignIncome() > 0) {
            if (taxpayer.mustDeclareForeignIncome()) {
                sb.append(String.format(
                    "  Foreign Income  : Rs.%,.0f  <- INCLUDED in taxable income (RES)%n",
                    taxpayer.getForeignIncome()));
            } else {
                sb.append(String.format(
                    "  Foreign Income  : Rs.%,.0f  <- EXEMPT (not taxed in India)%n",
                    taxpayer.getForeignIncome()));
            }
        }

        sb.append(String.format("  Total Taxable   : Rs.%,.0f%n",
                                taxpayer.getGrossIncome()));

        if (!taxpayer.isHRAApplicable()) {
            sb.append("\n  WARNING: HRA deduction blocked for Non Residents.\n");
            sb.append("    Any HRA amount entered has been set to 0.\n");
        }

        sb.append("=".repeat(65)).append("\n\n");
        return sb.toString();
    }
}
