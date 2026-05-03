package view;

import controller.AppointmentController;
import model.Appointment;
import util.UIStyle;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.*;
import com.toedter.calendar.JCalendar;

public class CalendarUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppointmentController ctrl;
    private JPanel contentPane;
    private Set<String> appointmentDates = new HashSet<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        EventQueue.invokeLater(() -> new CalendarUI(new AppointmentController()).setVisible(true));
    }

    public CalendarUI(AppointmentController ctrl) {
        this.ctrl = ctrl;
        loadAppointmentDates();
        setTitle("Lịch hẹn");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        contentPane = UIStyle.gradientPanel();
        contentPane.setBorder(new EmptyBorder(15,15,15,15));
        contentPane.setLayout(new BorderLayout(0,20));
        setContentPane(contentPane);

        contentPane.add(createHeaderPanel(),   BorderLayout.NORTH);
        contentPane.add(createCalendarPanel(), BorderLayout.CENTER);
        contentPane.add(createButtonPanel(),   BorderLayout.SOUTH);
    }

    private void loadAppointmentDates() {
        appointmentDates.clear();
        List<Appointment> list = ctrl.getAllAppointments();
        for (Appointment a : list) {
            if (a.getMeetingDate() != null)
                appointmentDates.add(sdf.format(a.getMeetingDate()));
        }
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lbl = new JLabel("Lịch hẹn của bạn");
        lbl.setFont(UIStyle.HDR_FONT);
        lbl.setForeground(new Color(50,50,50));
        header.add(lbl, BorderLayout.WEST);

        URL u = getClass().getClassLoader().getResource("icons/calendar.png");
        JButton btnMy = UIStyle.primaryButton("Lịch của tôi");
        btnMy.setIcon(u != null ? new ImageIcon(u) : null);
        btnMy.addActionListener(e -> new MyCalendarView(ctrl).setVisible(true));
        header.add(btnMy, BorderLayout.EAST);
        return header;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel cont = UIStyle.cardPanel();
        cont.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        cont.setLayout(new BorderLayout());

        JCalendar calendar = new JCalendar(new java.util.Locale("vi", "VN"));
        calendar.setFont(UIStyle.MAIN_FONT);
        calendar.setWeekOfYearVisible(false);
        calendar.setDecorationBackgroundVisible(false);
        calendar.setDecorationBordersVisible(false);
        calendar.setSundayForeground(new Color(220,50,50));
        calendar.setWeekdayForeground(new Color(50,50,50));
        styleMonthYearChooser(calendar);
        styleDayButtons(calendar);
        calendar.addPropertyChangeListener("calendar", evt -> styleDayButtons(calendar));

        cont.add(calendar, BorderLayout.CENTER);
        panel.add(cont, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));
        btns.setOpaque(false);
        JButton ok = UIStyle.primaryButton("Thêm cuộc hẹn");
        ok.setPreferredSize(new Dimension(150,40));
        ok.addActionListener(e -> new AddAppointmentView(calendar.getDate(), ctrl).setVisible(true));

        JButton cancel = UIStyle.secondaryButton("Đóng");
        cancel.setPreferredSize(new Dimension(150,40));
        cancel.addActionListener(e -> dispose());

        btns.add(ok); btns.add(cancel);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createButtonPanel() { return new JPanel(); }

    private void styleMonthYearChooser(JCalendar calendar) {
        Component monthComp = calendar.getMonthChooser().getComboBox();
        if (monthComp instanceof JComboBox) {
            @SuppressWarnings("unchecked")
            JComboBox<String> combo = (JComboBox<String>) monthComp;
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            combo.setBackground(Color.WHITE);
            combo.setForeground(UIStyle.PRIMARY);
            combo.setPreferredSize(new Dimension(130, 32));
        }
        Component yearComp = calendar.getYearChooser().getSpinner();
        if (yearComp instanceof JSpinner) {
            JSpinner spinner = (JSpinner) yearComp;
            spinner.setFont(new Font("Segoe UI", Font.BOLD, 16));
            spinner.setPreferredSize(new Dimension(90, 32));
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setForeground(UIStyle.PRIMARY);
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(new Font("Segoe UI", Font.BOLD, 16));
            }
        }
    }

    private void styleDayButtons(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();
        Calendar cal = calendar.getCalendar();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear  = cal.get(Calendar.YEAR);

        for (Component c : dayPanel.getComponents()) {
            if (!(c instanceof JButton)) continue;
            JButton b = (JButton) c;
            b.setFont(UIStyle.MAIN_FONT);
            b.setOpaque(false); b.setContentAreaFilled(false);
            b.setBorderPainted(false); b.setFocusPainted(false);

            boolean hasAppointment = false;
            try {
                int day = Integer.parseInt(b.getText());
                Calendar tmp = Calendar.getInstance();
                tmp.set(currentYear, currentMonth, day);
                String dateStr = sdf.format(tmp.getTime());
                hasAppointment = appointmentDates.contains(dateStr);
            } catch (NumberFormatException ignored) {}

            final boolean hasApp = hasAppointment;

            b.setUI(new BasicButtonUI() {
                @Override public void paint(Graphics g, JComponent c) {
                    AbstractButton btn = (AbstractButton) c;
                    ButtonModel m = btn.getModel();
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int sz = Math.min(c.getWidth()-6, c.getHeight()-6);
                    int x=(c.getWidth()-sz)/2, y=(c.getHeight()-sz)/2;
                    if (m.isSelected()) {
                        g2.setPaint(new GradientPaint(0,0,UIStyle.PRIMARY,0,c.getHeight(),UIStyle.PRIMARY.darker()));
                        g2.fillOval(x,y,sz,sz);
                        g2.setColor(new Color(255,255,255,60)); g2.fillArc(x,y,sz,sz/2,0,180);
                    } else if (hasApp) {
                        g2.setColor(new Color(255, 80, 80, 180));
                        g2.fillOval(x,y,sz,sz);
                    } else if (m.isPressed()) {
                        g2.setColor(new Color(180,210,255)); g2.fillOval(x,y,sz,sz);
                    } else if (m.isRollover()) {
                        g2.setColor(new Color(230,240,255)); g2.fillOval(x,y,sz,sz);
                    }
                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle vr = new Rectangle(0,0,c.getWidth(),c.getHeight()), tr = new Rectangle();
                    String text = SwingUtilities.layoutCompoundLabel(fm,btn.getText(),null,
                        btn.getVerticalAlignment(),btn.getHorizontalAlignment(),
                        btn.getVerticalTextPosition(),btn.getHorizontalTextPosition(),vr,new Rectangle(),tr,0);
                    g2.setFont(m.isSelected()?new Font("Segoe UI",Font.BOLD,14):btn.getFont());
                    g2.setColor(m.isSelected() ? Color.WHITE : (hasApp && !m.isSelected()) ? Color.WHITE : Color.DARK_GRAY);
                    g2.drawString(text, tr.x, tr.y+fm.getAscent());
                    g2.dispose();
                }
            });
            for (MouseListener ml : b.getMouseListeners())
                if (ml instanceof DayButtonMouseListener) b.removeMouseListener(ml);
            b.addMouseListener(new DayButtonMouseListener(calendar));
        }
        dayPanel.revalidate(); dayPanel.repaint();
    }

    private class DayButtonMouseListener extends MouseAdapter {
        private final JCalendar cal;
        DayButtonMouseListener(JCalendar cal){ this.cal=cal; }
        @Override public void mouseEntered(MouseEvent e){((JButton)e.getComponent()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
        @Override public void mouseExited(MouseEvent e){((JButton)e.getComponent()).setCursor(Cursor.getDefaultCursor());}
        @Override public void mouseReleased(MouseEvent e){
            try{
                int d=Integer.parseInt(((JButton)e.getComponent()).getText());
                Calendar c=cal.getCalendar(); c.set(Calendar.DAY_OF_MONTH,d); cal.setCalendar(c);
                java.util.Date clickedDate = c.getTime();
                String dateStr = sdf.format(clickedDate);
                if (appointmentDates.contains(dateStr)) {
                    new MyCalendarView(ctrl, clickedDate).setVisible(true);
                }
            }catch(NumberFormatException ignored){}
        }
    }
}
