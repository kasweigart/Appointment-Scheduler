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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Contact;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.ResourceBundle;


/**
 * This class is the controller for the contact schedule report.
 */
public class ContactScheduleController implements Initializable {

    /**
     * table view and columns for displaying the appointments for each contact
     */
    public TableView<Appointment> appointmentsTableView;
    public TableColumn<Appointment, Integer> apptIDColumn;
    public TableColumn<Appointment, String> apptTitleColumn;
    public TableColumn<Appointment, String> apptDescColumn;
    public TableColumn<Appointment, String> apptTypeColumn;
    public TableColumn<Appointment, LocalDateTime> apptStartDateTimeColumn;
    public TableColumn<Appointment, LocalDateTime> apptEndDateTimeColumn;
    public TableColumn<Appointment, Integer> apptCustomerIDColumn;
    public ObservableList<Contact> allContacts = FXCollections.observableArrayList();
    public ObservableList<Appointment> allScheduledAppointments = FXCollections.observableArrayList();
    public ComboBox<Contact> contactComboBox;

    /**
     * @param url takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        apptIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        apptTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        apptDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        apptTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        apptStartDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        apptEndDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endDateTime"));
        apptCustomerIDColumn.setCellValueFactory(new PropertyValueFactory<>("apptCustomerId"));

        appointmentsTableView.setItems(allScheduledAppointments);

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

    }

    /**
     * @param actionEvent clicking the 'Exit' menu item is the action event
     */
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    /**
     * @param actionEvent clicking the back button is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void onBack(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 550);
        stage.setTitle("Home");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent selecting a contact from the combo box is the action event
     */
    public void onSelectContact(ActionEvent actionEvent) {
        allScheduledAppointments.removeAll();
        appointmentsTableView.getItems().clear();
        int contactId = contactComboBox.getSelectionModel().getSelectedItem().getContactId();

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectAppointmentsStatement = "Select appointments.Appointment_ID, appointments.title, " +
                    "appointments.User_ID, appointments.Description, appointments.Location, appointments.Type, " +
                    "appointments.Start, appointments.End, appointments.Customer_ID, contacts.Contact_Name " +
                    "FROM appointments JOIN contacts ON contacts.Contact_ID=appointments.Contact_ID " +
                    "WHERE contacts.Contact_ID = " + contactId;

            DatabaseQuery.setPreparedStatement(connect, selectAppointmentsStatement);
            PreparedStatement appointmentsPreparedStatement = DatabaseQuery.getPreparedStatement();
            appointmentsPreparedStatement.execute(selectAppointmentsStatement);
            ResultSet appointmentsResultSet = appointmentsPreparedStatement.getResultSet();

            while (appointmentsResultSet.next()) {

                int appointmentId = appointmentsResultSet.getInt("Appointment_ID");
                String title = appointmentsResultSet.getString("title");
                String description = appointmentsResultSet.getString("Description");
                String location = appointmentsResultSet.getString("Location");
                String contactName = appointmentsResultSet.getString("Contact_Name");
                String type = appointmentsResultSet.getString("Type");
                LocalDateTime startDateTime = appointmentsResultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endDateTime = appointmentsResultSet.getTimestamp("End").toLocalDateTime();

                LocalDate startDate = startDateTime.toLocalDate();
                LocalTime startTime = startDateTime.toLocalTime();
                LocalDate endDate = endDateTime.toLocalDate();
                LocalTime endTime = endDateTime.toLocalTime();

                ZoneId zoneId = ZoneId.systemDefault();
                ZoneId zoneIdUTC = ZoneId.of("UTC");

                ZonedDateTime zonedStartDateTime = ZonedDateTime.of(startDate, startTime, zoneIdUTC);
                ZonedDateTime zonedEndDateTime = ZonedDateTime.of(endDate, endTime, zoneIdUTC);

                Instant startDateTimeInstant = zonedStartDateTime.toInstant();
                Instant endDateTimeInstant = zonedEndDateTime.toInstant();

                ZonedDateTime localZonedStartDateTime = startDateTimeInstant.atZone(zoneId);
                ZonedDateTime localZonedEndDateTime = endDateTimeInstant.atZone(zoneId);

                LocalDateTime localStartDateTime = localZonedStartDateTime.toLocalDateTime();
                LocalDateTime localEndDateTime = localZonedEndDateTime.toLocalDateTime();

                int customerId = appointmentsResultSet.getInt("Customer_ID");
                int userId = appointmentsResultSet.getInt("User_ID");

                Appointment appointment = new Appointment(appointmentId, customerId, userId, title, contactName,
                        description,
                        location, type, localStartDateTime, localEndDateTime);
                allScheduledAppointments.add(appointment);

            }
            appointmentsTableView.setItems(allScheduledAppointments);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
