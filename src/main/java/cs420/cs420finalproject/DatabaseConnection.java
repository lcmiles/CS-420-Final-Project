package cs420.cs420finalproject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:FarmManagement.db";

    // Connect to SQLite database
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

    // Get all crop growth data
    public static List<CropGrowthData> getCropGrowthData() {
        List<CropGrowthData> dataList = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT timestamp, growth_level, field_id FROM crop_growth ORDER BY timestamp;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String timestamp = rs.getString("timestamp");
                int growthLevel = rs.getInt("growth_level");
                String fieldId = rs.getString("field_id");
                dataList.add(new CropGrowthData(timestamp, fieldId, growthLevel));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving crop growth data: " + e.getMessage());
        }
        return dataList;
    }

    // Insert crop growth data into the database
    public static void insertCropGrowthData(CropGrowthData cropData) {
        String sql = "INSERT INTO crop_growth(timestamp, growth_level, field_id) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cropData.getTimestamp());
            pstmt.setInt(2, cropData.getGrowthLevel());
            pstmt.setString(3, cropData.getFieldId());
            pstmt.executeUpdate();
            System.out.println("Data inserted: " + cropData.getTimestamp() + ", Growth Level: " + cropData.getGrowthLevel() + ", Field: " + cropData.getFieldId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Insert item into the database (no recursion needed now)
    public static void insertItem(Item item) {
        String sql = "INSERT INTO items (name, type, x, y) VALUES(?, ?, ?, ?)";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item.getName());
                pstmt.setString(2, item.getType());
                pstmt.setDouble(3, item.getX());
                pstmt.setDouble(4, item.getY());
                pstmt.executeUpdate();
                System.out.println("Item inserted: " + item.getName());
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
    }


    public static void insertContainedItem(Container container, Item item) {
        String sql = "INSERT INTO contained_items (container_name, item_name) VALUES(?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, container.getName());
            pstmt.setString(2, item.getName());
            pstmt.executeUpdate();
            System.out.println("Contained item inserted: " + item.getName() + " in container: " + container.getName());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static List<Item> getItems() {
        List<Item> items = new ArrayList<>();
        Map<String, Container> containerMap = new HashMap<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM items ORDER BY id";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                if ("container".equals(type)) {
                    Container container = new Container(name, type, x, y);
                    containerMap.put(name, container);
                    items.add(container);
                } else {
                    Item item = new Item(name, type, x, y) {
                        @Override
                        public void saveToDatabase() {
                            insertItem(this);
                        }
                    };
                    items.add(item);
                }
            }

            // Load contained items for each container
            sql = "SELECT container_name, item_name FROM contained_items";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String containerName = rs.getString("container_name");
                String itemName = rs.getString("item_name");
                Container container = containerMap.get(containerName);
                if (container != null) {
                    Item item = getItemByName(itemName);
                    if (item != null) container.addItem(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving items: " + e.getMessage());
        }
        return items;
    }


    // Get item by name from the database
    public static Item getItemByName(String name) {
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM items WHERE name = ?")) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                return new Item(name, type, x, y) {
                    @Override
                    public void saveToDatabase() {
                        insertItem(this); // Save to database
                    }
                };
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Initialize the database (tables creation)
    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                // Create the items table
                String itemsSql = "CREATE TABLE IF NOT EXISTS items (\n" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " name TEXT NOT NULL,\n" +
                        " type TEXT NOT NULL,\n" +
                        " x REAL NOT NULL,\n" +
                        " y REAL NOT NULL\n" +
                        ");";
                conn.createStatement().execute(itemsSql);
                // Create the crop_growth table
                String cropGrowthSql = "CREATE TABLE IF NOT EXISTS crop_growth (\n" +
                        " id integer PRIMARY KEY AUTOINCREMENT,\n" +
                        " timestamp text NOT NULL,\n" +
                        " growth_level integer NOT NULL,\n" +
                        " field_id text NOT NULL\n" + // Add field_id column
                        ");";
                conn.createStatement().execute(cropGrowthSql);
                // Create the contained items table
                String containedItemsSql = "CREATE TABLE IF NOT EXISTS contained_items (\n" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " container_name TEXT NOT NULL,\n" +
                        " item_name TEXT NOT NULL,\n" +
                        " FOREIGN KEY(container_name) REFERENCES items(name),\n" +
                        " FOREIGN KEY(item_name) REFERENCES items(name)\n" +
                        ");";
                conn.createStatement().execute(containedItemsSql);
                System.out.println("Database initialized with contained_items table.");
                System.out.println("Database initialized.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}