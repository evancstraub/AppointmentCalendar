/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 * @author Evan Straub evan.c.straub@gmail.com
 */
public class SQLService { 

    private Boolean connected;
    private String server;
    private String username;
    private String password;
    private String database;
    private String query;

    public SQLService(String server, String username, String password, String database) {
        this.server = server;
        this.username = username;
        this.password = password;
        this.database = database;
    }
        
        
    public SQLService() {
    }

    /**
     * 
     * @return 
     */
    public String getServer() {
        return server;
    }

    /**
     * 
     * @param server 
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * 
     * @param username 
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 
     * @param password 
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @return 
     */
    public Boolean isConnected() {
        return connected;
    }

    /**
     * 
     * @param database 
     */
    public void setDatabase(String database) {
        this.database = database;
    }
    
    
    /**
     * Using the month argument, this method queries the database for the selected
     * month and returns a list of CalendarDayObjects 
     * @param selectedMonth
     * @return List
     */
    public List getDBMonth(LocalDate firstDay, LocalDate lastDay) {
        ArrayList month;
        String query = "SELECT * FROM appointment WHERE userId = " + User.userId + " AND DATE(start) BETWEEN '" + firstDay.toString() + "' AND ' " + lastDay.toString() + "';";
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                String description = rs.getString("description");
                if (description == null) {
                    return null;
                }
                month = new ArrayList<Appointment>();
                rs.beforeFirst();
            }
            else {
                return null;
            }
            while(rs.next()) {
                int id = rs.getInt("appointmentId");
                String description = rs.getString("description");
                String title = rs.getString("title");
                String location = rs.getString("location");
                String contact = rs.getString("contact");
                int customerID = rs.getInt("customerid");
                String url = rs.getString("url");
                String type = rs.getString("appointmentType");
                Timestamp startUtc;
                startUtc =  rs.getTimestamp("start");
                Timestamp endUtc;
                endUtc = rs.getTimestamp("end");
                ZonedDateTime start = ZonedDateTime.of(startUtc.toLocalDateTime(), ZoneOffset.UTC);
                ZonedDateTime end = ZonedDateTime.of(endUtc.toLocalDateTime(), ZoneOffset.UTC);
                start = start.withZoneSameInstant(User.zoneId);
                end = end.withZoneSameInstant(User.zoneId);
                month.add(new Appointment(id, title, description, location, contact, customerID, url, type, start, end));
                }
            return month;
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("I failed to get the appointments");
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    //supressed
                }
            }
        }
    }
    
    /**
     * Using the month argument, this method queries the database for the selected
     * month and returns a list of CalendarDayObjects
     * @param selectedMonth
     * @return List
     */
    public List getDBMonth(Month selectedMonth, int userId) {
        LocalDate firstDay = LocalDate.of(LocalDate.now().getYear(), selectedMonth, 1);
        LocalDate lastDay = LocalDate.of(LocalDate.now().getYear(), selectedMonth, selectedMonth.length(Year.isLeap(LocalDate.now().getYear())));
        ArrayList month;
        String query = "SELECT * FROM appointment WHERE userId = " + userId + " AND DATE(start) BETWEEN '" + firstDay.toString() + "' AND ' " + lastDay.toString() + "';";
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.first()) {
                String description = rs.getString("description");
                if (description == null) {
                    return null;
                }
                month = new ArrayList<Appointment>();
                rs.beforeFirst();
            }
            else {
                return null;
            }
            while(rs.next()) {
                int id = rs.getInt("appointmentId");
                String description = rs.getString("description");
                String title = rs.getString("title");
                String location = rs.getString("location");
                String contact = rs.getString("contact");
                int customerID = rs.getInt("customerid");
                String url = rs.getString("url");
                String type = rs.getString("appointmentType");
                Timestamp startUtc;
                startUtc =  rs.getTimestamp("start");
                Timestamp endUtc;
                endUtc = rs.getTimestamp("end");
                ZonedDateTime start = ZonedDateTime.of(startUtc.toLocalDateTime(), ZoneOffset.UTC);
                ZonedDateTime end = ZonedDateTime.of(endUtc.toLocalDateTime(), ZoneOffset.UTC);
                start = start.withZoneSameInstant(User.zoneId);
                end = end.withZoneSameInstant(User.zoneId);
                month.add(new Appointment(id, title, description, location, contact, customerID, url, type, start, end));
                }
            return month;
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("I failed to get the appointments");
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    //supressed
                }
            }
        }
    }
    
    /**
     * @param query
     * @return ArrayList of results
     */
    public ArrayList sendQuery(String query) throws SQLException {
        ArrayList result = new ArrayList();
        Connection conn = null;
        PreparedStatement stmt;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnNumber = rsmd.getColumnCount();
            while(rs.next()) {
                int i = 1;
                while (i<= columnNumber) {
                    result.add(rs.getString(i++));
                }
            }           
        } catch (SQLException e) {
            System.out.println("Something went wrong with your query\n" + query);
            throw new SQLException("Bad Query");
        } catch (ClassNotFoundException e) {
            System.out.println("The JDBC Driver was not loaded properly");
            throw new RuntimeException("SQLServerNotConnected");
        }
        
        finally {
            try {
                if (conn != null){
                    conn.close();
                }
            }
            catch (SQLException e) {
                
            }
        }
        return result;
    }
    
    public void setQuery(String query) {
        this.query = query;
    } 
    
    public List sendQuery() {
        ArrayList result = new ArrayList();
        Connection conn = null;
        PreparedStatement stmt;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnNumber = rsmd.getColumnCount();
            while(rs.next()) {
                List<String> innerList = new ArrayList<>();                
                int i = 1;
                while (i<= columnNumber) {
                    innerList.add(rs.getString(i++));
                }
                result.add(innerList);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("something went wrong " + e.getMessage());
        }
        query = null;
        return result;
    }
    
    public ArrayList getUsers(){
        ArrayList result = new ArrayList();
        Connection conn = null;
        PreparedStatement stmt;
        String query = "SELECT userId, firstName FROM user";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("userId");
                String name = rs.getString("firstName");
                result.add(new User(id, name));
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong with your query\n" + query);
        } catch (ClassNotFoundException e) {
            System.out.println("The JDBC Driver was not loaded properly");
            throw new RuntimeException("SQLServerNotConnected");
        }
        
        finally {
            try {
                if (conn != null){
                    conn.close();
                }
            }
            catch (SQLException e) {
                
            }
        }
        return result;
    }
    
    /**
     * Under construction
     * Saves appointment argument to the database
     * @param appointment
     * @return 
     */
    public Boolean saveAppointment(Appointment appointment) {
        try (Connection conn = DriverManager.getConnection(getConnectionString())) {
            PreparedStatement stmt = conn.prepareStatement("SQL Statement Goes here");
            int result = stmt.executeUpdate();
            if (result <= 0) {
                return false;
            }
            else {
                return true;
            }           
        } catch (SQLException e) {
            throw new RuntimeException("There was an issue sending a database update" + e.getStackTrace().toString());
        }
    }
    
    /**
     * Under construction
     * Sends a DB update
     * untested
     * @param update
     * @return 
     */
    public Boolean sendDbUpdate (List update) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            Statement stmt = conn.createStatement();
            for (Object update1 : update) {
                stmt.addBatch(update1.toString());
            }
            int[] result = stmt.executeBatch();
            for (int i : result) {
                if (i == 0) {
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
            return false;
        } finally {
            try {
                if (conn != null){
                    conn.close();
                }
            }
            catch (SQLException e) {   
            }
        }
        return true;
    }
    
    public Boolean sendDbUpdate (String update) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            Statement stmt = conn.createStatement();
            stmt.execute(update);
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println(e.getStackTrace());
            return false;
        } finally {
            try {
                if (conn != null){
                    conn.close();
                }
            }
            catch (SQLException e) {   
            }
        }
        return true;
    }
    
    public Set<Customer> loadCustomers() {
        Set<Customer> customers = new HashSet<>();
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(getConnectionString(), username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT customer.customerId, customer.active, customer.customerName, address.address, address.address2, city.city, address.postalCode, country.country, address.phone FROM customer "
                    + "JOIN address ON customer.addressId = address.addressId JOIN city ON address.cityId = city.cityId JOIN country ON city.countryId = country.countryId");
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customerId"),
                        rs.getString("customerName"),
                        rs.getBoolean("active"));
                customer.setLocation(customer.new Location(
                        rs.getString("address"),
                        rs.getString("address2"),
                        rs.getString("city"),
                        rs.getString("postalCode"),
                        rs.getString("country"),
                        rs.getString("phone")));
                customers.add(customer);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("System Error, no class found");
        } catch (SQLException e) {
            System.out.println(e);
            throw new RuntimeException("Query Error");
        }
        return customers;
    }
    
    /**
     * 
     * @param timestamp
     * @param zoneId
     * @return 
     */
    public static ZonedDateTime toZonedDateTime(Timestamp timestamp, ZoneId zoneId) {
         Instant i = timestamp.toInstant();
         ZonedDateTime t;
         return t = ZonedDateTime.ofInstant(i, zoneId);
        
    }
    
    /**
     * 
     * @param dateTime
     * @return 
     */
    public static Timestamp toTimestamp(ZonedDateTime dateTime) {
         return new Timestamp(dateTime.toInstant().getEpochSecond());
    }
    
    /**
     * 
     * @return 
     */
    private String getConnectionString() {
        return "jdbc:mysql://" + server + ":3306/" + database;
    }
    
    /**
     * 
     * @return 
     */
    public Boolean connectDatabase() {
        //connects to database
        return false;
    }
    
    private int getRSSize(ResultSet rs) throws SQLException {
        if (rs == null) { return 0; }
        rs.last();
        int last = rs.getRow();
        rs.beforeFirst();
        return last;
    }
    
//    public List<Appointment> getDayAppointments(ZonedDateTime day) {
//        List<Appointment> appointments = null;
//        ArrayList todayAppointments;
//        String query = "SELECT * FROM appointment WHERE userId = " + User.userId + " AND DATE(start) = '" + day.toLocalDate() + "';";
//        Connection conn = null;
////        System.out.println(query);
////        return appointments;
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            
//            conn = DriverManager.getConnection(getConnectionString(), username, password);
//            
//            PreparedStatement stmt = conn.prepareStatement(query);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.first()) {
//                String description = rs.getString("description");
//                if (description == null) {
//                    return null;
//                }
//                todayAppointments = new ArrayList<Appointment>();
//                rs.beforeFirst();
//            }
//            else {
//                return null;
//            }
//            while(rs.next()) {
//                int id = rs.getInt("appointmentId");
//                String description = rs.getString("description");
//                String title = rs.getString("title");
//                String location = rs.getString("location");
//                String contact = rs.getString("contact");
//                int customerID = rs.getInt("customerid");
//                String url = rs.getString("url");
//                String type = rs.getString("appointmentType");
//                Timestamp startUtc;
//                startUtc =  rs.getTimestamp("start");
//                Timestamp endUtc;
//                endUtc = rs.getTimestamp("end");
//                ZonedDateTime start = ZonedDateTime.of(startUtc.toLocalDateTime(), ZoneOffset.UTC);
//                ZonedDateTime end = ZonedDateTime.of(endUtc.toLocalDateTime(), ZoneOffset.UTC);
//                start = start.withZoneSameInstant(User.zoneId);
//                end = end.withZoneSameInstant(User.zoneId);
//                todayAppointments.add(new Appointment(id, title, description, location, contact, customerID, url, type, start, end));
//                }
//            return todayAppointments;
//        } catch (SQLException | ClassNotFoundException e) {
//            System.out.println("I failed to get the appointments");
//            return null;
//        } finally {
//            if (conn != null) {
//                try {
//                    conn.close();
//                } catch (Exception e) {
//                    //supressed
//                }
//            }
//        }
//    }
//    
    /**
     * Method takes the table string as a value and queries the database for the last ID in the table
     * The method then returns the last id + 1
     * @param table
     * @return 
     */
    public int getNextId(String table) {
        int nextId = 1;
        try {
            nextId = Integer.parseInt(this.sendQuery("SELECT MAX(" + table + "Id) FROM " + table + ";").get(0).toString());
            nextId++;
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong getting the new ID from table " + table + e.getStackTrace().toString());        
        } catch (NumberFormatException e) {}
        return nextId;
    }

    public int getId(String table, String entry) {
        int result = -1;
        try { 
            result = Integer.parseInt(this.sendQuery("SELECT " + table + "Id FROM " + table + " WHERE " + table + " = \"" + entry + "\";").get(0).toString());
        } catch (SQLException e) {
            throw new RuntimeException("Something went wrong getting the new ID from table " + table + e.getStackTrace().toString());        
        } catch (NumberFormatException e) {}
        catch (IndexOutOfBoundsException e) {}
        return result;
    }
}
    