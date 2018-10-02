/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import com.sun.javafx.stage.StageHelper;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author estraub
 */
public class MainPageController implements Initializable {
    
    Scheduler scheduler = null;
    @FXML
    private Button closeButton;
    @FXML
    private AnchorPane anchor;
    @FXML 
    private GridPane monthCalendar;
    @FXML 
    private GridPane weekCalendar;
    @FXML 
    private RadioButton weekSelection;
    @FXML 
    private RadioButton monthSelection;
    @FXML 
    private Label monthText;
    @FXML 
    private Button nextButton;
    @FXML 
    private Button previousButton;
    @FXML 
    private Button appointmentsButton;
    @FXML
    private ToggleGroup viewMode;
    private LoginController loginController;
    private static boolean week;
    private static LocalDate selectedDate;
    private static boolean systemReboot;

    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        scheduler = new Scheduler(ZonedDateTime.now());
        updateMonthCalendar();
        if (User.firstStart) {
            checkFifteen();
            User.firstStart = false;
        }
        if (systemReboot) {
            if (MainPageController.week) {
                setCalendarView(true);
            } 
            if (selectedDate != null) {
                locateDay();
            } 
            systemReboot = false;
        }
    } 

    private void locateDay() {
        if (MainPageController.week) {
            List calendarObjects = scheduler.getWeekObjects();
            for (int i = 0; i < 7; i++) {
                Scheduler.CalendarDayObject date = (Scheduler.CalendarDayObject) calendarObjects.get(i);
                if (date.getToday().toLocalDate().equals(MainPageController.selectedDate)) {
                    updateWeekCalendar();
                    return;
                }

            }
            scheduler.loadNextWeek();
            locateDay();
        } else {
            List calendarObjects = scheduler.getCalendar();
            for (int i = 0; i < 42; i++) {
                Scheduler.CalendarDayObject date = (Scheduler.CalendarDayObject) calendarObjects.get(i);
                if (date.getToday().toLocalDate().equals(MainPageController.selectedDate)) {
                    updateMonthCalendar();
                    return;
                }
            }
            scheduler.nextMonth();
            locateDay();
        }
    }
    
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    private void checkFifteen() {
        Appointment appointmentWithin15 = scheduler.appointmentWithin15();
        if (appointmentWithin15 != null) {
            alertWindow("You have an appointment within 15 minutes\nTitle: " + appointmentWithin15.getTitle() + "\nStarts at: " + appointmentWithin15.getStart().toLocalTime());
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
    
    public void setMonthLabel(LocalDate localDate) {
        monthText.setText(localDate.getMonth().getDisplayName(TextStyle.FULL, User.locale) + " " + localDate.getYear());
    }

   
    public void updateWeekCalendar() {
        List dayObjects = weekCalendar.getChildren();
        List calendarObjects = scheduler.getWeekObjects();
        for (int i = 0; i < 7; i++) {
            Scheduler.CalendarDayObject day = (Scheduler.CalendarDayObject) calendarObjects.get(i);
            TableView table = (TableView) dayObjects.get(i);
            TableColumn column = table.getVisibleLeafColumn(0);
            column.setText("" + day.getDayOfMonth());
            column.setStyle("-fx-border-color: black; -fx-border-width:0.25; -fx-background-color:lightgrey;");
            ObservableList<Appointment> list = FXCollections.observableArrayList(day.getAppointments());
            if (list.size() < 1) {
                list.add(new Appointment());
            }
            table.setItems(list);
            column.setSortable(false);
        }
        updateWeekLabel();
        
    }
    
    private void updateWeekLabel() {
        List calendarObjects = scheduler.getWeekObjects();
        Scheduler.CalendarDayObject day = (Scheduler.CalendarDayObject) calendarObjects.get(0);
        monthText.setText("Week: " + day.getToday().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }
    
    public void updateMonthCalendar() {
        
        setMonthLabel(LocalDate.of(scheduler.getCurrentYear(), scheduler.getCurrentMonth(), 1));
        List dayObjects = monthCalendar.getChildren();
        List calendarObjects = scheduler.getCalendar();
        for (int j = 0; j < 42; j++) {
            Scheduler.CalendarDayObject day = (Scheduler.CalendarDayObject) calendarObjects.get(j);
            TableView table =  (TableView) dayObjects.get(j);
            TableColumn column = table.getVisibleLeafColumn(0);
            column.setText("" + day.getDayOfMonth());
            column.setStyle("-fx-border-color: black; -fx-border-width:0.25; -fx-background-color:lightgrey;");
            column.setSortable(false);
            ObservableList<Appointment> list = FXCollections.observableArrayList(day.getAppointments());
            if (list.size() < 1) {
                list.add(new Appointment());
            }
            table.setItems(list);

        }
    }
    
    
    /*
     * used to select between day and month view
    */
    @FXML
    private void buttonAction(ActionEvent event) {
        if (event.getSource().equals(nextButton)){
            if (weekCalendar.isVisible()) {
                scheduler.loadNextWeek();
                updateWeekCalendar();
            } else {
                scheduler.nextMonth();
                updateMonthCalendar();
            }
        } else if (event.getSource().equals(previousButton)){
            if (weekCalendar.isVisible()) {
                scheduler.loadPreviousWeek();
                updateWeekCalendar();
            } else { 
                scheduler.previousMonth();
                updateMonthCalendar();
            }
        }
        MainPageController.selectedDate = null;
    }
    
    
    /**
     * 
     * @param event 
     */
    @FXML
    private void setCalendarView(ActionEvent event) {
        if (event.getSource().equals(weekSelection)) {
            monthCalendar.setVisible(false);
            updateWeekCalendar();
            weekCalendar.setVisible(true);
            MainPageController.week = true;
        } else if (event.getSource().equals(monthSelection)) {
            monthCalendar.setVisible(true);
            updateMonthCalendar();
            weekCalendar.setVisible(false);
            MainPageController.week = false;

        }
    }
    

    private void setCalendarView(boolean weekView) {
        if (weekView) {
            weekSelection.setSelected(true);
            monthCalendar.setVisible(false);
            updateWeekCalendar();
            weekCalendar.setVisible(true);
            MainPageController.week = true;
        } else if (!weekView) {
            monthSelection.setSelected(true);
            monthCalendar.setVisible(true);
            updateMonthCalendar();
            weekCalendar.setVisible(false);
            MainPageController.week = false;

        }
    }
    
    /**
     * 
     */
    protected void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 
     */
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchor.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            
        }  
    }
    
    @FXML
    private void appointmentWindow() {
        try {
            if (scheduler.getSelectedDate() == null) {
            return;
            }
        } catch (NullPointerException e) {
            return;
        }
        if (weekSelection.isSelected()) {
            MainPageController.selectedDate = scheduler.selectedDay.toLocalDate();
        }
        
        AppointmentController.loadedDay = Scheduler.selectedDay;
        try {
            Stage customerWindow = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXML/Appointments.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            customerWindow.setScene(scene);
            customerWindow.initModality(Modality.WINDOW_MODAL);
            customerWindow.showAndWait();
        } catch (IOException e) {
            System.out.println("Issue loading appointment window" + e.toString());
        }
    }
    
    @FXML
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
    
    @FXML
    private void reportsWindow() {
        try {
            Stage customerWindow = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXML/Reports.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            customerWindow.setScene(scene);
            customerWindow.initModality(Modality.WINDOW_MODAL);
            customerWindow.showAndWait();
        } catch (IOException e) {
            System.out.println("Issue loading reports window" + e.toString());
        }
    }
    
    
    @FXML
    private void settingsWindow() {
        try {
            Stage customerWindow = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("FXML/Settings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            customerWindow.setScene(scene);
            customerWindow.initModality(Modality.WINDOW_MODAL);
            customerWindow.showAndWait();
        } catch (IOException e) {
            System.out.println("Issue loading settings window" + e.toString());
        }
    }
    
    /**
     * 
     */
    @FXML
    private void logoutButtonAction() {
        MainPageController.selectedDate = null;
        MainPageController.week = false;
        User.reset();
        logout();   
    }
    
    /**
     * 
     */
    @FXML
    private void closeButtonAction() {
        System.exit(0);
    }
    
    private void clearUnderlines() {
        List dayObjects = monthCalendar.getChildren();
        for (int i = 0; i < dayObjects.size(); i++) {
            Label label = (Label) dayObjects.get(i);
            label.setUnderline(false);
            String style = label.getStyle();
            style += "-fx-border-color: black; -fx-border-width: 1;";
            label.setStyle(style);
        }
    }
    
    private void clearWeekUnderlines() {
        List dayObjects = weekCalendar.getChildren();
        for (int i = 0; i < dayObjects.size(); i++) {
            Label label = (Label) dayObjects.get(i);
            label.setStyle("-fx-border-color: black; -fx-border-width: 1;");
            label.setUnderline(false);
        }
    }
    
    @FXML
    private void selectionAction(MouseEvent event) {
        TableView table = (TableView) event.getSource();
        TableColumn column = table.getVisibleLeafColumn(0);
        Appointment appointment = (Appointment) table.getSelectionModel().getSelectedItem();
        List dayObjects = null;
        if (weekCalendar.isVisible()) {
            dayObjects = weekCalendar.getChildren();
            scheduler.setSelectedWeekDayObject(dayObjects.indexOf(table));
        } else {
            dayObjects = monthCalendar.getChildren();
        scheduler.setSelectedMonthDayObject(dayObjects.indexOf(table));
        MainPageController.selectedDate = scheduler.getSelectedDate();
        }
        if (event.getClickCount() == 2) {
            appointmentWindow();
        }
    }
    
    @FXML
    private void calendarSelection(MouseEvent event) {
        List dayObjects;
        int index;
        if (weekCalendar.isVisible()){
            clearWeekUnderlines();
            dayObjects = weekCalendar.getChildren();
            int weekIndex = dayObjects.indexOf(event.getSource());
            index = dayObjects.indexOf(dayObjects.get(weekIndex));
            scheduler.setSelectedWeekDayObject(index);
        } else {
            updateMonthCalendar();
            dayObjects = monthCalendar.getChildren();
            index = dayObjects.indexOf(event.getSource());
            scheduler.setSelectedMonthDayObject(index);
        }
        Label label = (Label) event.getSource();
        label.setUnderline(true);
        label.setStyle("-fx-border-color: black; -fx-border-width: 2;");
        if (event.getClickCount() == 2) {
            appointmentWindow();
        }
    }
    
    /*
    * Used to restart the application after a setting change like the timezone
    */
    protected void restart() {
        List<Stage> stages = StageHelper.getStages();
        stages.get(1).close();
        MainPageController.systemReboot = true;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/MainPage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = stages.get(0);
            stage.setScene(scene);
            stage.setX(400.0);
            
            stage.show();
            
        } catch (IOException e) {
            System.out.println("Couldn't load the main page\n");
            e.printStackTrace();
        }
    }
    

}
