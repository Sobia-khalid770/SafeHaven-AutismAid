package safehaven.ui;

import safehaven.utils.AppColors;
import safehaven.utils.AppFonts;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ManualFrame extends BaseFrame {

    public ManualFrame() {
        super("SafeHaven  Welcome Guide", 680, 620);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Logo
        LogoPainter logo = new LogoPainter(90, true);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(logo);
        content.add(Box.createVerticalStrut(16));

        // Title
        JLabel title = new JLabel(" Welcome to SafeHaven  User Guide");
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(14));

        // Rules card
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        String[] rules = {
            "  Account Types",
            "    Parent    One account per email. Manages children accounts.",
            "    Child     Own email OR same email as parent (only ONE child per parent email).",
            "",
            "  Sign Up Rules",
            "    A 4-digit code is sent to your real email for verification.",
            "    Enter the code to complete registration (valid 10 minutes).",
            "    Each email can hold only ONE account.",
            "",
            "  Linking Accounts (Parent Dashboard)",
            "    To add a child: the child must already have a registered account.",
            "    Parent provides the child's email + password to confirm ownership.",
            "    To add another child: a new (already registered) child email + password is needed.",
            "",
            "  Security",
            "    Passwords are securely stored (SHA-256 hashed).",
            "    Do not share your OTP code with anyone.",
            "",
            "  Contact & Support",
            "    Email: sobiakhalid0603@gmail.com",
        };

        for (String r : rules) {
            JLabel l = new JLabel(r.isEmpty() ? " " : r);
            l.setFont(r.startsWith("") || r.startsWith("") || r.startsWith("") || r.startsWith("") || r.startsWith("")
                    ? AppFonts.BODY_BOLD : AppFonts.BODY);
            l.setForeground(AppColors.TEXT_DARK);
            card.add(l);
        }

        JScrollPane scroll = new JScrollPane(card);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(560, 320));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(scroll);
        content.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);

        RoundedButton btnSignup = new RoundedButton(" Sign Up", AppColors.ROSE_DEEP);
        RoundedButton btnLogin  = new RoundedButton(" Login",  AppColors.LAVENDER_DEEP);

        btnSignup.setPreferredSize(new Dimension(160, 44));
        btnLogin.setPreferredSize(new Dimension(160, 44));

        btnSignup.addActionListener(e -> { dispose(); new SignUpFrame().setVisible(true); });
        btnLogin.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        btnRow.add(btnSignup);
        btnRow.add(btnLogin);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(btnRow);

        JScrollPane outerScroll = new JScrollPane(content);
        outerScroll.setOpaque(false);
        outerScroll.getViewport().setOpaque(false);
        outerScroll.setBorder(null);

        addContent(outerScroll);
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(220, 190, 160, 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }
}
