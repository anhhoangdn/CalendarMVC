import controller.AppointmentController;
import util.ReminderScheduler;
import view.CalendarUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        ReminderScheduler.start();

        SwingUtilities.invokeLater(() -> {
            AppointmentController ctrl = new AppointmentController();
            new CalendarUI(ctrl).setVisible(true);
        });
    }
}
