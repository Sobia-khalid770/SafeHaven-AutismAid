package safehaven.games;

import safehaven.models.UserAccount;
import safehaven.ui.*;
import safehaven.utils.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PixelColoringGame extends BaseFrame {
    private final UserAccount user;
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 22;
    private final Color[][] grid = new Color[GRID_SIZE][GRID_SIZE];
    private Color selectedColor = new Color(244, 114, 182);
    private JLabel selectedColorDisplay;

    private static final Color[] PALETTE = {
        new Color(244,114,182), new Color(239,68,68),   new Color(249,115,22),
        new Color(234,179,8),   new Color(132,204,22),  new Color(52,211,153),
        new Color(56,189,248),  new Color(99,102,241),  new Color(168,85,247),
        new Color(255,255,255), new Color(180,180,180), new Color(30,30,30),
        new Color(253,186,116), new Color(167,243,208), new Color(196,181,253),
    };

    public PixelColoringGame(UserAccount user) {
        super("SafeHaven   Pixel Coloring", 700, 580);
        this.user = user;

        // Init grid white
        for (int r = 0; r < GRID_SIZE; r++)
            for (int c = 0; c < GRID_SIZE; c++)
                grid[r][c] = Color.WHITE;

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel(" Pixel Coloring  Paint your masterpiece!", SwingConstants.CENTER);
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        content.add(title, BorderLayout.NORTH);

        // Canvas
        JPanel canvas = buildCanvas();
        content.add(canvas, BorderLayout.CENTER);

        // Right panel - palette + buttons
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel palTitle = new JLabel(" Colors");
        palTitle.setFont(AppFonts.LABEL);
        palTitle.setForeground(AppColors.TEXT_MED);
        palTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(palTitle);
        rightPanel.add(Box.createVerticalStrut(6));

        // Palette grid
        JPanel palGrid = new JPanel(new GridLayout(5, 3, 4, 4));
        palGrid.setOpaque(false);
        for (Color c : PALETTE) {
            JPanel swatch = new JPanel() {
                { setOpaque(false); setPreferredSize(new Dimension(32, 32));
                  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  addMouseListener(new MouseAdapter() {
                      public void mouseClicked(MouseEvent e) {
                          selectedColor = c;
                          selectedColorDisplay.setBackground(c);
                          SoundManager.getInstance().playClick();
                          // Repaint all swatches to update selection indicator
                          palGrid.repaint();
                      }
                  });
                }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    if (c.equals(selectedColor)) {
                        g2.setColor(Color.BLACK);
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 6, 6);
                    }
                    g2.dispose();
                }
            };
            swatch.setBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1));
            palGrid.add(swatch);
        }
        palGrid.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(palGrid);
        rightPanel.add(Box.createVerticalStrut(10));

        // Selected color display
        JLabel selLbl = new JLabel("Selected:");
        selLbl.setFont(AppFonts.SMALL);
        selLbl.setForeground(AppColors.TEXT_MED);
        selLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(selLbl);
        rightPanel.add(Box.createVerticalStrut(3));
        selectedColorDisplay = new JLabel("   ");
        selectedColorDisplay.setOpaque(true);
        selectedColorDisplay.setBackground(selectedColor);
        selectedColorDisplay.setPreferredSize(new Dimension(60, 28));
        selectedColorDisplay.setMaximumSize(new Dimension(80, 28));
        selectedColorDisplay.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        selectedColorDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(selectedColorDisplay);
        rightPanel.add(Box.createVerticalStrut(14));

        // Custom color picker
        RoundedButton btnCustom = new RoundedButton("+ Custom", AppColors.LAVENDER_DEEP);
        btnCustom.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCustom.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Pick Color", selectedColor);
            if (c != null) { selectedColor = c; selectedColorDisplay.setBackground(c); }
        });
        rightPanel.add(btnCustom);
        rightPanel.add(Box.createVerticalStrut(8));

        // Clear
        RoundedButton btnClear = new RoundedButton(" Clear", AppColors.CORAL_DEEP);
        btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClear.addActionListener(e -> {
            for (int r=0;r<GRID_SIZE;r++) for(int c2=0;c2<GRID_SIZE;c2++) grid[r][c2]=Color.WHITE;
            canvas.repaint();
        });
        rightPanel.add(btnClear);
        rightPanel.add(Box.createVerticalStrut(8));

        // Save as PNG
        RoundedButton btnSave = new RoundedButton(" Save PNG", AppColors.TEAL_DEEP);
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSave.addActionListener(e -> savePNG());
        rightPanel.add(btnSave);

        content.add(rightPanel, BorderLayout.EAST);

        JLabel hint = new JLabel("Click or drag to color the grid! ", SwingConstants.CENTER);
        hint.setFont(AppFonts.SMALL);
        hint.setForeground(AppColors.TEXT_LIGHT);
        content.add(hint, BorderLayout.SOUTH);

        addContent(content);
    }

    private JPanel buildCanvas() {
        JPanel canvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                int offsetX = (getWidth()  - GRID_SIZE*CELL_SIZE)/2;
                int offsetY = (getHeight() - GRID_SIZE*CELL_SIZE)/2;
                for (int r=0;r<GRID_SIZE;r++) {
                    for (int c=0;c<GRID_SIZE;c++) {
                        g2.setColor(grid[r][c]);
                        g2.fillRect(offsetX+c*CELL_SIZE, offsetY+r*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                        g2.setColor(new Color(200,200,200,80));
                        g2.drawRect(offsetX+c*CELL_SIZE, offsetY+r*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
                g2.dispose();
            }
        };
        canvas.setOpaque(false);
        canvas.setBackground(new Color(250, 245, 235));

        MouseAdapter ma = new MouseAdapter() {
            void paint(MouseEvent e) {
                int offsetX = (canvas.getWidth()  - GRID_SIZE*CELL_SIZE)/2;
                int offsetY = (canvas.getHeight() - GRID_SIZE*CELL_SIZE)/2;
                int c = (e.getX()-offsetX)/CELL_SIZE;
                int r = (e.getY()-offsetY)/CELL_SIZE;
                if (r>=0&&r<GRID_SIZE&&c>=0&&c<GRID_SIZE) {
                    grid[r][c] = selectedColor;
                    canvas.repaint();
                }
            }
            @Override public void mouseClicked(MouseEvent e) { paint(e); }
            @Override public void mouseDragged(MouseEvent e) { paint(e); }
        };
        canvas.addMouseListener(ma);
        canvas.addMouseMotionListener(ma);
        canvas.setPreferredSize(new Dimension(GRID_SIZE*CELL_SIZE+4, GRID_SIZE*CELL_SIZE+4));
        return canvas;
    }

    private void savePNG() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("my_artwork.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".png")) f = new File(f.getAbsolutePath()+".png");
            try {
                int scale = 8;
                BufferedImage img = new BufferedImage(GRID_SIZE*scale, GRID_SIZE*scale, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                for (int r=0;r<GRID_SIZE;r++)
                    for (int c=0;c<GRID_SIZE;c++) {
                        g.setColor(grid[r][c]);
                        g.fillRect(c*scale, r*scale, scale, scale);
                    }
                g.dispose();
                ImageIO.write(img, "png", f);
                JOptionPane.showMessageDialog(this,"Saved to: "+f.getAbsolutePath(),"Saved!",JOptionPane.INFORMATION_MESSAGE);
                SoundManager.getInstance().playCheer();
                user.updateProgress("PixelColoring", 100);
                Logger.info("Artwork saved to: " + f.getAbsolutePath());
            } catch (Exception ex) {
                Logger.error("Failed to save artwork", ex);
                JOptionPane.showMessageDialog(this,"Could not save: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
