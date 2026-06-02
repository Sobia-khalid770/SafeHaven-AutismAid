package safehaven.games;

import safehaven.models.UserAccount;
import safehaven.ui.*;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LetterLearningGame extends BaseFrame {
    private final UserAccount user;
    private int currentIdx = 0;

    private static final String[][] LETTERS = {
        {"A", "images/A.png", "Apple"},
        {"B", "images/B.png", "Balloon"},
        {"C", "images/C.png", "Car"},
        {"D", "images/D.png", "Dog"},
        {"E", "images/E.png", "Egg"},
        {"F", "images/F.png", "Flower"},
        {"G", "images/G.png", "Grapes"},
        {"H", "images/H.png", "House"},
        {"I", "images/I.png", "Ice Cream"},
        {"J", "images/J.png", "Jar"},
        {"K", "images/K.png", "Key"},
        {"L", "images/L.png", "Lion"},
        {"M", "images/M.png", "Moon"},
        {"N", "images/N.png", "Nest"},
        {"O", "images/O.png", "Orange"},
        {"P", "images/P.png", "Pencil"},
        {"Q", "images/Q.png", "Queen"},
        {"R", "images/R.png", "Rainbow"},
        {"S", "images/S.png", "Sun"},
        {"T", "images/T.png", "Tree"},
        {"U", "images/U.png", "Umbrella"},
        {"V", "images/V.png", "Vegetable"},
        {"W", "images/W.png", "Water"},
        {"X", "images/X.png", "X Mark"},
        {"Y", "images/Y.png", "Yarn"},
        {"Z", "images/Z.png", "Zebra"},
    };

    private JLabel bigLetter, emojiLabel, wordLabel, scoreLabel;
    private int score = 0;

    public LetterLearningGame(UserAccount user) {
        super("SafeHaven   Learn Letters", 620, 520);
        this.user = user;

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Let's Learn A B C!", SwingConstants.CENTER);
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        content.add(title, BorderLayout.NORTH);

        // Center card
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 240, 220), 0, getHeight(), new Color(255, 220, 190));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Big letter display
        bigLetter = new JLabel("A", SwingConstants.CENTER);
        bigLetter.setFont(new Font("Comic Sans MS", Font.BOLD, 130));
        bigLetter.setForeground(new Color(244, 114, 182));
        bigLetter.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Emoji display (now Image display)
        emojiLabel = new JLabel();
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ...existing code...
        // Word label (English only)
        wordLabel = new JLabel("Apple", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        wordLabel.setForeground(AppColors.TEXT_DARK);
        wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel = new JLabel(" Score: 0", SwingConstants.CENTER);
        scoreLabel.setFont(AppFonts.BODY_BOLD);
        scoreLabel.setForeground(new Color(200, 130, 30));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(bigLetter);
        card.add(Box.createVerticalStrut(8));
        card.add(emojiLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(wordLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(scoreLabel);

        content.add(card, BorderLayout.CENTER);

        // Bottom controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        controls.setOpaque(false);

        RoundedButton btnPrev = new RoundedButton(" Previous", AppColors.LAVENDER_DEEP);
        RoundedButton btnSound = new RoundedButton(" Listen", AppColors.TEAL_DEEP);
        RoundedButton btnNext = new RoundedButton("Next ", AppColors.MINT_DEEP);

        btnPrev.addActionListener(e -> navigate(-1));
        btnNext.addActionListener(e -> navigate(+1));
        btnSound.addActionListener(e -> playLetterSound());

        controls.add(btnPrev);
        controls.add(btnSound);
        controls.add(btnNext);
        content.add(controls, BorderLayout.SOUTH);

        // Letter strip on left
        JPanel strip = buildLetterStrip();
        content.add(strip, BorderLayout.WEST);

        addContent(content);
        updateDisplay();
    }

    private JPanel buildLetterStrip() {
        JPanel strip = new JPanel();
        strip.setOpaque(false);
        strip.setLayout(new BoxLayout(strip, BoxLayout.Y_AXIS));
        strip.setPreferredSize(new Dimension(50, 0));

        JScrollPane sc = new JScrollPane(strip);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        sc.setBorder(null);
        sc.setPreferredSize(new Dimension(50, 400));

        Color[] cols = {AppColors.ROSE, AppColors.PEACH, AppColors.MINT, AppColors.SKY, AppColors.LAVENDER};

        for (int i = 0; i < LETTERS.length; i++) {
            final int idx = i;
            final Color btnColor = cols[i % cols.length];
            JButton b = new JButton(LETTERS[i][0]) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color c = getModel().isRollover() ? btnColor.darker() : btnColor;
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    if (currentIdx == idx) {
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
            b.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setPreferredSize(new Dimension(44, 34));
            b.setMaximumSize(new Dimension(44, 34));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> {
                currentIdx = idx;
                updateDisplay();
            });
            strip.add(b);
            strip.add(Box.createVerticalStrut(2));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sc, BorderLayout.CENTER);
        return wrapper;
    }

    private void navigate(int dir) {
        currentIdx = (currentIdx + dir + LETTERS.length) % LETTERS.length;
        updateDisplay();
        score = Math.min(100, score + 5);
        user.updateProgress("Letters", score);
        scoreLabel.setText(" Score: " + score);
        // Auto-play letter sound when navigating
        playLetterSound();
    }

    private void updateDisplay() {
        String[] data = LETTERS[currentIdx];

        bigLetter.setText(data[0]);
        
        // Load image icon
        try {
            ImageIcon originalIcon = new ImageIcon(data[1]);
            Image img = originalIcon.getImage();
            Image newimg = img.getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH); 
            emojiLabel.setIcon(new ImageIcon(newimg));
            emojiLabel.setText("");
        } catch (Exception e) {
            emojiLabel.setIcon(null);
            emojiLabel.setText(data[1]);
        }
        
        wordLabel.setText(data[2]);  // English word only

        // Color cycle
        Color[] colors = {
            new Color(244, 114, 182), // Pink
            new Color(99, 102, 241),  // Blue
            new Color(20, 184, 166), // Teal
            new Color(249, 115, 22), // Orange
            new Color(234, 179, 8)   // Yellow
        };
        bigLetter.setForeground(colors[currentIdx % colors.length]);

        SoundManager.getInstance().playClick();
    }

    private void playLetterSound() {
        ThreadPoolManager.execute(() -> {
            String letter = LETTERS[currentIdx][0];
            TextToSpeech.speakLetter(letter);
        });

        score = Math.min(100, score + 10);
        user.updateProgress("Letters", score);
        scoreLabel.setText(" Score: " + score);
    }
}