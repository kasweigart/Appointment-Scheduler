package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Appointment;
import model.Contact;
import model.User;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * This class is the controller for updating appointments.
 */
public class UpdateAppointmentController implements Initializable {

    private static Appointment appointmentData = null;
    /**
     * text fields and combo boxes for editing data of an appointment
     * observable list for holding the current contacts
     * static appointment data for sending information from one scene to another
     */
    public TextField apptIdField;
    public TextField apptStartDateTimeField;
    public TextField apptTitleField;
    public TextField apptEndDateTimeField;
    public TextField apptDescField;
    public TextField apptLocationField;
    public TextField apptCustIdField;
    public TextField apptTypeField;
    public TextField apptUserIdField;
    public ComboBox<Contact> contactComboBox;
    public ObservableList<Contact> allContacts = FXCollections.observableArrayList();

    /**
     * @param selectedAppointment takes in an appointment and sets it to a static variable
     */
    public static void receiveAppointment(Appointment selectedAppointment) {
        appointmentData = selectedAppointment;
    }

    /**
     * @param url            clicking the cancel button is the action event for this event handler
     * @param resourceBundle an IO exception is thrown when there is an internal problem with the event handler
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            Connection connect = DatabaseConnection.getConnection();
            String contactsSelectStatement = "SELECT Contact_ID, Contact_Name, Email FROM contacts";

            DatabaseQuery.setPreparedStatement(connect, contactsSelectStatement);
            PreparedStatement contactsPreparedStatement = DatabaseQuery.getPreparedStatement();
            contactsPreparedStatement.execute(contactsSelectStatement);
            ResultSet contactsResultSet = contactsPreparedStatement.getResultSet();

            while (contactsResultSet.next()) {
                int contactId = contactsResultSet.getInt("Contact_ID");
                String contactName = contactsResultSet.getString("Contact_Name");
                String email = contactsResultSet.getString("Email");

                Contact contact = new Contact(contactId, contactName, email);
                allContacts.add(contact);
            }
            contactComboBox.setItems(allContacts);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        apptIdField.setText(String.valueOf(appointmentData.getAppointmentId()));
        apptTitleField.setText(appointmentData.getTitle());
        apptDescField.setText(appointmentData.getDescription());
        apptLocationField.setText(appointmentData.getLocation());

        for (Contact contact : contactComboBox.getItems()) {
            if (contact.getContactName().equals(appointmentData.getContactName())) {
                contactComboBox.setPromptText(contact.getContactName());
            }
        }

        apptTypeField.setText(appointmentData.getAppointmentType());
        apptStartDateTimeField.setText(String.valueOf(appointmentData.getStartDateTime()).replace("T", " "));
        apptEndDateTimeField.setText(String.valueOf(appointmentData.getEndDateTime()).replace("T", " "));
        apptCustIdField.setText(String.valueOf(appointmentData.getApptCustomerId()));
        apptUserIdField.setText(String.valueOf(appointmentData.getUserId()));

        System.out.println("Initialized");
    }

    /**
     * @param startDateTime starting time of an appointment as first parameter
     * @param endDateTime   ending time of an appointment as second parameter
     * @return returns true or false depending on whether there is a collision with an appointment
     */
    public boolean getAppointmentCollision(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        boolean found = false;

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectAppointmentsStatement = "SELECT Start, End FROM appointments WHERE Appointment_ID != " +
                    appointmentData.getAppointmentId();

            DatabaseQuery.setPreparedStatement(connect, selectAppointmentsStatement);
            PreparedStatement appointmentsPreparedStatement = DatabaseQuery.getPreparedStatement();
            appointmentsPreparedStatement.execute(selectAppointmentsStatement);
            ResultSet appointmentsResultSet = appointmentsPreparedStatement.getResultSet();

            while (appointmentsResultSet.next()) {

                LocalDateTime selectStartDateTime = appointmentsResultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime selectEndDateTime = appointmentsResultSet.getTimestamp("End").toLocalDateTime();

                LocalDate startDate = selectStartDateTime.toLocalDate();
                LocalTime startTime = selectStartDateTime.toLocalTime();
                LocalDate endDate = selectEndDateTime.toLocalDate();
                LocalTime endTime = selectEndDateTime.toLocalTime();

                ZoneId zoneIdUTC = ZoneId.of("UTC");

                ZonedDateTime zonedStartDateTimeUTC = ZonedDateTime.of(startDate, startTime, zoneIdUTC);
                ZonedDateTime zonedEndDateTimeUTC = ZonedDateTime.of(endDate, endTime, zoneIdUTC);

                LocalDateTime startDateTimeUTC = zonedStartDateTimeUTC.toLocalDateTime();
                LocalDateTime endDateTimeUTC = zonedEndDateTimeUTC.toLocalDateTime();

                if (startDateTime.equals(startDateTimeUTC) || (startDateTime.isAfter(startDateTimeUTC) && startDateTime.isBefore(endDateTimeUTC))) {
                    found = true;
                    break;
                } else if (endDateTime.isAfter(startDateTimeUTC) && endDateTime.isBefore(endDateTimeUTC)) {
                    found = true;
                    break;
                } else {
                    found = false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return found;
    }

    /**
     * @param startDateTime starting time of an appointment as the first parameter
     * @param endDateTime   ending time of an appointment as the second parameter
     * @return returns true or false depending on whether the times are within business hours
     */
    public boolean isWithinBusinessHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        boolean withinBusinessHours = false;

        ZoneId zoneIdEST = ZoneId.of("-05:00");

        LocalTime openBusinessTime = LocalTime.of(7, 59);
        LocalTime closeBusinessTime = LocalTime.of(20, 0);

        LocalTime openTimeEST = LocalDateTime.of(LocalDate.now(), openBusinessTime).atZone(zoneIdEST).toLocalTime();
        LocalTime closeTimeEST = LocalDateTime.of(LocalDate.now(), closeBusinessTime).atZone(zoneIdEST).toLocalTime();

        if (startDateTime.toLocalTime().isAfter(openTimeEST) && startDateTime.toLocalTime().isBefore(closeTimeEST) &&
                endDateTime.toLocalTime().isAfter(openTimeEST) && endDateTime.toLocalTime().isBefore(closeTimeEST)) {
            withinBusinessHours = true;
        }

        return withinBusinessHours;
    }

    /**
     * @param actionEvent clicking the 'Save' button is the action event
     */
    public void addAppointment(ActionEvent actionEvent) {

        try {
            Connection connect = DatabaseConnection.getConnection();
            String appointmentUpdateStatement = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, " +
                    "Type" +
                    " " +
                    "=" +
                    " " +
                    "?, Start = ?, End = ?," +
                    "Last_Update = ?, Last_Updated_By = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ? WHERE " +
                    "Appointment_ID = " + appointmentData.getAppointmentId();


            DatabaseQuery.setPreparedStatement(connect, appointmentUpdateStatement);
            PreparedStatement preparedStatement = DatabaseQuery.getPreparedStatement();

            String title = apptTitleField.getText();
            String description = apptDescField.getText();
            String location = apptLocationField.getText();
            String type = apptTypeField.getText();
            String startDateTimeString = apptStartDateTimeField.getText();
            String endDateTimeString = apptEndDateTimeField.getText();

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime startLocalDateTime = LocalDateTime.parse(startDateTimeString, dateTimeFormatter);
            LocalDateTime endLocalDateTime = LocalDateTime.parse(endDateTimeString, dateTimeFormatter);

            LocalDate startDate = startLocalDateTime.toLocalDate();
            LocalTime startTime = startLocalDateTime.toLocalTime();
            LocalDate endDate = endLocalDateTime.toLocalDate();
            LocalTime endTime = endLocalDateTime.toLocalTime();

            ZoneId zoneId = ZoneId.systemDefault();
            ZoneId zoneIdUTC = ZoneId.of("UTC");
            ZoneId zoneIdEST = ZoneId.of("-05:00");

            ZonedDateTime zonedStartDateTimeEST = ZonedDateTime.of(startDate, startTime, zoneIdEST);
            ZonedDateTime zonedEndDateTimeEST = ZonedDateTime.of(endDate, endTime, zoneIdEST);

            ZonedDateTime zonedStartDateTime = ZonedDateTime.of(startDate, startTime, zoneId);
            ZonedDateTime zonedEndDateTime = ZonedDateTime.of(endDate, endTime, zoneId);
            ZonedDateTime zonedCreateDateTime = ZonedDateTime.now(zoneId);

            Instant startDateTimeInstant = zonedStartDateTime.toInstant();
            Instant endDateTimeInstant = zonedEndDateTime.toInstant();
            Instant lastUpdateDateTimeInstant = zonedCreateDateTime.toInstant();

            DateTimeFormatter instantDateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneIdUTC);

            String formattedStartInstant = instantDateTimeFormatter.format(startDateTimeInstant);
            String formattedEndInstant = instantDateTimeFormatter.format(endDateTimeInstant);

            String lastUpdatedBy = User.getInstance().getUsername();
            int customerId = Integer.parseInt(apptCustIdField.getText());
            int userId = Integer.parseInt(apptUserIdField.getText());
            int contactId = 0;

            for (Contact contact : allContacts) {
                if (contact.getContactName().equals(contactComboBox.getPromptText())) {
                    contactId = contact.getContactId();
                }
            }

            if (title.length() > 0 && description.length() > 0 && location.length() > 0 && type.length() > 0 &&
                    startDateTimeString.length() > 0 && endDateTimeString.length() > 0 && customerId != 0 &&
                    userId != 0 && contactId != 0) {

                if (!(getAppointmentCollision(LocalDateTime.ofInstant(startDateTimeInstant, ZoneOffset.UTC),
                        LocalDateTime.ofInstant(endDateTimeInstant, ZoneOffset.UTC)))) {

                    if (isWithinBusinessHours(zonedStartDateTimeEST.toLocalDateTime(),
                            zonedEndDateTimeEST.toLocalDateTime())) {

                        preparedStatement.setString(1, title);
                        preparedStatement.setString(2, description);
                        preparedStatement.setString(3, location);
                        preparedStatement.setString(4, type);
                        preparedStatement.setTimestamp(5, Timestamp.valueOf(formattedStartInstant));
                        preparedStatement.setTimestamp(6, Timestamp.valueOf(formattedEndInstant));
                        preparedStatement.setTimestamp(7, Timestamp.from(lastUpdateDateTimeInstant));
                        preparedStatement.setString(8, lastUpdatedBy);
                        preparedStatement.setInt(9, customerId);
                        preparedStatement.setInt(10, userId);
                        preparedStatement.setInt(11, contactId);

                        preparedStatement.execute();

                        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
                        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                        Scene scene = new Scene(root, 800, 550);
                        stage.setTitle("Home");
                        stage.setScene(scene);
                        stage.show();

                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Update Appointment");
                        alert.setContentText("Appointment is not within business hours.");
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Update Appointment");
                    alert.setContentText("Appointment times overlap. Please change your times.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Update Appointment");
                alert.setContentText("Please input data in all fields to save an appointment.");
                alert.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Appointment");
            alert.setContentText("Your input data is incorrect or there is a problem with the database.");
            alert.showAndWait();
        }

    }

    /**
     * @param actionEvent clicking the 'Cancel' button is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void cancelAddAppt(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 550);
        stage.setTitle("Home");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the 'Exit' menu item is the action event
     */
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}