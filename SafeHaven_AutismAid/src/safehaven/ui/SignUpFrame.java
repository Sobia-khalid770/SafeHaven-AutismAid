package safehaven.ui;

import safehaven.auth.*;
import safehaven.models.UserAccount;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SignUpFrame extends BaseFrame {
    private StyledTextField tfName, tfEmail;
    private StyledPasswordField tfPass, tfConfirm;
    private JComboBox<String> roleCombo;
    private JLabel statusLabel;

    public SignUpFrame() {
        super("SafeHaven  Create Account", 520, 640);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 60, 20, 60));

        // Logo
        LogoPainter logo = new LogoPainter(70, true);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(logo);
        content.add(Box.createVerticalStrut(10));

        JLabel title = new JLabel("Create Your Account");
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(20));

        // Form
        tfName    = new StyledTextField("Full Name", 20);
        tfEmail   = new StyledTextField("Email Address", 20);
        tfPass    = new StyledPasswordField(20);
        tfConfirm = new StyledPasswordField(20);
        roleCombo = new JComboBox<>(new String[]{" Parent", " Child"});  // Removed Therapist
        styleCombo(roleCombo);

        addField(content, "Full Name",       tfName);
        addField(content, "Email Address",   tfEmail);
        addField(content, "Password",        tfPass);
        addField(content, "Confirm Password",tfConfirm);
        addField(content, "Account Type",    roleCombo);

        content.add(Box.createVerticalStrut(10));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(AppFonts.SMALL);
        statusLabel.setForeground(new Color(200, 50, 50));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(8));

        // Send OTP button
        RoundedButton btnOTP = new RoundedButton(" Send Verification Code", AppColors.TEAL_DEEP);
        btnOTP.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOTP.setMaximumSize(new Dimension(300, 44));
        content.add(btnOTP);
        content.add(Box.createVerticalStrut(10));

        // Sign Up button
        RoundedButton btnSignUp = new RoundedButton(" Create Account", AppColors.ROSE_DEEP);
        btnSignUp.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignUp.setMaximumSize(new Dimension(300, 44));
        content.add(btnSignUp);
        content.add(Box.createVerticalStrut(12));

        // Already have account
        JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loginRow.setOpaque(false);
        JLabel lbl = new JLabel("Already have an account?");
        lbl.setFont(AppFonts.SMALL);
        lbl.setForeground(AppColors.TEXT_MED);
        JButton loginLink = makeLinkBtn("Login");
        loginRow.add(lbl);
        loginRow.add(loginLink);
        loginRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(loginRow);

        // Actions
        btnOTP.addActionListener(e -> sendOTP());
        btnSignUp.addActionListener(e -> doSignUp());
        loginLink.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        addContent(scroll);
    }

    private void sendOTP() {
        String email = tfEmail.getText().trim();
        if (!isValidEmail(email)) {
            setStatus("Please enter a valid email address.", true);
            return;
        }
        if (UserDatabase.getInstance().emailExists(email)) {
            setStatus("This email is already registered. Please login.", true);
            return;
        }
        setStatus("Sending code to " + email + "...", false);
        // Use thread pool instead of raw Thread
        ThreadPoolManager.execute(() -> {
            String result = EmailService.getInstance().generateAndSend(email);
            String code   = EmailService.getInstance().getPendingCode(email);
            SwingUtilities.invokeLater(() -> {
                if ("console".equals(result)) {
                    // SMTP not configured  show code in dialog for testing
                    JOptionPane.showMessageDialog(this,
                        " (SMTP not configured)\nYour test code is: " + code,
                        "Verification Code", JOptionPane.INFORMATION_MESSAGE);
                } else if ("error".equals(result)) {
                    setStatus(" Failed to send code. Please try again.", true);
                } else {
                    setStatus(" Code sent to your email! Check your inbox.", false);
                }
            });
        });
    }

    private void doSignUp() {
        String name    = tfName.getText().trim();
        String email   = tfEmail.getText().trim();
        String pass    = new String(tfPass.getPassword());
        String confirm = new String(tfConfirm.getPassword());
        int roleIdx    = roleCombo.getSelectedIndex();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            setStatus("Please fill all fields.", true); return;
        }
        if (name.length() < 2 || name.length() > 100) {
            setStatus("Name must be 2-100 characters.", true); return;
        }
        if (!isValidEmail(email)) {
            setStatus("Please enter a valid email address.", true); return;
        }
        if (!pass.equals(confirm)) {
            setStatus("Passwords do not match.", true); return;
        }
        if (pass.length() < 6) {
            setStatus("Password must be at least 6 characters.", true); return;
        }
        if (pass.length() > 128) {
            setStatus("Password is too long (max 128 characters).", true); return;
        }
        if (UserDatabase.getInstance().emailExists(email)) {
            setStatus("Email already registered.", true); return;
        }

        // Ask for OTP
        String inputCode = JOptionPane.showInputDialog(this,
            "Enter the 4-digit code sent to your email:",
            "Verify Email", JOptionPane.PLAIN_MESSAGE);
        if (inputCode == null || inputCode.trim().isEmpty()) { return; }

        if (!EmailService.getInstance().verify(email, inputCode.trim())) {
            setStatus("Invalid or expired code. Please try again.", true); return;
        }

        UserAccount.Role role;
        switch (roleIdx) {
            case 1:
                role = UserAccount.Role.CHILD;
                break;
            default:
                role = UserAccount.Role.PARENT;
                break;
        }

        UserDatabase.getInstance().register(email, pass, name, role);
        JOptionPane.showMessageDialog(this, " Account created! Welcome to SafeHaven, " + name + "!",
            "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new LoginFrame().setVisible(true);
    }

    private boolean isValidEmail(String email) {
        // Simple email validation: must contain @ and domain
        if (email == null || email.isEmpty()) return false;
        if (!email.contains("@")) return false;
        String[] parts = email.split("@");
        if (parts.length != 2) return false;
        if (parts[0].isEmpty() || parts[1].isEmpty()) return false;
        if (!parts[1].contains(".")) return false;
        return email.length() <= 254; // RFC 5321
    }

    private void addField(JPanel parent, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXT_MED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        parent.add(lbl);
        parent.add(Box.createVerticalStrut(3));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(AppFonts.BODY);
        cb.setBackground(Color.WHITE);
        cb.setForeground(AppColors.TEXT_DARK);
    }

    private JButton makeLinkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(AppFonts.SMALL);
        b.setForeground(AppColors.LAVENDER_DEEP);
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
