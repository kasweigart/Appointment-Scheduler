package utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class is a utility class for creating a query to the database.
 */
public class DatabaseQuery {

    /**
     * static variable holds a prepared statement for a query
     */
    private static PreparedStatement statement;

    /**
     * @param connect takes in a connection object as the first parameter
     * @param sqlStatement takes in an SQL statement as the second parameter
     * @throws SQLException throws an SQL exception when there is a problem setting the prepared statement
     */
    public static void setPreparedStatement(Connection connect, String sqlStatement) throws SQLException {
        statement = connect.prepareStatement(sqlStatement);
    }

    /**
     * @return returns the prepared statement
     */
    public static PreparedStatement getPreparedStatement() {
        return statement;
    }

}
