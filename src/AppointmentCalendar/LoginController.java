/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.io.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
/**
 * FXML Controller class
 *
 * @author estraub
 */
public class LoginController implements Initializable {

    SQLService sql = null;
    private Locale locale;
    @FXML private AnchorPane anchor;
    @FXML private SplitMenuButton languageMenu;
    @FXML private MenuItem en;
    @FXML private MenuItem es;
    @FXML private Label loginText;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label usernameText;
    @FXML private Label passwordText;
    @FXML private Label errorText;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;

    
    /*
    * Error messages 
    */
    private String userError;
    private String sqlError;
    private String driverError;
    
    private char[] password;
    private String logFile = "logs/login.log";
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        locale = Locale.getDefault();
        if(User.firstStart) {
            User.zoneId = Calendar.getInstance().getTimeZone().toZoneId();
            User.locale = locale;
            updateLanguage();
            loginButton.setDisable(true);
            sql = new SQLService();
            sql.setServer("server-IP");
            sql.setUsername("");
            sql.setPassword("");
            sql.setDatabase("");
        }
        
    }   
    
    /**
     * 
     * @param username
     * @return 
     */
    private Boolean checkUsername(String username) {
        List results = null;
        try {
            results = sql.sendQuery("SELECT userId, password FROM user WHERE userName = \"" + username + "\";");
        }catch (SQLException e) {
             setErrorText(sqlError);
        }catch (RuntimeException e) {
            setErrorText(driverError);
        }
        if (results.isEmpty() || results == null) {
            return false;
        }
        else {
            User.userId = Integer.valueOf(results.get(0).toString());
            User.username = username;
            password = results.get(1).toString().toCharArray();
            return true;
        }
    }
    
    /**
     * 
     * @return 
     */
    private Boolean checkPassword() {
        char[] enteredPassword = passwordField.getText().toCharArray();
        if (password.length != enteredPassword.length) { 
        throw new IllegalArgumentException("Invalid password");
        }
        for (int i = 0; i < password.length; i++) {
            if (password[i] != enteredPassword[i]){
                throw new IllegalArgumentException("Invalid password");
            }
        }
        return true;
    } 
    
    /**
     * 
     */
    @FXML
    private void loginButtonAction() {
        errorText.setVisible(false);
        try {
            if (this.checkUsername(usernameField.getText())) {
            
                checkPassword();
                this.writeLog("Login SUCCESS Username: " + usernameField.getText());
                this.loadMainPage();
            } else { 
                throw new IllegalArgumentException("Invalid username");
            }
                
            
        } catch (IllegalArgumentException e) {
            this.loginFailed();
        }
    }
    
    /**
     * 
     */
    protected void loadMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/MainPage.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) anchor.getScene().getWindow();
            stage.setScene(scene);
            stage.setX(400.0);
            
            stage.show();
            
        } catch (IOException e) {
            System.out.println("Couldn't load the main page\n");
            e.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private void loginFailed() {
        setErrorText(userError);
        this.writeLog("Login FAILED  Username: " + usernameField.getText());
    }
    
    /**
     * 
     * @param text 
     */
    private void setErrorText(String text) {
        errorText.setText(text);
        errorText.setAlignment(Pos.CENTER);
        errorText.setVisible(true);

    }
    
    /**
     * Appends log entry to log file. Logs stored in logs/ file
     * @param message
     * @return 
     */
    private boolean writeLog(String message) {
        File file = new File(logFile);
        try (FileWriter fw = new FileWriter(file, true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter pw = new PrintWriter(bw);)
        {
            pw.println(LocalDate.now().toString() + " " + message);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    /**
     * 
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * 
     */
    @FXML
    private void cancelButtonAction() {
        closeWindow();
    }
    
    /**
     * 
     * @param event 
     */
    @FXML
    private void setLanguage(ActionEvent event) {
        errorText.setVisible(false);
        if (event.getSource().equals(en)) {
            locale = new Locale(en.getId(), locale.getCountry());
        }
        else if (event.getSource().equals(es)) {
            locale = new Locale(es.getId(), locale.getCountry());
        }
        updateLanguage();
    }
    
    /**
     * Updates all objects with current language
     * Assigns appropriate language error to the error variables
     */
    private void updateLanguage() {
        ResourceBundle bundle = ResourceBundle.getBundle("AppointmentCalendar.properties.Login", locale);
        languageMenu.setText(bundle.getString("language"));
        loginText.setText(bundle.getString("loginText"));
        usernameText.setText(bundle.getString("username") + ":");
        passwordText.setText(bundle.getString("password") + ":");
        loginButton.setText(bundle.getString("login"));
        cancelButton.setText(bundle.getString("cancel"));
        userError = bundle.getString("userError");
        sqlError = bundle.getString("sqlError");
        driverError = bundle.getString("driverError");
    }
    
    @FXML
    public void handleKeyReleased() {
        String text = usernameField.getText();
        String password = passwordField.getText();
        boolean disableButtons = (text.isEmpty() || text.trim().isEmpty()) || (password.isEmpty() || password.trim().isEmpty());
        loginButton.setDisable(disableButtons);
    }
    
    @FXML
    public void handleEnterPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER){
            if (!loginButton.isDisabled()) {
                loginButtonAction();
            }
        }
    }   
}
