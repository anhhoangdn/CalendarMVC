package util;

import dao.AppointmentDAO;
import model.Appointment;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class ReminderScheduler {

    private static final Map<String, Boolean> fired = new ConcurrentHashMap<>();
    private static ScheduledExecutorService executor;

    public static void start() {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "reminder-thread");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(ReminderScheduler::check, 0, 1, TimeUnit.MINUTES);
    }

    public static void stop() {
        if (executor != null) executor.shutdownNow();
    }

    private static void check() {
        AppointmentDAO dao = new AppointmentDAO();
        List<Appointment> appointments = dao.getAllAppointments();
        Calendar now = Calendar.getInstance();
        int nowH = now.get(Calendar.HOUR_OF_DAY);
        int nowM = now.get(Calendar.MINUTE);

        String today = String.format("%tF", now);

        for (Appointment a : appointments) {
            if (a.getMeetingDate() == null) continue;
            String aDate = String.format("%tF", a.getMeetingDate());

            List<Integer> remIds = dao.getReminderIdsByAppointment(a.getId());

            for (int remId : remIds) {
                int minutesBefore = switch (remId) {
                    case 1 -> 15;
                    case 2 -> 30;
                    case 3 -> 24 * 60;
                    default -> -1;
                };
                if (minutesBefore < 0) continue;

                Calendar target = Calendar.getInstance();
                target.setTime(a.getMeetingDate());
                target.set(Calendar.HOUR_OF_DAY, a.getStartHour());
                target.set(Calendar.MINUTE, 0);
                target.set(Calendar.SECOND, 0);
                target.add(Calendar.MINUTE, -minutesBefore);

                String targetDate = String.format("%tF", target);
                int targetH = target.get(Calendar.HOUR_OF_DAY);
                int targetM = target.get(Calendar.MINUTE);

                String key = a.getId() + "-" + remId;

                if (today.equals(targetDate) && nowH == targetH && nowM == targetM && !fired.getOrDefault(key, false)) {
                    fired.put(key, true);
                    String label = switch (remId) {
                        case 1 -> "15 phút";
                        case 2 -> "30 phút";
                        case 3 -> "1 ngày";
                        default -> "";
                    };
                    String msg = "<html><b>" + a.getName() + "</b><br>sẽ diễn ra sau <b>" + label + "</b><br>tại " + a.getLocation() + "</html>";
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null, msg, "Nhắc nhở lịch hẹn", JOptionPane.INFORMATION_MESSAGE)
                    );
                }

                Calendar midnight = Calendar.getInstance();
                midnight.set(Calendar.HOUR_OF_DAY, 0);
                midnight.set(Calendar.MINUTE, 0);
                if (now.before(midnight)) fired.remove(key);
            }
        }
    }
}
