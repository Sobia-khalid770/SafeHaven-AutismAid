package safehaven.games;

import safehaven.models.UserAccount;
import safehaven.ui.*;
import safehaven.utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class ShapeMatchingGame extends BaseFrame {
    private final UserAccount user;
    private int score = 0;
    private int round = 1;
    private JLabel scoreLabel, messageLabel;
    private JPanel gamePanel;
    private final List<DraggableShape> pieces = new ArrayList<>();
    private final List<ShadowSlot>     slots  = new ArrayList<>();
    private DraggableShape dragging = null;
    private Point dragOffset = new Point();
    private javax.swing.Timer delayTimer = null;

    private static final String[] SHAPE_NAMES = {"Circle","Square","Triangle","Rectangle","Pentagon"};
    private static final String[] SHAPE_EN    = {"Circle","Square","Triangle","Rectangle","Pentagon"};
    private static final Color[] SHAPE_COLORS = {
        new Color(244,114,182), new Color(56,189,248), new Color(52,211,153),
        new Color(249,115,22),  new Color(168,85,247)
    };

    public ShapeMatchingGame(UserAccount user) {
        super("SafeHaven \u2013 Shape Matching", 720, 560);
        this.user = user;

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Shape Matching", SwingConstants.CENTER);
        title.setFont(AppFonts.HEADING);
        title.setForeground(new Color(160, 80, 30));
        header.add(title, BorderLayout.CENTER);
        scoreLabel = new JLabel("Score: 0  |  Round: 1", SwingConstants.RIGHT);
        scoreLabel.setFont(AppFonts.BODY_BOLD);
        scoreLabel.setForeground(new Color(200,130,30));
        header.add(scoreLabel, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        messageLabel = new JLabel("Drag each shape to its shadow!", SwingConstants.CENTER);
        messageLabel.setFont(AppFonts.BODY_BOLD);
        messageLabel.setForeground(new Color(99,102,241));

        gamePanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint bg = new GradientPaint(0,0,new Color(255,248,235),0,getHeight(),new Color(255,235,210));
                g2.setPaint(bg); g2.fillRect(0,0,getWidth(),getHeight());
                Color[] dc=AppColors.POLKA_COLORS; int idx=0;
                for(int y=0;y<getHeight()+55;y+=55){int off=(y/55%2==0)?0:27;for(int x=-27+off;x<getWidth()+27;x+=55){g2.setColor(dc[idx%dc.length]);g2.fillOval(x-9,y-9,18,18);idx++;}}
                for(ShadowSlot s:slots) s.draw(g2);
                for(DraggableShape p:pieces) if(p!=dragging) p.draw(g2);
                if(dragging!=null) dragging.draw(g2);
                g2.dispose();
            }
        };

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                for(int i=pieces.size()-1;i>=0;i--){
                    DraggableShape p=pieces.get(i);
                    if(!p.matched&&p.contains(e.getPoint())){dragging=p;dragOffset=new Point(e.getX()-p.x,e.getY()-p.y);break;}
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if(dragging!=null){dragging.x=e.getX()-dragOffset.x;dragging.y=e.getY()-dragOffset.y;gamePanel.repaint();}
            }
            @Override public void mouseReleased(MouseEvent e) {
                if(dragging!=null){checkMatch(dragging);dragging=null;gamePanel.repaint();}
            }
        };
        gamePanel.addMouseListener(ma);
        gamePanel.addMouseMotionListener(ma);
        content.add(gamePanel, BorderLayout.CENTER);

        RoundedButton btnNew = new RoundedButton("New Game", AppColors.TEAL_DEEP);
        btnNew.addActionListener(e -> newRound());
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(messageLabel, BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER,14,4));
        btnRow.setOpaque(false); btnRow.add(btnNew);
        south.add(btnRow, BorderLayout.SOUTH);
        content.add(south, BorderLayout.SOUTH);

        addContent(content);

        gamePanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if(pieces.isEmpty()) newRound();
            }
        });
        
        // Cleanup timers on window close
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (delayTimer != null && delayTimer.isRunning()) {
                    delayTimer.stop();
                }
            }
        });
    }

    private void newRound() {
        pieces.clear(); slots.clear();
        int w=gamePanel.getWidth(), h=gamePanel.getHeight();
        if(w<100||h<100) return;
        int count=3;
        List<Integer> idxList=new ArrayList<>(Arrays.asList(0,1,2,3,4));
        Collections.shuffle(idxList);
        List<Integer> chosen=idxList.subList(0,count);
        int slotSpacing=w/(count+1);
        for(int i=0;i<count;i++){
            int st=chosen.get(i);
            slots.add(new ShadowSlot(slotSpacing*(i+1),90,55,st,SHAPE_NAMES[st],SHAPE_EN[st]));
        }
        List<Integer> shuffled=new ArrayList<>(chosen);
        Collections.shuffle(shuffled);
        int pieceSpacing=w/(count+1);
        for(int i=0;i<count;i++){
            int st=shuffled.get(i);
            pieces.add(new DraggableShape(pieceSpacing*(i+1),h-120,52,st,SHAPE_COLORS[st]));
        }
        messageLabel.setText("Drag shape to its shadow!");
        messageLabel.setForeground(new Color(99,102,241));
        scoreLabel.setText("Score: "+score+"  |  Round: "+round);
        gamePanel.repaint();
    }

    private void checkMatch(DraggableShape piece) {
        for(ShadowSlot slot:slots){
            if(slot.shapeType==piece.shapeType&&!slot.filled){
                int dx=piece.cx()-slot.x, dy=piece.cy()-slot.y;
                if(dx*dx+dy*dy<70*70){
                    piece.x=slot.x-piece.r; piece.y=slot.y-piece.r;
                    piece.matched=true; slot.filled=true; score+=20;
                    ThreadPoolManager.execute(()->SoundManager.getInstance().playWow());
                    showMessage("Well done! "+slot.nameEn+"!", new Color(20,184,166));
                    user.updateProgress("Shapes",Math.min(100,score));
                    scoreLabel.setText("Score: "+score+"  |  Round: "+round);
                    checkRoundComplete(); return;
                }
            }
        }
        ThreadPoolManager.execute(()->SoundManager.getInstance().playWrong());
        showMessage("Almost there! Look carefully!", new Color(249,115,22));
    }

    private void checkRoundComplete() {
        boolean allDone=slots.stream().allMatch(s->s.filled);
        if(allDone){
            round++;
            delayTimer=new javax.swing.Timer(1200,e->newRound());
            delayTimer.setRepeats(false);
            delayTimer.start();
            showMessage("Well done! All matched!", new Color(244,114,182));
            ThreadPoolManager.execute(()->SoundManager.getInstance().playCheer());
        }
    }

    private void showMessage(String msg, Color color) {
        SwingUtilities.invokeLater(()->{messageLabel.setText(msg);messageLabel.setForeground(color);});
    }

    static Shape makeShape(int type,int cx,int cy,int r){
        switch(type){
            case 0: return new Ellipse2D.Double(cx-r,cy-r,2*r,2*r);
            case 1: return new Rectangle2D.Double(cx-r,cy-r,2*r,2*r);
            case 2: return makeTriangle(cx,cy,r);
            case 3: return new Rectangle2D.Double(cx-(int)(r*1.4),cy-r*2/3,(int)(r*2.8),r*4/3);
            case 4: return makePentagon(cx,cy,r);
            default: return new Ellipse2D.Double(cx-r,cy-r,2*r,2*r);
        }
    }
    static Shape makeTriangle(int cx,int cy,int r){Path2D p=new Path2D.Double();p.moveTo(cx,cy-r);p.lineTo(cx+r,cy+r);p.lineTo(cx-r,cy+r);p.closePath();return p;}
    static Shape makePentagon(int cx,int cy,int r){Path2D p=new Path2D.Double();for(int i=0;i<5;i++){double a=-Math.PI/2+i*2*Math.PI/5;double x=cx+r*Math.cos(a),y=cy+r*Math.sin(a);if(i==0)p.moveTo(x,y);else p.lineTo(x,y);}p.closePath();return p;}

    static class ShadowSlot {
        int x,y,r,shapeType; String nameUrdu,nameEn; boolean filled=false;
        ShadowSlot(int x,int y,int r,int type,String nu,String ne){this.x=x;this.y=y;this.r=r;this.shapeType=type;nameUrdu=nu;nameEn=ne;}
        void draw(Graphics2D g2){
            Shape s=makeShape(shapeType,x,y,r);
            g2.setColor(filled?new Color(52,211,153,100):new Color(80,60,50,90)); g2.fill(s);
            g2.setColor(new Color(80,60,50,160));
            g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,0,new float[]{6,4},0));
            g2.draw(s); g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Segoe UI",Font.PLAIN,11)); g2.setColor(new Color(120,80,50));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(nameEn,x-fm.stringWidth(nameEn)/2,y+r+20);
        }
    }

    static class DraggableShape {
        int x,y,r,shapeType; Color color; boolean matched=false;
        DraggableShape(int cx,int cy,int r,int type,Color color){this.x=cx-r;this.y=cy-r;this.r=r;this.shapeType=type;this.color=color;}
        int cx(){return x+r;} int cy(){return y+r;}
        boolean contains(Point p){return makeShape(shapeType,cx(),cy(),r).contains(p);}
        void draw(Graphics2D g2){
            Shape s=makeShape(shapeType,cx(),cy(),r);
            g2.setColor(new Color(0,0,0,30));
            g2.fill(AffineTransform.getTranslateInstance(3,5).createTransformedShape(s));
            GradientPaint gp=new GradientPaint(x,y,color.brighter(),x,y+2*r,color.darker());
            g2.setPaint(gp); g2.fill(s);
            g2.setColor(new Color(255,255,255,80)); g2.fillOval(cx()-r/3,cy()-r/2,r*2/3,r/3);
            g2.setColor(new Color(255,255,255,140)); g2.setStroke(new BasicStroke(2f)); g2.draw(s);
            g2.setStroke(new BasicStroke(1f));
            if(matched){g2.setColor(new Color(50,200,100,180));g2.setStroke(new BasicStroke(3f));g2.draw(s);g2.setStroke(new BasicStroke(1f));}
        }
    }
}
