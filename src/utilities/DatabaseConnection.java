package utilities;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * This class is a utility class for connecting to the database.
 */
public class DatabaseConnection {

    /**
     * static connection variable that holds the connection to the database
     */
    private static Connection connect;

    /**
     * static function to start the connection to the database
     */
    public static void startConnection() {

        try {
            Connection connect = DriverManager.getConnection("jdbc:mysql://wgudb.ucertify.com:3306/WJ08aI9", "U08aI9",
                    "53689235081");
            setConnection(connect);
            System.out.println("Connection successful");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return returns the connection to the database
     */
    public static Connection getConnection() {
        return connect;
    }

    /**
     * @param connect sets the connection to the database
     */
    public static void setConnection(Connection connect) {
        DatabaseConnection.connect = connect;
    }
}
