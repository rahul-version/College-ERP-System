package edu.univ.erp.ui;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

public class AdminUsersPanel extends JPanel {

    private final JTextField userIdField = new JTextField(8);
    private final JTextField usernameField = new JTextField(12);
    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"student", "instructor", "admin"});
    private final JPasswordField passwordField = new JPasswordField(10);

    // Student-specific fields
    private final JLabel rollNoLabel = new JLabel("Student rollNo:");
    private final JTextField rollNoField = new JTextField(10);
    private final JLabel programLabel = new JLabel("Program:");
    private final JTextField programField = new JTextField(10);
    private final JLabel yearLabel = new JLabel("Year:");
    private final JTextField yearField = new JTextField(5);

    // Instructor-specific fields
    private final JLabel deptLabel = new JLabel("Instructor dept:");
    private final JTextField deptField = new JTextField(10);

    private final AdminService adminService = new AdminService();

    public AdminUsersPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1; add(userIdField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; add(usernameField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; add(roleBox, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; add(passwordField, gbc);
        row++;

        // Student fields row block
        gbc.gridx = 0; gbc.gridy = row; add(rollNoLabel, gbc);
        gbc.gridx = 1; add(rollNoField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(programLabel, gbc);
        gbc.gridx = 1; add(programField, gbc);
        row++;
        gbc.gridx = 0; gbc.gridy = row; add(yearLabel, gbc);
        gbc.gridx = 1; add(yearField, gbc);
        row++;

        // Instructor fields row block
        gbc.gridx = 0; gbc.gridy = row; add(deptLabel, gbc);
        gbc.gridx = 1; add(deptField, gbc);
        row++;

        JButton createButton = new JButton("Create user");
        createButton.addActionListener(e -> onCreateUser());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        add(createButton, gbc);

        // React to role changes to show/hide fields
        roleBox.addActionListener(e -> updateRoleFields());
        updateRoleFields();
    }

    /**
     * Show only the fields relevant to the selected role.
     * - student: show rollNo/program/year, hide dept
     * - instructor: show dept, hide student fields
     * - admin/others: hide both blocks
     */
    private void updateRoleFields() {
        String role = (String) roleBox.getSelectedItem();
        boolean isStudent = "student".equals(role);
        boolean isInstructor = "instructor".equals(role);

        rollNoLabel.setVisible(isStudent);
        rollNoField.setVisible(isStudent);
        programLabel.setVisible(isStudent);
        programField.setVisible(isStudent);
        yearLabel.setVisible(isStudent);
        yearField.setVisible(isStudent);

        deptLabel.setVisible(isInstructor);
        deptField.setVisible(isInstructor);

        revalidate();
        repaint();
    }

    private void onCreateUser() {
        try {
            String rawId = userIdField.getText().trim();
            String role = (String) roleBox.getSelectedItem();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            // User ID must be present and strictly numeric
            if (rawId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "User ID is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!rawId.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "User ID must be numeric (e.g. 1, 2, 100)",
                        "Invalid User ID",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int userIdNumeric = Integer.parseInt(rawId);

            // Username must be non-empty and strictly alphanumeric (letters and digits only)
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!username.matches("[A-Za-z0-9]+")) {
                JOptionPane.showMessageDialog(this,
                        "Username must be alphanumeric (letters and digits only)",
                        "Invalid username",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Password must be non-empty and strictly alphanumeric (letters and digits only)
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password is required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.matches("[A-Za-z0-9]+")) {
                JOptionPane.showMessageDialog(this,
                        "Password must be alphanumeric (letters and digits only)",
                        "Invalid password",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String err = adminService.createUser(userIdNumeric, username, role, password);
            if (err != null) {
                JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if ("student".equals(role)) {
                String rollNo = rollNoField.getText().trim();
                String program = programField.getText().trim();
                String yearStr = yearField.getText().trim();

                // Strict validation rules for student details:
                // - rollNo must be numeric
                // - program must be alphabetical only
                // - year must be numeric
                if (rollNo.isEmpty() || program.isEmpty() || yearStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Student rollNo, program, and year are required",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!rollNo.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this,
                            "Student rollNo must be numeric",
                            "Invalid rollNo",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!program.matches("[A-Za-z]+")) {
                    JOptionPane.showMessageDialog(this,
                            "Program must contain only alphabetic letters",
                            "Invalid program",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!yearStr.matches("\\d+")) {
                    JOptionPane.showMessageDialog(this,
                            "Year must be numeric",
                            "Invalid year",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int year = Integer.parseInt(yearStr);

                Student s = new Student(userIdNumeric, rollNo, program, year);
                err = adminService.addStudent(s);
                if (err != null) {
                    JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if ("instructor".equals(role)) {
                String dept = deptField.getText().trim();
                Instructor i = new Instructor(userIdNumeric, dept);
                err = adminService.addInstructor(i);
                if (err != null) {
                    JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "User created", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Reset form fields after successful creation
            userIdField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            roleBox.setSelectedIndex(0); // back to "student" by default
            rollNoField.setText("");
            programField.setText("");
            yearField.setText("");
            deptField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "User ID and year must be numbers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
