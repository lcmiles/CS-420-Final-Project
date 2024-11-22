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
            //System.out.println("Connected to SQLite database.");
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
            System.out.println("Crop growth data inserted: " + cropData.getTimestamp() + ", Growth Level: " + cropData.getGrowthLevel() + ", Field: " + cropData.getFieldId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Insert soil moisture data into the database
    public static void insertSoilMoistureData(SoilMoistureData soilData) {
        String sql = "INSERT INTO soil_moisture(timestamp, moisture_level, field_id) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, soilData.getTimestamp());
            pstmt.setInt(2, soilData.getMoistureLevel());
            pstmt.setString(3, soilData.getFieldId());
            pstmt.executeUpdate();
            System.out.println("Soil moisture data inserted: " + soilData.getTimestamp() + ", Moisture Level: " + soilData.getMoistureLevel() + ", Field: " + soilData.getFieldId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Get all soil moisture data
    public static List<SoilMoistureData> getSoilMoistureData() {
        List<SoilMoistureData> dataList = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT timestamp, moisture_level, field_id FROM soil_moisture ORDER BY timestamp;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String timestamp = rs.getString("timestamp");
                int moistureLevel = rs.getInt("moisture_level");
                String fieldId = rs.getString("field_id");
                dataList.add(new SoilMoistureData(timestamp, fieldId, moistureLevel));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving soil moisture data: " + e.getMessage());
        }
        return dataList;
    }

    // Insert livestock feeding data into the database
    public static void insertLivestockFeedingData(LivestockFeedingData livestockData) {
        String sql = "INSERT INTO livestock_feeding(timestamp, feeding_level, pasture_id) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, livestockData.getTimestamp());
            pstmt.setInt(2, livestockData.getFeedingLevel());
            pstmt.setString(3, livestockData.getPastureId());
            pstmt.executeUpdate();
            System.out.println("Livestock feeding data inserted: " + livestockData.getTimestamp() + ", Feeding Level: " + livestockData.getFeedingLevel()  + ", Pasture: " + livestockData.getPastureId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Get all livestock feeding data
    public static List<LivestockFeedingData> getLivestockFeedingData() {
        List<LivestockFeedingData> dataList = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT timestamp, feeding_level, pasture_id FROM livestock_feeding ORDER BY timestamp;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String timestamp = rs.getString("timestamp");
                int feedingLevel = rs.getInt("feeding_level");
                String pastureId = rs.getString("pasture_id");
                dataList.add(new LivestockFeedingData(timestamp, pastureId, feedingLevel));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving livestock feeding data: " + e.getMessage());
        }
        return dataList;
    }

    // Insert pest data into the database
    public static void insertPestData(PestData pestData) {
        String sql = "INSERT INTO pest_data(timestamp, pest_level, field_id) VALUES(?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pestData.getTimestamp());
            pstmt.setInt(2, pestData.getPestLevel());
            pstmt.setString(3, pestData.getFieldId());
            pstmt.executeUpdate();
            System.out.println("Pest data inserted: " + pestData.getTimestamp() + ", Pest Level: " + pestData.getPestLevel()  + ", Field: " + pestData.getFieldId());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Get all pest data
    public static List<PestData> getPestData() {
        List<PestData> dataList = new ArrayList<>();
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT timestamp, pest_level, field_id FROM pest_data ORDER BY timestamp;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String timestamp = rs.getString("timestamp");
                int pestLevel = rs.getInt("pest_level");
                String fieldId = rs.getString("field_id");
                dataList.add(new PestData(timestamp, fieldId, pestLevel));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving pest data: " + e.getMessage());
        }
        return dataList;
    }

    public static void insertItem(Item item) {
        String sql = "INSERT INTO items (name, type, x, y, length, width, price) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getType());
            pstmt.setDouble(3, item.getX());
            pstmt.setDouble(4, item.getY());
            pstmt.setDouble(5, item.getLength());
            pstmt.setDouble(6, item.getWidth());
            pstmt.setDouble(7, item.getPrice()); // Insert price
            pstmt.executeUpdate();
            System.out.println("Item inserted: " + item.getName());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Edit an existing item
    public static void updateItem(Item item, String originalName) {
        String sql = "UPDATE items SET name = ?, type = ?, x = ?, y = ?, length = ?, width = ?, price = ? WHERE name = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getType());
            pstmt.setDouble(3, item.getX());
            pstmt.setDouble(4, item.getY());
            pstmt.setDouble(5, item.getLength());
            pstmt.setDouble(6, item.getWidth());
            pstmt.setDouble(7, item.getPrice()); // Update price
            pstmt.setString(8, originalName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteItem(String itemName) {
        String sql = "DELETE FROM items WHERE name = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);  // Make sure to pass the item name
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Delete all relationships where this item is contained in a container
    public static void deleteContainedItemsRelationships(String itemToDelete) {
        String sql = "DELETE FROM contained_items WHERE container_name = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemToDelete);
            pstmt.executeUpdate();
            System.out.println("Deleted contained item relationships for: " + itemToDelete);
        } catch (SQLException e) {
            System.err.println("Error deleting contained item relationships: " + e.getMessage());
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

    // Method to check if an item name already exists in the database
    public static boolean isItemNameTaken(String itemName) {
        String sql = "SELECT COUNT(*) FROM items WHERE name = ?"; // Assuming 'items' is the table containing item data

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName); // Set the item name in the query
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1); // Get the count of items with the same name
                return count > 0; // Return true if count > 0 (meaning the name exists)
            }
        } catch (SQLException e) {
            System.out.println("Error checking item name: " + e.getMessage());
        }

        return false; // Return false if no item with that name is found
    }

    public static List<Item> getItems() {
        List<Item> items = new ArrayList<>();
        Map<String, Container> containerMap = new HashMap<>();

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT * FROM items ORDER BY id";
            ResultSet rs = stmt.executeQuery(sql);

            // First pass: Add items to their respective lists and check if they are containers
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                double price = rs.getDouble("price");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double length = rs.getDouble("length");  // Fetch length
                double width = rs.getDouble("width");    // Fetch width

                // Create an item with length and width
                Item item = new Item(name, type, price, x, y, length, width);

                // Check if the item is a container
                if (isContainer(name)) {
                    Container container = new Container(name, type, price, x, y, length, width);
                    containerMap.put(name, container);  // Add the container to the map
                    items.add(container);  // Add the container to the list
                } else {
                    items.add(item);  // Regular item, add to the list
                }
            }

            // Second pass: Add contained items to their respective containers
            sql = "SELECT container_name, item_name FROM contained_items";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String containerName = rs.getString("container_name");
                String itemName = rs.getString("item_name");

                Container container = containerMap.get(containerName);
                if (container != null) {
                    Item item = getItemByName(itemName); // Retrieve the item by name
                    if (item != null) {
                        container.addItem(item); // Add the item to the container's containedItems list
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving items: " + e.getMessage());
        }

        return items;
    }

    public static boolean isContainer(String itemName) {
        String sql = "SELECT COUNT(*) FROM contained_items WHERE container_name = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // If count > 0, it's a container
            }
        } catch (SQLException e) {
            System.out.println("Error checking if item is a container: " + e.getMessage());
        }
        return false;
    }

    public static Item getItemByName(String name) {
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM items WHERE name = ?")) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type");
                double price = rs.getDouble("price");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double length = rs.getDouble("length");  // Retrieve length
                double width = rs.getDouble("width");    // Retrieve width
                return new Item(name, type, price, x, y, length, width) {
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

    public static Item getItemID(int id) {
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM items WHERE id = ?")) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                double price = rs.getDouble("price");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double length = rs.getDouble("length");  // Retrieve length
                double width = rs.getDouble("width");    // Retrieve width
                return new Item(name, type, price, x, y, length, width) {
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

    public static int getItemIDByName(String name) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT id FROM items WHERE name = ?")) {

            pstmt.setString(1, name);  // Bind the name to the query

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");  // Return the item ID
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;  // Return -1 if no item with the given name is found
    }


    // Get a list of all contained items (i.e., items that are part of containers)
    public static List<Item> getContainedItems() {
        List<Item> containedItems = new ArrayList<>();

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT item_name FROM contained_items";
            ResultSet rs = stmt.executeQuery(sql);

            // Retrieve each contained item and add it to the list
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                Item item = getItemByName(itemName);  // Retrieve item by its name
                if (item != null) {
                    containedItems.add(item);  // Add item to the list
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving contained items: " + e.getMessage());
        }

        return containedItems;
    }

    // Get the container for a specific item
    public static Container getContainerForItem(Item item) {
        Container container = null;
        String sql = "SELECT container_name FROM contained_items WHERE item_name = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            ResultSet rs = pstmt.executeQuery();

            // If a container is found, fetch it from the database
            if (rs.next()) {
                String containerName = rs.getString("container_name");
                container = getContainerByName(containerName);  // Method to get container by its name
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving container for item: " + e.getMessage());
        }
        return container;
    }

    // Helper method to get a container by its name
    public static Container getContainerByName(String containerName) {
        for (Item item : getItems()) {
            if (item instanceof Container && item.getName().equals(containerName)) {
                return (Container) item;
            }
        }
        return null;
    }

    public static List<Item> getContainedItemsForContainer(Item item) {
        List<Item> containedItems = new ArrayList<>();

        // Only proceed if the item is a container
        if (item instanceof Container) {
            String sql = "SELECT item_name FROM contained_items WHERE container_name = ?";

            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item.getName());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String itemName = rs.getString("item_name");
                    Item containedItem = getItemByName(itemName); // Fetch item details
                    if (containedItem != null) {
                        containedItems.add(containedItem);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error fetching contained items for container: " + e.getMessage());
            }
        }

        return containedItems;
    }

    public static void insertFlightPlan(List<String> taskOrder) {
        String deleteSql = "DELETE FROM flight_plan"; // Delete existing flight plan
        String insertSql = "INSERT INTO flight_plan(task_order, task_name) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement deletePstmt = conn.prepareStatement(deleteSql);
             PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {

            // Delete existing flight plan data
            deletePstmt.executeUpdate();

            // Insert new flight plan
            int order = 1;
            for (String task : taskOrder) {
                insertPstmt.setInt(1, order);
                insertPstmt.setString(2, task);
                insertPstmt.executeUpdate();
                order++;
            }

            System.out.println("Flight plan saved successfully.");
        } catch (SQLException e) {
            System.err.println("Error saving flight plan: " + e.getMessage());
        }
    }

    public static List<String> getFlightPlan() {
        List<String> flightPlan = new ArrayList<>();
        String sql = "SELECT task_name FROM flight_plan ORDER BY task_order;";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String taskName = rs.getString("task_name");
                flightPlan.add(taskName);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving flight plan: " + e.getMessage());
        }
        return flightPlan;
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
                        " price REAL DEFAULT 0.0,\n" +
                        " x REAL NOT NULL,\n" +
                        " y REAL NOT NULL,\n" +
                        " length REAL NOT NULL,\n" +
                        " width REAL NOT NULL\n" +
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
                System.out.println("Database initialized.");
                String soilMoistureSql = "CREATE TABLE IF NOT EXISTS soil_moisture (\n" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " timestamp TEXT NOT NULL,\n" +
                        " moisture_level INTEGER NOT NULL,\n" +
                        " field_id TEXT NOT NULL\n" +
                        ");";
                conn.createStatement().execute(soilMoistureSql);
                String livestockFeedingSql = "CREATE TABLE IF NOT EXISTS livestock_feeding (\n" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " timestamp TEXT NOT NULL,\n" +
                        " feeding_level INTEGER NOT NULL,\n" +
                        " pasture_id TEXT NOT NULL\n" +
                        ");";
                conn.createStatement().execute(livestockFeedingSql);
                String pestDataSql = "CREATE TABLE IF NOT EXISTS pest_data (\n" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " timestamp TEXT NOT NULL,\n" +
                        " pest_level INTEGER NOT NULL,\n" +
                        " field_id TEXT NOT NULL\n" +
                        ");";
                conn.createStatement().execute(pestDataSql);
                String flightPlanSql = "CREATE TABLE IF NOT EXISTS flight_plan (\n" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        " task_name TEXT NOT NULL,\n" +
                        " task_order INTEGER NOT NULL\n" +
                        ");";
                conn.createStatement().execute(flightPlanSql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}