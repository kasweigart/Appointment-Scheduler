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
import model.MonthCount;
import model.TypeCount;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormatSymbols;
import java.util.ResourceBundle;

/**
 * This class is the controller for the total appointments report.
 */
public class TotalAppointmentsController implements Initializable {

    /**
     * observable lists for holding the data needed to display in the list views
     */
    public ListView<TypeCount> byTypeListView;
    public ListView<MonthCount> byMonthListView;
    public ObservableList<TypeCount> allTypeCounts = FXCollections.observableArrayList();
    public ObservableList<MonthCount> allMonthCounts = FXCollections.observableArrayList();

    /**
     * @param url takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            Connection connect = DatabaseConnection.getConnection();

            String selectDistinctTypeStatement = "SELECT DISTINCT Type, count(Type) FROM appointments GROUP BY Type";

            DatabaseQuery.setPreparedStatement(connect, selectDistinctTypeStatement);
            PreparedStatement selectDistinctType = DatabaseQuery.getPreparedStatement();
            selectDistinctType.execute(selectDistinctTypeStatement);
            ResultSet distinctTypeResultSet = selectDistinctType.getResultSet();

            while (distinctTypeResultSet.next()) {
                String type = distinctTypeResultSet.getString("Type");
                int count = distinctTypeResultSet.getInt("count(Type)");

                TypeCount newTypeCount = new TypeCount(type, count);
                allTypeCounts.add(newTypeCount);
            }

            byTypeListView.setItems(allTypeCounts);

            String selectMonthStatement = "SELECT month(Start), COUNT(Start) FROM appointments GROUP BY month(Start)";

            DatabaseQuery.setPreparedStatement(connect, selectMonthStatement);
            PreparedStatement selectMonthPreparedStatement = DatabaseQuery.getPreparedStatement();
            selectMonthPreparedStatement.execute(selectMonthStatement);
            ResultSet selectMonthResultSet = selectMonthPreparedStatement.getResultSet();

            while (selectMonthResultSet.next()) {
                int monthInt = selectMonthResultSet.getInt("month(Start)");
                String monthString = new DateFormatSymbols().getMonths()[monthInt-1];
                int count = selectMonthResultSet.getInt("COUNT(Start)");

                MonthCount monthCount = new MonthCount(monthString, count);
                allMonthCounts.add(monthCount);
            }

            byMonthListView.setItems(allMonthCounts);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param actionEvent clicking the 'Back' button is the action event
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
     * @param actionEvent clicking the 'Exit' menu item is the action event
     */
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}