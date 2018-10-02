/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author estraub
 */
public class CustomersController implements Initializable {
    
    
    @FXML private Button closeButton;
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private TextField address2Field;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private TextField countryField;
    @FXML private TextField phoneField;
    @FXML private TextField searchField;
    @FXML private CheckBox activeCheckbox;
    @FXML private TableView<Customer> customerTable;
    private ObservableList<Customer> customerList;
    private MainPageController mainPageController;
    private SQLService sql;
    private Customer currentCustomer;
    public static Customer loadCustomer;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button searchButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setMainPageController();
        updateCustomers();
        if (loadCustomer != null) {
            setCurrentCustomer(loadCustomer);
            updateFields();
        }
    }

    private boolean addCustomer(Customer customer) {
        
        return true;
    }
    
    private boolean updateCustomer(Customer customer, Customer update) {
        return false;
    }
    

    
    @FXML
    private void closeButtonAction() {
        loadCustomer = null;
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        
    }
    
    private void closeWindow() {
        
    }
    
    @FXML
    private void searchButtonAction() {
        clearButtonAction();
        String search = searchField.getText();
        Customer searchedCustomer = null;
        try {
            int searchId = Integer.parseInt(search);
            searchedCustomer = mainPageController.scheduler.findCustomer(searchId);
        } catch (NumberFormatException e) {
            searchedCustomer = mainPageController.scheduler.findCustomer(search);
        }
        if (searchedCustomer == null) {
            alertWindow("Customer Not Found", "The customer you were searching for was not found");
        } else {
            setCurrentCustomer(searchedCustomer);
            updateFields();
        }
    }
    
    @FXML
    private void deleteButtonAction() {
        if (deleteConfirmation()) {
            mainPageController.scheduler.removeCustomer(currentCustomer);
            updateCustomers();
            clearButtonAction();
        } else {
            return;
        }
    }
    
    
    @FXML
    private void clearButtonAction() {
        nameField.clear();
        addressField.clear();
        address2Field.clear();
        cityField.clear();
        postalCodeField.clear();
        countryField.clear();
        phoneField.clear();
        currentCustomer = null;
        activeCheckbox.setSelected(false);
        customerTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void saveButtonAction () {
        if (isFieldEmpty()) {
            alertWindow("Empty Fields", "You must fill in all fields");
            return;
        }
        if (currentCustomer == null) {
            createNewCustomer();
        } else {
            updateCustomer();
        }
    }
    
    private boolean isFieldEmpty() {
        if (nameField.getText().isEmpty() || 
                addressField.getText().isEmpty() || 
                cityField.getText().isEmpty() ||
                postalCodeField.getText().isEmpty() ||
                countryField.getText().isEmpty() ||
                phoneField.getText().isEmpty()) {
            return true;
        }
        return false;
    }
    
    private void createNewCustomer () {
        Customer customer;
        
        if (mainPageController.scheduler.validateNewCustomer(nameField.getText())) {
            try {
                if(!duplicateNameWindow(nameField.getText())) {
                    return;
                }
            } catch (RuntimeException e) {
                return;
            }
        }
        customer = new Customer(nameField.getText(), true);
        Customer.Location location = customer.new Location(addressField.getText(), address2Field.getText(), cityField.getText(), postalCodeField.getText(), countryField.getText(), phoneField.getText());
        customer.setLocation(location);
        if (activeCheckbox.isSelected()) {
            customer.setActive(Boolean.TRUE);
        } else {
            customer.setActive(Boolean.FALSE);
        }
        mainPageController.scheduler.addCustomer(customer);
        mainPageController.scheduler.sql.sendDbUpdate(toSQL(customer));
        updateCustomers();
        setCurrentCustomer(customer);
    }
    
    private boolean duplicateNameWindow(String name) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Duplicate Name");
        String alertHeader = String.format("There is already a customer named %s. Are you sure you want to create a new customer?", name);
        alert.setHeaderText(alertHeader);
        ButtonType create = new ButtonType("Create");
        ButtonType cancel = new ButtonType("Cancel");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(create, cancel);
        
        Optional<ButtonType> option = alert.showAndWait();
        
        if (option.get() == create) {
            return true;
        }
        return false;
    }
    
    private boolean deleteConfirmation() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete: " + currentCustomer.getName());
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(yes, no);
        
        Optional<ButtonType> option = alert.showAndWait();
        
        if (option.get() == yes) {
            return true;
        }
        return false;
    }
    
    private void setCurrentCustomer(Customer customer) {
        if (currentCustomer == null && mainPageController.scheduler.hasCustomer(customer)) {
            currentCustomer = customer;
            customerTable.getSelectionModel().clearSelection();
            customerTable.getSelectionModel().select(customer);
        } 
    }
    
    @FXML
    private void selectCustomer() {
        currentCustomer = customerTable.getSelectionModel().selectedItemProperty().get();
        updateFields();
    }
    
    private void alertWindow(String title, String alertText) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(alertText);
        alert.showAndWait();
    }
    
    private void updateCustomer() {
        Customer customer = new Customer(currentCustomer.getId(), nameField.getText(), activeCheckbox.isSelected());
        Customer.Location location = customer.new Location(addressField.getText(), address2Field.getText(), cityField.getText(), postalCodeField.getText(), countryField.getText(), phoneField.getText());
        customer.setLocation(location);
        mainPageController.scheduler.updateCustomer(customer.getId(), customer);
        mainPageController.scheduler.sql.sendDbUpdate(toSQL(customer));
        clearButtonAction();
        updateCustomers();
    }
    
    /**
     * method to create SQL string for customer
     * @param customer
     * @return 
     */
    private List<String> toSQL(Customer customer) {
        List<String> result = new ArrayList<>();
        
        int addressId = sql.getId("address", addressField.getText());
        int cityId;
        int countryId;
        if (addressId == -1) {
            cityId = sql.getId("city", cityField.getText());
            if (cityId == -1) {
                countryId = sql.getId("country", countryField.getText());
                if (countryId == -1) {
                    countryId = sql.getNextId("country");
                    result.add("INSERT INTO country VALUES(" + countryId + ", '" + customer.location.getCountry() + "', NOW(), '" + User.username + "', NOW(), '" + User.username + "');");
                }
                cityId = sql.getNextId("city");
                result.add("INSERT INTO city VALUES(" + cityId + ", '" + customer.location.getCity() + "', " + countryId + ", NOW(), '" + User.username + "', NOW(), '" + User.username + "');"); 
            }
            addressId = sql.getNextId("address");
            result.add("INSERT INTO address VALUES(" + addressId + ", '" + 
                    customer.location.getAddress() + "', '" + 
                    customer.location.getAddress2() + "', " + cityId + ", '" + 
                    customer.location.getZip() + "', '" +
                    customer.location.getPhone() + "',  " + "NOW(), '" + 
                    User.username + "', NOW(), '" + 
                    User.username + "');");
        }
        if (customer.isNewCustomer()) {
            result.add("INSERT INTO customer VALUES(" + customer.getId() + ", '" + customer.getName() + "', " + addressId + ", " + customer.isActive() + ", NOW(),'" + User.username + "', NOW(), '" + User.username + "');");
        } else {
             result.add("UPDATE customer SET customerName = '" + nameField.getText() + "', addressId = " + addressId + ", active =" + customer.isActive() + ", lastUpdate = NOW(), lastUpdateBy = '" + User.username + "' WHERE customerId = " + customer.getId() + ";");
        }
        return result;
    }
    
    private void updateCustomers() {
        System.out.println("Updating customers");
        customerList = (ObservableList) mainPageController.scheduler.getCustomers();
        customerTable.setItems(customerList.sorted());
    }
    
    private void updateFields() {
        
        nameField.setText(currentCustomer.getName());
        addressField.setText(currentCustomer.location.getAddress());
        address2Field.setText(currentCustomer.location.getAddress2());
        cityField.setText(currentCustomer.location.getCity());
        postalCodeField.setText(currentCustomer.location.getZip());
        countryField.setText(currentCustomer.location.getCountry());
        phoneField.setText(currentCustomer.location.getPhone());
        if (currentCustomer.isActive()) {
            activeCheckbox.setSelected(true);
        } else {
            activeCheckbox.setSelected(false);
        }
    }

    @FXML
    public void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER){

        }
    }
    
    private void setMainPageController() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXML/MainPage.fxml"));
            Parent root = loader.load();
            this.mainPageController = loader.getController();
            this.sql = this.mainPageController.scheduler.sql;
        } catch (IOException e) {
            System.out.println("Error setting main page");
        }
    }
}
