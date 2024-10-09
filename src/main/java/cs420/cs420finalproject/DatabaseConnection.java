package cs420.cs420finalproject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:FarmManagement.db";
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    public static void collectData() {
        // Placeholder for data collection logic from the drones
        System.out.println("Data collected from the farm.");
    }
    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                String sql = "CREATE TABLE IF NOT EXISTS drones (\n"
                        + " droneID integer PRIMARY KEY,\n"
                        + " status text NOT NULL,\n"
                        + " batteryLevel real,\n"
                        + " currentLocation text\n"
                        + ");";
                conn.createStatement().execute(sql);
                System.out.println("Database initialized.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}