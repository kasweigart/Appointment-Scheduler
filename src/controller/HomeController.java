package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Customer;
import model.User;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * This class is the controller for the home screen.
 */
public class HomeController implements Initializable {

    /**
     * table view and table columns for displaying all customers
     */
    public MenuItem newCustomerMenuItem;
    public Label customersLabel;
    public TableView<Customer> customersTableView;
    public TableColumn<Customer, Integer> customerIDColumn;
    public TableColumn<Customer, String> customerNameColumn;
    public TableColumn<Customer, String> customerAddressColumn;
    public TableColumn<Customer, String> custFirstLevelDivisionColumn;
    public TableColumn<Customer, String> customerPostalCodeColumn;
    public TableColumn<Customer, String> customerPhoneNumberColumn;
    public ObservableList<Customer> allCustomers = FXCollections.observableArrayList();

    /**
     * table view and table columns for displaying all appointments
     */
    public TableView<Appointment> appointmentsTableView;
    public TableColumn<Appointment, Integer> apptIDColumn;
    public TableColumn<Appointment, String> apptTitleColumn;
    public TableColumn<Appointment, String> apptDescColumn;
    public TableColumn<Appointment, String> apptLocationColumn;
    public TableColumn<Appointment, String> apptContactColumn;
    public TableColumn<Appointment, String> apptTypeColumn;
    public TableColumn<Appointment, LocalDateTime> apptStartDateTimeColumn;
    public TableColumn<Appointment, LocalDateTime> apptEndDateTimeColumn;
    public TableColumn<Appointment, Integer> apptCustomerIDColumn;
    public ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();

    public Label welcomeMessage;
    public RadioButton monthRadioButton;
    public ToggleGroup toggleGroup;
    public RadioButton weekRadioButton;
    public Label upcomingAppointmentLabel;
    public RadioButton allRadioButton;

    /**
     * These two lambda expressions are essentially flushing the local buffers of customers and appointments.
     * returns the removal of each customer and appointment object
     */
    public void clearLocalCustAndApptBufferLambdas() {
        allCustomers.forEach(customer -> allCustomers.remove(customer));
        customersTableView.getItems().clear();

        allAppointments.forEach(appointment -> allAppointments.remove(appointment));
        appointmentsTableView.getItems().clear();
    }

    /**
     * @param url takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        clearLocalCustAndApptBufferLambdas();

        /**
         * welcome message that displays the username that is logged in
         */
        welcomeMessage.setText("Welcome, " + User.getInstance().getUsername());

        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));
        custFirstLevelDivisionColumn.setCellValueFactory(new PropertyValueFactory<>("firstLevelDivision"));
        customerPostalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        customerPhoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        apptIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        apptTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        apptDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        apptLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        apptContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        apptTypeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        apptStartDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        apptEndDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endDateTime"));
        apptCustomerIDColumn.setCellValueFactory(new PropertyValueFactory<>("apptCustomerId"));

        /**
         * connects to the database, executes two queries to populate the two table views
         */
        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectCustomersStatement = "SELECT customers.Customer_ID, customers.Customer_Name, customers" +
                    ".Address," +
                    " " +
                    "customers.Postal_Code, customers.Phone, customers.Division_ID, first_level_divisions.Division FROM " +
                    "customers JOIN first_level_divisions ON first_level_divisions.Division_ID=customers.Division_ID";

            DatabaseQuery.setPreparedStatement(connect, selectCustomersStatement);
            PreparedStatement customersPreparedStatement = DatabaseQuery.getPreparedStatement();
            customersPreparedStatement.execute(selectCustomersStatement);
            ResultSet customersResultSet = customersPreparedStatement.getResultSet();

            String selectAppointmentsStatement = "Select appointments.Appointment_ID, appointments.title," +
                    " appointments.User_ID, appointments.Description, appointments.Location, appointments.Type," +
                    " appointments.Start, appointments.End, appointments.Customer_ID, contacts.Contact_Name" +
                    " FROM appointments JOIN contacts ON contacts.Contact_ID=appointments.Contact_ID";

            DatabaseQuery.setPreparedStatement(connect, selectAppointmentsStatement);
            PreparedStatement appointmentsPreparedStatement = DatabaseQuery.getPreparedStatement();
            appointmentsPreparedStatement.execute(selectAppointmentsStatement);
            ResultSet appointmentsResultSet = appointmentsPreparedStatement.getResultSet();


            while (customersResultSet.next()) {
                int customerId = customersResultSet.getInt("Customer_ID");
                String customerName = customersResultSet.getString("Customer_Name");
                String address = customersResultSet.getString("Address");
                String firstLevelDivision = customersResultSet.getString("Division");
                String postalCode = customersResultSet.getString("Postal_Code");
                String phoneNumber = customersResultSet.getString("Phone");

                Customer customer = new Customer(customerId, customerName, address, firstLevelDivision, postalCode,
                        phoneNumber);
                allCustomers.add(customer);
            }

            customersTableView.setItems(allCustomers);


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
                allAppointments.add(appointment);

            }
            appointmentsTableView.setItems(allAppointments);


            try {
                upcomingAppointmentLabel.setText("Upcoming appointment: " + findUpcomingAppointments().getAppointmentId() +
                        " " + findUpcomingAppointments().getStartDateTime().toString());
            } catch (NullPointerException e) {
                upcomingAppointmentLabel.setText("No upcoming appointments.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return returns an appointment if there exists an appointment that starts in less than 15 minutes from logging in
     */
    public Appointment findUpcomingAppointments() {
        for (Appointment appointment : allAppointments) {
            if (appointment.getStartDateTime().isBefore(LocalDateTime.now().plusMinutes(15)) &&
                    appointment.getStartDateTime().isAfter(LocalDateTime.now())) {
                return appointment;
            }
        }
        return null;
    }


    /**
     * @param actionEvent clicking the 'New Customer' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void toAddCustomer(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/AddCustomer.fxml"));
        Stage stage = (Stage) customersLabel.getScene().getWindow();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Add Customer");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the 'New Appointment' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void toAddAppointment(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/AddAppointment.fxml"));
        Stage stage = (Stage) customersLabel.getScene().getWindow();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Add Appointment");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the 'Update Customer' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void toUpdateCustomer(ActionEvent actionEvent) throws IOException {
        try {
            Customer selectedCustomer = customersTableView.getSelectionModel().getSelectedItem();
            UpdateCustomerController.receiveCustomer(selectedCustomer);
            Parent root = FXMLLoader.load(getClass().getResource("/view/UpdateCustomer.fxml"));
            Stage stage = (Stage) customersLabel.getScene().getWindow();
            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("Update Customer");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Customer");
            alert.setContentText("Please select a customer from the list to update.");
            alert.showAndWait();
        }

    }

    /**
     * @param actionEvent clicking the 'Update Appointment' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void toUpdateAppointment(ActionEvent actionEvent) throws IOException {
        try {
            Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
            UpdateAppointmentController.receiveAppointment(selectedAppointment);
            Parent root = FXMLLoader.load(getClass().getResource("/view/UpdateAppointment.fxml"));
            Stage stage = (Stage) customersLabel.getScene().getWindow();
            Scene scene = new Scene(root, 600, 400);
            stage.setTitle("Update Appointment");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Appointment");
            alert.setContentText("Please select an appointment from the list to update.");
            alert.showAndWait();
        }

    }

    /**
     * @param actionEvent clicking the 'Delete Customer' menu item is the action event
     *                    the customer must be selected first before clicking the menu item
     */
    public void onDeleteCustomer(ActionEvent actionEvent) {
        Customer selectedCustomer = customersTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete the customer with ID: " + selectedCustomer.getCustomerId()
                            + "?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Delete Customer");
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {
                    Connection connect = DatabaseConnection.getConnection();

                    String deleteAppointmentStatement =
                            "DELETE FROM customers WHERE Customer_ID = " + selectedCustomer.getCustomerId();
                    DatabaseQuery.setPreparedStatement(connect, deleteAppointmentStatement);
                    PreparedStatement customersPreparedStatement = DatabaseQuery.getPreparedStatement();
                    customersPreparedStatement.execute(deleteAppointmentStatement);
                    customersTableView.getItems().remove(selectedCustomer);
                    Alert sqlAlert = new Alert(Alert.AlertType.INFORMATION);
                    sqlAlert.setTitle("Delete Customer");
                    sqlAlert.setContentText("Customer with ID: '" + selectedCustomer.getCustomerId() + "' has been " +
                            "deleted.");
                    sqlAlert.showAndWait();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                    sqlAlert.setTitle("Delete Customer");
                    sqlAlert.setContentText("Customer is assigned to an appointment(s).");
                    sqlAlert.showAndWait();
                }

            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setContentText("Please select a customer from the list to delete.");
            alert.showAndWait();
        }
    }

    /**
     * @param actionEvent clicking the 'Delete Appointment' menu item is the action event
     *                    the appointment must be selected first before selecting the menu item
     */
    public void onDeleteAppointment(ActionEvent actionEvent) {
        Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
        if (selectedAppointment != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete the appointment with ID: " + selectedAppointment.getAppointmentId()
                            + "?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Delete Appointment");
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                try {
                    Connection connect = DatabaseConnection.getConnection();

                    String deleteAppointmentStatement =
                            "DELETE FROM appointments WHERE Appointment_ID = " + selectedAppointment.getAppointmentId();
                    DatabaseQuery.setPreparedStatement(connect, deleteAppointmentStatement);
                    PreparedStatement appointmentPreparedStatement = DatabaseQuery.getPreparedStatement();
                    appointmentPreparedStatement.execute(deleteAppointmentStatement);
                    Alert sqlAlert = new Alert(Alert.AlertType.INFORMATION);
                    sqlAlert.setTitle("Delete Appointment");
                    sqlAlert.setContentText("Appointment with ID: '" + selectedAppointment.getAppointmentId() + "' and " +
                            "type: '" +
                            selectedAppointment.getAppointmentType() +
                            "' has been " +
                            "deleted.");
                    sqlAlert.showAndWait();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                    sqlAlert.setTitle("Delete Appointment");
                    sqlAlert.setContentText("There was a problem deleting the appointment.");
                    sqlAlert.showAndWait();
                }
                appointmentsTableView.getItems().remove(selectedAppointment);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setContentText("Please select an appointment from the list to delete.");
            alert.showAndWait();
        }
    }

    /**
     * @param actionEvent clicking the 'This Month' radio button is the action event
     */
    public void onMonthRadioButton(ActionEvent actionEvent) {

        allAppointments.removeAll();
        appointmentsTableView.getItems().clear();

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectAppointmentsStatement = "Select appointments.Appointment_ID, appointments.title," +
                    " appointments.User_ID, appointments.Description, appointments.Location, appointments.Type," +
                    " appointments.Start, appointments.End, appointments.Customer_ID, contacts.Contact_Name" +
                    " FROM appointments JOIN contacts ON contacts.Contact_ID=appointments.Contact_ID";

            DatabaseQuery.setPreparedStatement(connect, selectAppointmentsStatement);
            PreparedStatement appointmentsPreparedStatement = DatabaseQuery.getPreparedStatement();
            appointmentsPreparedStatement.execute(selectAppointmentsStatement);
            ResultSet appointmentsResultSet = appointmentsPreparedStatement.getResultSet();


            while (appointmentsResultSet.next()) {
                int appointmentId = appointmentsResultSet.getInt("Appointment_ID");
                String title = appointmentsResultSet.getString("Title");
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

                if (startDateTime.getMonth().equals(LocalDateTime.now().getMonth())) {
                    Appointment appointment = new Appointment(appointmentId, customerId, userId, title, contactName,
                            description,
                            location, type, localStartDateTime, localEndDateTime);
                    allAppointments.add(appointment);
                }

            }
            appointmentsTableView.setItems(allAppointments);


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param actionEvent clicking the 'This Week' radio button is the action event
     */
    public void onWeekRadioButton(ActionEvent actionEvent) {

        allAppointments.removeAll();
        appointmentsTableView.getItems().clear();

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectAppointmentsStatement = "Select appointments.Appointment_ID, appointments.title, " +
                    "appointments.User_ID, appointments.Description, appointments.Location, appointments.Type, " +
                    "appointments.Start, appointments.End, appointments.Customer_ID, contacts.Contact_Name " +
                    "FROM appointments JOIN contacts ON contacts.Contact_ID=appointments.Contact_ID";

            DatabaseQuery.setPreparedStatement(connect, selectAppointmentsStatement);
            PreparedStatement appointmentsPreparedStatement = DatabaseQuery.getPreparedStatement();
            appointmentsPreparedStatement.execute(selectAppointmentsStatement);
            ResultSet appointmentsResultSet = appointmentsPreparedStatement.getResultSet();


            while (appointmentsResultSet.next()) {
                int appointmentId = appointmentsResultSet.getInt("Appointment_ID");
                String title = appointmentsResultSet.getString("Title");
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


                if (startDateTime.isAfter(LocalDateTime.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)) &&
                        startDateTime.isBefore(LocalDateTime.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7))) {

                    Appointment appointment = new Appointment(appointmentId, customerId, userId, title, contactName,
                            description,
                            location, type, localStartDateTime, localEndDateTime);
                    allAppointments.add(appointment);
                }

            }
            appointmentsTableView.setItems(allAppointments);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param actionEvent clicking the 'Appointments Report' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void onApptsReportMenuItem(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/TotalAppointments.fxml"));
        Stage stage = (Stage) customersLabel.getScene().getWindow();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Appointments Report");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the 'Contact Schedule' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void onContactScheduleMenuItem(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/ContactSchedule.fxml"));
        Stage stage = (Stage) customersLabel.getScene().getWindow();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Contact Schedule");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the "Customer Locations' menu item is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void onCustomerLocationMenuItem(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/CustomerLocations.fxml"));
        Stage stage = (Stage) customersLabel.getScene().getWindow();
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("Customer Location");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the 'All' radio button is the action event
     */
    public void onAllRadioButton(ActionEvent actionEvent) {

        allAppointments.removeAll();
        appointmentsTableView.getItems().clear();

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectAppointmentsStatement = "Select appointments.Appointment_ID, appointments.title, " +
                    "appointments.User_ID, appointments.Description, appointments.Location, appointments.Type, " +
                    "appointments.Start, appointments.End, appointments.Customer_ID, contacts.Contact_Name " +
                    "FROM appointments JOIN contacts ON contacts.Contact_ID=appointments.Contact_ID";

            DatabaseQuery.setPreparedStatement(connect, selectAppointmentsStatement);
            PreparedStatement appointmentsPreparedStatement = DatabaseQuery.getPreparedStatement();
            appointmentsPreparedStatement.execute(selectAppointmentsStatement);
            ResultSet appointmentsResultSet = appointmentsPreparedStatement.getResultSet();

            while (appointmentsResultSet.next()) {

                int appointmentId = appointmentsResultSet.getInt("Appointment_ID");
                String title = appointmentsResultSet.getString("Title");
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
                allAppointments.add(appointment);

            }
            appointmentsTableView.setItems(allAppointments);

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
}
