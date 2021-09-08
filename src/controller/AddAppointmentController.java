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
 * This class is the controller for adding appointments to the database.
 */
public class AddAppointmentController implements Initializable {

    /**
     * Initialize text fields for user input.
     */
    public TextField apptIdField;
    public TextField apptStartDateTimeField;
    public TextField apptTitleField;
    public TextField apptEndDateTimeField;
    public TextField apptDescField;
    public TextField apptLocationField;
    public ComboBox<Contact> contactComboBox;
    public TextField apptCustIdField;
    public TextField apptTypeField;
    public TextField apptUserIdField;
    public ObservableList<Contact> allContacts = FXCollections.observableArrayList();

    /**
     * @param url            takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
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

            String selectAppointmentsStatement = "SELECT Start, End FROM appointments";

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

                if (startDateTime.equals(startDateTimeUTC) || startDateTime.isAfter(startDateTimeUTC) && startDateTime.isBefore(endDateTimeUTC)) {
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
     * @param actionEvent clicking the save button is the action event for this event handler
     */
    public void addAppointment(ActionEvent actionEvent) {

        try {
            Connection connect = DatabaseConnection.getConnection();
            String appointmentInsertStatement = "INSERT INTO appointments(Title, Description, Location, Type, Start, " +
                    "End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

            DatabaseQuery.setPreparedStatement(connect, appointmentInsertStatement);
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

            ZonedDateTime zonedStartDateTime = ZonedDateTime.of(startDate, startTime, zoneId);
            ZonedDateTime zonedEndDateTime = ZonedDateTime.of(endDate, endTime, zoneId);

            ZonedDateTime zonedStartDateTimeEST = ZonedDateTime.of(startDate, startTime, zoneIdEST);
            ZonedDateTime zonedEndDateTimeEST = ZonedDateTime.of(endDate, endTime, zoneIdEST);

            ZonedDateTime zonedCreateDateTime = ZonedDateTime.now(zoneId);

            Instant startDateTimeInstant = zonedStartDateTime.toInstant();
            Instant endDateTimeInstant = zonedEndDateTime.toInstant();
            Instant createDateTimeInstant = zonedCreateDateTime.toInstant();

            DateTimeFormatter instantDateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneIdUTC);

            String formattedStartInstant = instantDateTimeFormatter.format(startDateTimeInstant);
            String formattedEndInstant = instantDateTimeFormatter.format(endDateTimeInstant);

            String createdBy = User.getInstance().getUsername();
            String lastUpdatedBy = User.getInstance().getUsername();
            int customerId = Integer.parseInt(apptCustIdField.getText());
            int userId = Integer.parseInt(apptUserIdField.getText());
            int contactId = contactComboBox.getSelectionModel().getSelectedItem().getContactId();

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
                        preparedStatement.setTimestamp(7, Timestamp.from(createDateTimeInstant));
                        preparedStatement.setString(8, createdBy);
                        preparedStatement.setTimestamp(9, Timestamp.from(createDateTimeInstant));
                        preparedStatement.setString(10, lastUpdatedBy);
                        preparedStatement.setInt(11, customerId);
                        preparedStatement.setInt(12, userId);
                        preparedStatement.setInt(13, contactId);

                        preparedStatement.execute();

                        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
                        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                        Scene scene = new Scene(root, 800, 550);
                        stage.setTitle("Home");
                        stage.setScene(scene);
                        stage.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Add Appointment");
                        alert.setContentText("Appointment is not within business hours.");
                        alert.showAndWait();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Add Appointment");
                    alert.setContentText("Appointment times overlap. Please change your times.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Add Appointment");
                alert.setContentText("Please input data in all fields to save an appointment.");
                alert.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add Appointment");
            alert.setContentText("Your input data is incorrect or there is a problem with the database.");
            alert.showAndWait();
        }

    }

    /**
     * @param actionEvent clicking the cancel button is the action event for this event handler
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
     * @param actionEvent an IO exception is thrown when there is an internal problem with the event handler
     */
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onSelectContact(ActionEvent actionEvent) {
    }
}