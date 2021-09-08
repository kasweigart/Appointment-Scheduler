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
import model.*;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * This class is the controller for updating customers.
 */
public class UpdateCustomerController implements Initializable {

    private static Customer customerData = null;
    /**
     * text fields for editing customer data
     */
    public TextField idField;
    public TextField customerNameField;
    public TextField addressField;
    public TextField postalCodeField;
    public TextField phoneNumberField;
    public ComboBox<Country> countryComboBox;
    public ComboBox<FirstLevelDivision> stateProvinceComboBox;
    public Menu fileMenu;

    /**
     * This lambda returns the country name for the combo box.
     * @return returns the US country interface from the parameterless lambda expression
     */
    public CountryInterface setUSCountryComboBoxLambda() {
        CountryInterface setCountryUS = () -> "U.S";
        return setCountryUS;
    }

    /**
     * This lambda returns the country name for the combo box.
     * @return returns the UK country interface from the parameterless lambda expression
     */
    public CountryInterface setUKCountryComboBoxLambda() {
        CountryInterface setCountryUK = () -> "UK";
        return setCountryUK;
    }

    /**
     * This lambda returns the country name for the combo box.
     * @return returns the Canada country interface from the parameterless lambda expression
     */
    public CountryInterface setCanadaCountryComboBoxLambda() {
        CountryInterface setCountryCanada = () -> "Canada";
        return setCountryCanada;
    }

    /**
     * @param selectedCustomer takes in a customer and sets it to a static variable
     */
    public static void receiveCustomer(Customer selectedCustomer) {
        customerData = selectedCustomer;
    }

    /**
     * @param url            takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        FirstLevelDivision.allFirstLevelDivisions.clear();
        Country.allCountries.clear();

        try {

            Connection connect = DatabaseConnection.getConnection();

            String countriesSelectStatement = "select countries.Country_ID, countries.Country," +
                    " first_level_divisions.Division_ID, first_level_divisions.Division FROM countries" +
                    " JOIN first_level_divisions ON countries.Country_ID=first_level_divisions.COUNTRY_ID";

            DatabaseQuery.setPreparedStatement(connect, countriesSelectStatement);
            PreparedStatement countriesPreparedStatement = DatabaseQuery.getPreparedStatement();
            countriesPreparedStatement.execute(countriesSelectStatement);
            ResultSet countriesResultSet = countriesPreparedStatement.getResultSet();

            String countryStringRef = null;
            while (countriesResultSet.next()) {

                int countryId = countriesResultSet.getInt("Country_ID");
                String countryString = countriesResultSet.getString("Country");
                int divisionId = countriesResultSet.getInt("Division_ID");
                String division = countriesResultSet.getString("Division");

                if (!countryString.equals(countryStringRef)) {
                    Country country = new Country(countryId, countryString);
                    Country.allCountries.add(country);
                    countryStringRef = countryString;
                }
                FirstLevelDivision firstLevelDivision = new FirstLevelDivision(divisionId, division, countryId);
                FirstLevelDivision.allFirstLevelDivisions.add(firstLevelDivision);
            }

            countryComboBox.setItems(Country.allCountries);
            stateProvinceComboBox.setItems(FirstLevelDivision.allFirstLevelDivisions);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        FirstLevelDivision firstLevelDivisionReference = null;
        for (FirstLevelDivision firstLevelDivision : stateProvinceComboBox.getItems()) {

            if (firstLevelDivision.getDivision().equals(customerData.getFirstLevelDivision())) {
                stateProvinceComboBox.setPromptText(firstLevelDivision.getDivision());
                firstLevelDivisionReference = firstLevelDivision;
            }
        }

        /**
         * these lambda functions set the country for the country combo box
         * @param none
         * @returns the selected country to display on the country combo box
         */

        if (firstLevelDivisionReference.getCountryId() == 1) {
            countryComboBox.setPromptText(setUSCountryComboBoxLambda().printCountry());
            stateProvinceComboBox.setItems(FirstLevelDivision.populateUSFirstLevelDivisions());

        } else if (firstLevelDivisionReference.getCountryId() == 2) {
            countryComboBox.setPromptText(setUKCountryComboBoxLambda().printCountry());
            stateProvinceComboBox.setItems(FirstLevelDivision.populateUKFirstLevelDivisions());

        } else if (firstLevelDivisionReference.getCountryId() == 3) {
            countryComboBox.setPromptText(setCanadaCountryComboBoxLambda().printCountry());
            stateProvinceComboBox.setItems(FirstLevelDivision.populateCanadaFirstLevelDivisions());
        }


        idField.setText(String.valueOf(customerData.getCustomerId()));
        customerNameField.setText(customerData.getCustomerName());
        addressField.setText(customerData.getCustomerAddress());
        postalCodeField.setText(customerData.getPostalCode());
        phoneNumberField.setText(customerData.getPhoneNumber());

        System.out.println("Initialized");
    }

    /**
     * @param actionEvent clicking the 'Cancel' button is the action event
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
     * @param actionEvent clicking the 'Save' button is the action event
     * @throws SQLException throws an SQL exception when there is a problem with the query
     */
    public void updateCustomer(ActionEvent actionEvent) throws SQLException {

        try {
            Connection connect = DatabaseConnection.getConnection();
            String customerUpdateStatement = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?," +
                    "Phone = ?, Last_Update = ?, Last_Updated_By = ?, Division_ID = ? WHERE Customer_ID = " + customerData.getCustomerId();

            DatabaseQuery.setPreparedStatement(connect, customerUpdateStatement);
            PreparedStatement preparedStatement = DatabaseQuery.getPreparedStatement();

            String customerName = customerNameField.getText();
            String address = addressField.getText();
            String postalCode = postalCodeField.getText();
            String phoneNumber = phoneNumberField.getText();
            Timestamp lastUpdate = new Timestamp(System.currentTimeMillis());
            String lastUpdatedBy = User.getUsername();

            int divisionId = 0;
            try {
                divisionId = stateProvinceComboBox.getSelectionModel().getSelectedItem().getDivisionId();
            } catch (NullPointerException e) {

                try {
                    String firstLevelDivisionSelectStatement = "select Division_ID, Division from " +
                            "first_level_divisions WHERE Division = \"" + stateProvinceComboBox.getPromptText() + "\"";

                    DatabaseQuery.setPreparedStatement(connect, firstLevelDivisionSelectStatement);
                    PreparedStatement firstLevelDivisionPreparedStatement = DatabaseQuery.getPreparedStatement();
                    firstLevelDivisionPreparedStatement.execute(firstLevelDivisionSelectStatement);
                    ResultSet resultSet = firstLevelDivisionPreparedStatement.getResultSet();

                    while (resultSet.next()) {
                        int foundDivisionId = resultSet.getInt("Division_ID");
                        divisionId = foundDivisionId;
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }


            if (customerName.length() > 0 && address.length() > 0 && postalCode.length() > 0 && phoneNumber.length() > 0
                    && divisionId != 0) {

                preparedStatement.setString(1, customerName);
                preparedStatement.setString(2, address);
                preparedStatement.setString(3, postalCode);
                preparedStatement.setString(4, phoneNumber);
                preparedStatement.setTimestamp(5, lastUpdate);
                preparedStatement.setString(6, lastUpdatedBy);
                preparedStatement.setInt(7, divisionId);

                preparedStatement.execute();

                Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, 800, 550);
                stage.setTitle("Home");
                stage.setScene(scene);
                stage.show();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Update Customer");
                alert.setContentText("Please input data in all fields to update a customer.");
                alert.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Update Customer");
            alert.setContentText("Your input data is incorrect/database problem.");
            alert.showAndWait();
        }
    }

    /**
     * @param actionEvent clicking the 'Exit' menu item is the action event
     */
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onCountrySelected(ActionEvent actionEvent) {

        FirstLevelDivision.unitedStatesFirstLevelDivisions.clear();
        FirstLevelDivision.unitedKingdomFirstLevelDivisions.clear();
        FirstLevelDivision.canadaFirstLevelDivisions.clear();

        try {
            if (countryComboBox.getSelectionModel().getSelectedItem().getCountryId() == 1) {
                stateProvinceComboBox.setItems(FirstLevelDivision.populateUSFirstLevelDivisions());
            }

            if (countryComboBox.getSelectionModel().getSelectedItem().getCountryId() == 2) {
                stateProvinceComboBox.setItems(FirstLevelDivision.populateUKFirstLevelDivisions());
            }

            if (countryComboBox.getSelectionModel().getSelectedItem().getCountryId() == 3) {
                stateProvinceComboBox.setItems(FirstLevelDivision.populateCanadaFirstLevelDivisions());
            }
        } catch (NullPointerException e) {

        }
    }
}