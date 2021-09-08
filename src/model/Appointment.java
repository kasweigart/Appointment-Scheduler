package model;

import java.time.LocalDateTime;

/**
 * This class is the model for an appointment.
 */
public class Appointment {


    /**
     * ten private members are used to create an appointment object
     */
    private int appointmentId;
    private int apptCustomerId;
    private int userId;
    private String title;
    private String contactName;
    private String description;
    private String location;
    private String appointmentType;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;


    /**
     * @param appointmentId unique ID of an appointment
     * @param apptCustomerId customer ID associated with an appointment
     * @param userId user ID associated with an appointment
     * @param title title of an appointment
     * @param contactName contact name associated with an appointment
     * @param description description of an appointment
     * @param location location of an appointment
     * @param appointmentType type of appointment
     * @param startDateTime start date and time of an appointment
     * @param endDateTime end date and time of an appointment
     */
    public Appointment(int appointmentId, int apptCustomerId, int userId, String title, String contactName,
                       String description,
                       String location, String appointmentType, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.appointmentId = appointmentId;
        this.apptCustomerId = apptCustomerId;
        this.userId = userId;
        this.title = title;
        this.contactName = contactName;
        this.description = description;
        this.location = location;
        this.appointmentType = appointmentType;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    /**
     * @return returns the appointment ID
     */
    public int getAppointmentId() {
        return appointmentId;
    }

    /**
     * @return returns the customer ID associated with an appointment
     */
    public int getApptCustomerId() {
        return apptCustomerId;
    }

    /**
     * @return returns the user ID associated with an appointment
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return returns the title of an appointment
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title sets the title of an appointment
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return returns the contact name associated with an appointment
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * @return returns the description of an appointment
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return returns the location of an appointment
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location sets the location of an appointment
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return returns the type of appointment
     */
    public String getAppointmentType() {
        return appointmentType;
    }

    /**
     * @return returns the start date and time of an appointment
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * @return returns the end date and time of an appointment
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}
