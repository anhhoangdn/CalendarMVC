package view;

import controller.AppointmentController;
import model.Reminder;
import util.UIStyle;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ReminderView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppointmentController ctrl;
    private final int                   appId;   
    private JTextArea          textArea;
    private JComboBox<String>  cbReminder;

    public ReminderView(AppointmentController ctrl, int appId) {
        this.ctrl  = ctrl;
        this.appId = appId;
        setTitle("Bộ nhắc");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(100,100,500,450);
        setResizable(false);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = UIStyle.gradientPanel();
        root.setBorder(new EmptyBorder(20,20,20,20));
        root.setLayout(new BorderLayout(0,15));
        setContentPane(root);

        JLabel title = new JLabel("Bộ nhắc", SwingConstants.CENTER);
        title.setFont(UIStyle.TITLE_FONT); title.setForeground(new Color(50,50,50));
        root.add(title, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int s=5; for(int i=0;i<s;i++){g2.setColor(new Color(0,0,0,(int)(255*0.1f*(s-i)/s)));g2.fill(new RoundRectangle2D.Double(i,i,getWidth()-2*i,getHeight()-2*i,15,15));}
                g2.setColor(Color.WHITE);g2.fill(new RoundRectangle2D.Double(s,s,getWidth()-2*s,getHeight()-2*s,15,15));g2.dispose();
            }
        };
        card.setOpaque(false); card.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JPanel inner = new JPanel(new BorderLayout(0,15)); inner.setOpaque(false);

        cbReminder = new JComboBox<>();
        cbReminder.setFont(UIStyle.MAIN_FONT); cbReminder.setBackground(Color.WHITE);
        cbReminder.setPreferredSize(new Dimension(250,30));
        for (Reminder r : ctrl.getAllReminders()) cbReminder.addItem(r.getTitle());

        JButton btnAdd = UIStyle.primaryButton("Thêm");
        btnAdd.setPreferredSize(new Dimension(100,30));
        btnAdd.addActionListener(e -> {
            String sel = (String) cbReminder.getSelectedItem();
            if (sel==null) return;
            List<String> existing = getLinesFromTextArea(textArea);
            if (!existing.contains(sel)) textArea.append(sel+"\n");
            else JOptionPane.showMessageDialog(this,"Lời nhắc này đã được thêm rồi!","Thông báo",JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel top = new JPanel(new BorderLayout(10,0)); top.setOpaque(false);
        top.add(cbReminder, BorderLayout.CENTER); top.add(btnAdd, BorderLayout.EAST);

        textArea = new JTextArea();
        textArea.setFont(UIStyle.MAIN_FONT); textArea.setBackground(Color.WHITE);
        textArea.setLineWrap(true); textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,230),1,true),
            BorderFactory.createEmptyBorder(5,5,5,5)));
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);

        inner.add(top, BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10)); btns.setOpaque(false);

        JButton btnOk = UIStyle.primaryButton("Xác nhận"); btnOk.setPreferredSize(new Dimension(150,40));
        btnOk.addActionListener(e -> {
            
            for (String r : getLinesFromTextArea(textArea))
                if (!r.isBlank()) ctrl.addReminder(appId, r);
            new MyCalendarView(ctrl).setVisible(true);
            dispose();
        });

        JButton btnCancel = UIStyle.secondaryButton("Hủy bỏ"); btnCancel.setPreferredSize(new Dimension(150,40));
        btnCancel.addActionListener(e -> dispose());

        btns.add(btnOk); btns.add(btnCancel);
        root.add(btns, BorderLayout.SOUTH);
    }

    private List<String> getLinesFromTextArea(JTextArea ta) {
        List<String> lines = new ArrayList<>();
        try{
            for(int i=0;i<ta.getLineCount();i++){
                int s=ta.getLineStartOffset(i), e=ta.getLineEndOffset(i);
                lines.add(ta.getText(s,e-s).trim());
            }
        }catch(Exception ex){ex.printStackTrace();}
        return lines;
    }
}