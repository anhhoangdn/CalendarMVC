package model;

import java.util.Date;

public class Appointment {

    private int id;
    private String name;
    private String location;
    private Date meetingDate;
    private int startHour;
    private int endHour;
    private String typeAppointment;

    public Appointment() {}

    public Appointment(int id, String name, String location, Date meetingDate,
                       int startHour, int endHour, String typeAppointment) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.meetingDate = meetingDate;
        this.startHour = startHour;
        this.endHour = endHour;
        this.typeAppointment = typeAppointment;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getMeetingDate() { return meetingDate; }
    public void setMeetingDate(Date meetingDate) { this.meetingDate = meetingDate; }

    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }

    public int getEndHour() { return endHour; }
    public void setEndHour(int endHour) { this.endHour = endHour; }

    public String getTypeAppointment() { return typeAppointment; }
    public void setTypeAppointment(String typeAppointment) { this.typeAppointment = typeAppointment; }
}