// File: com/taxsystem/models/Taxpayer.java
package com.taxsystem.models;

public class Taxpayer {

    private String name;
    private String pan;
    private String aadhaar;
    private double grossIncome;
    private int age;
    private String taxYear; // "2025-26" — replaces old "Assessment Year" concept

    // Constructor
    public Taxpayer(String name, String pan, String aadhaar,
                    double grossIncome, int age, String taxYear) {
        this.name = name;
        this.pan = pan;
        this.aadhaar = aadhaar;
        this.grossIncome = grossIncome;
        this.age = age;
        this.taxYear = taxYear;
    }

    // Getters
    public String getName()        { return name; }
    public String getPan()         { return pan; }
    public String getAadhaar()     { return aadhaar; }
    public double getGrossIncome() { return grossIncome; }
    public int getAge()            { return age; }
    public String getTaxYear()     { return taxYear; }

    @Override
    public String toString() {
        return "Taxpayer[" + name + ", PAN=" + pan + ", Income=" + grossIncome + "]";
    }
}