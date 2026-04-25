package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.DisabledUsers;
import edu.univ.erp.data.StudentData;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField userIdField = new JTextField(5);
    private final JTextField rollNoField = new JTextField(10);
    private final JTextField programField = new JTextField(10);
    private final JTextField yearField = new JTextField(5);

    private final AdminService adminService = new AdminService();

    public StudentsPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"User ID", "Username", "Roll No", "Program", "Year", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };
        table = new JTable(model);

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton deleteButton = new JButton("Delete selected");
        deleteButton.addActionListener(e -> onDeleteSelected());
        JButton disableButton = new JButton("Disable account");
        disableButton.addActionListener(e -> onToggleDisableSelected(true));
        JButton enableButton = new JButton("Enable account");
        enableButton.addActionListener(e -> onToggleDisableSelected(false));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadStudents());

        controls.add(deleteButton);
        controls.add(disableButton);
        controls.add(enableButton);
        controls.add(refreshButton);

        add(controls, BorderLayout.SOUTH);

        loadStudents();
    }

    public void loadStudents() {
        model.setRowCount(0);

        // Build a map userId -> username from auth DB for display.
        Map<Integer, String> usernames = new HashMap<>();
        for (User u : AuthDB.loadUsers()) {
            usernames.put(u.getUserId(), u.getUsername());
        }

        List<Student> students = StudentData.listAll();
        // Sort alphabetically by username (falling back to empty string if missing)
        students.sort(Comparator.comparing(s -> usernames.getOrDefault(s.getUserId(), ""), String.CASE_INSENSITIVE_ORDER));

        for (Student s : students) {
            String status = DisabledUsers.isDisabled(s.getUserId()) ? "disabled" : "active";
            String username = usernames.getOrDefault(s.getUserId(), "");
            model.addRow(new Object[]{
                    s.getUserId(),
                    username,
                    s.getRollNo(),
                    s.getProgram(),
                    s.getYear(),
                    status
            });
        }
    }

    private void onAddStudent() {
        try {
            int userId = Integer.parseInt(userIdField.getText().trim());
            String rollNo = rollNoField.getText().trim();
            String program = programField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            if (rollNo.isEmpty() || program.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Roll No and Program are required",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Student s = new Student(userId, rollNo, program, year);
            String err = adminService.addStudent(s);
            if (err == null) {
                loadStudents();
                userIdField.setText("");
                rollNoField.setText("");
                programField.setText("");
                yearField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        err,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "User ID and Year must be integers",
                    "Invalid input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a student row to delete",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0); // User ID column
        int userId;
        try {
            if (val instanceof Integer) {
                userId = (Integer) val;
            } else {
                userId = Integer.parseInt(val.toString());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid user id in table",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete student with userId=" + userId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = adminService.deleteStudent(userId);
        if (err == null) {
            loadStudents();
        } else {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onToggleDisableSelected(boolean disable) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a student row first",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0); // User ID column
        int userId;
        try {
            if (val instanceof Integer) {
                userId = (Integer) val;
            } else {
                userId = Integer.parseInt(val.toString());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid user id in table",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String action = disable ? "disable" : "enable";
        int confirm = JOptionPane.showConfirmDialog(this,
                "" + (disable ? "Disable" : "Enable") + " account for userId=" + userId + "?",
                "Confirm " + action,
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = disable ? adminService.disableUser(userId) : adminService.enableUser(userId);
        if (err != null) {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
