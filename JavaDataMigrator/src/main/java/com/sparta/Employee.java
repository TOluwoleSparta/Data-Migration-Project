package com.sparta;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Employee {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private int employeeID;
    private String honorific;
    private String firstName;
    private char middleInitial;
    private String lastName;
    private char gender;
    private String email;
    private Date dateOfBirth;
    private Date startDate;
    private float salary;

    public void setEmployeeID(String employeeID) {
        this.employeeID = Integer.parseInt(employeeID);
    }

    public void setHonorific(String honorific) {
        this.honorific = honorific;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial.charAt(0);
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        this.gender = gender.charAt(0);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDateOfBirth(String dateOfBirth) throws ParseException{
        this.dateOfBirth = dateFormat.parse(dateOfBirth);
    }

    public void setStartDate(String startDate) throws ParseException {
        this.startDate = dateFormat.parse(startDate);
    }

    public void setSalary(String salary) {
        this.salary = Float.parseFloat(salary);
    }

    public int getEmployeeID() {
        return this.employeeID;
    }

    public String getHonorific() {
        return this.honorific;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public char getMiddleInitial() {
        return this.middleInitial;
    }

    public String getLastName() {
        return this.lastName;
    }

    public char getGender() {
        return this.gender;
    }

    public String getEmail() {
        return this.email;
    }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public float getSalary() {
        return this.salary;
    }
}
