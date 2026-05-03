package util;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;


public class UIStyle {
    public static final Color PRIMARY    = new Color(75, 119, 190);
    public static final Color HOVER      = new Color(106, 149, 215);
    public static final Color BG         = new Color(245, 245, 250);
    public static final Font  TITLE_FONT = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font  MAIN_FONT  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font  BOLD_FONT  = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font  HDR_FONT   = new Font("Segoe UI", Font.BOLD,  22);

    public static JPanel gradientPanel() {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(250,250,255), 0, getHeight(), BG));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 5;
                for (int i = 0; i < s; i++) {
                    g2.setColor(new Color(0, 0, 0, (int)(255 * 0.1f * (s - i) / s)));
                    g2.fill(new RoundRectangle2D.Double(i, i, getWidth()-2*i, getHeight()-2*i, 15, 15));
                }
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(s, s, getWidth()-2*s, getHeight()-2*s, 15, 15));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    public static JButton primaryButton(String text) {
        return styledButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton secondaryButton(String text) {
        JButton b = styledButton(text, new Color(230,230,230), new Color(80,80,80));
        return b;
    }

    public static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(150, 40));

        btn.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel m = ((AbstractButton)c).getModel();
                Color base = m.isPressed() ? bg.darker() : m.isRollover() ? HOVER : bg;
                g2.setPaint(new GradientPaint(0,0,base,0,c.getHeight(),
                    new Color(Math.max(base.getRed()-20,0),Math.max(base.getGreen()-20,0),Math.max(base.getBlue()-20,0))));
                g2.fillRoundRect(0,0,c.getWidth(),c.getHeight(),12,12);
                if (!m.isPressed()) { g2.setColor(new Color(255,255,255,60)); g2.fillRoundRect(2,2,c.getWidth()-4,c.getHeight()/2-2,10,10); }
                g2.setColor(new Color(0,0,0,30));
                g2.drawRoundRect(0,0,c.getWidth()-1,c.getHeight()-1,12,12);
                g2.dispose();
                super.paint(g,c);
            }
        });
        btn.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
            @Override public void mouseExited(MouseEvent e){btn.setCursor(Cursor.getDefaultCursor());}
        });
        return btn;
    }

    public static JTextField styledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(MAIN_FONT);
        tf.setPreferredSize(new Dimension(300, 30));
        normalBorder(tf);
        tf.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e){ focusBorder(tf); }
            @Override public void focusLost(FocusEvent e){ normalBorder(tf); }
        });
        return tf;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(MAIN_FONT); cb.setBackground(Color.WHITE);
        cb.setForeground(new Color(50,50,50)); cb.setPreferredSize(new Dimension(100,30));
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,230),1,true),
            BorderFactory.createEmptyBorder(2,5,2,5)));
        return cb;
    }

    public static javax.swing.plaf.basic.BasicScrollBarUI thinScrollBarUI() {
        return new javax.swing.plaf.basic.BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=new Color(180,190,240);trackColor=Color.WHITE;}
            @Override protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));return b;}
            @Override protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));b.setMaximumSize(new Dimension(0,0));return b;}
            @Override protected void paintThumb(Graphics g,JComponent c,Rectangle r){
                if(r.isEmpty()||!scrollbar.isEnabled())return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,10,10);g2.dispose();
            }
        };
    }

    public static void styleTableHeader(JTable table) {
        JTableHeader h = table.getTableHeader();
        h.setFont(BOLD_FONT); h.setBackground(PRIMARY); h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(h.getWidth(), 35));
        h.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,s,f,r,c);
                l.setBackground(PRIMARY);l.setForeground(Color.WHITE);l.setFont(BOLD_FONT);
                l.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));l.setHorizontalAlignment(JLabel.CENTER);return l;
            }
        });
    }

    public static javax.swing.table.DefaultTableCellRenderer stripeRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                Component comp=super.getTableCellRendererComponent(t,v,s,f,r,c);
                if(!s) comp.setBackground(r%2==0?new Color(250,250,255):Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                setHorizontalAlignment(SwingConstants.CENTER);return comp;
            }
        };
    }

    private static void normalBorder(JTextField tf){
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,230),1,true),
            BorderFactory.createEmptyBorder(5,10,5,10)));
    }
    private static void focusBorder(JTextField tf){
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY,2,true),
            BorderFactory.createEmptyBorder(4,9,4,9)));
    }
}