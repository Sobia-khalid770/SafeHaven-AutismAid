package safehaven.ui;

import safehaven.utils.AppColors;
import safehaven.utils.AppFonts;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class StyledTextField extends JTextField {
    public StyledTextField(String placeholder, int cols) {
        super(cols);
        setFont(AppFonts.BODY);
        setForeground(AppColors.TEXT_DARK);
        setBackground(new Color(255, 255, 255, 220));
        setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 190, 160), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        setOpaque(true);
    }
}
