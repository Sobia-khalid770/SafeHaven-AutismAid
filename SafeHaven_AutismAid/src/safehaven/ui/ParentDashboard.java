package safehaven.ui;

import safehaven.auth.UserDatabase;
import safehaven.models.UserAccount;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ParentDashboard extends BaseFrame {
    private final UserAccount parent;
    private JPanel childrenPanel;

    public ParentDashboard(UserAccount parent) {
        super("SafeHaven  Parent Dashboard", 860, 640);
        this.parent = parent;
        buildUI();
    }

    private void buildUI() {
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        LogoPainter logo = new LogoPainter(60, false);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(10));

        JLabel nameLabel = new JLabel(parent.getName());
        nameLabel.setFont(AppFonts.BODY_BOLD);
        nameLabel.setForeground(AppColors.TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(nameLabel);

        JLabel roleLabel = new JLabel("Parent Account");
        roleLabel.setFont(AppFonts.SMALL);
        roleLabel.setForeground(AppColors.TEXT_LIGHT);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(20));

        // Sidebar buttons
        String[][] sideItems = {
            {" My Children", "children"},
            {" Progress",    "progress"},
            {" Reviews",     "review"},
            {" Logout",      "logout"}
        };

        for (String[] item : sideItems) {
            RoundedButton b = new RoundedButton(item[0], AppColors.PEACH, AppColors.TEXT_DARK);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setMaximumSize(new Dimension(180, 38));
            b.addActionListener(e -> handleSidebar(item[1]));
            sidebar.add(b);
            sidebar.add(Box.createVerticalStrut(8));
        }

        mainPanel.add(sidebar, BorderLayout.WEST);

        // Main content  children cards
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        JLabel heading = new JLabel("   Children's Dashboard");
        heading.setFont(AppFonts.HEADING);
        heading.setForeground(new Color(160, 80, 30));
        heading.setBorder(new EmptyBorder(16, 10, 10, 10));
        mainContent.add(heading, BorderLayout.NORTH);

        childrenPanel = new JPanel();
        childrenPanel.setOpaque(false);
        childrenPanel.setLayout(new BoxLayout(childrenPanel, BoxLayout.Y_AXIS));
        refreshChildren();

        JScrollPane scroll = new JScrollPane(childrenPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        mainContent.add(scroll, BorderLayout.CENTER);

        // Add child button
        RoundedButton btnAdd = new RoundedButton(" Add Child Account", AppColors.TEAL_DEEP);
        btnAdd.addActionListener(e -> showAddChildDialog());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        bottom.setOpaque(false);
        bottom.add(btnAdd);
        mainContent.add(bottom, BorderLayout.SOUTH);

        mainPanel.add(mainContent, BorderLayout.CENTER);
    }

    private void refreshChildren() {
        childrenPanel.removeAll();
        List<UserAccount> children = UserDatabase.getInstance().getLinkedChildren(parent.getEmail());
        if (children.isEmpty()) {
            JLabel empty = new JLabel("No children linked yet. Click 'Add Child Account' to start.");
            empty.setFont(AppFonts.BODY);
            empty.setForeground(AppColors.TEXT_LIGHT);
            empty.setBorder(new EmptyBorder(30, 20, 10, 10));
            childrenPanel.add(empty);
        } else {
            for (UserAccount child : children) {
                childrenPanel.add(buildChildCard(child));
                childrenPanel.add(Box.createVerticalStrut(10));
            }
        }
        childrenPanel.revalidate();
        childrenPanel.repaint();
    }

    private JPanel buildChildCard(UserAccount child) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,210));
                g2.fillRoundRect(0,0,getWidth()-4,getHeight()-4,16,16);
                g2.setColor(new Color(220,190,160,100));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0,0,getWidth()-5,getHeight()-5,16,16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // Avatar
        JLabel avatar = new JLabel("");
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        card.add(avatar, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameL = new JLabel(child.getName());
        nameL.setFont(AppFonts.BODY_BOLD);
        nameL.setForeground(AppColors.TEXT_DARK);

        JLabel emailL = new JLabel(child.getEmail());
        emailL.setFont(AppFonts.SMALL);
        emailL.setForeground(AppColors.TEXT_LIGHT);

        String about = child.getAbout() != null ? child.getAbout() : "Learning and growing!";
        JLabel aboutL = new JLabel(about);
        aboutL.setFont(AppFonts.SMALL);
        aboutL.setForeground(AppColors.TEXT_MED);

        info.add(nameL);
        info.add(Box.createVerticalStrut(2));
        info.add(emailL);
        info.add(Box.createVerticalStrut(4));
        info.add(aboutL);
        info.add(Box.createVerticalStrut(6));

        // Progress bars
        JPanel progRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        progRow.setOpaque(false);
        String[] acts = {"SoundBoard","PixelColoring","Letters","Numbers","BubblePop","Shapes"};
        String[] icons = {"","","","","",""};
        for (int i = 0; i < acts.length; i++) {
            int score = child.getProgress().getOrDefault(acts[i], 0);
            JLabel ic = new JLabel(icons[i]);
            ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            ic.setToolTipText(acts[i] + ": " + score + "%");
            progRow.add(ic);
        }
        info.add(progRow);
        card.add(info, BorderLayout.CENTER);

        // View btn
        RoundedButton btnView = new RoundedButton(" View", AppColors.LAVENDER_DEEP);
        btnView.addActionListener(e -> showChildProgress(child));
        card.add(btnView, BorderLayout.EAST);

        return card;
    }

    private void showChildProgress(UserAccount child) {
        JDialog dlg = new JDialog(this, "Progress  " + child.getName(), true);
        dlg.setSize(400, 380);
        dlg.setLocationRelativeTo(this);
        JPanel p = new PolkaDotPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel(" " + child.getName() + "'s Progress");
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160,80,30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(16));

        String[] acts  = {"SoundBoard","PixelColoring","Letters","Numbers","BubblePop","Shapes"};
        String[] icons = {" Sound Board"," Pixel Coloring"," Letters"," Numbers"," Bubble Pop"," Shapes"};
        Color[]  cols  = {AppColors.LAVENDER_DEEP, AppColors.MINT_DEEP, AppColors.SKY_DEEP,
                          AppColors.PEACH_DEEP, AppColors.ROSE_DEEP, AppColors.TEAL_DEEP};

        for (int i = 0; i < acts.length; i++) {
            int score = child.getProgress().getOrDefault(acts[i], 0);
            JLabel lbl = new JLabel(icons[i] + "  " + score + "%");
            lbl.setFont(AppFonts.BODY_BOLD);
            lbl.setForeground(AppColors.TEXT_DARK);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(lbl);
            p.add(Box.createVerticalStrut(4));

            JPanel bar = buildBar(score, cols[i]);
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(bar);
            p.add(Box.createVerticalStrut(10));
        }

        dlg.add(p);
        dlg.setVisible(true);
    }

    private JPanel buildBar(int score, Color color) {
        return new JPanel() {
            { setOpaque(false); setPreferredSize(new Dimension(300, 16)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 16)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220,200,180));
                g2.fillRoundRect(0,2,getWidth(),12,8,8);
                int w = (int)(getWidth() * Math.min(score,100)/100.0);
                if(w>0){g2.setColor(color);g2.fillRoundRect(0,2,w,12,8,8);}
                g2.dispose();
            }
        };
    }

    private void showAddChildDialog() {
        JDialog dlg = new JDialog(this, "Add Child Account", true);
        dlg.setSize(380, 260);
        dlg.setLocationRelativeTo(this);

        JPanel p = new PolkaDotPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Link Child Account");
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160,80,30));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(14));

        JLabel info = new JLabel("<html><center>Child must already have a registered account.<br>Enter their email and password to verify.</center></html>");
        info.setFont(AppFonts.SMALL);
        info.setForeground(AppColors.TEXT_MED);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(info);
        p.add(Box.createVerticalStrut(12));

        StyledTextField tfChildEmail = new StyledTextField("Child's Email", 20);
        StyledPasswordField tfChildPass = new StyledPasswordField(20);
        tfChildEmail.setAlignmentX(Component.CENTER_ALIGNMENT);
        tfChildPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        tfChildEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tfChildPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        p.add(new JLabel("Child Email:") {{ setFont(AppFonts.LABEL); setForeground(AppColors.TEXT_MED); setAlignmentX(LEFT_ALIGNMENT); }});
        p.add(Box.createVerticalStrut(3));
        p.add(tfChildEmail);
        p.add(Box.createVerticalStrut(8));
        p.add(new JLabel("Child Password:") {{ setFont(AppFonts.LABEL); setForeground(AppColors.TEXT_MED); setAlignmentX(LEFT_ALIGNMENT); }});
        p.add(Box.createVerticalStrut(3));
        p.add(tfChildPass);
        p.add(Box.createVerticalStrut(12));

        RoundedButton btnLink = new RoundedButton(" Link Child", AppColors.TEAL_DEEP);
        btnLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(btnLink);

        btnLink.addActionListener(e -> {
            String result = UserDatabase.getInstance().linkChildToParent(
                parent.getEmail(),
                tfChildEmail.getText().trim(),
                new String(tfChildPass.getPassword()));
            if ("ok".equals(result)) {
                JOptionPane.showMessageDialog(dlg, "Child linked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                refreshChildren();
            } else {
                JOptionPane.showMessageDialog(dlg, "Error: " + result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(p);
        dlg.setVisible(true);
    }

    private void handleSidebar(String action) {
        switch (action) {
            case "children":
                refreshChildren();
                break;
            case "progress":
                showAllProgress();
                break;
            case "review":
                new ReviewFrame(parent).setVisible(true);
                break;
            case "logout":
                dispose();
                new LoginFrame().setVisible(true);
                break;
        }
    }

    private void showAllProgress() {
        List<UserAccount> children = UserDatabase.getInstance().getLinkedChildren(parent.getEmail());
        if (children.isEmpty()) { JOptionPane.showMessageDialog(this,"No children linked yet."); return; }
        for (UserAccount child : children) showChildProgress(child);
    }
}