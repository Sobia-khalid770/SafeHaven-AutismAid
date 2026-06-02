package safehaven.ui;

import safehaven.utils.AppColors;
import javax.swing.*;
import java.awt.*;

public class PolkaDotPanel extends JPanel {
    private final Color bgColor;
    private static final int DOT_SIZE = 18;
    private static final int SPACING  = 55;

    public PolkaDotPanel(Color bg) {
        this.bgColor = bg;
        setOpaque(true);
    }

    public PolkaDotPanel() {
        this(AppColors.BG_MAIN);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(bgColor);
        g2.fillRect(0, 0, getWidth(), getHeight());

        Color[] dots = AppColors.POLKA_COLORS;
        int idx = 0;
        for (int y = 0; y < getHeight() + SPACING; y += SPACING) {
            int offset = (y / SPACING % 2 == 0) ? 0 : SPACING / 2;
            for (int x = -SPACING + offset; x < getWidth() + SPACING; x += SPACING) {
                g2.setColor(dots[idx % dots.length]);
                g2.fillOval(x - DOT_SIZE / 2, y - DOT_SIZE / 2, DOT_SIZE, DOT_SIZE);
                idx++;
            }
        }
        g2.dispose();
    }
}
