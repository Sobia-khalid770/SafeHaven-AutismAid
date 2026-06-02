package safehaven.ui;

import safehaven.utils.AppColors;
import safehaven.utils.AppFonts;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class LogoPainter extends JPanel {
    private final int size;
    private final boolean showText;

    public LogoPainter(int size, boolean showText) {
        this.size = size;
        this.showText = showText;
        setOpaque(false);
        int totalH = showText ? size + 60 : size;
        setPreferredSize(new Dimension(size + 30, totalH));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = getWidth() / 2;
        int cy = size / 2 + 10;

        // Kid-friendly pastel colors
        Color warmPeach = new Color(255, 229, 180);
        Color softPink = new Color(255, 182, 193);
        Color lightPurple = new Color(221, 160, 221);
        Color softBlue = new Color(135, 206, 250);
        Color softGreen = new Color(144, 238, 144);
        Color cream = new Color(255, 253, 208);
        Color white = new Color(255, 255, 255);
        Color darkPink = new Color(219, 112, 147);
        Color skyBlue = new Color(100, 149, 237);

        // Outer soft glow - rainbow feel
        g2.setColor(new Color(255, 200, 180, 50));
        g2.fillOval(cx - size/2 - 12, cy - size/2 - 12, size + 24, size + 24);

        // Main circle - warm sunshine gradient
        GradientPaint bgGP = new GradientPaint(
            cx - size/2, cy - size/2, warmPeach,
            cx + size/2, cy + size/2, softPink
        );
        g2.setPaint(bgGP);
        g2.fillOval(cx - size/2, cy - size/2, size, size);

        // Fun colorful border - dashed feel
        g2.setStroke(new BasicStroke(4f));
        g2.setColor(lightPurple);
        g2.drawOval(cx - size/2, cy - size/2, size, size);

        // Inner colorful ring
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(softBlue);
        g2.drawOval(cx - size/2 + 6, cy - size/2 + 6, size - 12, size - 12);

        // Cute house shape - cozy home
        int houseW = (int)(size * 0.6);
        int houseH = (int)(size * 0.45);
        int hx = cx - houseW / 2;
        int hy = cy - houseH / 2 + 8;

        // House body - colorful
        g2.setColor(softBlue);
        g2.fillRoundRect(hx, hy + 10, houseW, houseH, 10, 10);
        
        // House body border
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(skyBlue);
        g2.drawRoundRect(hx, hy + 10, houseW, houseH, 10, 10);

        // Roof - big triangle
        int roofH = (int)(size * 0.3);
        int[] rx = {hx - 8, cx, hx + houseW + 8};
        int[] ry = {hy + 10, hy - roofH + 10, hy + 10};
        
        g2.setColor(softPink);
        g2.fillPolygon(rx, ry, 3);
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(darkPink);
        g2.drawPolygon(rx, ry, 3);

        // Door - cute arch
        int doorW = houseW / 4;
        int doorH = houseH / 2 + 5;
        int dox = cx - doorW / 2;
        int doy = hy + houseH - doorH + 15;
        
        g2.setColor(softGreen);
        g2.fillRoundRect(dox, doy, doorW, doorH, doorW/2, doorW/2);
        g2.setColor(new Color(0, 180, 100));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(dox, doy, doorW, doorH, doorW/2, doorW/2);

        // Door knob
        g2.setColor(new Color(255, 215, 0));
        g2.fillOval(dox + doorW - 6, doy + doorH/2, 6, 6);

        // Windows - two cute squares
        int winW = houseW / 5;
        int winH = houseH / 4;
        g2.setColor(cream);
        g2.fillRoundRect(hx + 6, hy + houseH/3 + 15, winW, winH, 5, 5);
        g2.fillRoundRect(hx + houseW - winW - 6, hy + houseH/3 + 15, winW, winH, 5, 5);
        
        // Window crosses
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(skyBlue);
        g2.drawLine(hx + winW/2 + 6, hy + houseH/3 + 15, hx + winW/2 + 6, hy + houseH/3 + winH + 15);
        g2.drawLine(hx + 6, hy + houseH/3 + winH/2 + 15, hx + winW + 6, hy + houseH/3 + winH/2 + 15);
        g2.drawLine(hx + houseW - winW - 6 + winW/2, hy + houseH/3 + 15, hx + houseW - winW - 6 + winW/2, hy + houseH/3 + winH + 15);
        g2.drawLine(hx + houseW - winW - 6, hy + houseH/3 + winH/2 + 15, hx + houseW - 6, hy + houseH/3 + winH/2 + 15);

        // Sun rays around house
        g2.setColor(new Color(255, 223, 0, 180));
        int sunX = cx;
        int sunY = cy - houseH - 5;
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 + i * Math.PI / 4;
            int sx1 = sunX + (int)(12 * Math.cos(angle));
            int sy1 = sunY + (int)(12 * Math.sin(angle));
            int sx2 = sunX + (int)(18 * Math.cos(angle));
            int sy2 = sunY + (int)(18 * Math.sin(angle));
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(sx1, sy1, sx2, sy2);
        }

        // Sun face - happy
        g2.setColor(new Color(255, 223, 0));
        g2.fillOval(sunX - 8, sunY - 8, 16, 16);
        
        // Happy eyes
        g2.setColor(new Color(180, 100, 50));
        g2.fillOval(sunX - 5, sunY - 3, 3, 3);
        g2.fillOval(sunX + 2, sunY - 3, 3, 3);
        
        // Happy smile
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(sunX - 4, sunY, 8, 6, 20, 140);

        // Small hearts floating
        g2.setColor(new Color(255, 105, 180, 200));
        drawHeartShape(g2, cx - houseW/2 - 15, cy + 5, 8);
        drawHeartShape(g2, cx + houseW/2 + 10, cy - 10, 6);

        // Small stars
        g2.setColor(new Color(255, 215, 0, 200));
        drawStarShape(g2, cx - houseW/2 + 5, cy - houseH - 8, 5);
        drawStarShape(g2, cx + houseW/2 - 12, cy + houseH + 5, 4);

        // Text below
        if (showText) {
            // Fun bouncy name
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, (int)(size * 0.22)));
            g2.setColor(new Color(219, 112, 147));
            String name = "SafeHaven";
            FontMetrics fm = g2.getFontMetrics();
            int textY = cy + size / 2 + 30;
            g2.drawString(name, cx - fm.stringWidth(name) / 2, textY);

            // Friendly tagline
            g2.setFont(new Font("Comic Sans MS", Font.PLAIN, (int)(size * 0.12)));
            g2.setColor(new Color(100, 149, 237));
            String tag = "Learning with Love ";
            fm = g2.getFontMetrics();
            g2.drawString(tag, cx - fm.stringWidth(tag) / 2, textY + 22);
        }

        g2.dispose();
    }

    private void drawHeartShape(Graphics2D g2, int x, int y, int s) {
        Path2D heart = new Path2D.Double();
        heart.moveTo(x, y + s * 0.3);
        heart.curveTo(x, y, x - s, y, x - s, y + s * 0.3);
        heart.curveTo(x - s, y + s * 0.7, x, y + s * 1.0, x, y + s * 1.2);
        heart.curveTo(x, y + s * 1.0, x + s, y + s * 0.7, x + s, y + s * 0.3);
        heart.curveTo(x + s, y, x, y, x, y + s * 0.3);
        g2.fill(heart);
    }

    private void drawStarShape(Graphics2D g2, int x, int y, int r) {
        int[] px = new int[10];
        int[] py = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            int rad = (i % 2 == 0) ? r : r / 2;
            px[i] = x + (int)(rad * Math.cos(angle));
            py[i] = y - (int)(rad * Math.sin(angle));
        }
        g2.fillPolygon(px, py, 10);
    }
}
