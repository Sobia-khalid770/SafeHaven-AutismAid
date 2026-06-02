package safehaven.ui;

import safehaven.utils.AppColors;
import safehaven.utils.AppFonts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomTitleBar extends JPanel {
    private Point dragStart;
    private final JFrame owner;

    public CustomTitleBar(JFrame owner, String title) {
        this.owner = owner;
        setLayout(new BorderLayout());
        setBackground(AppColors.TITLE_BAR);
        setPreferredSize(new Dimension(0, 38));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 190, 160)));

        // Title label
        JLabel lbl = new JLabel("   " + title);
        lbl.setFont(AppFonts.BODY_BOLD);
        lbl.setForeground(AppColors.TEXT_DARK);
        add(lbl, BorderLayout.CENTER);

        // Control buttons panel
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        ctrl.setOpaque(false);

        JButton btnMin   = makeBtn("", AppColors.TITLE_BTN_MIN);
        JButton btnMax   = makeBtn("", AppColors.TITLE_BTN_MAX);
        JButton btnClose = makeBtn("", AppColors.TITLE_BTN_CLOSE);

        btnMin.addActionListener(e -> owner.setState(Frame.ICONIFIED));
        btnMax.addActionListener(e -> {
            if (owner.getExtendedState() == JFrame.MAXIMIZED_BOTH)
                owner.setExtendedState(JFrame.NORMAL);
            else
                owner.setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
        btnClose.addActionListener(e -> owner.dispose());

        ctrl.add(btnMin);
        ctrl.add(btnMax);
        ctrl.add(btnClose);
        add(ctrl, BorderLayout.EAST);

        // Drag to move
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { dragStart = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart != null && owner.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    Point loc = owner.getLocation();
                    owner.setLocation(loc.x + e.getX() - dragStart.x,
                                      loc.y + e.getY() - dragStart.y);
                }
            }
        };
        addMouseListener(drag);
        addMouseMotionListener(drag);
        lbl.addMouseListener(drag);
        lbl.addMouseMotionListener(drag);
    }

    private JButton makeBtn(String text, Color color) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? color.darker() : color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (getWidth()-fm.stringWidth(text))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(22, 22));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
