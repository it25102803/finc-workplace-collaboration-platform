package com.example.demo.service;

import com.example.demo.model.Employee;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    private final String FILE_NAME = "employees.txt";

    // ✅ Get all employees
    public List<Employee> getAll() {
        List<Employee> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                Employee e = new Employee(
                        Long.parseLong(data[0]),
                        data[1],
                        data[2],
                        Double.parseDouble(data[3])
                );
                list.add(e);
            }
        } catch (Exception e) {
            // ignore if file not exists
        }

        return list;
    }

    // ✅ Save employee
    public Employee save(Employee e) {
        List<Employee> list = getAll();

        // generate ID
        long newId = list.size() + 1;
        e.setId(newId);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(e.getId() + "," + e.getName() + "," + e.getRole() + "," + e.getSalary());
            bw.newLine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return e;
    }

    // ✅ Get by ID
    public Employee getById(Long id) {
        return getAll().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // ✅ Delete
    public void delete(Long id) {
        List<Employee> list = getAll();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Employee e : list) {
                if (!e.getId().equals(id)) {
                    bw.write(e.getId() + "," + e.getName() + "," + e.getRole() + "," + e.getSalary());
                    bw.newLine();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
//test