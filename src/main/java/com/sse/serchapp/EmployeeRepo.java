package com.sse.serchapp;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@org.springframework.stereotype.Repository
public interface EmployeeRepo extends JpaRepository<Employee, UUID>,
    JpaSpecificationExecutor<Employee> {

  List<Employee> findByStatus(String status);

  List<Employee> findByStatusAndRole(String status, String role);
}
