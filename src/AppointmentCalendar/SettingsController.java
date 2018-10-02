/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import com.sun.javafx.stage.StageHelper;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author estraub
 */
public class SettingsController implements Initializable {

    @FXML
    private Label userLabel;
    @FXML
    private ChoiceBox timezoneSelector;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private AnchorPane anchor;

    @FXML
    private Button backButton;
    
    private ObservableList zones;
    private MainPageController mainPageController;
    private LoginController loginController;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setMainPageController();
        generateTimeZones();
    }   
    
    @FXML
    private void backButtonAction() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void saveButtonAction() {
        User.zoneId = ZoneId.of(timezoneSelector.getSelectionModel().getSelectedItem().toString());
        mainPageController.restart();
    }
    
  

    private void generateTimeZones() {
        zones = FXCollections.observableArrayList(ZoneId.getAvailableZoneIds());
        timezoneSelector.setItems(zones.sorted(Comparator.reverseOrder()));
        
    }
     
    
    private void setMainPageController() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXML/MainPage.fxml"));
            Parent root = loader.load();
            mainPageController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error setting main page");
        }
    }
}
