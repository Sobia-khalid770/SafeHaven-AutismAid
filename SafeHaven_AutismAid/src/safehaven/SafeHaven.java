package safehaven;

import safehaven.ui.ManualFrame;
import javax.swing.*;

public class SafeHaven {
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ManualFrame().setVisible(true);
        });
    }
}