package org.osyntra.osyntrainventorymanagement.repositories;

import org.osyntra.osyntrainventorymanagement.models.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class EmployeeRepository {
    private final JdbcClient jdbcClient;

    public EmployeeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Employee> findByEmail(String email) {
        return jdbcClient.sql("""
                SELECT u.*, e.employee_id, e.job_title, e.employee_role
                FROM users u JOIN employees e ON e.user_id = u.id WHERE lower(u.email) = lower(:email)
                """).param("email", email).query((rs, row) -> {
            Employee employee = new Employee(rs.getString("first_name"), rs.getString("last_name"),
                    rs.getString("email"), rs.getString("password_hash"), rs.getString("employee_id"),
                    rs.getString("job_title"), EmployeeRole.valueOf(rs.getString("employee_role")));
            employee.setId(rs.getInt("id"));
            employee.setStatus(UserStatus.valueOf(rs.getString("status")));
            employee.setCreatedAt(rs.getTimestamp("created_at").toInstant());
            employee.setModifiedAt(rs.getTimestamp("modified_at").toInstant());
            return employee;
        }).optional();
    }

    public void save(Employee employee) {
        Integer id = jdbcClient.sql("""
                INSERT INTO users(first_name, last_name, email, password_hash, status)
                VALUES (:firstName, :lastName, :email, :passwordHash, 'ACTIVE') RETURNING id
                """).param("firstName", employee.getFirstName()).param("lastName", employee.getLastName())
                .param("email", employee.getEmail()).param("passwordHash", employee.getPasswordHash())
                .query(Integer.class).single();
        Integer employeeId = jdbcClient.sql("""
                INSERT INTO employees(user_id, job_title, employee_role)
                VALUES (:id, :jobTitle, :role) RETURNING employee_id
                """).param("id", id).param("jobTitle", employee.getJobTitle())
                .param("role", employee.getRole().name()).query(Integer.class).single();
        employee.setId(id);
        employee.setEmpId(employeeId.toString());
    }

    public boolean hasEmployees() {
        return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM employees)").query(Boolean.class).single();
    }

    public void lockBootstrap() {
        jdbcClient.sql("LOCK TABLE employees IN EXCLUSIVE MODE").update();
    }
}
