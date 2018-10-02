/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Evan Straub <evan.c.straub@gmail.com>
 */
public class AppointmentController implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableView<?> appointmentTable;
    @FXML
    private TableColumn<?, ?> appointmentColumn;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ChoiceBox<?> customerChoiceBox;
    @FXML
    private TextField locationField;
    @FXML
    private TextField urlField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField contactField;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label dateLabel;
    @FXML
    private Spinner startHourSpinner;
    @FXML
    private Spinner startMinuteSpinner;
    @FXML
    private Spinner endHourSpinner;
    @FXML
    private Spinner endMinuteSpinner;
    @FXML
    private RadioButton startAmButton;
    @FXML
    private RadioButton startPmButton;
    @FXML
    private RadioButton endAmButton;
    @FXML
    private RadioButton endPmButton;
    
    private MainPageController mainPageController;
    private SQLService sql;
    private Appointment selectedAppointment = null;
    private Customer currentCustomer;
    private ObservableList customers;
    public static ZonedDateTime loadedDay;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setMainPageController();
        loadCustomers();
        updateDateLabel();
        loadAppointments();  
    }    

    private void loadCustomers() {
        customers = (ObservableList) mainPageController.scheduler.getCustomers();
        customers.sort((c1,  c2) -> c1.toString().compareTo(c2.toString())); //Comparator lambda used to sort customers alphebetically 
        customerChoiceBox.setItems(customers);
    }
    
    private void loadAppointments() {
        try {
            ObservableList appointments = FXCollections.observableArrayList(mainPageController.scheduler.getSelectedAppointments());
            appointmentTable.setItems(appointments);
        } catch (NullPointerException e) {
            System.out.println("I found a null exception");
            return;
        }
    }
    
    private void clearAppointment() {
        selectedAppointment = null;
        titleField.clear();
        descriptionField.clear();
        locationField.clear();
        contactField.clear();
        typeField.clear();
        urlField.clear();
        startHourSpinner.getValueFactory().setValue(1);
        startMinuteSpinner.getValueFactory().setValue(0);
        startAmButton.setSelected(false);
        startPmButton.setSelected(false);
        endHourSpinner.getValueFactory().setValue(1);
        endMinuteSpinner.getValueFactory().setValue(0);
        endAmButton.setSelected(false);
        endPmButton.setSelected(false);
        appointmentTable.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void clearButtonAction() {
        if (selectedAppointment == null) {
            return;
        }
        clearAppointment();
    }
    
    private void loadAppointmentFields() {
        titleField.setText(selectedAppointment.getTitle());
        descriptionField.setText(selectedAppointment.getDescription());
        locationField.setText(selectedAppointment.getLocation());
        contactField.setText(selectedAppointment.getContact());
        customerChoiceBox.getSelectionModel().select(customers.indexOf(selectedAppointment.getCustomer()));
        urlField.setText(selectedAppointment.getUrl());
        startMinuteSpinner.getValueFactory().setValue(selectedAppointment.getStart().getMinute());
        endMinuteSpinner.getValueFactory().setValue(selectedAppointment.getEnd().getMinute());
        typeField.setText(selectedAppointment.getType());
        if (selectedAppointment.getStart().getHour() > 12) {
            startHourSpinner.getValueFactory().setValue(selectedAppointment.getStart().getHour() - 12);
            startPmButton.setSelected(true);
        } else {
            startHourSpinner.getValueFactory().setValue(selectedAppointment.getStart().getHour());
            startAmButton.setSelected(true);
        }
        if (selectedAppointment.getEnd().getHour() > 12)  {
            endHourSpinner.getValueFactory().setValue(selectedAppointment.getEnd().getHour() - 12);
            endPmButton.setSelected(true);
        } else {
            endHourSpinner.getValueFactory().setValue(selectedAppointment.getEnd().getHour());
            endAmButton.setSelected(true);
        }
    }
    

    @FXML
    private void saveButtonAction(ActionEvent event) {
        if (checkEmptyFields()) {
            alertWindow("There are empty mandatory fields");
            return;
        }
        Appointment appointment = null;
        currentCustomer = (Customer) customers.get(customerChoiceBox.getSelectionModel().getSelectedIndex());
        try {
            if (selectedAppointment == null) {
                try {
                    appointment = new Appointment(titleField.getText(), descriptionField.getText(), locationField.getText(), contactField.getText(), currentCustomer, urlField.getText(), typeField.getText(), setStartTime(), setEndTime());
                    if (mainPageController.scheduler.addAppointment(appointment)) {
                        loadAppointments();
                    } else {
                        alertWindow("There is already an appointment during that time");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    alertWindow("No Customer Selected");
                } 
            } else {
                int id = selectedAppointment.getId();
                Appointment newAppointment = new Appointment(id,
                        titleField.getText(), 
                        descriptionField.getText(), 
                        locationField.getText(), 
                        contactField.getText(), 
                        currentCustomer.getId(), 
                        urlField.getText(),
                        typeField.getText(),
                        setStartTime(), 
                        setEndTime());
                if (mainPageController.scheduler.updateAppointment(selectedAppointment, newAppointment)) {
                    loadAppointments();
                } else {
                    alertWindow("There is already an appointment during that time");
                }
            } 
        } catch (IllegalArgumentException e) {
            alertWindow(e.getMessage());
        }
        clearButtonAction();
    }
    
    private boolean checkEmptyFields() {
        if (titleField.getText().isEmpty() || typeField.getText().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean alertWindow(String description) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        String alertHeader = String.format(description);
        alert.setHeaderText(alertHeader);
        alert.showAndWait();
        return false;
    }

    @FXML
    private void deleteButtonAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete: " + selectedAppointment.getTitle());
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(yes, no);
        
        Optional<ButtonType> option = alert.showAndWait();
        
        if (option.get() == yes) {
            deleteAppointment(selectedAppointment);
        } else {
            return;
        }
    }

    @FXML
    private void closeButtonAction(ActionEvent event) {
        mainPageController.restart();
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    private void deleteAppointment(Appointment appointment) {
        mainPageController.scheduler.deleteAppointment(appointment);
        loadAppointments();
    }
    
    @FXML
    private void selectAppointmentAction() {
        try{
            selectedAppointment = (Appointment) appointmentTable.getSelectionModel().getSelectedItem();
            loadAppointmentFields();
        } catch (NullPointerException e) {
            
        }
        
    }
    
    private ZonedDateTime setStartTime() {
        int startHour;
        int startMinute = Integer.parseInt(startMinuteSpinner.getValue().toString());
        if (startPmButton.isSelected() && (Integer.parseInt(startHourSpinner.getValue().toString()) < 12)) {
            startHour = Integer.parseInt(startHourSpinner.getValue().toString()) + 12;
        } else {
            startHour = Integer.parseInt(startHourSpinner.getValue().toString());
        }
        LocalTime localStart = LocalTime.of(startHour, startMinute);
        ZonedDateTime startTime = ZonedDateTime.of(loadedDay.toLocalDate(), localStart, User.zoneId);
        checkHours(startTime);
        return startTime;
    }
    
    private ZonedDateTime setEndTime() {
        int endHour;
        int endMinute = Integer.parseInt(endMinuteSpinner.getValue().toString());
        if (endPmButton.isSelected() && (Integer.parseInt(endHourSpinner.getValue().toString()) < 12)) {
            endHour = Integer.parseInt(endHourSpinner.getValue().toString()) + 12;
        } else {
            endHour = Integer.parseInt(endHourSpinner.getValue().toString());
        }
        LocalTime localEnd = LocalTime.of(endHour, endMinute);
        ZonedDateTime endTime = ZonedDateTime.of(loadedDay.toLocalDate(), localEnd, User.zoneId);
        checkHours(endTime);
        return endTime;
    }
    
    private void checkHours(ZonedDateTime time) {
        if(time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("You're attempting to start this appointment on a weekend");
        } else if (time.getHour() < 7 || time.getHour() > 18) {
            throw new IllegalArgumentException("You're attempting to schedule this appointment outside of business hours");
        }
    }
    
    private void updateDateLabel() {
        String date = mainPageController.scheduler.getSelectedDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)).toString();
        dateLabel.setText(date);
    }
    
    @FXML
    private void viewButtonAction() {
        Customer customer = (Customer) customerChoiceBox.getValue();
        CustomersController.loadCustomer = customer;
        customerWindow();
    }
   
    private void customerWindow () {
        try {
            Stage customerWindow = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXML/Customers.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            customerWindow.setScene(scene);
            customerWindow.initModality(Modality.WINDOW_MODAL);
            customerWindow.showAndWait();
        } catch (IOException e) {
            System.out.println("Issue loading customer window" + e.toString());
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
