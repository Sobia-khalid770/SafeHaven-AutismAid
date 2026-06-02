package safehaven.games;

import safehaven.models.UserAccount;
import safehaven.ui.*;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class BubblePopGame extends BaseFrame {
    private final UserAccount user;
    private final List<Bubble> bubbles = new ArrayList<>();
    private int score = 0;
    private int totalPopped = 0;
    private JLabel scoreLabel;
    private JPanel gamePanel;
    private javax.swing.Timer spawnTimer, moveTimer;
    private boolean running = true;

    private static final Color[] BUBBLE_COLORS = {
        new Color(244,114,182,180), new Color(56,189,248,180),
        new Color(52,211,153,180),  new Color(251,191,36,180),
        new Color(168,85,247,180),  new Color(249,115,22,180),
        new Color(239,68,68,180),   new Color(20,184,166,180),
    };

    public BubblePopGame(UserAccount user) {
        super("SafeHaven \u2013 \uD83E\uDEA7 Bubble Pop!", 660, 560);
        this.user = user;

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel(" Bubble Pop!", SwingConstants.CENTER);
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        topBar.add(title, BorderLayout.CENTER);

        scoreLabel = new JLabel("\u2B50 Score: 0 | Popped: 0", SwingConstants.RIGHT);
        scoreLabel.setFont(AppFonts.BODY_BOLD);
        scoreLabel.setForeground(new Color(200, 130, 30));
        topBar.add(scoreLabel, BorderLayout.EAST);
        content.add(topBar, BorderLayout.NORTH);

        gamePanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint sky = new GradientPaint(0,0,new Color(200,235,255),0,getHeight(),new Color(255,245,225));
                g2.setPaint(sky);
                g2.fillRect(0,0,getWidth(),getHeight());
                Color[] dots = AppColors.POLKA_COLORS;
                int dotIdx=0;
                for (int y=0;y<getHeight();y+=60){int off=(y/60%2==0)?0:30;for(int x=-30+off;x<getWidth()+30;x+=60){g2.setColor(dots[dotIdx%dots.length]);g2.fillOval(x-10,y-10,20,20);dotIdx++;}}
                synchronized(bubbles){for(Bubble b:bubbles)b.draw(g2);}
                g2.dispose();
            }
        };
        gamePanel.setOpaque(false);

        gamePanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                synchronized(bubbles){
                    Iterator<Bubble> it=bubbles.iterator();
                    while(it.hasNext()){
                        Bubble b=it.next();
                        int dx=e.getX()-b.x, dy=e.getY()-b.y;
                        if(dx*dx+dy*dy<=b.r*b.r){
                            it.remove(); score+=b.r>40?10:5; totalPopped++;
                            ThreadPoolManager.execute(()->SoundManager.getInstance().playBubblePop());
                            scoreLabel.setText("\u2B50 Score: "+score+" | Popped: "+totalPopped);
                            user.updateProgress("BubblePop",Math.min(100,score));
                            showPopEffect(b.x,b.y,b.color); break;
                        }
                    }
                }
            }
        });

        content.add(gamePanel, BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        ctrl.setOpaque(false);
        RoundedButton btnPause  = new RoundedButton(" Pause",  AppColors.LAVENDER_DEEP);
        RoundedButton btnResume = new RoundedButton(" Resume",  AppColors.MINT_DEEP);
        RoundedButton btnClear  = new RoundedButton(" Clear",  AppColors.CORAL_DEEP);
        btnPause.addActionListener(e  -> { running=false; spawnTimer.stop(); moveTimer.stop(); });
        btnResume.addActionListener(e -> { running=true;  spawnTimer.start(); moveTimer.start(); });
        btnClear.addActionListener(e  -> { synchronized(bubbles){bubbles.clear();} gamePanel.repaint(); });
        ctrl.add(btnPause); ctrl.add(btnResume); ctrl.add(btnClear);
        content.add(ctrl, BorderLayout.SOUTH);

        addContent(content);
        startGame();
        addWindowListener(new WindowAdapter(){
            @Override public void windowClosed(WindowEvent e){spawnTimer.stop();moveTimer.stop();}
        });
    }

    private void startGame() {
        spawnTimer = new javax.swing.Timer(1200, e -> {
            if(running && gamePanel.getWidth()>0) spawnBubble();
        });
        spawnTimer.start();
        moveTimer = new javax.swing.Timer(40, e -> {
            if(!running) return;
            synchronized(bubbles){
                Iterator<Bubble> it=bubbles.iterator();
                while(it.hasNext()){
                    Bubble b=it.next();
                    b.y-=b.speed;
                    b.x+=(int)(Math.sin(b.wobble)*1.2);
                    b.wobble+=0.07;
                    if(b.y+b.r<0) it.remove();
                }
            }
            gamePanel.repaint();
        });
        moveTimer.start();
    }

    private void spawnBubble() {
        int w=gamePanel.getWidth(), h=gamePanel.getHeight();
        if(w<=0||h<=0) return;
        int r=25+(int)(Math.random()*30);
        int x=r+(int)(Math.random()*(w-2*r));
        int y=h-r-10;
        double spd=1.2+Math.random()*1.8;
        Color c=BUBBLE_COLORS[(int)(Math.random()*BUBBLE_COLORS.length)];
        synchronized(bubbles){if(bubbles.size()<20)bubbles.add(new Bubble(x,y,r,spd,c));}
    }

    private void showPopEffect(int x, int y, Color c) {
        javax.swing.Timer t=new javax.swing.Timer(60,null);
        int[] frame={0};
        t.addActionListener(e->{
            frame[0]++;
            if(frame[0]>6){t.stop();return;}
            Graphics g=gamePanel.getGraphics();
            if(g!=null){
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int r2=frame[0]*8;
                g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),Math.max(0,180-frame[0]*30)));
                g2.fillOval(x-r2,y-r2,r2*2,r2*2); g2.dispose();
            }
        });
        t.start();
    }

    private static class Bubble {
        int x, y, r; double speed, wobble; Color color;
        Bubble(int x,int y,int r,double speed,Color color){
            this.x=x;this.y=y;this.r=r;this.speed=speed;
            this.wobble=Math.random()*Math.PI*2;this.color=color;
        }
        void draw(Graphics2D g2) {
            g2.setColor(color);g2.fillOval(x-r,y-r,2*r,2*r);
            int hr=r/3;
            g2.setColor(new Color(255,255,255,160));g2.fillOval(x-r+r/5,y-r+r/6,hr,hr);
            g2.setColor(new Color(255,255,255,100));g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x-r,y-r,2*r,2*r);g2.setStroke(new BasicStroke(1f));
        }
    }
}