package view;

import controller.AppointmentController;
import model.Users;
import util.UIStyle;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class InfoDetailView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppointmentController ctrl;
    private DefaultTableModel tableModel;

    public InfoDetailView(int appId, String name, String location,
                          Date date, int start, int end,
                          AppointmentController ctrl) {
        this.ctrl = ctrl;
        setTitle("Thông tin chi tiết");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(100,100,600,650);
        setResizable(false);
        setLocationRelativeTo(null);
        buildUI(appId, name, location, date, start, end);
    }

    private void buildUI(int appId, String name, String location, Date date, int start, int end) {
        JPanel root = UIStyle.gradientPanel();
        root.setBorder(new EmptyBorder(20,20,20,20));
        root.setLayout(new BorderLayout(0,15));
        setContentPane(root);

        JLabel title = new JLabel("Thông tin chi tiết sự kiện", SwingConstants.CENTER);
        title.setFont(UIStyle.HDR_FONT); title.setForeground(new Color(50,50,50));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0,15)); center.setOpaque(false);

        JPanel infoCard = UIStyle.cardPanel();
        infoCard.setLayout(new BorderLayout());
        JPanel infoInner = new JPanel(new GridBagLayout()); infoInner.setOpaque(false);
        infoInner.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        GridBagConstraints g = new GridBagConstraints();
        g.anchor=GridBagConstraints.WEST; g.insets=new Insets(8,10,8,5);

        String[][] rows = {
            {"Tên sự kiện:", name},
            {"Vị trí:", location},
            {"Ngày diễn ra:", new SimpleDateFormat("dd/MM/yyyy").format(date)},
            {"Thời gian bắt đầu:", start+" giờ"},
            {"Thời gian kết thúc:", end+" giờ"}
        };
        for(int i=0;i<rows.length;i++){
            g.gridx=0; g.gridy=i; JLabel lbl=new JLabel(rows[i][0]); lbl.setFont(UIStyle.BOLD_FONT); infoInner.add(lbl,g);
            g.gridx=1; g.weightx=1; g.fill=GridBagConstraints.HORIZONTAL; JLabel val=new JLabel(rows[i][1]); val.setFont(UIStyle.MAIN_FONT); infoInner.add(val,g);
            g.weightx=0; g.fill=GridBagConstraints.NONE;
        }

        g.gridx=2; g.gridy=0; g.insets=new Insets(8,30,8,5);
        JLabel lblRmd=new JLabel("Bộ nhắc:"); lblRmd.setFont(UIStyle.BOLD_FONT); infoInner.add(lblRmd,g);

        g.gridx=2; g.gridy=1; g.gridheight=4; g.fill=GridBagConstraints.BOTH;
        JPanel rmdPanel = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g2){
                Graphics2D g2d=(Graphics2D)g2.create();
                g2d.setColor(new Color(240,240,250));g2d.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2d.setColor(new Color(220,220,240));g2d.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);g2d.dispose();
            }
        };
        rmdPanel.setOpaque(false); rmdPanel.setPreferredSize(new Dimension(150,120));
        JTextArea taRmd=new JTextArea(); taRmd.setFont(UIStyle.MAIN_FONT);
        taRmd.setBackground(new Color(240,240,250)); taRmd.setEditable(false);
        taRmd.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        for(int remId : ctrl.getReminderIds(appId)){
            String s = switch(remId){case 1->"Nhắc trước 15 phút";case 2->"Nhắc trước 30 phút";case 3->"Nhắc trước 1 ngày";default->"Nhắc #"+remId;};
            taRmd.append(s+"\n");
        }
        rmdPanel.add(taRmd); infoInner.add(rmdPanel,g);

        infoCard.add(infoInner); center.add(infoCard, BorderLayout.NORTH);

        JPanel pCard = UIStyle.cardPanel();
        pCard.setLayout(new BorderLayout(0,10));
        pCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel lblP=new JLabel("Thông tin người tham gia:"); lblP.setFont(UIStyle.BOLD_FONT); pCard.add(lblP,BorderLayout.NORTH);

        tableModel=new DefaultTableModel(){@Override public boolean isCellEditable(int r,int c){return false;}};
        tableModel.addColumn("STT"); tableModel.addColumn("ID");
        tableModel.addColumn("Họ và tên"); tableModel.addColumn("SĐT");

        JTable table=new JTable(tableModel);
        table.setFont(UIStyle.MAIN_FONT); table.setRowHeight(30);
        table.setShowGrid(false); table.setSelectionBackground(new Color(230,240,255));
        UIStyle.styleTableHeader(table);
        table.setDefaultRenderer(Object.class, UIStyle.stripeRenderer());

        int[] widths={50,80,200,150};
        for(int i=0;i<widths.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        int stt=1;
        for(Users u : ctrl.getParticipants(appId))
            tableModel.addRow(new Object[]{stt++,u.getId(),u.getName(),u.getPhoneNumber()});

        JScrollPane scroll=new JScrollPane(table);
        scroll.setOpaque(false); scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false); scroll.setPreferredSize(new Dimension(scroll.getWidth(),150));
        scroll.getVerticalScrollBar().setUI(UIStyle.thinScrollBarUI());
        pCard.add(scroll,BorderLayout.CENTER);
        center.add(pCard, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        JPanel btns=new JPanel(new FlowLayout(FlowLayout.CENTER,20,5)); btns.setOpaque(false);
        JButton btnClose=UIStyle.primaryButton("Đóng");
        btnClose.addActionListener(e->dispose());
        btns.add(btnClose);
        root.add(btns, BorderLayout.SOUTH);
    }
}