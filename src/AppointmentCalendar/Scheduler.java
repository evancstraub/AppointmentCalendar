/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import static java.lang.Math.abs;
import java.sql.SQLException;
import java.time.*;
import java.time.ZonedDateTime;
import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author estraub
 */
public class Scheduler {
    
    private Integer selectedYear;
    public static ZonedDateTime selectedDay;
    private ZonedDateTime today;
    private Set<Customer> customers = new HashSet<>();
    public SQLService sql;
    public Month currentMonth;
    private List<CalendarDayObject> calendarObjects;
    private List<CalendarDayObject> weekObjects;
    private int firstDay = 0;
    public static CalendarDayObject selectedDayObject;
    
    
    //Scheduler controls
    
    public Scheduler() {
        sql = new SQLService("ip-address", "DB-username", "DB-password", "Database");
    }
    
    /**
     * 
     * @param currentYear 
     */
    public Scheduler(ZonedDateTime currentYear) {
        sql = new SQLService("ip-address", "DB-username", "DB-password", "Database");
        this.loadCustomers();
        this.selectedYear = currentYear.getYear();
        today = currentYear.withZoneSameInstant(User.zoneId);
        currentMonth = today.getMonth();
        selectedDay = today;
        calendarDayFactory();
        createWeekObjects();
        loadAppointments();
    }
    
    
    public Appointment appointmentWithin15() {
        CalendarDayObject calendarDay = getCalendarDay(today.toLocalDate());
        List<Appointment> appointments = calendarDay.getAppointments();
        for (Appointment appointment : appointments) {
            if ((MINUTES.between(today, appointment.getStart()) > 0) && (MINUTES.between(today, appointment.getStart()) <= 15)){
                return appointment;
            }
        }
        return null;
    }
    
    //Customer controls
    
    public int getNextCustomerId() {
        List results = null;
        int Id = 0;
        try {
            results = sql.sendQuery("SELECT MAX(customerId) FROM customer");
            Id = Integer.parseInt(results.get(0).toString());
        } catch (SQLException e) { 
        } catch (NullPointerException e) {
            return 1;
        }
        return Id + 1;
    }
    
    public List<Customer> getCustomers() {
        ObservableList<Customer> customerObservableList = FXCollections.observableArrayList(customers);
       return customerObservableList;
    }
    
    private void loadCustomers() {
        customers = sql.loadCustomers();
    }

    public boolean addCustomer(Customer customer) {
        if (customers.isEmpty()) {
            customers.add(customer);
            return true;
        } else if (!hasCustomer(customer)) {
            customers.add(customer);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean hasCustomer(Customer customer) {
        if (customers.isEmpty()){
            return false;
        } else if (customers.contains(customer)) {
            return true;
        } else {
            return false;
        }
    }
    
    public Customer findCustomer(String name) {
        for (Customer cust : customers) {
            if (cust.getName().equals(name)) {
                return cust;
            }
        } 
        return null;
    }
    
    public Customer findCustomer(int id) {
        for (Customer cust : customers) {
            if (cust.getId() == id) {
                return cust;
            }
        }
        return null;
    }

    public boolean validateNewCustomer(String name) {
        for (Customer cust: customers) {
            if (cust.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean updateCustomer(int id, Customer customer) {
        Customer currentCustomer = findCustomer(id);
        if (currentCustomer != null) {
            customers.remove(currentCustomer);
            customers.add(customer);
            return true;
        } else {
            return false;
        }
    }
 
    public boolean removeCustomer(Customer customer) {
        boolean result = sql.sendDbUpdate("DELETE FROM customer WHERE customerId = " + customer.getId());
        customers.remove(customer);
        loadCustomers();
        return result;
    }
    
    //Appointment controls
    public boolean addAppointment(Appointment appointment) {
        appointment.setId(getNextAppointmentId());
        if(Scheduler.selectedDayObject.addAppointment(appointment)) {
            sql.sendDbUpdate(appointment.toSQL());
            return true;
        } else{
            return false;
        }
    }
    
    public boolean updateAppointment(Appointment oldAppointment, Appointment newAppointment) {
        newAppointment.setCustomer(findCustomer(newAppointment.getCustomerId()));
        if (selectedDayObject.removeAppointment(oldAppointment)) {
            if (selectedDayObject.addAppointment(newAppointment)) {
                sql.sendDbUpdate(newAppointment.toSQL());
                return true;
            } else {
                selectedDayObject.addAppointment(oldAppointment);
                return false;
            }
        } else {
            return false;
        }
    }
    
    public boolean deleteAppointment(Appointment appointment) {
        if(selectedDayObject.removeAppointment(appointment)) {
            sql.sendDbUpdate("DELETE FROM appointment WHERE appointmentId = " + appointment.getId() + ";");
        } else {
            return false;
        }
        return false;
    }
    
    public List getSelectedAppointments() {
        List<Appointment> appointments = selectedDayObject.getAppointments();
        return appointments;
    }
    
    public int getNextAppointmentId() {
        List results = null;
        int Id = 0;
        try {
            results = sql.sendQuery("SELECT MAX(appointmentId) FROM appointment");
            Id = Integer.parseInt(results.get(0).toString());
        } catch (SQLException e) { 
        } catch (NullPointerException e) {
            return 1;
        }
        return Id + 1;
    }
    
    //Calendar controls

    /**
     * 
     * @return 
     */
    public Integer getCurrentYear() {
        return selectedYear;
    }

    /**
     * 
     * @return 
     */
    public ZonedDateTime getToday() {
        return today;
    }

    /**
     * 
     * @param currentYear 
     */
    public void setCurrentYear(Integer currentYear) {
        this.selectedYear = currentYear;
    }
    
    /**
     * 
     * @param year
     * @return 
     */
    public Boolean isYearLoaded(ZonedDateTime year) {
        return false;
    }

    public Month getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(Month currentMonth) {
        this.currentMonth = currentMonth;
    }
    
    public List<CalendarDayObject> getCalendar() {
        return calendarObjects;
    }
    
    private void calendarDayFactory() {
        
        calendarObjects = new ArrayList();
        
        for (int i = 0; i < 6; i++) {
            calendarObjects.add(new CalendarDayObject(DayOfWeek.SUNDAY));
            for (DayOfWeek day : DayOfWeek.values()) {
                calendarObjects.add(new CalendarDayObject(day));
                if (day.equals(DayOfWeek.SATURDAY)) {
                    break;
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            if(calendarObjects.get(i).getDayOfWeek().equals(firstDay(currentMonth))) {
                calendarObjects.get(i).setDate(LocalDate.of(selectedYear, currentMonth, 1));
                calendarObjects.get(i).setActive(true);
                firstDay = i;
            }
        }
        for (int i = firstDay + 1, x = 2; x <= currentMonth.length(Year.isLeap(selectedYear)); i++) {
            calendarObjects.get(i).setDate(LocalDate.of(selectedYear, currentMonth, x));
            calendarObjects.get(i).setDayOfMonth(x);
            calendarObjects.get(i).setActive(true);
            x++;
        }
        fillInactive();
    }
    
    private void loadAppointments() {
        LocalDate firstDay = calendarObjects.get(0).getToday().toLocalDate();
        LocalDate lastDay = calendarObjects.get(41).getToday().toLocalDate();
        List<Appointment> appointmentList = sql.getDBMonth(firstDay, lastDay);
        if (appointmentList == null) {
            return;
        }
        while (!appointmentList.isEmpty()) {
            for (CalendarDayObject day : calendarObjects) {
                if(appointmentList.get(0).getStart().toLocalDate().equals(day.today.toLocalDate())) {
                    Appointment appointment = (Appointment) appointmentList.remove(0);
                    appointment.setCustomer(findCustomer(appointment.getCustomerId()));
                    day.addAppointment(appointment);
                    break;
                }
            }
        }
    }
//    
//    private void setInactiveAppointments() {
//        for (CalendarDayObject day : calendarObjects) {
//            if (day.isActive()) {
//                continue;
//            } else {
//                List<Appointment> appointments = sql.getDayAppointments(day.getToday());
//                if (appointments == null) {
//                    continue;
//                }
//                if (appointments.isEmpty()) {
//                    continue;
//                } else {
//                    for (Appointment appointment : appointments) {
//                        addAppointment(appointment);
//                    }
//                }
//            }
//        }
//    }
    
    private DayOfWeek firstDay(Month month) {
        LocalDate date = LocalDate.of(selectedYear, month, 1);
        return date.getDayOfWeek();
    }
       
    private int getLastDay() {
        for (int i = firstDay; i <= calendarObjects.size(); i++) {
            if (!calendarObjects.get(i).isActive()) {
                return i;
            }
        }
        return 0;
    }
    
    private void fillInactive() {
        for (int i = firstDay - 1; i >= 0; i--) {
            calendarObjects.get(i).setDate(calendarObjects.get(i + 1).getToday().toLocalDate().minusDays(1));
        }
        for (int i = getLastDay(); i < calendarObjects.size(); i++) {
            calendarObjects.get(i).setDate(calendarObjects.get(i - 1).getToday().toLocalDate().plusDays(1));
        }
    }
    
    public void nextMonth() {
        if (currentMonth == Month.DECEMBER) {
            selectedYear++;
        }
        currentMonth = LocalDate.of(selectedYear, currentMonth, 1).plusMonths(1).getMonth();
        selectedDay = ZonedDateTime.of(LocalDate.of(selectedYear, currentMonth, 1), LocalTime.now(), User.zoneId);
        calendarDayFactory();
        createWeekObjects();
        loadAppointments();
    }
    
    public void previousMonth() {
        if (currentMonth == Month.JANUARY) {
            selectedYear--;
        }
        currentMonth = LocalDate.of(selectedYear, currentMonth, 1).minusMonths(1).getMonth();
        selectedDay = ZonedDateTime.of(LocalDate.of(selectedYear, currentMonth, 1), LocalTime.now(), User.zoneId);
        calendarDayFactory();
        createWeekObjects();
        loadAppointments();
    }

    public ZonedDateTime getSelectedDay() {
        return selectedDay;
    }

    private void createWeekObjects() {
        weekObjects = new ArrayList();
        for (int i = 0; i < 7; i++) {
            weekObjects.add(calendarObjects.get(i));
        }
    }
    
    private void updateWeekObjects() {
        this.weekObjects.clear();
        for (int i = 0; i < 7; i++) {
            weekObjects.add(calendarObjects.get(i));
        }
    }
    
     /*
    * There is an Obvious bug in the way this code works. 
    * Locates the last day of the week according to the 42 day objects and the gui. The method then loads the next 7 days worth of days into the week subset 
    * If the next month isn't yet loaded, the next month gets loaded, then the action is performed on the week objects
    */
    public void loadNextWeek(){
        try {
            int nextItem = calendarObjects.indexOf(weekObjects.get(weekObjects.size() - 1)) + 1; //complex way of getting the index from the last calendar object displayed
            if (nextItem > 28) { //28 because there are duplicate weeks. Remember the 42 objects for the month are larger than the month itself
                nextMonth();
                nextItem = 0;
            }
            weekObjects.clear();
            for (int i = 0; i < 7; i++) {
                weekObjects.add(calendarObjects.get(nextItem));
                nextItem++;
            }
        } catch (Exception e) {
            nextMonth();
            updateWeekObjects();
        }
    }
    
    /*
    * Loads the 7 day objects previous to the current first gui day object. if this object lies outside the loaded month, the previous month is loaded. 
    */
    public void loadPreviousWeek() {
        int previousItem = calendarObjects.indexOf(weekObjects.get(0));
        try {
            if (previousItem <= 0) {
                previousMonth();
                previousItem = 21;
            } else {
                previousItem -= 7;
            }
            weekObjects.clear();
            for (int i = 0; i < 7; i++) {
                weekObjects.add(calendarObjects.get(previousItem));
                previousItem++;
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e.getStackTrace().toString());
        }
    }
    
    public void setSelectedMonthDayObject(int index) {
        selectedDayObject = calendarObjects.get(index);
        Scheduler.selectedDay = selectedDayObject.getToday();
    }
    
    public void setSelectedWeekDayObject(int index) {
        selectedDayObject = weekObjects.get(index);
        Scheduler.selectedDay = selectedDayObject.getToday();
    }

    public CalendarDayObject getSelectedDayObject() {
        return selectedDayObject;
    }
    
    public LocalDate getSelectedDate() {
        return selectedDayObject.getToday().toLocalDate();
    }
    
    public List<CalendarDayObject> getWeekObjects() {
        return weekObjects;
    }
    
    private CalendarDayObject getCalendarDay(LocalDate today) {
        for (CalendarDayObject day : calendarObjects) {
            if (day.getToday().toLocalDate().equals(today)) {
                return day;
            }
        }
        return null;
    }

    /**
     * an inner class is used to interface with the GUI. Each day of the calendar is represented by an object of this inner class. Those objects
     * are loaded into a list that have the same number of days as the calendar gui.
     * The inner class contains date information about the calendar list object. It also holds a list containing all of the appointments occurring on the objects configured day.
     */
    public class CalendarDayObject {
        ZonedDateTime today;
        private int dayOfMonth;
        public boolean active = false;
        private List<Appointment> appointments;
        private DayOfWeek dayOfWeek;

        public CalendarDayObject(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            appointments = new ArrayList();
        }
        
        public void setDate(LocalDate date) {
            today = ZonedDateTime.of(date, LocalTime.MIN, User.zoneId);
            setDayOfMonth(today.getDayOfMonth());
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public void setDayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
        
        public String toString() {
            return dayOfWeek + " - " + today.toLocalDate().getMonth() + "/" +  getDayOfMonth();
        }

        public ZonedDateTime getToday() {
            return today;
        }

        public void setToday(ZonedDateTime today) {
            this.today = today;
        }
        
        public boolean addAppointment(Appointment appointment) {
            if (isOverlapping(appointment)) {
                return false;
            } else {
                appointments.add(appointment);
           
//                Collections.sort(appointments, new Comparator<Appointment>() {
//                    @Override
//                    public int compare(Appointment a1, Appointment a2) {
//                        if (a1.getStart().isBefore(a2.getStart())) {
//                            return -1;
//                        } else {
//                            return 1;
//                        }
//                    }
//                });  
                appointments.sort((Appointment a1, Appointment a2)->a1.getStart().isBefore(a2.getStart()) ? -1 : 1 ); //This lambda expression was used to simplify the anonymous inner class above

                return true;
            }
        }
        
        public boolean isOverlapping(Appointment newAppointment) {
            if (appointments.isEmpty()){
                return false;
            }
            for (Appointment appointment : appointments) {
                if ((newAppointment.getStart().toEpochSecond() < appointment.getStart().toEpochSecond()) && (newAppointment.getEnd().toEpochSecond() > appointment.getEnd().toEpochSecond())) {
                    return true;
                } else if (newAppointment.getStart().toEpochSecond() == appointment.getStart().toEpochSecond()) {
                    return true;
                } else if (newAppointment.getEnd().toEpochSecond() == appointment.getEnd().toEpochSecond()) {
                    return true;
                } else if ((newAppointment.getStart().toEpochSecond() > appointment.getStart().toEpochSecond()) && (newAppointment.getStart().toEpochSecond() < appointment.getEnd().toEpochSecond())) {
                    return true;
                } else if ((newAppointment.getStart().toEpochSecond() > appointment.getStart().toEpochSecond()) && (newAppointment.getEnd().toEpochSecond() < appointment.getEnd().toEpochSecond())) {
                    return true;
                } 
            }
            return false;
        }
        
        public boolean removeAppointment(Appointment appointment) {
            if(appointments.remove(appointment)) {
                return true;
            } else {
                return false;
            }
        }
        
        public List<Appointment> getAppointments() {
            return appointments;
        }
        
    }
    
    
    
}