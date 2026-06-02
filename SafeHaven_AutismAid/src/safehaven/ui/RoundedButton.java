package safehaven.ui;

import safehaven.utils.AppFonts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RoundedButton extends JButton {
    private Color baseColor;
    private Color hoverColor;
    private Color pressColor;
    private Color textColor;
    private int radius;
    private boolean hovered = false;
    private boolean pressed = false;

    public RoundedButton(String text, Color base, Color textColor) {
        super(text);
        this.baseColor  = base;
        this.hoverColor = base.brighter();
        this.pressColor = base.darker();
        this.textColor  = textColor;
        this.radius     = 24;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(AppFonts.BTN);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(textColor);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
            public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
    }

    public RoundedButton(String text, Color base) {
        this(text, base, Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c = pressed ? pressColor : hovered ? hoverColor : baseColor;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 25));
        g2.fillRoundRect(2, 4, getWidth()-2, getHeight()-2, radius, radius);

        // Button body
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-3, radius, radius);

        // Shine
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(3, 2, getWidth()-8, getHeight()/2-2, radius, radius);

        // Text
        g2.setFont(getFont());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
        int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 - 1;
        g2.drawString(getText(), tx, ty);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 30, d.height + 12);
    }
}