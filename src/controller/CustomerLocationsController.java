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
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.DivisionCount;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * This class is the controller for the customer locations report.
 */
public class CustomerLocationsController implements Initializable {

    /**
     * observable list of division counts for displaying on the list view
     */
    public ObservableList<DivisionCount> allDivisionCounts = FXCollections.observableArrayList();
    public ListView<DivisionCount> customersByRegionListView;

    /**
     * @param url takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectDistinctDivisionStatement = "select distinct first_level_divisions.Division," +
                    "count(first_level_divisions.Division) from customers JOIN first_level_divisions on customers" +
                    ".Division_ID=first_level_divisions.Division_ID group by first_level_divisions.Division";

            DatabaseQuery.setPreparedStatement(connect, selectDistinctDivisionStatement);
            PreparedStatement selectDistinctDivision = DatabaseQuery.getPreparedStatement();
            selectDistinctDivision.execute(selectDistinctDivisionStatement);
            ResultSet distinctDivisionResultSet = selectDistinctDivision.getResultSet();

            while (distinctDivisionResultSet.next()) {
                String division = distinctDivisionResultSet.getString("Division");
                int count = distinctDivisionResultSet.getInt("count(first_level_divisions.Division)");

                DivisionCount divisionCount = new DivisionCount(division, count);
                allDivisionCounts.add(divisionCount);
            }

            /**
             * This lambda function takes each division count from the allDivisionCounts observable list and appends
             * them to the list view
             * @param divisionCount takes in a division count object
             * @return returns the addition of the division count object in the customers by region list view
             */
            allDivisionCounts.forEach(divisionCount -> customersByRegionListView.getItems().add(divisionCount));

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
}