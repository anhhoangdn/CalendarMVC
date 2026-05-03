package dao;

import model.Appointment;
import model.Reminder;
import model.Users;
import util.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class AppointmentDAO {

    public Appointment getExistGroupAppointment(String name, Date date, int start, int end) {
        String sql = "SELECT * FROM appointment WHERE name=? AND type_appointment='Nhóm' " +
                     "AND meeting_date=? AND start_hour=? AND end_hour=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setInt(3, start); ps.setInt(4, end);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Appointment getUserExistAppointment(int userId, Date date, int start, int end) {
        String sql = "SELECT a.* FROM appointment a JOIN take t ON a.id=t.appointment_id " +
                     "WHERE t.user_id=? AND a.meeting_date=? " +
                     "AND a.start_hour < ? AND a.end_hour > ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setInt(3, end);
            ps.setInt(4, start);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int insertAppointment(int userId, String name, String location,
                                  Date date, int start, int end, String type) {
        String sql = "INSERT INTO appointment(name,location,meeting_date,start_hour,end_hour,type_appointment) " +
                     "VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, location);
            ps.setDate(3, new java.sql.Date(date.getTime()));
            ps.setInt(4, start); ps.setInt(5, end); ps.setString(6, type);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                linkUser(c, userId, newId);
                return newId;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public int insertMemberGroupAppointment(int userId, Date date, int start, int end,
                                             String name, String location) {
        Appointment gm = getExistGroupAppointment(name, date, start, end);
        if (gm == null) return 0;
        String sql = "INSERT IGNORE INTO take(user_id,appointment_id) VALUES(?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, gm.getId());
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int updateAppointment(int userId, Date date, int start, int end,
                                  String name, String location) {
        Appointment old = getUserExistAppointment(userId, date, start, end);
        if (old == null) return 0;
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                exec(c, "DELETE FROM take_rmd WHERE appointment_id=?", old.getId());
                exec(c, "DELETE FROM take WHERE appointment_id=?", old.getId());
                exec(c, "DELETE FROM appointment WHERE id=?", old.getId());

                String insertSql = "INSERT INTO appointment(name,location,meeting_date,start_hour,end_hour,type_appointment) " +
                                   "VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, location);
                    ps.setDate(3, new java.sql.Date(date.getTime()));
                    ps.setInt(4, start);
                    ps.setInt(5, end);
                    ps.setString(6, old.getTypeAppointment());
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        linkUser(c, userId, newId);
                        c.commit();
                        return newId;
                    }
                }
                c.rollback();
                return 0;
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
                return 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int updateFull(int appId, String name, String location,
                           Date date, int start, int end, String type) {
        String sql = "UPDATE appointment SET name=?,location=?,meeting_date=?," +
                     "start_hour=?,end_hour=?,type_appointment=? WHERE id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, location);
            ps.setDate(3, new java.sql.Date(date.getTime()));
            ps.setInt(4, start); ps.setInt(5, end); ps.setString(6, type); ps.setInt(7, appId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public boolean deleteAppointment(int appId) {
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                exec(c, "DELETE FROM take_rmd WHERE appointment_id=?", appId);
                exec(c, "DELETE FROM take WHERE appointment_id=?", appId);
                exec(c, "DELETE FROM appointment WHERE id=?", appId);
                c.commit(); return true;
            } catch (SQLException e) { c.rollback(); e.printStackTrace(); return false; }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointment ORDER BY meeting_date, start_hour";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM reminder");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Reminder(rs.getInt("id"), rs.getString("title")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int insertTakeReminder(int appId, String remTitle) {
        String findRem = "SELECT id FROM reminder WHERE title=?";
        String insert  = "INSERT INTO take_rmd(appointment_id,reminder_id) VALUES(?,?)";
        try (Connection c = DBConnection.get()) {
            PreparedStatement ps1 = c.prepareStatement(findRem);
            ps1.setString(1, remTitle);
            ResultSet rs1 = ps1.executeQuery();
            if (!rs1.next()) return 0;
            int remId = rs1.getInt("id");

            PreparedStatement ps2 = c.prepareStatement(insert);
            ps2.setInt(1, appId); ps2.setInt(2, remId);
            return ps2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public List<Users> getParticipants(int appointmentId) {
        List<Users> list = new ArrayList<>();
        String sql = "SELECT u.* FROM users u JOIN take t ON u.id=t.user_id WHERE t.appointment_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new Users(rs.getInt("id"), rs.getString("name"), rs.getString("phone_number")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Integer> getReminderIdsByAppointment(int appId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT reminder_id FROM take_rmd WHERE appointment_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rs.getInt("reminder_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Appointment getUserOverlappingAppointment(int userId, Date date, int start, int end) {
        String sql = "SELECT a.* FROM appointment a JOIN take t ON a.id=t.appointment_id " +
                     "WHERE t.user_id=? AND a.meeting_date=? " +
                     "AND a.start_hour < ? AND a.end_hour > ?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setInt(3, end);
            ps.setInt(4, start);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Appointment map(ResultSet rs) throws SQLException {
        return new Appointment(rs.getInt("id"), rs.getString("name"), rs.getString("location"),
            rs.getDate("meeting_date"), rs.getInt("start_hour"), rs.getInt("end_hour"),
            rs.getString("type_appointment"));
    }

    private void linkUser(Connection c, int userId, int appId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO take(user_id,appointment_id) VALUES(?,?)")) {
            ps.setInt(1, userId); ps.setInt(2, appId); ps.executeUpdate();
        }
    }

    private void exec(Connection c, String sql, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
}
