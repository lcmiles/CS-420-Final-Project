package cs420.cs420finalproject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static List<CropGrowthData> getCropGrowthData() {
        List<CropGrowthData> dataList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            String sql = "SELECT timestamp, growth_level, field_id FROM crop_growth ORDER BY timestamp;";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String timestamp = rs.getString("timestamp");
                int growthLevel = rs.getInt("growth_level");
                String fieldId = rs.getString("field_id");
                dataList.add(new CropGrowthData(timestamp, fieldId, growthLevel)); // Include growth level
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving crop growth data: " + e.getMessage());
        }

        return dataList;
    }

    public static void insertCropGrowthData(CropGrowthData cropData) {
        String sql = "INSERT INTO crop_growth(timestamp, growth_level, field_id) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cropData.getTimestamp());
            pstmt.setInt(2, cropData.getGrowthLevel());
            pstmt.setString(3, cropData.getFieldId()); // Add field ID
            pstmt.executeUpdate();
            System.out.println("Data inserted: " + cropData.getTimestamp() + ", Growth Level: " + cropData.getGrowthLevel() + ", Field: " + cropData.getFieldId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                // Create the drones table
                String dronesSql = "CREATE TABLE IF NOT EXISTS drones (\n"
                        + " droneID integer PRIMARY KEY,\n"
                        + " status text NOT NULL,\n"
                        + " batteryLevel real,\n"
                        + " currentLocation text\n"
                        + ");";
                conn.createStatement().execute(dronesSql);

                // Create the crop_growth table with the necessary columns
                String cropGrowthSql = "CREATE TABLE IF NOT EXISTS crop_growth (\n"
                        + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                        + " timestamp text NOT NULL,\n"
                        + " growth_level integer NOT NULL,\n"
                        + " field_id text NOT NULL\n" // Add field_id column
                        + ");";
                conn.createStatement().execute(cropGrowthSql);

                System.out.println("Database initialized.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}