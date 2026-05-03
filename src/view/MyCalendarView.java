package view;

import controller.AppointmentController;
import model.Appointment;
import util.UIStyle;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.*;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;

public class MyCalendarView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppointmentController ctrl;
    private JTable             table;
    private DefaultTableModel  tableModel;
    private JButton            btnSave;
    private boolean            dataChanged = false;
    private boolean            isLoading   = false; 
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public MyCalendarView(AppointmentController ctrl) {
        this.ctrl = ctrl;
        setTitle("Lịch hẹn của tôi");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(100,100,1000,600);
        setLocationRelativeTo(null);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel root = UIStyle.gradientPanel();
        root.setBorder(new EmptyBorder(20,20,20,20));
        root.setLayout(new BorderLayout(0,20));
        setContentPane(root);

        JLabel title = new JLabel("Danh sách các buổi hẹn", SwingConstants.CENTER);
        title.setFont(UIStyle.TITLE_FONT); title.setForeground(new Color(50,50,50));
        root.add(title, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int s=5; for(int i=0;i<s;i++){g2.setColor(new Color(0,0,0,(int)(255*0.1f*(s-i)/s)));g2.fill(new RoundRectangle2D.Double(i,i,getWidth()-2*i,getHeight()-2*i,15,15));}
                g2.setColor(Color.WHITE);g2.fill(new RoundRectangle2D.Double(s,s,getWidth()-2*s,getHeight()-2*s,15,15));g2.dispose();
            }
        };
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        buildTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setOpaque(false); scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.getVerticalScrollBar().setUI(UIStyle.thinScrollBarUI());
        tablePanel.add(scroll, BorderLayout.CENTER);
        root.add(tablePanel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,30,10)); btns.setOpaque(false);

        btnSave = UIStyle.primaryButton("Lưu thay đổi"); btnSave.setEnabled(false);
        btnSave.setPreferredSize(new Dimension(180,40));
        btnSave.addActionListener(e -> saveAllChanges());

        JButton btnDetail = UIStyle.primaryButton("Chi tiết");
        btnDetail.setPreferredSize(new Dimension(150,40));
        btnDetail.addActionListener(e -> openDetail());

        JButton btnDelete = UIStyle.styledButton("Xóa", new Color(190,50,50), Color.WHITE);
        btnDelete.setPreferredSize(new Dimension(150,40));
        btnDelete.addActionListener(e -> deleteSelected());

        JButton btnRefresh = UIStyle.styledButton("Làm mới", new Color(75,190,137), Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(150,40));
        btnRefresh.addActionListener(e -> {
            if (dataChanged) {
                int opt = JOptionPane.showConfirmDialog(this,"Có thay đổi chưa lưu, vẫn làm mới?","Cảnh báo",JOptionPane.YES_NO_OPTION);
                if (opt!=JOptionPane.YES_OPTION) return;
            }
            loadData();
        });

        JButton btnClose = UIStyle.secondaryButton("Đóng");
        btnClose.setPreferredSize(new Dimension(150,40));
        btnClose.addActionListener(e -> {
            if (dataChanged) {
                int opt = JOptionPane.showConfirmDialog(this,"Có thay đổi chưa lưu. Lưu trước khi đóng?","Cảnh báo",JOptionPane.YES_NO_CANCEL_OPTION);
                if (opt==JOptionPane.YES_OPTION && saveAllChanges()) dispose();
                else if (opt==JOptionPane.NO_OPTION) dispose();
            } else dispose();
        });

        btns.add(btnSave); btns.add(btnDetail); btns.add(btnDelete); btns.add(btnRefresh); btns.add(btnClose);
        root.add(btns, BorderLayout.SOUTH);
    }

    private void buildTable() {
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c){ return c>=2 && c<=7; }
            @Override public Class<?> getColumnClass(int c){
                if(c==4) return Date.class;
                if(c==5||c==6) return Integer.class;
                return super.getColumnClass(c);
            }
        };
        String[] cols = {"STT","Mã sự kiện","Tên sự kiện","Vị trí","Ngày diễn ra","Giờ bắt đầu","Giờ kết thúc","Kiểu nhóm"};
        for (String col : cols) tableModel.addColumn(col);

        table = new JTable(tableModel);
        table.setFont(UIStyle.MAIN_FONT); table.setRowHeight(30);
        table.setShowGrid(true); table.setGridColor(new Color(230,230,240));
        table.setSelectionBackground(new Color(230,240,255)); table.setSelectionForeground(UIStyle.PRIMARY);
        UIStyle.styleTableHeader(table);
        table.setDefaultRenderer(Object.class, UIStyle.stripeRenderer());

        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()){
            private JTextField tf; private Date orig;
            @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){
                tf=(JTextField)super.getTableCellEditorComponent(t,v,s,r,c);
                orig=(v instanceof Date)?(Date)v:null;
                if(orig!=null) tf.setText(sdf.format(orig)); else tf.setText("");
                return tf;
            }
            @Override public Object getCellEditorValue(){
                try{return sdf.parse(tf.getText());}
                catch(ParseException e){JOptionPane.showMessageDialog(null,"Định dạng ngày không hợp lệ (yyyy-MM-dd)");return orig;}
            }
        });

        DefaultCellEditor hourEditor = new DefaultCellEditor(new JTextField()){
            private JTextField tf; private Integer orig;
            @Override public Component getTableCellEditorComponent(JTable t,Object v,boolean s,int r,int c){
                tf=(JTextField)super.getTableCellEditorComponent(t,v,s,r,c);
                orig=(v instanceof Integer)?(Integer)v:null;
                tf.setText(orig!=null?orig.toString():""); return tf;
            }
            @Override public Object getCellEditorValue(){
                try{int n=Integer.parseInt(tf.getText());if(n<0||n>23)throw new NumberFormatException();return n;}
                catch(NumberFormatException e){JOptionPane.showMessageDialog(null,"Giờ phải từ 0-23");return orig;}
            }
        };
        table.getColumnModel().getColumn(5).setCellEditor(hourEditor);
        table.getColumnModel().getColumn(6).setCellEditor(hourEditor);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                Component comp=super.getTableCellRendererComponent(t,v,s,f,r,c);
                if(v instanceof Date) setText(sdf.format((Date)v));
                if(!s) comp.setBackground(r%2==0?new Color(250,250,255):Color.WHITE);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); setHorizontalAlignment(CENTER); return comp;
            }
        });

        tableModel.addTableModelListener(e -> {
            if (isLoading) return;
            dataChanged = true;
            if (btnSave != null) btnSave.setEnabled(true);
        });

        int[] widths={50,100,180,150,120,110,110,100};
        for(int i=0;i<widths.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private void loadData() {
        isLoading = true; 
        tableModel.setRowCount(0);
        List<Appointment> list = ctrl.getAllAppointments();
        int stt=1;
        for(Appointment a : list) {
            tableModel.addRow(new Object[]{stt++,a.getId(),a.getName(),a.getLocation(),
                a.getMeetingDate(),a.getStartHour(),a.getEndHour(),a.getTypeAppointment()});
        }
        isLoading = false;  
        dataChanged = false;
        if (btnSave != null) btnSave.setEnabled(false);
    }

    private boolean saveAllChanges() {
        if(table.isEditing()) table.getCellEditor().stopCellEditing();
        boolean ok=true;
        for(int r=0;r<table.getRowCount();r++){
            try{
                int id=(Integer)table.getValueAt(r,1);
                String name=(String)table.getValueAt(r,2);
                String loc=(String)table.getValueAt(r,3);
                Date date=(Date)table.getValueAt(r,4);
                int s=(Integer)table.getValueAt(r,5), e=(Integer)table.getValueAt(r,6);
                String type=(String)table.getValueAt(r,7);
                if(name==null||name.isBlank()){JOptionPane.showMessageDialog(this,"Tên không được để trống hàng "+(r+1));ok=false;continue;}
                if(s>=e){JOptionPane.showMessageDialog(this,"Giờ bắt đầu phải nhỏ hơn kết thúc hàng "+(r+1));ok=false;continue;}
                if(ctrl.updateFull(id,name,loc,date,s,e,type)<=0){JOptionPane.showMessageDialog(this,"Lỗi cập nhật hàng "+(r+1));ok=false;}
            }catch(Exception ex){JOptionPane.showMessageDialog(this,"Lỗi hàng "+(r+1)+": "+ex.getMessage());ok=false;}
        }
        if(ok){JOptionPane.showMessageDialog(this,"Đã lưu tất cả thay đổi thành công!");dataChanged=false;btnSave.setEnabled(false);}
        return ok;
    }

    private void openDetail() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Vui lòng chọn một buổi hẹn từ danh sách");return;}
        try{
            int appId=(Integer)table.getValueAt(row,1);
            String name=(String)table.getValueAt(row,2), loc=(String)table.getValueAt(row,3);
            Date date; Object dv=table.getValueAt(row,4);
            date = dv instanceof String ? sdf.parse((String)dv) : (Date)dv;
            int s=(Integer)table.getValueAt(row,5), e=(Integer)table.getValueAt(row,6);
            new InfoDetailView(appId,name,loc,date,s,e,ctrl).setVisible(true);
        }catch(Exception ex){ex.printStackTrace();}
    }

    private void deleteSelected() {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Vui lòng chọn một buổi hẹn để xóa");return;}
        int id=(Integer)table.getValueAt(row,1);
        String name=(String)table.getValueAt(row,2);
        int opt=JOptionPane.showConfirmDialog(this,"Bạn có chắc muốn xóa '"+name+"' không?","Xác nhận xóa",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(opt==JOptionPane.YES_OPTION){
            if(ctrl.deleteAppointment(id)){JOptionPane.showMessageDialog(this,"Xóa thành công!");loadData();}
            else JOptionPane.showMessageDialog(this,"Không thể xóa, vui lòng thử lại.","Lỗi",JOptionPane.ERROR_MESSAGE);
        }
    }
}