
package safehaven.ui;

import safehaven.auth.UserDatabase;
import safehaven.games.*;
import safehaven.models.UserAccount;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Map;

public class ChildDashboard extends BaseFrame {
    private final UserAccount child;

    public ChildDashboard(UserAccount child) {
        super("SafeHaven  " + child.getName(), 820, 620);
        this.child = child;

        mainPanel.setLayout(new BorderLayout(10, 10));

        // Top bar
        JPanel topBar = new PolkaDotPanel(new Color(255, 240, 220));
        topBar.setLayout(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 70));

        JLabel welcome = new JLabel("    Hello, " + child.getName() + "! What do you want to do today?");
        welcome.setFont(AppFonts.HEADING);
        welcome.setForeground(new Color(160, 80, 30));
        topBar.add(welcome, BorderLayout.CENTER);

        RoundedButton btnProfile = new RoundedButton(" My Profile", AppColors.MINT_DEEP);
        RoundedButton btnLogout  = new RoundedButton(" Logout",      AppColors.CORAL_DEEP);
        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        topBtns.setOpaque(false);
        topBtns.add(btnProfile);
        topBtns.add(btnLogout);
        topBar.add(topBtns, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        btnProfile.addActionListener(e -> new ChildProfileFrame(child).setVisible(true));
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        // Activity grid
        JPanel grid = new JPanel(new GridLayout(3, 3, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(20, 30, 20, 30));

        addActivityBtn(grid, "images/soundboard.png",    " Sound Board",        AppColors.LAVENDER,     e -> openGame(new SoundBoardGame(child)));
        addActivityBtn(grid, "images/pixelcoloring.png", " Pixel Coloring",     AppColors.MINT,         e -> openGame(new PixelColoringGame(child)));
        addActivityBtn(grid, "images/letters.png",       " Learn Letters",      AppColors.SKY,          e -> openGame(new LetterLearningGame(child)));
        addActivityBtn(grid, "images/numbers.png",       " Learn Numbers",      AppColors.LEMON,        e -> openGame(new NumberLearningGame(child)));
        addActivityBtn(grid, "images/bubblepop.png",     " Bubble Pop",         AppColors.PEACH,        e -> openGame(new BubblePopGame(child)));
        addActivityBtn(grid, "images/shapes.png",        " Shape Matching",     AppColors.ROSE,         e -> openGame(new ShapeMatchingGame(child)));

        mainPanel.add(grid, BorderLayout.CENTER);

        // Progress bar bottom
        JPanel progressBar = buildProgressPanel();
        mainPanel.add(progressBar, BorderLayout.SOUTH);
    }

    private void openGame(BaseFrame game) {
        game.setVisible(true);
        game.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Refresh progress panel
                BorderLayout layout = (BorderLayout) mainPanel.getLayout();
                Component oldProg = layout.getLayoutComponent(BorderLayout.SOUTH);
                if (oldProg != null) mainPanel.remove(oldProg);
                mainPanel.add(buildProgressPanel(), BorderLayout.SOUTH);
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }

    private void addActivityBtn(JPanel parent, String imagePath, String labelStr, Color color, ActionListener al) {
        JPanel card = new JPanel() {
            boolean hov = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hov = true;  repaint(); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
                public void mouseExited(MouseEvent e)  { hov = false; repaint(); setCursor(Cursor.getDefaultCursor()); }
                public void mouseClicked(MouseEvent e) { al.actionPerformed(new ActionEvent(this,0,"")); }
            }); setOpaque(false); }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = hov ? color.darker() : color;
                // Shadow
                g2.setColor(new Color(0,0,0,20));
                g2.fillRoundRect(4,6,getWidth()-4,getHeight()-4,20,20);
                // Body
                GradientPaint gp = new GradientPaint(0,0,c,0,getHeight(),c.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,getWidth()-4,getHeight()-4,20,20);
                // Shine
                g2.setColor(new Color(255,255,255,60));
                g2.fillRoundRect(4,4,getWidth()-12,(getHeight()-8)/2,14,14);
                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());

        ImageIcon icon = null;
        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image img = originalIcon.getImage();
            Image newimg = img.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH); 
            icon = new ImageIcon(newimg);
        } catch (Exception e) {}
        
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        JLabel emoji = new JLabel(icon);
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel name = new JLabel(labelStr.trim());
        name.setFont(AppFonts.BODY_BOLD);
        name.setForeground(AppColors.TEXT_DARK);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(emoji);
        inner.add(Box.createVerticalStrut(4));
        inner.add(name);
        card.add(inner);

        parent.add(card);
    }

    private JPanel buildProgressPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 20, 8, 20));

        JLabel lbl = new JLabel("Your Progress: ");
        lbl.setFont(AppFonts.LABEL);
        lbl.setForeground(AppColors.TEXT_MED);
        p.add(lbl);

        String[] acts = {"SoundBoard","PixelColoring","Letters","Numbers","BubblePop","Shapes"};
        Map<String,Integer> prog = child.getProgress();
        for (String a : acts) {
            int score = prog.getOrDefault(a, 0);
            JPanel bar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(220,200,180));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(AppColors.MINT_DEEP);
                    int w = (int)(getWidth() * Math.min(score,100) / 100.0);
                    if(w>0) g2.fillRoundRect(0,0,w,getHeight(),8,8);
                    g2.dispose();
                }
            };
            bar.setPreferredSize(new Dimension(60,12));
            bar.setToolTipText(a + ": " + score);
            p.add(bar);
        }
        return p;
    }
}