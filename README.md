# 🇮🇳 Indian Income Tax Generator — FY 2025-26

A Java-based desktop application that calculates and compares income tax liability
under both the **Old Regime (Income Tax Act 1961)** and the **New Regime (Finance Act 2025)**
for Indian taxpayers, effective April 2026.

Built by a 1st-year engineering student as a practical implementation of
Java OOP, Swing GUI, multithreading, and Indian tax law.

---

## ✨ Features

- **Regime Comparator** — Calculates tax under both Old and New Regime simultaneously
  and automatically recommends the cheaper option
- **Finance Act 2025 Slabs** — Hardcoded updated New Regime slabs (Nil up to Rs.4L,
  5% up to Rs.8L, 10% up to Rs.12L, etc.)
- **Section 87A Rebate** — Zero tax for taxable income up to Rs.12 Lakh under New Regime
- **Marginal Relief** — Prevents unfair tax jumps just above the Rs.12L rebate limit
- **Residential Status Support** — Handles RES, RNOR, and NR taxpayers with correct
  tax implications for each
- **Old Regime Deductions** — Full support for 80C, 80D, HRA, NPS, and Standard Deduction
- **Age-based Slabs** — Separate tax calculation for Normal, Senior (60+),
  and Super Senior (80+) citizens
- **Input Validation** — PAN (regex), Aadhaar (12-digit), income, and age validation
- **Background Threading** — SwingWorker keeps the GUI responsive during calculation
- **Foreign Income Handling** — RES taxpayers are taxed on global income;
  RNOR and NR are taxed on India-sourced income only

---

## 🏗️ Project Structure

```
src/
└── com/
    └── taxsystem/
        ├── models/
        │   ├── Taxpayer.java           ← Stores personal + residential data
        │   ├── TaxResult.java          ← Holds tax breakdown output
        │   └── Deductions.java         ← Old Regime deduction container
        ├── calculations/
        │   ├── NewRegimeCalculator.java ← Finance Act 2025 slab logic
        │   ├── OldRegimeCalculator.java ← Income Tax Act 1961 slab logic
        │   ├── RegimeComparator.java    ← Runs both and picks the best
        │   └── ValidationHelper.java   ← PAN, Aadhaar, income validation
        └── ui/
            ├── MainFrame.java          ← Main Swing window
            └── App.java                ← Entry point
```

---

## 💻 Requirements

- Java JDK 11 or higher
- Windows / macOS / Linux
- No external libraries required — pure Java SE

---

## ⚙️ How to Compile and Run

### Step 1 — Navigate to the src directory
```bash
cd src
```

### Step 2 — Compile all files
```bash
javac -encoding UTF-8 com/taxsystem/models/*.java com/taxsystem/calculations/*.java com/taxsystem/ui/*.java
```

### Step 3 — Run
```bash
java com.taxsystem.ui.App
```

### PowerShell Users (Windows)
If you get `.class` file errors, clean first:
```powershell
Get-ChildItem -Recurse -Filter "*.class" | Remove-Item -Force
javac -encoding UTF-8 com/taxsystem/models/*.java com/taxsystem/calculations/*.java com/taxsystem/ui/*.java
java com.taxsystem.ui.App
```

---

## 🧾 Tax Slabs Implemented

### New Regime — Finance Act 2025 (Default)

| Income Range         | Tax Rate |
|----------------------|----------|
| Rs.0 – Rs.4,00,000   | Nil      |
| Rs.4L – Rs.8L        | 5%       |
| Rs.8L – Rs.12L       | 10%      |
| Rs.12L – Rs.16L      | 15%      |
| Rs.16L – Rs.20L      | 20%      |
| Rs.20L – Rs.24L      | 25%      |
| Above Rs.24L         | 30%      |

> Standard Deduction: Rs.75,000 | Section 87A Rebate: Zero tax up to Rs.12L taxable income

### Old Regime — Income Tax Act 1961

| Income Range           | Tax Rate |
|------------------------|----------|
| Rs.0 – Rs.2,50,000     | Nil      |
| Rs.2.5L – Rs.5L        | 5%       |
| Rs.5L – Rs.10L         | 20%      |
| Above Rs.10L           | 30%      |

> Senior Citizens (60+): Basic exemption Rs.3L | Super Senior (80+): Rs.5L

---

## 🌍 Residential Status Support

| Status | Full Form                            | Taxed On              | HRA |
|--------|--------------------------------------|-----------------------|-----|
| RES    | Resident                             | Global income         | Yes |
| RNOR   | Resident But Not Ordinarily Resident | India income only     | Yes |
| NR     | Non Resident                         | India income only     | No  |

---

## 🔧 Known Issues and Fixes

### BOM Error on Windows (`illegal character: '\ufeff'`)
PowerShell's `Set-Content` writes a UTF-8 BOM that Java cannot read.
Fix all files with:
```powershell
Get-ChildItem -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw -Encoding UTF8
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($_.FullName, $content, $utf8NoBom)
}
```

### Rupee Symbol Displays as Garbage (`â,¹`)
The Rs. prefix is used throughout instead of the Unicode rupee symbol
to ensure compatibility across all Windows terminal and Java versions.

---

## 📐 Architecture Decisions

**Why three packages?**
Each package has exactly one responsibility. `models` holds data,
`calculations` holds logic, `ui` holds display. This means if India
changes tax slabs next year, only `calculations` needs to be touched.
The GUI never breaks.

**Why SwingWorker?**
Java's Event Dispatch Thread (EDT) runs all GUI updates. Running tax
calculations directly on it would freeze the window. SwingWorker moves
the computation to a background thread and safely returns the result
to the EDT when done.

**Why is Deductions passed to OldRegimeCalculator but not NewRegimeCalculator?**
The New Regime legally does not allow any deductions. Accepting a
Deductions object in that calculator would be architecturally misleading.
The separation makes the law visible in the code structure itself.

---

## 📄 Legal Disclaimer

This application is for educational purposes only.
Tax calculations are based on publicly available Finance Act 2025 provisions.
Always consult a Chartered Accountant for official tax filing.
The author is not responsible for any financial decisions made
based on this software.
