package safehaven.ui;

import safehaven.models.UserAccount;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ReviewFrame extends BaseFrame {
    private static final String REVIEWS_FILE = System.getProperty("user.home") + "/.safehaven_reviews.dat";
    private final UserAccount user;
    private JTextArea tfReview;
    private int rating = 5;
    private JPanel starsPanel;
    private JPanel reviewsListPanel;

    public ReviewFrame(UserAccount user) {
        super("SafeHaven  Reviews & Feedback", 560, 600);
        this.user = user;

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel title = new JLabel(" Reviews & Feedback");
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(16));

        // Write review section
        JPanel writeCard = createCard();
        writeCard.setLayout(new BoxLayout(writeCard, BoxLayout.Y_AXIS));
        writeCard.setBorder(new EmptyBorder(14, 16, 14, 16));
        writeCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel writeTitle = new JLabel("Write a Review");
        writeTitle.setFont(AppFonts.BODY_BOLD);
        writeTitle.setForeground(AppColors.TEXT_DARK);
        writeCard.add(writeTitle);
        writeCard.add(Box.createVerticalStrut(8));

        // Stars
        starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        starsPanel.setOpaque(false);
        buildStars();
        writeCard.add(starsPanel);
        writeCard.add(Box.createVerticalStrut(8));

        tfReview = new JTextArea(4, 30);
        tfReview.setFont(AppFonts.BODY);
        tfReview.setLineWrap(true);
        tfReview.setWrapStyleWord(true);
        tfReview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,190,160)),
            BorderFactory.createEmptyBorder(6,8,6,8)));
        JScrollPane rScroll = new JScrollPane(tfReview);
        rScroll.setBorder(null);
        rScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        writeCard.add(rScroll);
        writeCard.add(Box.createVerticalStrut(10));

        RoundedButton btnSubmit = new RoundedButton(" Submit Review", AppColors.ROSE_DEEP);
        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmit.addActionListener(e -> submitReview());
        writeCard.add(btnSubmit);

        content.add(writeCard);
        content.add(Box.createVerticalStrut(16));

        // Existing reviews
        JLabel existTitle = new JLabel("Community Reviews");
        existTitle.setFont(AppFonts.BODY_BOLD);
        existTitle.setForeground(AppColors.TEXT_MED);
        existTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(existTitle);
        content.add(Box.createVerticalStrut(8));

        reviewsListPanel = new JPanel();
        reviewsListPanel.setOpaque(false);
        reviewsListPanel.setLayout(new BoxLayout(reviewsListPanel, BoxLayout.Y_AXIS));
        loadReviews();
        content.add(reviewsListPanel);

        JScrollPane outer = new JScrollPane(content);
        outer.setOpaque(false);
        outer.getViewport().setOpaque(false);
        outer.setBorder(null);
        addContent(outer);
    }

    private void buildStars() {
        starsPanel.removeAll();
        JLabel lbl = new JLabel("Rating: ");
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXT_MED);
        starsPanel.add(lbl);
        for (int i = 1; i <= 5; i++) {
            final int r = i;
            JButton star = new JButton(i <= rating ? "" : "");
            star.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            star.setContentAreaFilled(false);
            star.setBorderPainted(false);
            star.setFocusPainted(false);
            star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            star.addActionListener(e -> { rating = r; buildStars(); starsPanel.revalidate(); starsPanel.repaint(); });
            starsPanel.add(star);
        }
    }

    private void submitReview() {
        String text = tfReview.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write something before submitting!", "Empty Review", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String entry = user.getName() + "|" + rating + "|" + text + "|" + new Date();
        try (PrintWriter pw = new PrintWriter(new FileWriter(REVIEWS_FILE, true))) {
            pw.println(entry);
        } catch (Exception ignored) {}
        tfReview.setText("");
        rating = 5;
        buildStars();
        loadReviews();
        JOptionPane.showMessageDialog(this, " Thank you for your review!", "Submitted!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadReviews() {
        reviewsListPanel.removeAll();
        File f = new File(REVIEWS_FILE);
        if (!f.exists()) { reviewsListPanel.revalidate(); return; }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            List<String[]> reviews = new ArrayList<>();
            List<String> rawLines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length >= 3) {
                    reviews.add(parts);
                    rawLines.add(line);
                }
            }
            // Show last 6
            int start = Math.max(0, reviews.size() - 6);
            for (int i = reviews.size()-1; i >= start; i--) {
                reviewsListPanel.add(buildReviewCard(reviews.get(i), rawLines.get(i)));
                reviewsListPanel.add(Box.createVerticalStrut(8));
            }
        } catch (Exception ignored) {}
        reviewsListPanel.revalidate();
        reviewsListPanel.repaint();
    }

    private JPanel buildReviewCard(String[] parts, String rawLine) {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        String name   = parts[0];
        int    stars  = Integer.parseInt(parts[1].trim());
        String text   = parts[2];
        String date   = parts.length > 3 ? parts[3] : "";

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel nameL = new JLabel(name);
        nameL.setFont(AppFonts.BODY_BOLD);
        nameL.setForeground(AppColors.TEXT_DARK);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) sb.append("\u2B50");
        for (int i = 0; i < 5 - stars; i++) sb.append("\u2606");
        String starStr = sb.toString();
        JLabel starL = new JLabel(starStr);
        starL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        topRow.add(nameL, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(starL);
        
        if (name.equals(user.getName())) {
            JButton btnDel = new JButton("X");
            btnDel.setFont(AppFonts.SMALL);
            btnDel.setForeground(Color.WHITE);
            btnDel.setBackground(AppColors.CORAL_DEEP);
            btnDel.setFocusPainted(false);
            btnDel.setBorder(new EmptyBorder(2, 6, 2, 6));
            btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnDel.addActionListener(e -> deleteReview(rawLine));
            rightPanel.add(btnDel);
        }
        
        topRow.add(rightPanel, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createVerticalStrut(4));

        JLabel textL = new JLabel("<html>" + text + "</html>");
        textL.setFont(AppFonts.BODY);
        textL.setForeground(AppColors.TEXT_MED);
        card.add(textL);

        if (!date.isEmpty()) {
            JLabel dateL = new JLabel(date);
            dateL.setFont(AppFonts.SMALL);
            dateL.setForeground(AppColors.TEXT_LIGHT);
            card.add(dateL);
        }
        return card;
    }

    private void deleteReview(String targetLine) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this review?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        File f = new File(REVIEWS_FILE);
        if (!f.exists()) return;
        List<String> allLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals(targetLine)) {
                    allLines.add(line);
                }
            }
        } catch (Exception e) { return; }

        try (PrintWriter pw = new PrintWriter(new FileWriter(f, false))) {
            for (String l : allLines) pw.println(l);
        } catch (Exception e) { return; }

        loadReviews();
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,200));
                g2.fillRoundRect(0,0,getWidth()-2,getHeight()-2,14,14);
                g2.setColor(new Color(220,190,160,80));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-3,getHeight()-3,14,14);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }
}
