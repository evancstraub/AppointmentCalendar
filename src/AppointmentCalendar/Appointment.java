/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author estraub
 */
public class Appointment  {
    private int id;
    private String title;
    private String description;
    private String location;
    private String contact;
    private Customer customer;
    private int customerId;
    private String url;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private Boolean newAppointment;
    private String type;



    
    
    /**
     * Constructor for creating a new Appointment
     * 
     * @param description
     * @param location
     * @param customerID
     * @param start
     * @param end 
     */
    public Appointment(String title, String description, String location, String contact, Customer customer, String url, String type, ZonedDateTime start, ZonedDateTime end) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.customer = customer;
        this.url = url;
        this.start = start;
        this.end = end;
        this.type = type;
        newAppointment = true;
    }

    /**
     * Constructor for loading from a SQL DB
     * 
     * @param id
     * @param description
     * @param location
     * @param customerID
     * @param url
     * @param start
     * @param end
     * @param newAppointment 
     */
    public Appointment(int id, String title, String description, String location, String contact, int customerId, String url, String type, ZonedDateTime start, ZonedDateTime end) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.contact = contact;
        this.customerId = customerId;
        this.url = url;
        this.start = start;
        this.end = end;
        this.type = type;
        newAppointment = false;
    }

    public Appointment() {
        title = " ";
    }
    
    

    /**
     * @return 
     */
    public String toSQL() {
        String result = null;
        String startTime = start.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().toString();
        String endTime = end.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().toString();
        if (newAppointment) {
            result = "INSERT INTO appointment VALUES('" +
                    id +"', " +
                    customer.getId() + ", '" +
                    title + "', '" +
                    description + "', ' " +
                    location + "', '" +
                    contact + "', '" +
                    url + "', '" + 
                    startTime + "', '" +
                    endTime + "', " + 
                    "NOW(), " +
                    User.userId + "," +
                    "NOW(), " +
                    User.userId + ", " +
                    User.userId + ", '" +
                    type + "');";
        }
        else {
            result = "UPDATE appointment SET " + 
                     "customerId = " + customer.getId() + ", "+
                     "title = '" + title + "', " +
                     "description = '" + description + "', " +
                     "location = '" + location + "', " +
                     "contact = '" + contact + "', " + 
                     "url = '" + url + "', " +
                     "start = '" + startTime + "', " +
                     "end = '" + endTime + "', " +
                     "appointmentType = '" + type + "', " +
                     "lastUpdate = NOW(), " + 
                     "lastUpdateBy = " + User.userId + " " + 
                     "WHERE appointmentId = " + id + ";";
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "Appointment: " + title;        
    }

    public String getDescription() {
        return description;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getType() {
        return type;
    }
    
    
    
    /**
     * 
     * @return 
     */
    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }
    
    /**
     * 
     * @return 
     */
    public String getName() {
        if (start == null) {
            return title;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("hh:mm a");
        return df.format(start).toString() + " - " + df.format(end).toString() + "   " + title;
    }

    /**
     * 
     * @return 
     */
    public String getLocation() {
        return location;
    }

    /**
     * 
     * @return 
     */
    public String getContact() {
        return contact;
    }

    /**
     * 
     * @return 
     */
    public String getUrl() {
        return url;
    }

    /**
     * 
     * @return 
     */
    public ZonedDateTime getStart() {
        return start;
    }

    /**
     * 
     * @return 
     */
    public ZonedDateTime getEnd() {
        return end;
    }

    /**
     * 
     * @param description 
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @param location 
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 
     * @param contact 
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * 
     * @param url 
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * @param start 
     */
    public void setStart(ZonedDateTime start) {
        this.start = start;
    }

    /**
     * 
     * @param end 
     */
    public void setEnd(ZonedDateTime end) {
        this.end = end;
    }  

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
}
