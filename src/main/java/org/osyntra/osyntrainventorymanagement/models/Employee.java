package org.osyntra.osyntrainventorymanagement.models;

public class Employee extends User {
    private String employeeId;
    private String jobTitle;
    private EmployeeRole role;

    //CONSTRUCTORS
    public Employee() {
    }

    public Employee(
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String empId,
            String jobTitle,
            EmployeeRole role
    ){
        super(firstName, lastName, email, passwordHash);
        this.employeeId = empId;
        this.jobTitle = jobTitle;
        this.role = role;
    }

    public Employee(Employee employee){
        super(employee);
        this.employeeId = employee.employeeId;
        this.jobTitle = employee.jobTitle;
        this.role = employee.role;
    }

    //ACCESSORS
    public String getEmpId() {
        return employeeId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public EmployeeRole getRole() {
        return role;
    }

    //MUTATORS
    public void setEmpId(String empId) {
        this.employeeId = empId;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }
}