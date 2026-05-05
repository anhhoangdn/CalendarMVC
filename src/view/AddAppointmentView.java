package view;

import controller.AppointmentController;
import util.UIStyle;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AddAppointmentView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Date                  selectedDate;
    private final AppointmentController ctrl;

    private JTextField        txtName, txtLocation;
    private JRadioButton      rdDon, rdNhom;
    private JComboBox<String> cbbStart, cbbEnd;

    private static final String[] HOURS = java.util.stream.IntStream
    	    .rangeClosed(0, 23)
    	    .mapToObj(Integer::toString)
    	    .toArray(String[]::new);

    public AddAppointmentView(Date date, AppointmentController ctrl) {
        this.selectedDate = date;
        this.ctrl         = ctrl;

        setTitle("Chi tiết cuộc hẹn");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(100, 100, 600, 450);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = UIStyle.gradientPanel();
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setLayout(new BorderLayout(0, 15));
        setContentPane(root);

        root.add(createHeader(),  BorderLayout.NORTH);
        root.add(createForm(),    BorderLayout.CENTER);
        root.add(createButtons(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel lbl = new JLabel("Chi tiết cuộc hẹn", SwingConstants.CENTER);
        lbl.setFont(UIStyle.TITLE_FONT); lbl.setForeground(new Color(50, 50, 50));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JPanel createForm() {
        JPanel container = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int s = 5;
                for (int i = 0; i < s; i++) { g2.setColor(new Color(0,0,0,(int)(255*0.1f*(s-i)/s))); g2.fill(new RoundRectangle2D.Double(i,i,getWidth()-2*i,getHeight()-2*i,15,15)); }
                g2.setColor(Color.WHITE); g2.fill(new RoundRectangle2D.Double(s,s,getWidth()-2*s,getHeight()-2*s,15,15)); g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill   = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; g.anchor=GridBagConstraints.EAST; g.gridwidth=1; g.weightx=0;
        form.add(label("Tên sự kiện:"), g);
        g.gridx=1; g.anchor=GridBagConstraints.WEST; g.weightx=1; g.gridwidth=3;
        txtName = UIStyle.styledTextField(); form.add(txtName, g);

        g.gridx=0; g.gridy=1; g.anchor=GridBagConstraints.EAST; g.gridwidth=1; g.weightx=0;
        form.add(label("Vị trí:"), g);
        g.gridx=1; g.anchor=GridBagConstraints.WEST; g.weightx=1; g.gridwidth=3;
        txtLocation = UIStyle.styledTextField(); form.add(txtLocation, g);

        g.gridx=0; g.gridy=2; g.anchor=GridBagConstraints.EAST; g.gridwidth=1; g.weightx=0;
        form.add(label("Thời gian bắt đầu:"), g);
        g.gridx=1; g.anchor=GridBagConstraints.WEST; g.weightx=0;
        cbbStart = UIStyle.styledCombo(HOURS); form.add(cbbStart, g);

        g.gridx=2; g.anchor=GridBagConstraints.EAST;
        form.add(label("Thời gian kết thúc:"), g);
        g.gridx=3; g.anchor=GridBagConstraints.WEST;
        cbbEnd = UIStyle.styledCombo(HOURS); form.add(cbbEnd, g);

        g.gridx=0; g.gridy=3; g.anchor=GridBagConstraints.EAST; g.gridwidth=1;
        form.add(label("Kiểu cuộc họp:"), g);
        g.gridx=1; g.anchor=GridBagConstraints.WEST; g.gridwidth=3;
        rdDon  = styledRadio("Đơn");
        rdNhom = styledRadio("Nhóm");
        ButtonGroup bg = new ButtonGroup(); bg.add(rdDon); bg.add(rdNhom); rdDon.setSelected(true);
        JPanel rp = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); rp.setOpaque(false);
        rp.add(rdDon); rp.add(rdNhom);
        form.add(rp, g);

        container.add(form, BorderLayout.CENTER);
        return container;
    }

    private JPanel createButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); p.setOpaque(false);

        JButton btnOk = UIStyle.primaryButton("Xác nhận");
        btnOk.setPreferredSize(new Dimension(150, 40));
        btnOk.addActionListener(e -> handleConfirm());

        JButton btnCancel = UIStyle.secondaryButton("Hủy bỏ");
        btnCancel.setPreferredSize(new Dimension(150, 40));
        btnCancel.addActionListener(e -> dispose());

        p.add(btnOk); p.add(btnCancel);
        return p;
    }

    private void handleConfirm() {
        String name     = txtName.getText().trim();
        String location = txtLocation.getText().trim();
        int start = Integer.parseInt((String) cbbStart.getSelectedItem());
        int end   = Integer.parseInt((String) cbbEnd.getSelectedItem());
        String type = rdNhom.isSelected() ? "Nhóm" : "Đơn";

        String err = ctrl.validate(name, location, start, end);
        if (err != null) { showMsg(err, "Thông báo"); return; }

        AppointmentController.AddStatus status = ctrl.checkBeforeAdd(name, selectedDate, start, end);

        switch (status) {

            case CONFLICT -> {
                
                showConflictDialog(name, location, start, end, type);
            }

            case GROUP_MATCH -> {
                
                if (confirm("Lịch hẹn này trùng với 1 group meeting.\nBạn có muốn tham gia không?", "Xác nhận")) {
                    ctrl.joinGroupMeeting(selectedDate, start, end, name, location);
                    showMsg("Đã tham gia group meeting thành công!", "Thông báo");
                    openMyCalendarAndClose();
                }
            }

            case SUCCESS -> {
                int newId = ctrl.addAppointment(name, location, selectedDate, start, end, type);
                if (newId == -1) { showMsg("Có lỗi khi thêm lịch hẹn, vui lòng thử lại!", "Lỗi"); return; }
                if (confirm("Bạn có muốn thêm bộ nhắc?", "Xác nhận")) {
                    new ReminderView(ctrl, newId).setVisible(true);
                    dispose();
                } else {
                    showMsg("Thêm lịch hẹn mới thành công!", "Thông báo");
                    openMyCalendarAndClose();
                }
            }
        }
    }

    private void showConflictDialog(String name, String location, int start, int end, String type) {
        JDialog d = new JDialog(this, "Xung đột lịch hẹn", true);
        d.setResizable(false);

        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        p.setBackground(Color.WHITE);

        JLabel msg = new JLabel("<html><center>Bạn đã có lịch hẹn trong khoảng thời gian này!<br>"
                + "Vui lòng chọn một trong các tùy chọn bên dưới.</center></html>", SwingConstants.CENTER);
        msg.setFont(UIStyle.MAIN_FONT);
        p.add(msg, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btns.setBackground(Color.WHITE);

        JButton btnChooseTime = UIStyle.primaryButton("Chọn giờ khác");
        btnChooseTime.setPreferredSize(new Dimension(150, 40));

        JButton btnReplace = UIStyle.styledButton("Thay thế", new Color(220, 100, 50), Color.WHITE);
        btnReplace.setPreferredSize(new Dimension(150, 40));

        JButton btnCancel = UIStyle.secondaryButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(100, 40));

        btns.add(btnChooseTime); btns.add(btnReplace); btns.add(btnCancel);
        p.add(btns, BorderLayout.CENTER);

        btnChooseTime.addActionListener(e -> d.dispose());

        btnReplace.addActionListener(e -> {
            d.dispose();
            int newId = ctrl.replaceConflict(selectedDate, start, end, name, location);
            if (newId > 0) {
                if (confirm("Bạn có muốn thêm bộ nhắc?", "Xác nhận")) {
                    new ReminderView(ctrl, newId).setVisible(true);
                    dispose();
                } else {
                    showMsg("Đã thay thế lịch hẹn thành công!", "Thông báo");
                    openMyCalendarAndClose();
                }
            } else {
                showMsg("Có lỗi khi thay thế lịch hẹn, vui lòng thử lại!", "Lỗi");
            }
        });

        btnCancel.addActionListener(e -> {
            d.dispose();
            dispose();
        });

        d.setContentPane(p);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void openMyCalendarAndClose() {
        new MyCalendarView(ctrl).setVisible(true);
        dispose();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text); l.setFont(UIStyle.BOLD_FONT); return l;
    }

    private JRadioButton styledRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(UIStyle.MAIN_FONT); rb.setForeground(new Color(50, 50, 50));
        rb.setOpaque(false); rb.setFocusPainted(false);
        rb.setIcon(new RadioIcon(false)); rb.setSelectedIcon(new RadioIcon(true));
        return rb;
    }

    private void showMsg(String msg, String title) {
        JPanel p = new JPanel(new GridBagLayout()); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints g = new GridBagConstraints(); g.gridx=0; g.insets=new Insets(0,0,20,0);
        JLabel l = new JLabel(msg); l.setFont(UIStyle.MAIN_FONT); p.add(l, g);
        JButton ok = UIStyle.primaryButton("OK"); ok.setPreferredSize(new Dimension(100,35));
        g.gridy=1; g.insets=new Insets(0,0,0,0); p.add(ok, g);
        JDialog d = new JDialog(this, title, true); d.setContentPane(p);
        ok.addActionListener(e2 -> d.dispose());
        d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
    }

    private boolean confirm(String msg, String title) {
        JPanel p = new JPanel(new BorderLayout()); p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel l = new JLabel("<html><center>" + msg.replace("\n","<br>") + "</center></html>", JLabel.CENTER);
        l.setFont(UIStyle.MAIN_FONT); l.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
        p.add(l, BorderLayout.NORTH);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        JButton yes = UIStyle.primaryButton("Có"); yes.setPreferredSize(new Dimension(100,35));
        JButton no  = UIStyle.secondaryButton("Không"); no.setPreferredSize(new Dimension(100,35));
        bp.add(yes); bp.add(no); p.add(bp, BorderLayout.CENTER);
        final boolean[] res = {false};
        JDialog d = new JDialog(this, title, true); d.setContentPane(p);
        yes.addActionListener(e -> { res[0]=true; d.dispose(); });
        no.addActionListener(e -> d.dispose());
        d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
        return res[0];
    }

    private class RadioIcon implements javax.swing.Icon {
        private final boolean selected;
        RadioIcon(boolean selected) { this.selected = selected; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? UIStyle.PRIMARY : new Color(180,180,180));
            g2.fillOval(x, y, 16, 16);
            if (selected) { g2.setColor(Color.WHITE); g2.fillOval(x+4, y+4, 8, 8); }
            g2.dispose();
        }
        @Override public int getIconWidth()  { return 16; }
        @Override public int getIconHeight() { return 16; }
    }
}
