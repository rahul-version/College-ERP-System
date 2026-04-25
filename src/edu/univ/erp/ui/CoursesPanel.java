package edu.univ.erp.ui;

import edu.univ.erp.data.CourseData;
import edu.univ.erp.data.SectionData;
import edu.univ.erp.data.EnrollmentData;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CoursesPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField codeField = new JTextField(8);
    private final JTextField titleField = new JTextField(15);
    private final JTextField creditsField = new JTextField(4);

    private final AdminService adminService = new AdminService();

    public CoursesPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"Code", "Title", "Credits"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Code:"));
        form.add(codeField);
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Credits:"));
        form.add(creditsField);

        JButton addButton = new JButton("Add Course");
        addButton.addActionListener(e -> onAddCourse());
        JButton deleteButton = new JButton("Delete selected");
        deleteButton.addActionListener(e -> onDeleteSelected());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadCourses());

        form.add(addButton);
        form.add(deleteButton);
        form.add(refreshButton);

        add(form, BorderLayout.SOUTH);

        loadCourses();
    }

    private void loadCourses() {
        model.setRowCount(0);
        List<Course> courses = CourseData.listAll();
        for (Course c : courses) {
            model.addRow(new Object[]{
                    c.getCode(),
                    c.getTitle(),
                    c.getCredits()
            });
        }
    }

    private void onAddCourse() {
        try {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            int credits = Integer.parseInt(creditsField.getText().trim());

            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Code and Title are required",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Course c = new Course(code, title, credits);
            String err = adminService.createCourse(c);
            if (err == null) {
                loadCourses();
                codeField.setText("");
                titleField.setText("");
                creditsField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        err,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Credits must be an integer",
                    "Invalid input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a course row to delete",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0); // Code column
        String code = val == null ? null : val.toString().trim();
        if (code == null || code.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid course code in table",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if this course has sections with instructors and/or enrolled students.
        java.util.List<String> sectionIds = new java.util.ArrayList<>();
        boolean hasSections = false;
        boolean hasEnrollments = false;

        for (Section s : SectionData.listAll()) {
            if (code.equalsIgnoreCase(s.getCourseCode())) {
                hasSections = true;
                sectionIds.add(s.getSectionId());
            }
        }
        if (!sectionIds.isEmpty()) {
            for (Enrollment e : EnrollmentData.listAll()) {
                if (sectionIds.contains(e.getSectionId()) && "enrolled".equals(e.getStatus())) {
                    hasEnrollments = true;
                    break;
                }
            }
        }

        StringBuilder msg = new StringBuilder("Delete course with code=" + code + "?");
        if (hasSections || hasEnrollments) {
            msg.append("\n\nWarning: ");
            if (hasSections) {
                msg.append("One or more sections (with instructors) are assigned to this course.");
            }
            if (hasEnrollments) {
                if (hasSections) msg.append(" ");
                msg.append("There are also student registrations for this course.");
            }
            msg.append("\nThis will remove the course, its sections, and those student registrations.");
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                msg.toString(),
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = adminService.deleteCourse(code);
        if (err == null) {
            loadCourses();
        } else {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
