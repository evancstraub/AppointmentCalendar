/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AppointmentCalendar;

import java.util.Objects;

/**
 * Customer Class
 *
 * @author estraub
 */
public class Customer implements Comparable {
    
    private int id;
    private String name;
    protected Location location;
    private Boolean active;
    private Boolean newCustomer;

    /**
     * Constructor for creating new customers. The ID should be assigned based on the database
     * @param name
     * @param active 
     */
    public Customer(String name, Boolean active) {
        this.name = name;
        this.active = active;
        newCustomer = true;
        Scheduler scheduler = new Scheduler(); //Want to make this obsolete by using auto_increment primary keys
        id = scheduler.getNextCustomerId();
        
    }
    
    /**
     * Constructor for creating customer objects from the DB
     * @param id
     * @param name
     * @param location
     * @param active 
     */
    protected Customer(int id, String name, Boolean active) {
        newCustomer = false;
        this.id = id;
        this.name = name;
        this.active = active;
        
    }

    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name 
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return 
     */
    public String getLocation() {
        return location.toString();
    }

    /**
     * 
     * @param location 
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * 
     * @return 
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * 
     * @param active 
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public boolean isNewCustomer() {
        return this.newCustomer;
    }

    protected void toggleNewCustomer() {
        if (newCustomer) {
            newCustomer = false;
        } else {
            newCustomer = true;
        }
    }
    /**
     * 
     * @return 
     */
    public int getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return name + " " + location.getPhone();
    }

    @Override
    public int compareTo(Object t) {
        if (t instanceof Customer) {
            Customer cust = (Customer) t;
            if (this.id > cust.id) {
                return 1;
            } else if (this.id == cust.id) {
                return 0;
            } else {
                return -1;
            }
        } else {
            throw new IllegalArgumentException ("Not of type Customer");
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.id;
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    @Override
    public boolean equals(Object cust) {
        if (cust == null) {
            return false;
        }
        Customer otherCustomer = (Customer) cust;
               
        return this.name.equals(otherCustomer.name);
    }

    /*
    * inner class to keep track of customer address information
    */
    public class Location {
        private String address;
        private String address2;
        private String city;
        private String country;
        private String zip;
        private String phone;

        public Location(String address, String address2,  String city, String zip, String country, String phone) {
            this.address = address;
            this.address2 = address2;
            this.city = city;
            
            this.zip = zip;
            this.country = country;
            this.phone = phone;
        }
        
        @Override
        public String toString() {
            return address + " " + address2 + " " + city + " " + zip + " " + country;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }   

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public String getAddress2() {
            return address2;
        }

        public void setAddress2(String address2) {
            this.address2 = address2;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
