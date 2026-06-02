package safehaven.games;

import safehaven.models.UserAccount;
import safehaven.ui.*;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class NumberLearningGame extends BaseFrame {
    private final UserAccount user;
    private int currentNum = 1;
    private int score = 0;

    // {digit, English name, emojis, object name for TTS}
    // Object names match the emoji visuals so TTS reads e.g. "Three Stars"
    private static final String[][] NUMBERS = {
        {"1",  "One",       "images/heart.png",      "Heart"},
        {"2",  "Two",       "images/smile.png",      "Smiles"},
        {"3",  "Three",     "images/star.png",       "Stars"},
        {"4",  "Four",      "images/flower.png",     "Flowers"},
        {"5",  "Five",      "images/clover.png",     "Clovers"},
        {"6",  "Six",       "images/cloud.png",      "Clouds"},
        {"7",  "Seven",     "images/umbrella.png",   "Umbrellas"},
        {"8",  "Eight",     "images/note.png",       "Notes"},
        {"9",  "Nine",      "images/star.png",       "Stars"},
        {"10", "Ten",       "images/heart.png",      "Hearts"},
        {"11", "Eleven",    "images/flower.png",     "Flowers"},
        {"12", "Twelve",    "images/smile.png",      "Smiles"},
        {"13", "Thirteen",  "images/clover.png",     "Clovers"},
        {"14", "Fourteen",  "images/cloud.png",      "Clouds"},
        {"15", "Fifteen",   "images/umbrella.png",   "Umbrellas"},
        {"16", "Sixteen",   "images/note.png",       "Notes"},
        {"17", "Seventeen", "images/star.png",       "Stars"},
        {"18", "Eighteen",  "images/heart.png",      "Hearts"},
        {"19", "Nineteen",  "images/flower.png",     "Flowers"},
        {"20", "Twenty",    "images/smile.png",      "Smiles"},
    };

    private JLabel bigNum, engLabel, emojiArea, scoreLabel;
    private JPanel dotPanel;

    public NumberLearningGame(UserAccount user) {
        super("SafeHaven   Learn Numbers", 680, 540);
        this.user = user;

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Let's Count!", SwingConstants.CENTER);
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        content.add(title, BorderLayout.NORTH);

        // Main card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 245, 225),
                        0, getHeight(), new Color(255, 225, 195));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth()-2, getHeight()-2, 22, 22);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Left: number display
        JPanel leftSide = new JPanel();
        leftSide.setOpaque(false);
        leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.Y_AXIS));

        bigNum = new JLabel("1", SwingConstants.CENTER);
        bigNum.setFont(new Font("Comic Sans MS", Font.BOLD, 130));
        bigNum.setForeground(new Color(99, 102, 241));
        bigNum.setAlignmentX(Component.CENTER_ALIGNMENT);

        engLabel = new JLabel("One", SwingConstants.CENTER);
        engLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        engLabel.setForeground(AppColors.TEXT_MED);
        engLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(AppFonts.BODY_BOLD);
        scoreLabel.setForeground(new Color(200, 130, 30));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftSide.add(Box.createVerticalStrut(10));
        leftSide.add(bigNum);
        leftSide.add(Box.createVerticalStrut(4));
        leftSide.add(engLabel);
        leftSide.add(Box.createVerticalStrut(10));
        leftSide.add(scoreLabel);

        card.add(leftSide, BorderLayout.WEST);

        // Right: dot/emoji counting visual
        dotPanel = new JPanel();
        dotPanel.setOpaque(false);
        dotPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
        dotPanel.setPreferredSize(new Dimension(320, 200));
        card.add(dotPanel, BorderLayout.CENTER);

        content.add(card, BorderLayout.CENTER);

        // Number strip bottom
        JPanel strip = buildNumberStrip();
        content.add(strip, BorderLayout.SOUTH);

        // Navigation
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        nav.setOpaque(false);
        RoundedButton btnPrev  = new RoundedButton(" Previous", AppColors.LAVENDER_DEEP);
        RoundedButton btnSound = new RoundedButton(" Listen", AppColors.TEAL_DEEP);
        RoundedButton btnNext  = new RoundedButton("Next ", AppColors.MINT_DEEP);
        btnPrev.addActionListener(e -> navigate(-1));
        btnNext.addActionListener(e -> navigate(+1));
        btnSound.addActionListener(e -> playNumberAudio());
        nav.add(btnPrev);
        nav.add(btnSound);
        nav.add(btnNext);

        JPanel southArea = new JPanel(new BorderLayout());
        southArea.setOpaque(false);
        southArea.add(strip, BorderLayout.NORTH);
        southArea.add(nav, BorderLayout.SOUTH);
        content.add(southArea, BorderLayout.SOUTH);

        addContent(content);
        updateDisplay();
    }

    private JPanel buildNumberStrip() {
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 4));
        strip.setOpaque(false);
        Color[] cols = {AppColors.ROSE, AppColors.PEACH, AppColors.MINT,
                        AppColors.SKY, AppColors.LAVENDER, AppColors.LEMON,
                        AppColors.CORAL, AppColors.TEAL};
        for (int i = 1; i <= 20; i++) {
            final int n = i;
            final Color btnColor = cols[i % cols.length];
            JButton b = new JButton(String.valueOf(i)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color c = getModel().isRollover() ? btnColor.darker() : btnColor;
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    if (currentNum == n) {
                        g2.setColor(new Color(0, 0, 0, 60));
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                    }
                    g2.setFont(getFont());
                    g2.setColor(AppColors.TEXT_DARK);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                    int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), tx, ty);
                    g2.dispose();
                }
            };
            b.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setPreferredSize(new Dimension(38, 32));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> { currentNum = n; updateDisplay(); });
            strip.add(b);
        }
        return strip;
    }

    private void navigate(int dir) {
        currentNum = Math.max(1, Math.min(20, currentNum + dir));
        updateDisplay();
        score = Math.min(100, score + 5);
        user.updateProgress("Numbers", score);
        scoreLabel.setText(" Score: " + score);
        // Auto-play number sound when navigating
        playNumberAudio();
    }

    private void updateDisplay() {
        String[] data = NUMBERS[currentNum - 1];
        bigNum.setText(data[0]);
        engLabel.setText(data[1]);  // English only

        // Color cycle
        Color[] colors = {new Color(99,102,241), new Color(244,114,182), new Color(20,184,166),
                          new Color(249,115,22), new Color(234,179,8)};
        bigNum.setForeground(colors[currentNum % colors.length]);

        // Update dot panel with emojis
        dotPanel.removeAll();
        String imagePath = data[2];
        
        ImageIcon icon = null;
        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image img = originalIcon.getImage();
            Image newimg = img.getScaledInstance(currentNum <= 10 ? 44 : 32, currentNum <= 10 ? 44 : 32, java.awt.Image.SCALE_SMOOTH); 
            icon = new ImageIcon(newimg);
        } catch (Exception e) {}
        
        int rows = (int) Math.ceil(currentNum / 5.0);
        dotPanel.setLayout(new GridLayout(rows, 5, 4, 4));
        for (int i = 0; i < currentNum; i++) {
            JLabel el = new JLabel(icon);
            el.setHorizontalAlignment(SwingConstants.CENTER);
            dotPanel.add(el);
        }
        dotPanel.revalidate();
        dotPanel.repaint();

        SoundManager.getInstance().playClick();
    }

    private void playNumberAudio() {
        String objectName = NUMBERS[currentNum - 1][3];
        ThreadPoolManager.execute(() -> {
            TextToSpeech.speakNumber(currentNum, objectName);
        });
        
        score = Math.min(100, score + 10);
        user.updateProgress("Numbers", score);
        scoreLabel.setText(" Score: " + score);
    }

    private String numberToEnglish(int n) {
        String[] names = {"","One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten",
                          "Eleven","Twelve","Thirteen","Fourteen","Fifteen","Sixteen","Seventeen",
                          "Eighteen","Nineteen","Twenty"};
        return n <= 20 ? names[n] : String.valueOf(n);
    }
}