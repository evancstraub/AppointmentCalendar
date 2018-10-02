/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.time.ZoneId;
import java.util.Locale;

/**
 *
 * @author Evan Straub <estraub@wgu.edu>
 */
public class User {
    
    public static int userId;
    public static String username;
    public static ZoneId zoneId;
    public static Locale locale;
    public static boolean firstStart = true;
    
    private int id;
    private String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return id + " - " + name; 
    }
    
    public static void reset() {
        User.userId = 0;
        zoneId = null;
        firstStart = true;
    }
}
