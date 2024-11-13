package cs420.cs420finalproject;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddItemController {

    @FXML private TextField itemNameField;
    @FXML private TextField xField;
    @FXML private TextField yField;
    @FXML private TextField itemTypeField; // Replaced ComboBox with a TextField for item type input
    private Item newItem;
    private boolean itemCreated = false;

    @FXML public void initialize() {
        // Set default values or initializations for other fields if needed
    }

    @FXML public void onCreateItem() {
        String itemType = itemTypeField.getText();
        String itemName = itemNameField.getText();
        double x = 0, y = 0;
        try {
            x = Double.parseDouble(xField.getText());
            y = Double.parseDouble(yField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Invalid coordinates entered.");
            return;
        }
        newItem = new Item(itemName, itemType, x, y) {
            @Override public void saveToDatabase() {
                // Implement database save logic here (e.g., save newItem to your database)
            }
        };
        itemCreated = true;
        System.out.println("Item created: " + newItem);
        closePopup();
    }

    public Item getItem() {
        return newItem;
    }

    public boolean isItemCreated() {
        return itemCreated;
    }

    private void closePopup() {
        Stage stage = (Stage) itemTypeField.getScene().getWindow();
        stage.close();
    }
}