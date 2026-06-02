package safehaven.ui;

import safehaven.utils.AppColors;
import javax.swing.*;
import java.awt.*;

public class BaseFrame extends JFrame {
    protected PolkaDotPanel mainPanel;
    protected CustomTitleBar titleBar;

    public BaseFrame(String title, int w, int h) {
        setUndecorated(true);
        setSize(w, h);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 175, 150), 1));

        setLayout(new BorderLayout());
        titleBar = new CustomTitleBar(this, title);
        add(titleBar, BorderLayout.NORTH);

        mainPanel = new PolkaDotPanel(AppColors.BG_MAIN);
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    protected void addContent(JComponent comp) {
        mainPanel.add(comp, BorderLayout.CENTER);
    }

    // Rounded corner support
    @Override public void update(Graphics g) { paint(g); }
}
