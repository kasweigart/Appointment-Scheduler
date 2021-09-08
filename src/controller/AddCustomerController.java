package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Country;
import model.FirstLevelDivision;
import model.User;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * This class is the controller for adding customers to the application.
 */
public class AddCustomerController implements Initializable {

    /**
     * text fields for entering the customer name, address, postal code, and phone number
     * combo boxes for selecting the country and first level division
     */
    public TextField customerNameField;
    public TextField addressField;
    public TextField postalCodeField;
    public TextField phoneNumberField;
    public ComboBox<Country> countryComboBox;
    public ComboBox<FirstLevelDivision> stateProvinceComboBox;

    /**
     * @param url takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        FirstLevelDivision.allFirstLevelDivisions.clear();
        FirstLevelDivision.unitedStatesFirstLevelDivisions.clear();
        FirstLevelDivision.unitedKingdomFirstLevelDivisions.clear();
        Country.allCountries.clear();

        try {
            Connection connect = DatabaseConnection.getConnection();
            String countriesSelectStatement = "SELECT Country_ID, Country FROM countries";

            String firstLevelDivisionsSelectStatement = "SELECT Division_ID, Division, COUNTRY_ID FROM " +
                    "first_level_divisions";

            DatabaseQuery.setPreparedStatement(connect, countriesSelectStatement);
            PreparedStatement countriesPreparedStatement = DatabaseQuery.getPreparedStatement();
            countriesPreparedStatement.execute(countriesSelectStatement);
            ResultSet countriesResultSet = countriesPreparedStatement.getResultSet();

            DatabaseQuery.setPreparedStatement(connect, firstLevelDivisionsSelectStatement);
            PreparedStatement firstLevelDivisionsPreparedStatement = DatabaseQuery.getPreparedStatement();
            firstLevelDivisionsPreparedStatement.execute(firstLevelDivisionsSelectStatement);
            ResultSet firstLevelDivisionsResultSet = firstLevelDivisionsPreparedStatement.getResultSet();

            while (countriesResultSet.next()) {
                int countryId = countriesResultSet.getInt("Country_ID");
                String countryString = countriesResultSet.getString("Country");

                Country country = new Country(countryId, countryString);
                Country.allCountries.add(country);
            }

            while (firstLevelDivisionsResultSet.next()) {
                int divisionId = firstLevelDivisionsResultSet.getInt("Division_ID");
                String division = firstLevelDivisionsResultSet.getString("Division");
                int countryId = firstLevelDivisionsResultSet.getInt("COUNTRY_ID");

                FirstLevelDivision firstLevelDivision = new FirstLevelDivision(divisionId, division, countryId);
                FirstLevelDivision.allFirstLevelDivisions.add(firstLevelDivision);

            }
            countryComboBox.setItems(Country.allCountries);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Initialized");
    }

    /**
     * this event handler will set the appropriate first level divisions based on which country is selected
     * @param actionEvent selecting the country in the combo box is the action event
     */
    public void onCountrySelected(ActionEvent actionEvent) {
        if (countryComboBox.getSelectionModel().getSelectedItem().getCountryId() == 1) {
            stateProvinceComboBox.setItems(FirstLevelDivision.populateUSFirstLevelDivisions());
        }

        if (countryComboBox.getSelectionModel().getSelectedItem().getCountryId() == 2) {
            stateProvinceComboBox.setItems(FirstLevelDivision.populateUKFirstLevelDivisions());
        }

        if (countryComboBox.getSelectionModel().getSelectedItem().getCountryId() == 3) {
            stateProvinceComboBox.setItems(FirstLevelDivision.populateCanadaFirstLevelDivisions());
        }
    }

    /**
     * @param actionEvent clicking the cancel button is the action event
     * @throws IOException an IO exception is thrown when there is an internal problem with the event handler
     */
    public void toHome(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 550);
        stage.setTitle("Home");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param actionEvent clicking the save button is the action event
     * @throws SQLException an SQL exception is thrown when the query fails to execute
     */
    public void addCustomer(ActionEvent actionEvent) throws SQLException {

        try {
            Connection connect = DatabaseConnection.getConnection();
            String customerInsertStatement = "INSERT INTO customers(Customer_Name, Address, Postal_Code, Phone, " +
                    "Create_Date," +
                    "Created_By, Last_Update, Last_Updated_By, Division_ID) VALUES(?,?,?,?,?,?,?,?,?)";

            DatabaseQuery.setPreparedStatement(connect, customerInsertStatement);

            PreparedStatement preparedStatement = DatabaseQuery.getPreparedStatement();

            String customerName = customerNameField.getText();
            String address = addressField.getText();
            String postalCode = postalCodeField.getText();
            String phoneNumber = phoneNumberField.getText();
            Timestamp createDate = new Timestamp(System.currentTimeMillis());
            String createdBy = User.getInstance().getUsername();
            Timestamp lastUpdate = new Timestamp(System.currentTimeMillis());
            String lastUpdatedBy = User.getInstance().getUsername();
            int divisionId = stateProvinceComboBox.getSelectionModel().getSelectedItem().getDivisionId();

            if (customerName.length() > 0 && address.length() > 0 && postalCode.length() > 0 && phoneNumber.length() > 0) {

                preparedStatement.setString(1, customerName);
                preparedStatement.setString(2, address);
                preparedStatement.setString(3, postalCode);
                preparedStatement.setString(4, phoneNumber);
                preparedStatement.setTimestamp(5, createDate);
                preparedStatement.setString(6, createdBy);
                preparedStatement.setTimestamp(7, lastUpdate);
                preparedStatement.setString(8, lastUpdatedBy);
                preparedStatement.setInt(9, divisionId);

                preparedStatement.execute();

                Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, 800, 550);
                stage.setTitle("Home");
                stage.setScene(scene);
                stage.show();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Add Customer");
                alert.setContentText("Please input data in all fields to add a customer.");
                alert.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add Customer");
            alert.setContentText("Your input data is incorrect or there is a problem with the database.");
            alert.showAndWait();
        }
    }

    /**
     * @param actionEvent clicking the 'Exit' menu item is the action event
     */
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }


}