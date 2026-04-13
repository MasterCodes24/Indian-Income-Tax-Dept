// File: com/taxsystem/ui/App.java
package com.taxsystem.ui;

import javax.swing.SwingUtilities;

/**
 * Entry point.
 * SwingUtilities.invokeLater ensures the GUI is created
 * on the Event Dispatch Thread (EDT) — this is mandatory for Swing.
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}