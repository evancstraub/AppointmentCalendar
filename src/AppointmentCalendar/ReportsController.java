/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author estraub
 */
public class ReportsController implements Initializable {

    @FXML
    private ChoiceBox<?> report1ChoiceBox;
    @FXML
    private ChoiceBox<?> report1ChoiceBox2;
    @FXML
    private ChoiceBox<?> report2ChoiceBox;
    @FXML
    private ChoiceBox<?> report3ChoiceBox;
    @FXML
    private Button backButton;
    private MainPageController mainPageController;
    private SQLService sql;
    private ObservableList userList;
    private ObservableList months;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setMainPageController();
        getUsers();
        loadChoiceBoxes();
    }    

    private void loadChoiceBoxes() {
        months = FXCollections.observableArrayList(Month.values());
        report1ChoiceBox.setItems(months);
        report1ChoiceBox2.setItems(userList);
        report2ChoiceBox.setItems(months);
        report3ChoiceBox.setItems(months);
    }
    
    private void getUsers() {
        List users;
        users = sql.getUsers();
        this.userList = FXCollections.observableArrayList(users);
    }
    
    @FXML
    private void report1ButtonAction(ActionEvent event) {
        runScheduleReport();
    }
    
    private void runScheduleReport() {
        User user = (User) userList.get(report1ChoiceBox2.getSelectionModel().getSelectedIndex());
        Month month = (Month) months.get(report1ChoiceBox.getSelectionModel().getSelectedIndex());
        List<Appointment> appointments = sql.getDBMonth(month, user.getId());
        if(appointments.isEmpty() || appointments == null) {
            return;
        }
        List reportList = new ArrayList<String>();
        for (Appointment appointment : appointments) {
            String reportLine = "Appointment ID: " + appointment.getId() + 
                    " Title: " + appointment.getTitle() + 
                    "\t Customer: " + mainPageController.scheduler.findCustomer(appointment.getCustomerId()) + 
                    " Start - End: " + appointment.getStart() + " - " + appointment.getEnd();
            reportList.add(reportLine);
        }
        String reportTitle = "Schedule_" + user.getId() + "_" + month.getDisplayName(TextStyle.FULL, Locale.US) + ".txt";
        String heading = "Report run: " + LocalDateTime.now()  + " Month: " + month.getDisplayName(TextStyle.FULL, Locale.US) + " User: " + user;
        writeReportToFile(reportTitle, reportList, heading, " ");
    }

    @FXML
    private void report2ButtonAction(ActionEvent event) {
        runAppointmentHoursReport();
    }
    
    private void runAppointmentHoursReport() {
        Month month = (Month) months.get(report2ChoiceBox.getSelectionModel().getSelectedIndex());
        LocalDate startDate = LocalDate.of(mainPageController.scheduler.getCurrentYear(), month, 1);
        LocalDate endDate = LocalDate.of(mainPageController.scheduler.getCurrentYear(), month, month.length(Year.isLeap(LocalDate.now().getYear())));
        String query = "SELECT appointment.userId, user.firstName, CONCAT('', SEC_TO_TIME( SUM( TIME_TO_SEC( TIMEDIFF(end, start))))) AS timeSum FROM appointment JOIN user on appointment.userId = user.userId WHERE DATE(start) BETWEEN '" + startDate + "' AND '" + endDate + "' GROUP BY userId;";
        sql.setQuery(query);
        List<List> results = sql.sendQuery();
        if (results.isEmpty() || results == null) {
            return;
        }
        List<String> reportLines = new ArrayList<>();
        for(List<String> result : results) {
            reportLines.add("User: " + result.get(0) + " - " + result.get(1) + " Appointment Hours: " + result.get(2));
        }
        String filename = "Hours_" + month.getDisplayName(TextStyle.FULL, Locale.US) + ".txt";
        String header = "Report run: " + LocalDateTime.now() + " Appointment hours\t  Month: " + month.getDisplayName(TextStyle.FULL, Locale.US);
        String header2 = "User | Total Hours";
        writeReportToFile(filename, reportLines, header, header2, "\n");
    }

    @FXML
    private void report3ButtonAction(ActionEvent event) {
        runTypesReport();
    }
    
    private void runTypesReport(){
        Month month = (Month) months.get(report3ChoiceBox.getSelectionModel().getSelectedIndex());
        LocalDate startDate = LocalDate.of(mainPageController.scheduler.getCurrentYear(), month, 1);
        LocalDate endDate = LocalDate.of(mainPageController.scheduler.getCurrentYear(), month, month.length(Year.isLeap(LocalDate.now().getYear())));
        String query = "SELECT appointmentType, COUNT(appointmentId) AS numberTypes FROM appointment WHERE DATE(start) BETWEEN '" + startDate + "' AND '" + endDate + "' GROUP BY appointmentType";
        sql.setQuery(query);
        List<List> results = sql.sendQuery();
        if (results.isEmpty() || results == null) {
            return;
        }
        List<String> reportLines = new ArrayList();
        
        for (List<String> result : results) {
            String resultingString = result.get(0) + " - " + result.get(1);
            reportLines.add(resultingString);
        }
        String filename = "Types_" + month.getDisplayName(TextStyle.FULL, Locale.US) + ".txt";
        String header = "Report run: " + LocalDateTime.now() + " Appointment Types Month: " + month.getDisplayName(TextStyle.FULL, Locale.US);
        String header2 = "Type | Count";
        writeReportToFile(filename, reportLines, header, header2, "\n");

    }

    @FXML
    private void backButtonAction(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    private void writeReportToFile(String filename, List<String> items, String... header) {
        File file = new File("./reports/" + filename);
        try (FileWriter fw = new FileWriter(file, false); BufferedWriter bw = new BufferedWriter(fw); PrintWriter pw = new PrintWriter(bw);) {
            if (header.length > 0) {
                for (String head : header) {
                    pw.println(head);
                }
            }
            for (String item : items) {
                pw.println(item);
            }
        } catch (IOException e) {
            System.out.println("Error writing to report file");
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
