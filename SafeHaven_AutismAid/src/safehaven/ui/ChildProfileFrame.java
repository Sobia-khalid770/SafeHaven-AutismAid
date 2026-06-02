package safehaven.ui;

import safehaven.auth.UserDatabase;
import safehaven.models.UserAccount;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ChildProfileFrame extends BaseFrame {
    private final UserAccount child;
    private JLabel picLabel;
    private JTextField tfName, tfAbout;

    public ChildProfileFrame(UserAccount child) {
        super("My Profile  " + child.getName(), 440, 540);
        this.child = child;

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Avatar
        picLabel = new JLabel();
        picLabel.setHorizontalAlignment(SwingConstants.CENTER);
        picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        picLabel.setPreferredSize(new Dimension(100, 100));
        updateAvatar();
        content.add(picLabel);
        content.add(Box.createVerticalStrut(8));

        RoundedButton btnPic = new RoundedButton(" Change Photo", AppColors.SKY_DEEP);
        btnPic.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(btnPic);
        content.add(Box.createVerticalStrut(16));

        // Stars decoration
        JLabel stars = new JLabel("    ");
        stars.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        stars.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(stars);
        content.add(Box.createVerticalStrut(12));

        // Name
        JLabel nlbl = new JLabel("Name");
        nlbl.setFont(AppFonts.LABEL); nlbl.setForeground(AppColors.TEXT_MED);
        nlbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfName = new JTextField(child.getName(), 20);
        styleField(tfName);
        content.add(nlbl);
        content.add(Box.createVerticalStrut(3));
        content.add(tfName);
        content.add(Box.createVerticalStrut(10));

        // About
        JLabel albl = new JLabel("About Me");
        albl.setFont(AppFonts.LABEL); albl.setForeground(AppColors.TEXT_MED);
        albl.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfAbout = new JTextField(child.getAbout() != null ? child.getAbout() : "I love colors and games!", 20);
        styleField(tfAbout);
        content.add(albl);
        content.add(Box.createVerticalStrut(3));
        content.add(tfAbout);
        content.add(Box.createVerticalStrut(16));

        // Badges panel
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        badges.setOpaque(false);
        String[] bgs = {"","","","","",""};
        for (String b : bgs) {
            JLabel bl = new JLabel(b);
            bl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            bl.setToolTipText("Activity badge");
            badges.add(bl);
        }
        badges.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(badges);
        content.add(Box.createVerticalStrut(16));

        RoundedButton btnSave = new RoundedButton(" Save Profile", AppColors.MINT_DEEP);
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(btnSave);

        btnPic.addActionListener(e -> choosePic());
        btnSave.addActionListener(e -> saveProfile());

        addContent(content);
    }

    private void choosePic() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images","png","jpg","jpeg","gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            child.setProfilePic(fc.getSelectedFile().getAbsolutePath());
            UserDatabase.getInstance().save();
            updateAvatar();
        }
    }

    private void updateAvatar() {
        String path = child.getProfilePic();
        if (path != null && new File(path).exists()) {
            try {
                BufferedImage img = ImageIO.read(new File(path));
                Image scaled = img.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                picLabel.setIcon(new ImageIcon(scaled));
                picLabel.setText("");
            } catch (Exception ignored) {}
        } else {
            picLabel.setIcon(null);
            picLabel.setText("");
            picLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        }
    }

    private void saveProfile() {
        child.setName(tfName.getText().trim());
        child.setAbout(tfAbout.getText().trim());
        UserDatabase.getInstance().save();
        JOptionPane.showMessageDialog(this, "Profile saved! ", "Saved", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void styleField(JTextField f) {
        f.setFont(AppFonts.BODY);
        f.setBackground(new Color(255,255,255,220));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,190,160)),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }
}