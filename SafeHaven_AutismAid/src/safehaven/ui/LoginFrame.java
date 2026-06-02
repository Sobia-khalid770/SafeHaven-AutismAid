package safehaven.ui;

import safehaven.auth.UserDatabase;
import safehaven.models.UserAccount;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends BaseFrame {
    private StyledTextField tfEmail;
    private StyledPasswordField tfPass;
    private JLabel statusLabel;

    public LoginFrame() {
        super("SafeHaven  Login", 480, 520);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 70, 30, 70));

        LogoPainter logo = new LogoPainter(80, true);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(logo);
        content.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Welcome Back! ");
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(6));

        JLabel sub = new JLabel("Log in to continue to SafeHaven");
        sub.setFont(AppFonts.SMALL);
        sub.setForeground(AppColors.TEXT_LIGHT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(24));

        tfEmail = new StyledTextField("Email Address", 20);
        tfPass  = new StyledPasswordField(20);

        addField(content, "Email Address", tfEmail);
        addField(content, "Password",      tfPass);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppFonts.SMALL);
        statusLabel.setForeground(new Color(200, 50, 50));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(10));

        RoundedButton btnLogin = new RoundedButton(" Login", AppColors.LAVENDER_DEEP);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(260, 44));
        content.add(btnLogin);
        content.add(Box.createVerticalStrut(14));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel("Don't have an account?");
        lbl.setFont(AppFonts.SMALL);
        lbl.setForeground(AppColors.TEXT_MED);
        JButton signupLink = makeLinkBtn("Sign Up");
        row.add(lbl);
        row.add(signupLink);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(row);

        btnLogin.addActionListener(e -> doLogin());
        tfPass.addActionListener(e -> doLogin());
        signupLink.addActionListener(e -> { dispose(); new SignUpFrame().setVisible(true); });

        addContent(content);
    }

    private void doLogin() {
        String email = tfEmail.getText().trim();
        String pass  = new String(tfPass.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            setStatus("Please enter email and password.", true); return;
        }
        if (!email.contains("@")) {
            setStatus("Please enter a valid email address.", true); return;
        }
        if (!UserDatabase.getInstance().authenticate(email, pass)) {
            setStatus("Incorrect email or password.", true); return;
        }
        UserAccount acc = UserDatabase.getInstance().getAccount(email);
        if (acc == null) {
            setStatus("Account not found.", true); return;
        }
        dispose();
        switch (acc.getRole()) {
            case PARENT:
                new ParentDashboard(acc).setVisible(true);
                break;
            case CHILD:
                new ChildDashboard(acc).setVisible(true);
                break;
        }
    }

    private void addField(JPanel p, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXT_MED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        p.add(Box.createVerticalStrut(12));
    }

    private JButton makeLinkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppFonts.SMALL);
        b.setForeground(AppColors.ROSE_DEEP);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setForeground(error ? new Color(200, 50, 50) : new Color(30, 150, 80));
    }
}
