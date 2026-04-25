package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.CourseData;
import edu.univ.erp.data.InstructorData;
import edu.univ.erp.data.SectionData;
import edu.univ.erp.data.SettingsData;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentCatalogPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField sectionIdField = new JTextField(10);
    private final StudentService studentService = new StudentService();

    // Search by course code.
    private final JTextField searchCodeField = new JTextField(10);

    // Countdown label and timer for registration deadline.
    private final JLabel registerTimerLabel = new JLabel();
    private final javax.swing.Timer registerTimer;

    public StudentCatalogPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{
                "SectionId", "CourseCode", "Title", "Credits", "Capacity", "Instructor", "Day/Time", "Room"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Section to register:"));
        bottom.add(sectionIdField);
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> onRegister());

        // Search by course code controls
        bottom.add(new JLabel("Search code:"));
        bottom.add(searchCodeField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> onSearchByCode());
        bottom.add(searchButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            searchCodeField.setText("");
            loadCatalog();
        });
        bottom.add(registerButton);
        bottom.add(refreshButton);

        // Countdown label shown next to the register controls.
        registerTimerLabel.setForeground(Color.BLUE);
        bottom.add(registerTimerLabel);

        add(bottom, BorderLayout.SOUTH);

        // Update countdown every second based on the currently selected/typed section.
        registerTimer = new javax.swing.Timer(1000, e -> updateRegisterCountdown());
        registerTimer.start();

        // When a table row is selected, auto-fill sectionIdField and refresh timer.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableSelectionChanged();
            }
        });

        loadCatalog();
    }

    private void onTableSelectionChanged() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0); // SectionId column
        if (val != null) {
            sectionIdField.setText(val.toString());
            updateRegisterCountdown();
        }
    }

    private void updateRegisterCountdown() {
        String sectionId = sectionIdField.getText().trim();
        if (sectionId.isEmpty()) {
            registerTimerLabel.setText("");
            return;
        }
        String v = SettingsData.get("registerUntil_" + sectionId);
        if (v == null) {
            registerTimerLabel.setText("No registration deadline set");
            return;
        }
        try {
            long until = Long.parseLong(v);
            long remainingMs = until - System.currentTimeMillis();
            if (remainingMs <= 0) {
                registerTimerLabel.setText("Registration blocked (deadline passed)");
            } else {
                long seconds = remainingMs / 1000L;
                long minutes = seconds / 60L;
                long remSec = seconds % 60L;
                registerTimerLabel.setText(String.format("Register deadline: %02d:%02d left", minutes, remSec));
            }
        } catch (NumberFormatException ex) {
            registerTimerLabel.setText("");
        }
    }

    private void loadCatalog() {
        loadCatalog(null);
    }

    private void loadCatalog(String courseCodeFilter) {
        model.setRowCount(0);
        List<Section> sections = SectionData.listAll();
        List<Course> courses = CourseData.listAll();
        List<Instructor> instructors = InstructorData.listAll();

        // Map instructor userId -> username so students can see instructor name
        java.util.Map<Integer, String> instructorNames = new java.util.HashMap<>();
        for (edu.univ.erp.domain.User u : AuthDB.loadUsers()) {
            if ("instructor".equals(u.getRole())) {
                instructorNames.put(u.getUserId(), u.getUsername());
            }
        }

        for (Section s : sections) {
            // If a course code filter is provided, skip sections that do not match.
            if (courseCodeFilter != null && !courseCodeFilter.isEmpty() &&
                    !s.getCourseCode().equalsIgnoreCase(courseCodeFilter)) {
                continue;
            }

            Course c = courses.stream()
                    .filter(course -> course.getCode().equals(s.getCourseCode()))
                    .findFirst().orElse(null);
            Instructor instr = instructors.stream()
                    .filter(i -> i.getUserId() == s.getInstructorId())
                    .findFirst().orElse(null);
            String title = c != null ? c.getTitle() : "";
            int credits = c != null ? c.getCredits() : 0;
            String instrName = "";
            if (instr != null) {
                String name = instructorNames.get(instr.getUserId());
                instrName = (name != null && !name.isEmpty()) ? name : ("ID " + instr.getUserId());
            }

            model.addRow(new Object[]{
                    s.getSectionId(),
                    s.getCourseCode(),
                    title,
                    credits,
                    s.getCapacity(),
                    instrName,
                    s.getDayTime(),
                    s.getRoom()
            });
        }
    }

    private void onSearchByCode() {
        String code = searchCodeField.getText().trim();
        loadCatalog(code);
    }

    private void onRegister() {
        String sectionId = sectionIdField.getText().trim();
        if (sectionId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter section id", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User u = Session.getUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "No user in session", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int studentId = u.getUserId();
        String err = studentService.registerSection(studentId, sectionId);
        if (err == null) {
            JOptionPane.showMessageDialog(this, "Registered successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, err, "Register failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
