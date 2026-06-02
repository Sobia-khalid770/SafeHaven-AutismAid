package safehaven.games;

import safehaven.models.UserAccount;
import safehaven.ui.*;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SoundBoardGame extends BaseFrame {
    private final UserAccount user;

    public SoundBoardGame(UserAccount user) {
        super("SafeHaven   Sound Board", 640, 520);
        this.user = user;

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Sound Board - Tap to Hear!", SwingConstants.CENTER);
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        content.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setOpaque(false);

        // Each button maps to a real sound file in the sounds/ folder
        // The first element is now the path to the image sticker, second is name.
        Object[][] buttons = {
            {"images/cow.png",    "Cow",    AppColors.MINT,     (Runnable) () -> SoundManager.getInstance().playCow()},
            {"images/bird.png",   "Bird",   AppColors.SKY,      (Runnable) () -> SoundManager.getInstance().playBird()},
            {"images/cat.png",    "Cat",    AppColors.LAVENDER,  (Runnable) () -> SoundManager.getInstance().playCat()},
            {"images/dog.png",    "Dog",    AppColors.TEAL,      (Runnable) () -> SoundManager.getInstance().playDog()},
            {"images/tabla.png",  "Tabla",  AppColors.CORAL,     (Runnable) () -> SoundManager.getInstance().playTabla()},
            {"images/horn.png",   "Horn",   AppColors.PEACH,     (Runnable) () -> SoundManager.getInstance().playHorn()},
            {"images/bell.png",   "Bell",   AppColors.ROSE,      (Runnable) () -> SoundManager.getInstance().playBell()},
            {"images/cheers.png", "Cheers", AppColors.LEMON,     (Runnable) () -> SoundManager.getInstance().playCheers()},
        };

        for (Object[] btn : buttons) {
            grid.add(makeSoundBtn((String)btn[0], (String)btn[1], (Color)btn[2], (Runnable)btn[3]));
        }

        content.add(grid, BorderLayout.CENTER);

        JLabel hint = new JLabel("Tap any button to hear sounds!", SwingConstants.CENTER);
        hint.setFont(AppFonts.BODY);
        hint.setForeground(AppColors.TEXT_MED);
        content.add(hint, BorderLayout.SOUTH);

        addContent(content);
    }

    /**
     * Slightly darkens a color for hover/gradient effects.
     * Uses a moderate factor to avoid making colors too dark.
     */
    private Color darken(Color c) {
        int factor = 40;
        int r = Math.max(c.getRed() - factor, 0);
        int g = Math.max(c.getGreen() - factor, 0);
        int b = Math.max(c.getBlue() - factor, 0);
        return new Color(r, g, b);
    }

    private JPanel makeSoundBtn(String imagePath, String nameStr, Color color, Runnable action) {
        final JPanel[] panelRef = new JPanel[1];
        
        panelRef[0] = new JPanel(new GridBagLayout()) {
            boolean hov = false;
            { 
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { 
                        hov = true; 
                        repaint(); 
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
                    }
                    public void mouseExited(java.awt.event.MouseEvent e) { 
                        hov = false; 
                        repaint(); 
                    }
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        // Run sound on background thread to avoid blocking UI
                        ThreadPoolManager.execute(action);
                        animatePress(panelRef[0]);
                    }
                }); 
                setOpaque(false); 
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hov ? darken(color) : color;
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 5, getWidth() - 4, getHeight() - 4, 18, 18);
                GradientPaint gp = new GradientPaint(0, 0, c, 0, getHeight(), darken(c));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 18, 18);
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fillRoundRect(3, 3, getWidth() - 9, (getHeight() - 8) / 2, 12, 12);
                g2.dispose();
            }
        };

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // Load the image icon, scale it so it fits nicely
        ImageIcon icon = null;
        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image img = originalIcon.getImage();
            Image newimg = img.getScaledInstance(64, 64,  java.awt.Image.SCALE_SMOOTH); 
            icon = new ImageIcon(newimg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JLabel imageLabel = new JLabel(icon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel name = new JLabel(nameStr);
        name.setFont(AppFonts.BODY_BOLD);
        name.setForeground(AppColors.TEXT_DARK);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        inner.add(Box.createVerticalGlue());
        inner.add(imageLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(name);
        inner.add(Box.createVerticalGlue());
        panelRef[0].add(inner);
        return panelRef[0];
    }

    private void animatePress(JPanel p) {
        if (p == null) return;
        Timer t = new Timer(100, e -> p.repaint());
        t.setRepeats(false);
        t.start();
    }
}