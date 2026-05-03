package controller;

import dao.AppointmentDAO;
import model.Appointment;
import model.Reminder;
import model.Users;

import java.util.Date;
import java.util.List;

public class AppointmentController {

    public enum AddStatus { SUCCESS, GROUP_MATCH, CONFLICT, VALIDATION_ERROR }

    private final AppointmentDAO dao = new AppointmentDAO();
    private int currentUserId = 1;

    public void setCurrentUserId(int id) { this.currentUserId = id; }
    public int  getCurrentUserId()       { return currentUserId; }

    public String validate(String name, String location, int start, int end) {
        if (name == null || name.trim().isEmpty())         return "Vui lòng điền đủ thông tin!";
        if (location == null || location.trim().isEmpty()) return "Vui lòng điền đủ thông tin!";
        if (start >= end) return "Giờ bắt đầu phải bé hơn giờ kết thúc!";
        return null;
    }

    public AddStatus checkBeforeAdd(String name, Date date, int start, int end) {

        if (dao.getExistGroupAppointment(name, date, start, end) != null)
            return AddStatus.GROUP_MATCH;

        if (dao.getUserOverlappingAppointment(currentUserId, date, start, end) != null)
            return AddStatus.CONFLICT;

        return AddStatus.SUCCESS;
    }

    public int addAppointment(String name, String location, Date date,
                               int start, int end, String type) {
        return dao.insertAppointment(currentUserId, name, location, date, start, end, type);
    }

    public int joinGroupMeeting(Date date, int start, int end, String name, String location) {
        return dao.insertMemberGroupAppointment(currentUserId, date, start, end, name, location);
    }

    public int replaceConflict(Date date, int start, int end, String name, String location) {
        return dao.updateAppointment(currentUserId, date, start, end, name, location);
    }

    public int updateFull(int appId, String name, String location,
                           Date date, int start, int end, String type) {
        return dao.updateFull(appId, name, location, date, start, end, type);
    }

    public boolean deleteAppointment(int appId) {
        return dao.deleteAppointment(appId);
    }

    public List<Appointment> getAllAppointments() {
        return dao.getAllAppointments();
    }

    public List<Reminder> getAllReminders()              { return dao.getAllReminders(); }
    public int addReminder(int appId, String title)      { return dao.insertTakeReminder(appId, title); }

    public List<Users>   getParticipants(int appId)      { return dao.getParticipants(appId); }
    public List<Integer> getReminderIds(int appId)       { return dao.getReminderIdsByAppointment(appId); }
}
