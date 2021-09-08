package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import utilities.DatabaseConnection;
import utilities.DatabaseQuery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * This class is the controller for the login form.
 */
public class LoginController implements Initializable {

    /**
     * text fields for entering a username and password
     */
    public Label titleLabel;
    public Label userNameLabel;
    public Label passwordLabel;
    public TextField usernameField;
    public PasswordField passwordField;
    public Button loginButton;
    public Label zoneIdLabel;
    ZoneId zoneId = ZoneId.systemDefault();

    /**
     * @param url takes in a unified resource locator as the first parameter
     * @param resourceBundle takes in a resource bundle as the second parameter
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            ResourceBundle rb = ResourceBundle.getBundle("main/Nat", Locale.getDefault());
            if (Locale.getDefault().getLanguage().equals("fr")) {
                titleLabel.setText(rb.getString("AppointmentScheduler"));
                userNameLabel.setText(rb.getString("Username"));
                passwordLabel.setText(rb.getString("Password"));
                loginButton.setText(rb.getString("Login"));
                zoneIdLabel.setText(rb.getString("Location") + " - " + zoneId.getDisplayName(TextStyle.FULL,
                        Locale.FRENCH));
            }
        } catch (MissingResourceException e) {

            zoneIdLabel.setText("Location - " + zoneId);
        }

        System.out.println("Initialized");
    }

    /**
     * @param actionEvent clicking the 'Login' button is the action event
     * @throws SQLException throws an SQL exception when the query fails to find a username and password
     */
    public void login(ActionEvent actionEvent) throws SQLException {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.length() > 0 && password.length() > 0) {
            try {
                Connection connect = DatabaseConnection.getConnection();
                String selectStatement = "SELECT User_ID, User_Name, Password FROM users";

                DatabaseQuery.setPreparedStatement(connect, selectStatement);
                PreparedStatement preparedStatement = DatabaseQuery.getPreparedStatement();
                preparedStatement.execute(selectStatement);
                ResultSet resultSet = preparedStatement.getResultSet();

                boolean found = false;
                while (resultSet.next()) {

                    int resultUserId = resultSet.getInt("User_ID");
                    String resultUsername = resultSet.getString("User_Name");
                    String resultPassword = resultSet.getString("Password");

                    if (username.equals(resultUsername) && password.equals(resultPassword)) {

                        User.getInstance().setUserId(resultUserId);
                        User.getInstance().setUsername(resultUsername);

                        Parent root = FXMLLoader.load(getClass().getResource("/view/Home.fxml"));
                        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                        Scene scene = new Scene(root, 800, 550);
                        stage.setTitle("Home");
                        stage.setScene(scene);
                        stage.show();

                        try {
                            File loginActivity = new File("login_activity.txt");
                            if (loginActivity.createNewFile()) {
                                System.out.println("File created: " + loginActivity.getName());
                            } else {
                                System.out.println("File already exists");
                            }

                            FileWriter loginActivityWriter = new FileWriter(loginActivity, true);
                            loginActivityWriter.write("Login successful for username '" + User.getInstance().getUsername()
                            + "' at " + LocalDateTime.now() + "\n");
                            loginActivityWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    try {
                        ResourceBundle rb = ResourceBundle.getBundle("main/Nat", Locale.getDefault());
                        if (Locale.getDefault().getLanguage().equals("fr")) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle(rb.getString("LoginAttempt"));
                            alert.setContentText(rb.getString("IncorrectUsernameOrPassword"));
                            ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText(rb.getString("Ok"));
                            alert.showAndWait();
                        }
                    } catch (MissingResourceException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Login Attempt");
                        alert.setContentText("Incorrect username or password");
                        alert.showAndWait();
                    }
                    try {
                        File loginActivity = new File("login_activity.txt");
                        if (loginActivity.createNewFile()) {
                            System.out.println("File created: " + loginActivity.getName());
                        } else {
                            System.out.println("File already exists");
                        }

                        FileWriter loginActivityWriter = new FileWriter(loginActivity, true);
                        loginActivityWriter.write("Login unsuccessful for username '" + usernameField.getText()
                                + "' at " + LocalDateTime.now() + "\n");
                        loginActivityWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Attempt");
            alert.setContentText("Please enter your username and password");
            alert.showAndWait();
        }
    }
}