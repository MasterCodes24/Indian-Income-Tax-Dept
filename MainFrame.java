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

public class MainFrame extends JFrame implements ActionListener {

    // ── Personal Detail Fields ────────────────────────────────
    private JTextField  nameField;
    private JTextField  panField;
    private JTextField  aadhaarField;
    private JTextField  ageField;
    private JTextField  incomeField;
    private JTextField  foreignIncomeField;

    // ── Residential Status Dropdown ───────────────────────────
    private JComboBox<String> residentialStatusBox;

    // ── Deduction Fields ──────────────────────────────────────
    private JTextField field80C;
    private JTextField field80D;
    private JTextField fieldHRA;
    private JTextField fieldNPS;

    // ── HRA label (shown/hidden based on status) ──────────────
    private JLabel hraLabel;

    // ── Buttons ───────────────────────────────────────────────
    private JButton calculateBtn;
    private JButton clearBtn;

    // ── Output ────────────────────────────────────────────────
    private JTextArea resultArea;
    private JLabel    statusLabel;

    // ── Logic ─────────────────────────────────────────────────
    private final RegimeComparator comparator = new RegimeComparator();

    // Residential status options shown in dropdown
    private static final String[] STATUS_OPTIONS = {
        "RES - Resident",
        "RNOR - Resident But Not Ordinarily Resident",
        "NR - Non Resident"
    };

    public MainFrame() {
        setTitle("Income Tax Comparator — FY 2025-26 (Finance Act 2025)");
        setSize(820, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        initComponents();
        setVisible(true);
    }

    private void initComponents() {

        // ── HEADER ────────────────────────────────────────────
        JLabel header = new JLabel(
            "India Income Tax System — FY 2025-26", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setOpaque(true);
        header.setBackground(new Color(0, 102, 204));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(header, BorderLayout.NORTH);

        // ── CENTER: Three sections ─────────────────────────────
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // SECTION 1: Residential Status (appears FIRST, before everything)
        JPanel statusPanel = buildResidentialStatusPanel();
        centerPanel.add(statusPanel);
        centerPanel.add(Box.createVerticalStrut(8));

        // SECTION 2: Personal + Income details
        JPanel detailsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        detailsRow.add(buildPersonalPanel());
        detailsRow.add(buildDeductionsPanel());
        centerPanel.add(detailsRow);

        add(centerPanel, BorderLayout.CENTER);

        // ── SOUTH: Buttons + Results ───────────────────────────
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

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

        statusLabel = new JLabel(
            "Select Residential Status, fill details, then click Compare.",
            JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        resultArea = new JTextArea(14, 65);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setBorder(BorderFactory.createTitledBorder("Comparison Results"));
        resultArea.setBackground(new Color(245, 245, 245));

        southPanel.add(buttonPanel,              BorderLayout.NORTH);
        southPanel.add(statusLabel,              BorderLayout.CENTER);
        southPanel.add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    // ────────────────────────────────────────────────────────────
    // BUILD: Residential Status Panel (shown at top, step 1)
    // ────────────────────────────────────────────────────────────
    private JPanel buildResidentialStatusPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            "Step 1 — Residential Status (Required First)",
            0, 0,
            new Font("Arial", Font.BOLD, 12),
            new Color(0, 102, 204)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 10, 6, 10);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        // Label
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        panel.add(new JLabel("Residential Status for FY 2025-26:"), gc);

        // Dropdown
        residentialStatusBox = new JComboBox<>(STATUS_OPTIONS);
        residentialStatusBox.setFont(new Font("Arial", Font.PLAIN, 13));
        residentialStatusBox.setBackground(Color.WHITE);
        gc.gridx = 1; gc.weightx = 0.4;
        panel.add(residentialStatusBox, gc);

        // Info label — updates when user changes selection
        JLabel infoLabel = new JLabel(getStatusInfoText("RES"));
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(new Color(80, 80, 80));
        gc.gridx = 2; gc.weightx = 0.6;
        panel.add(infoLabel, gc);

        // When user changes the dropdown, update info text and HRA field
        residentialStatusBox.addActionListener(e -> {
            String code = getSelectedStatusCode();
            infoLabel.setText(getStatusInfoText(code));
            updateHRAFieldState(code);
        });

        return panel;
    }

    // ────────────────────────────────────────────────────────────
    // BUILD: Personal Details Panel
    // ────────────────────────────────────────────────────────────
    private JPanel buildPersonalPanel() {

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Step 2 — Personal & Income Details"));

        nameField          = addLabeledField(panel, "Full Name:");
        panField           = addLabeledField(panel, "PAN Number:");
        aadhaarField       = addLabeledField(panel, "Aadhaar:");
        ageField           = addLabeledField(panel, "Age:");
        incomeField        = addLabeledField(panel, "India Gross Income (₹):");
        foreignIncomeField = addLabeledField(panel, "Foreign Income (₹):");

        panel.add(new JLabel(""));
        panel.add(new JLabel(
            "<html><small>Foreign income: 0 if none.<br>" +
            "For RES, it is added to taxable income.</small></html>"));

        return panel;
    }

    // ────────────────────────────────────────────────────────────
    // BUILD: Deductions Panel
    // ────────────────────────────────────────────────────────────
    private JPanel buildDeductionsPanel() {

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
            "Step 3 — Deductions (Old Regime Only)"));

        field80C = addLabeledField(panel, "Sec 80C (max ₹1.5L):");
        field80D = addLabeledField(panel, "Sec 80D (Medical):");

        // HRA — will be disabled for NR status
        hraLabel = new JLabel("HRA Exemption:");
        panel.add(hraLabel);
        fieldHRA = new JTextField();
        panel.add(fieldHRA);

        fieldNPS = addLabeledField(panel, "NPS 80CCD (max ₹50K):");

        panel.add(new JLabel(""));
        panel.add(new JLabel(
            "<html><small>New Regime ignores all deductions.<br>" +
            "HRA disabled for Non Residents.</small></html>"));

        return panel;
    }

    // ────────────────────────────────────────────────────────────
    // HRA Field: Enable/Disable based on residential status
    // ────────────────────────────────────────────────────────────
    private void updateHRAFieldState(String statusCode) {
        boolean hraAllowed = !Taxpayer.STATUS_NR.equals(statusCode);
        fieldHRA.setEnabled(hraAllowed);
        fieldHRA.setBackground(hraAllowed ? Color.WHITE : new Color(220, 220, 220));

        if (!hraAllowed) {
            fieldHRA.setText("0");
            fieldHRA.setToolTipText("HRA not applicable for Non Residents");
            hraLabel.setText("<html>HRA Exemption: <font color='red'>[NR — N/A]</font></html>");
        } else {
            fieldHRA.setToolTipText(null);
            hraLabel.setText("HRA Exemption:");
        }
    }

    // ────────────────────────────────────────────────────────────
    // ACTION LISTENER
    // ────────────────────────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearBtn) {
            clearAll();
        } else if (e.getSource() == calculateBtn) {
            runCalculationInBackground();
        }
    }

    private void clearAll() {
        nameField.setText("");        panField.setText("");
        aadhaarField.setText("");     ageField.setText("");
        incomeField.setText("");      foreignIncomeField.setText("");
        field80C.setText("");         field80D.setText("");
        fieldHRA.setText("");         fieldNPS.setText("");
        resultArea.setText("");
        residentialStatusBox.setSelectedIndex(0);
        statusLabel.setText("Cleared. Select status and enter new details.");
    }

    // ────────────────────────────────────────────────────────────
    // BACKGROUND CALCULATION — SwingWorker (no GUI freeze)
    // ────────────────────────────────────────────────────────────
    private void runCalculationInBackground() {

        if (!validateInputs()) return;

        // Collect all inputs
        final String name          = nameField.getText().trim();
        final String pan           = panField.getText().trim().toUpperCase();
        final String aadhaar       = aadhaarField.getText().trim();
        final int    age           = Integer.parseInt(ageField.getText().trim());
        final double indiaIncome   = ValidationHelper.parseIncome(incomeField.getText());
        final double foreignIncome = parseFieldSafely(foreignIncomeField);
        final double d80C          = parseFieldSafely(field80C);
        final double d80D          = parseFieldSafely(field80D);
        final double dHRA          = parseFieldSafely(fieldHRA);
        final double dNPS          = parseFieldSafely(fieldNPS);
        final String statusCode    = getSelectedStatusCode();

        calculateBtn.setEnabled(false);
        statusLabel.setText("Calculating... please wait.");
        resultArea.setText("");

        SwingWorker<TaxResult[], Void> worker = new SwingWorker<>() {

            @Override
            protected TaxResult[] doInBackground() {

                // Build taxpayer WITH residential status
                Taxpayer taxpayer = new Taxpayer(
                    name, pan, aadhaar,
                    indiaIncome, foreignIncome,
                    age, "2025-26", statusCode
                );

                // Deductions object checks HRA applicability internally
                Deductions deductions = new Deductions(
                    taxpayer, d80C, d80D, dHRA, dNPS
                );

                return comparator.compare(taxpayer, deductions);
            }

            @Override
            protected void done() {
                try {
                    TaxResult[] results = get();

                    // Re-create taxpayer just for the confirmation header
                    Taxpayer t = new Taxpayer(
                        name, pan, aadhaar,
                        indiaIncome, foreignIncome,
                        age, "2025-26", statusCode
                    );

                    displayResults(results, t);
                    statusLabel.setText(
                        "Calculation complete for FY 2025-26 | Status: "
                        + t.getResidentialStatusLabel());

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Error: " + ex.getMessage(),
                        "Calculation Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error. Please try again.");
                } finally {
                    calculateBtn.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    // ────────────────────────────────────────────────────────────
    // DISPLAY RESULTS
    // ────────────────────────────────────────────────────────────
    private void displayResults(TaxResult[] results, Taxpayer taxpayer) {

        TaxResult oldR   = results[0];
        TaxResult newR   = results[1];
        double savings   = comparator.getSavings(results);

        StringBuilder sb = new StringBuilder();

        // 1. Residential status confirmation block (appears first)
        sb.append(comparator.buildResidentialStatusConfirmation(taxpayer));

        // 2. Tax comparison
        sb.append("=".repeat(65)).append("\n");
        sb.append("  INCOME TAX COMPARISON REPORT — FY 2025-26\n");
        sb.append("=".repeat(65)).append("\n\n");

        appendRegimeBlock(sb, oldR);
        sb.append("\n");
        appendRegimeBlock(sb, newR);

        // 3. Verdict
        sb.append("=".repeat(65)).append("\n");
        TaxResult best = oldR.isRecommended() ? oldR : newR;
        sb.append(String.format("  GO WITH   : %s%n", best.getRegimeName()));
        sb.append(String.format("  YOU SAVE  : ₹%,.0f in taxes%n", savings));
        sb.append("=".repeat(65));

        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0); // Scroll to top
    }

    private void appendRegimeBlock(StringBuilder sb, TaxResult r) {
        String tag = r.isRecommended() ? "  ★ RECOMMENDED ★" : "";
        sb.append(String.format("  %s%s%n", r.getRegimeName(), tag));
        sb.append("-".repeat(65)).append("\n");
        sb.append(String.format("  Taxable Income  : ₹%,.0f%n", r.getTaxableIncome()));
        sb.append(String.format("  Base Tax        : ₹%,.0f%n",  r.getBaseTax()));
        sb.append(String.format("  Surcharge       : ₹%,.0f%n",  r.getSurcharge()));
        sb.append(String.format("  4%% Cess         : ₹%,.0f%n",  r.getCess()));
        sb.append(String.format("  TOTAL TAX       : ₹%,.0f%n",  r.getTotalTax()));
        sb.append(String.format("  Effective Rate  : %.2f%%%n",   r.getEffectiveRate()));
    }

    // ────────────────────────────────────────────────────────────
    // VALIDATION
    // ────────────────────────────────────────────────────────────
    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Name cannot be empty."); return false;
        }
        if (!ValidationHelper.isValidPAN(panField.getText())) {
            showError("Invalid PAN. Format must be: ABCDE1234F"); return false;
        }
        if (!ValidationHelper.isValidAadhaar(aadhaarField.getText())) {
            showError("Invalid Aadhaar. Must be exactly 12 digits."); return false;
        }
        if (!ValidationHelper.isValidAge(ageField.getText())) {
            showError("Invalid age. Must be between 18 and 120."); return false;
        }
        if (!ValidationHelper.isValidIncome(incomeField.getText())) {
            showError("Invalid India income. Enter a positive number."); return false;
        }
        String foreign = foreignIncomeField.getText().trim();
        if (!foreign.isEmpty() && !ValidationHelper.isValidIncome(foreign)) {
            showError("Invalid foreign income. Enter a number or leave blank."); return false;
        }
        return true;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(
            this, msg, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // ────────────────────────────────────────────────────────────
    // UTILITIES
    // ────────────────────────────────────────────────────────────

    private JTextField addLabeledField(JPanel panel, String labelText) {
        panel.add(new JLabel(labelText));
        JTextField field = new JTextField();
        panel.add(field);
        return field;
    }

    private double parseFieldSafely(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty() || text.equals("0")) return 0.0;
        try { return Double.parseDouble(text); }
        catch (NumberFormatException e) { return 0.0; }
    }

    /**
     * Extracts the 2-4 character code (RES / RNOR / NR)
     * from the selected dropdown string.
     */
    private String getSelectedStatusCode() {
        String selected = (String) residentialStatusBox.getSelectedItem();
        if (selected == null) return Taxpayer.STATUS_RES;
        if (selected.startsWith("RNOR")) return Taxpayer.STATUS_RNOR;
        if (selected.startsWith("NR"))   return Taxpayer.STATUS_NR;
        return Taxpayer.STATUS_RES;
    }

    /**
     * Returns a short one-line description shown next to the dropdown.
     */
    private String getStatusInfoText(String code) {
        switch (code) {
            case Taxpayer.STATUS_RNOR:
                return "Taxed on India income only. Foreign income exempt.";
            case Taxpayer.STATUS_NR:
                return "India income only. HRA blocked. Foreign income exempt.";
            default:
                return "Taxed on global income (India + Foreign). All deductions apply.";
        }
    }

    // ────────────────────────────────────────────────────────────
    // ENTRY POINT
    // ────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}