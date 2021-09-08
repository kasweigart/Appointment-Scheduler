package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utilities.DatabaseConnection;

/**
 * This class is where the program starts.
 */
public class Main extends Application {

    /**
     * @param args main parameter to the main static function
     */
    public static void main(String[] args) {

        /**
         * starts the connection to the database
         */
        DatabaseConnection.startConnection();
        launch(args);
    }

    /**
     * @param primaryStage the first stage loaded for the application
     * @throws Exception an exception is thrown when there is an internal problem with the function
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
}