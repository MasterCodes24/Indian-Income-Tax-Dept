// File: com/taxsystem/ui/MainFrame.java
package com.taxsystem.ui;

import com.taxsystem.calculations.RegimeComparator;
import com.taxsystem.calculations.ValidationHelper;
import com.taxsystem.models.Deductions;
import com.taxsystem.models.TaxResult;
import com.taxsystem.models.Taxpayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main application window.
 * IMPORTANT DESIGN RULE: This class handles ONLY display logic.
 * It delegates all math to RegimeComparator.
 */
public class MainFrame extends JFrame implements ActionListener {

    // ---- Input Fields ----
    private JTextField nameField;
    private JTextField panField;
    private JTextField aadhaarField;
    private JTextField incomeField;
    private JTextField ageField;
    private JTextField field80C;
    private JTextField field80D;
    private JTextField fieldHRA;
    private JTextField fieldNPS;

    // ---- Buttons ----
    private JButton calculateBtn;
    private JButton clearBtn;

    // ---- Output Area ----
    private JTextArea resultArea;
    private JLabel statusLabel;

    // ---- Logic Layer ----
    private final RegimeComparator comparator = new RegimeComparator();

    public MainFrame() {
        setTitle("Income Tax Comparator — FY 2025-26 (New Tax Act)");
        setSize(750, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10));

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        // ----- HEADER -----
        JLabel header = new JLabel(
            "India Income Tax System — FY 2025-26", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 204));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(header, BorderLayout.NORTH);

        // ----- CENTER: Two-column input panel -----
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Left: Personal Details
        JPanel personalPanel = new JPanel(new GridLayout(0, 2, 5, 8));
        personalPanel.setBorder(BorderFactory.createTitledBorder("Personal Details"));

        nameField    = addLabeledField(personalPanel, "Full Name:");
        panField     = addLabeledField(personalPanel, "PAN Number:");
        aadhaarField = addLabeledField(personalPanel, "Aadhaar:");
        ageField     = addLabeledField(personalPanel, "Age:");
        incomeField  = addLabeledField(personalPanel, "Gross Income (₹):");

        // Hint label
        personalPanel.add(new JLabel(""));
        personalPanel.add(new JLabel(
            "<html><small>e.g. 850000 for ₹8.5L</small></html>"));

        // Right: Deductions (Old Regime only)
        JPanel deductionPanel = new JPanel(new GridLayout(0, 2, 5, 8));
        deductionPanel.setBorder(BorderFactory.createTitledBorder(
            "Deductions (Old Regime Only)"));

        field80C = addLabeledField(deductionPanel, "Sec 80C (max ₹1.5L):");
        field80D = addLabeledField(deductionPanel, "Sec 80D (Medical):");
        fieldHRA = addLabeledField(deductionPanel, "HRA Exemption:");
        fieldNPS = addLabeledField(deductionPanel, "NPS 80CCD (max ₹50K):");

        JLabel infoLabel = new JLabel(
            "<html><small>New Regime ignores<br>all deductions above.</small></html>");
        deductionPanel.add(new JLabel(""));
        deductionPanel.add(infoLabel);

        centerPanel.add(personalPanel);
        centerPanel.add(deductionPanel);
        add(centerPanel, BorderLayout.CENTER);

        // ----- SOUTH: Buttons + Results -----
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Button row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        calculateBtn = new JButton("Compare Both Regimes");
        calculateBtn.setBackground(new Color(0, 153, 76));
        calculateBtn.setForeground(Color.WHITE);
        calculateBtn.setFont(new Font("Arial", Font.BOLD, 13));
        calculateBtn.addActionListener(this);

        clearBtn = new JButton("Clear All");
        clearBtn.addActionListener(this);

        buttonPanel.add(calculateBtn);
        buttonPanel.add(clearBtn);

        // Status bar (shows "Calculating..." during thread work)
        statusLabel = new JLabel("Ready. Enter details and click Compare.",
                                  JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        // Result area
        resultArea = new JTextArea(12, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setBorder(BorderFactory.createTitledBorder("Comparison Results"));
        resultArea.setBackground(new Color(245, 245, 245));

        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(statusLabel, BorderLayout.CENTER);
        southPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    // Helper method: adds a JLabel + JTextField pair to a panel
    private JTextField addLabeledField(JPanel panel, String labelText) {
        panel.add(new JLabel(labelText));
        JTextField field = new JTextField();
        panel.add(field);
        return field;
    }

    // ---- ACTION LISTENER ----
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearBtn) {
            clearAll();
        } else if (e.getSource() == calculateBtn) {
            runCalculationInBackground(); // Threading happens here
        }
    }

    private void clearAll() {
        nameField.setText("");    panField.setText("");
        aadhaarField.setText(""); ageField.setText("");
        incomeField.setText("");  field80C.setText("");
        field80D.setText("");     fieldHRA.setText("");
        fieldNPS.setText("");     resultArea.setText("");
        statusLabel.setText("Cleared. Enter new details.");
    }

    /**
     * CONCURRENCY SOLUTION — SwingWorker
     *
     * Problem: If you call comparator.compare() directly in actionPerformed(),
     * Java's Event Dispatch Thread (EDT) is blocked. The GUI freezes —
     * buttons stop responding, window won't repaint.
     *
     * Solution: SwingWorker runs the heavy computation on a BACKGROUND THREAD.
     * When done, it safely updates the GUI back on the EDT via done().
     *
     * Think of it as: a helper goes to the backroom to calculate (background),
     * then comes back to the counter to write the answer on the board (EDT).
     */
    private void runCalculationInBackground() {

        // Step 1: Validate ALL inputs before starting the thread
        if (!validateInputs()) return;

        // Step 2: Collect inputs
        final String name     = nameField.getText().trim();
        final String pan      = panField.getText().trim().toUpperCase();
        final String aadhaar  = aadhaarField.getText().trim();
        final int    age      = Integer.parseInt(ageField.getText().trim());
        final double income   = ValidationHelper.parseIncome(incomeField.getText());
        final double d80C     = parseFieldSafely(field80C);
        final double d80D     = parseFieldSafely(field80D);
        final double dHRA     = parseFieldSafely(fieldHRA);
        final double dNPS     = parseFieldSafely(fieldNPS);

        // Step 3: Disable button and show status
        calculateBtn.setEnabled(false);
        statusLabel.setText("Calculating... please wait.");
        resultArea.setText("");

        // Step 4: Create SwingWorker
        SwingWorker<TaxResult[], Void> worker = new SwingWorker<>() {

            @Override
            protected TaxResult[] doInBackground() throws Exception {
                // THIS RUNS ON BACKGROUND THREAD — safe for heavy computation
                Taxpayer taxpayer = new Taxpayer(
                    name, pan, aadhaar, income, age, "2025-26");
                Deductions deductions = new Deductions(d80C, d80D, dHRA, dNPS);
                return comparator.compare(taxpayer, deductions);
            }

            @Override
            protected void done() {
                // THIS RUNS ON EDT — safe to update GUI here
                try {
                    TaxResult[] results = get(); // Get result from doInBackground
                    displayResults(results, income);
                    statusLabel.setText("Calculation complete for FY 2025-26.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Error during calculation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error. Please try again.");
                } finally {
                    calculateBtn.setEnabled(true);
                }
            }
        };

        worker.execute(); // Start the background thread
    }

    private void displayResults(TaxResult[] results, double grossIncome) {
        TaxResult oldR = results[0];
        TaxResult newR = results[1];
        double savings = comparator.getSavings(results);

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(65)).append("\n");
        sb.append("  INCOME TAX COMPARISON REPORT — FY 2025-26\n");
        sb.append("=".repeat(65)).append("\n\n");

        sb.append(String.format("  Gross Income Entered  : ₹%,.0f%n", grossIncome));
        sb.append("\n");

        // Old Regime block
        String oldTag = oldR.isRecommended() ? "  ★ RECOMMENDED ★" : "";
        sb.append(String.format("  OLD REGIME (Act 1961)%s%n", oldTag));
        sb.append("-".repeat(65)).append("\n");
        sb.append(String.format("  Taxable Income  : ₹%,.0f%n", oldR.getTaxableIncome()));
        sb.append(String.format("  Base Tax        : ₹%,.0f%n", oldR.getBaseTax()));
        sb.append(String.format("  Surcharge       : ₹%,.0f%n", oldR.getSurcharge()));
        sb.append(String.format("  4%% Cess         : ₹%,.0f%n", oldR.getCess()));
        sb.append(String.format("  TOTAL TAX       : ₹%,.0f%n", oldR.getTotalTax()));
        sb.append(String.format("  Effective Rate  : %.2f%%%n%n", oldR.getEffectiveRate()));

        // New Regime block
        String newTag = newR.isRecommended() ? "  ★ RECOMMENDED ★" : "";
        sb.append(String.format("  NEW REGIME (Finance Act 2025)%s%n", newTag));
        sb.append("-".repeat(65)).append("\n");
        sb.append(String.format("  Taxable Income  : ₹%,.0f%n", newR.getTaxableIncome()));
        sb.append(String.format("  Base Tax        : ₹%,.0f%n", newR.getBaseTax()));
        sb.append(String.format("  Surcharge       : ₹%,.0f%n", newR.getSurcharge()));
        sb.append(String.format("  4%% Cess         : ₹%,.0f%n", newR.getCess()));
        sb.append(String.format("  TOTAL TAX       : ₹%,.0f%n", newR.getTotalTax()));
        sb.append(String.format("  Effective Rate  : %.2f%%%n%n", newR.getEffectiveRate()));

        // Verdict
        sb.append("=".repeat(65)).append("\n");
        TaxResult recommended = oldR.isRecommended() ? oldR : newR;
        sb.append(String.format("  GO WITH: %s%n", recommended.getRegimeName()));
        sb.append(String.format("  YOU SAVE: ₹%,.0f in taxes%n", savings));
        sb.append("=".repeat(65));

        resultArea.setText(sb.toString());
    }

    // ---- Validation ----
    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Name cannot be empty."); return false;
        }
        if (!ValidationHelper.isValidPAN(panField.getText())) {
            showError("Invalid PAN. Format: ABCDE1234F"); return false;
        }
        if (!ValidationHelper.isValidAadhaar(aadhaarField.getText())) {
            showError("Invalid Aadhaar. Must be 12 digits."); return false;
        }
        if (!ValidationHelper.isValidAge(ageField.getText())) {
            showError("Invalid age. Must be between 18 and 120."); return false;
        }
        if (!ValidationHelper.isValidIncome(incomeField.getText())) {
            showError("Invalid income. Enter a positive number like 850000");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error",
                                      JOptionPane.ERROR_MESSAGE);
    }

    // Safely parse optional deduction fields (empty = 0)
    private double parseFieldSafely(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) return 0.0;
        try { return Double.parseDouble(text); }
        catch (NumberFormatException e) { return 0.0; }
    }
}