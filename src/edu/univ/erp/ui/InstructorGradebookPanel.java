package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InstructorGradebookPanel extends JPanel {

    private final JComboBox<String> sectionBox = new JComboBox<>();
    private final DefaultTableModel model;
    private final JTable table;

    private final JTextField componentField = new JTextField(10);
    private final JTextField scoreField = new JTextField(5);

    private final InstructorService instructorService = new InstructorService();

    public InstructorGradebookPanel() {
        setLayout(new BorderLayout());

        // Top panel: choose section
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Section:"));
        top.add(sectionBox);
        JButton loadButton = new JButton("Load students");
        loadButton.addActionListener(e -> loadEnrollmentsForSelectedSection());
        top.add(loadButton);
        add(top, BorderLayout.NORTH);

        // Table of students in the selected section
        model = new DefaultTableModel(new Object[]{
                "EnrollmentId", "StudentId", "Name", "RollNo", "CourseCode", "SectionId", "FinalScore"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom panel: enter component + score for selected enrollment
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Component:"));
        bottom.add(componentField);
        bottom.add(new JLabel("Score:"));
        bottom.add(scoreField);

        JButton saveButton = new JButton("Save score for selected");
        saveButton.addActionListener(e -> onSaveScoreForSelected());
        bottom.add(saveButton);

        JButton computeButton = new JButton("Compute final for selected");
        computeButton.addActionListener(e -> onComputeFinalForSelected());
        bottom.add(computeButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadEnrollmentsForSelectedSection());
        bottom.add(refreshButton);

        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(e -> onExportCsvForSelectedSection());
        bottom.add(exportButton);

        add(bottom, BorderLayout.SOUTH);

        loadInstructorSections();
    }

    // Load sections taught by this instructor into the combo box
    private void loadInstructorSections() {
        sectionBox.removeAllItems();
        User u = Session.getUser();
        if (u == null) return;
        int instructorId = u.getUserId();
        List<Section> sections = new ArrayList<>();
        for (Section s : SectionData.listAll()) {
            if (s.getInstructorId() == instructorId) {
                sections.add(s);
                sectionBox.addItem(s.getSectionId());
            }
        }
        if (sectionBox.getItemCount() > 0) {
            sectionBox.setSelectedIndex(0);
            loadEnrollmentsForSelectedSection();
        }
    }

    // Load all enrolled students for the selected section into the table
    private void loadEnrollmentsForSelectedSection() {
        model.setRowCount(0);
        String sectionId = (String) sectionBox.getSelectedItem();
        if (sectionId == null || sectionId.isEmpty()) return;

        // Build maps for quick lookup
        List<Student> allStudents = StudentData.listAll();
        java.util.Map<Integer, String> rollByStudent = new java.util.HashMap<>();
        for (Student s : allStudents) {
            rollByStudent.put(s.getUserId(), s.getRollNo());
        }
        // Map userId -> username so instructor can see student names
        java.util.Map<Integer, String> nameByStudent = new java.util.HashMap<>();
        for (User u : AuthDB.loadUsers()) {
            nameByStudent.put(u.getUserId(), u.getUsername());
        }

        Section sec = SectionData.getById(sectionId);
        String courseCode = sec != null ? sec.getCourseCode() : "";

        for (Enrollment e : EnrollmentData.listAll()) {
            if (!"enrolled".equals(e.getStatus())) continue;
            if (!sectionId.equals(e.getSectionId())) continue;
            double finalScore = GradeData.computeFinalForEnrollment(e.getEnrollmentId());
            String roll = rollByStudent.getOrDefault(e.getStudentId(), "");
            String name = nameByStudent.getOrDefault(e.getStudentId(), "");
            model.addRow(new Object[]{
                    e.getEnrollmentId(),
                    e.getStudentId(),
                    name,
                    roll,
                    courseCode,
                    sectionId,
                    finalScore
            });
        }
    }

    // Helpers to get the selected enrollmentId from the table
    private Integer getSelectedEnrollmentId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a student row first",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0); // EnrollmentId column
        try {
            if (val instanceof Integer) {
                return (Integer) val;
            }
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid enrollment id in table",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void onSaveScoreForSelected() {
        Integer enrollmentId = getSelectedEnrollmentId();
        if (enrollmentId == null) return;
        try {
            String component = componentField.getText().trim();
            double score = Double.parseDouble(scoreField.getText().trim());
            if (component.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Component required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String err = instructorService.enterMarks(enrollmentId, component, score);
            if (err == null) {
                JOptionPane.showMessageDialog(this, "Score saved",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadEnrollmentsForSelectedSection();
            } else {
                JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score must be a number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onComputeFinalForSelected() {
        Integer enrollmentId = getSelectedEnrollmentId();
        if (enrollmentId == null) return;
        double finalScore = instructorService.computeFinal(enrollmentId);
        if (finalScore < 0) {
            JOptionPane.showMessageDialog(this, "Not allowed or invalid enrollment", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Final score: " + finalScore,
                    "Final", JOptionPane.INFORMATION_MESSAGE);
            loadEnrollmentsForSelectedSection();
        }
    }

    private void onExportCsvForSelectedSection() {
        String sectionId = (String) sectionBox.getSelectedItem();
        if (sectionId == null || sectionId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Select a section first",
                    "No section",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save gradebook as CSV");
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        String path = f.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            path = path + ".csv";
        }

        String err = instructorService.exportSectionGradesCsv(sectionId, path);
        if (err == null) {
            JOptionPane.showMessageDialog(this,
                    "Grades exported",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Export failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
