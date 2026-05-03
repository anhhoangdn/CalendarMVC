import controller.AppointmentController;
import view.CalendarUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            AppointmentController ctrl = new AppointmentController();
            // userId mặc định = 1 để tiện test, muốn đổi user thì gọi ctrl.setCurrentUserId(id) sau khi có id
            // Muốn có màn hình chọn user thì thêm LoginView ở đây
            new CalendarUI(ctrl).setVisible(true);
        });
    }
}
