package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.data.EnrollmentData;
import edu.univ.erp.data.SettingsData;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentRegistrationsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField sectionIdField = new JTextField(10);
    private final StudentService studentService = new StudentService();

    // Countdown label and timer for drop deadline.
    private final JLabel dropTimerLabel = new JLabel();
    private javax.swing.Timer dropTimer;

    public StudentRegistrationsPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"EnrollmentId", "SectionId", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Section to drop:"));
        bottom.add(sectionIdField);
        JButton dropButton = new JButton("Drop");
        dropButton.addActionListener(e -> onDrop());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRegistrations());
        bottom.add(dropButton);
        bottom.add(refreshButton);

        // Countdown label shown next to the drop controls.
        dropTimerLabel.setForeground(Color.BLUE);
        bottom.add(dropTimerLabel);

        add(bottom, BorderLayout.SOUTH);

        // Update countdown every second based on the currently selected/typed section.
        dropTimer = new javax.swing.Timer(1000, e -> updateDropCountdown());
        dropTimer.start();

        // When a table row is selected, auto-fill sectionIdField and refresh timer.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableSelectionChanged();
            }
        });

        loadRegistrations();
    }

    private void onTableSelectionChanged() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 1); // SectionId column
        if (val != null) {
            sectionIdField.setText(val.toString());
            updateDropCountdown();
        }
    }

    private void updateDropCountdown() {
        String sectionId = sectionIdField.getText().trim();
        if (sectionId.isEmpty()) {
            dropTimerLabel.setText("");
            return;
        }
        String v = SettingsData.get("dropUntil_" + sectionId);
        if (v == null) {
            dropTimerLabel.setText("No drop deadline set");
            return;
        }
        try {
            long until = Long.parseLong(v);
            long remainingMs = until - System.currentTimeMillis();
            if (remainingMs <= 0) {
                dropTimerLabel.setText("Drop blocked (deadline passed)");
            } else {
                long seconds = remainingMs / 1000L;
                long minutes = seconds / 60L;
                long remSec = seconds % 60L;
                dropTimerLabel.setText(String.format("Drop deadline: %02d:%02d left", minutes, remSec));
            }
        } catch (NumberFormatException ex) {
            dropTimerLabel.setText("");
        }
    }

    private void loadRegistrations() {
        model.setRowCount(0);
        User u = Session.getUser();
        if (u == null) return;
        int studentId = u.getUserId();
        List<Enrollment> list = studentService.getEnrollments(studentId);
        for (Enrollment e : list) {
            model.addRow(new Object[]{e.getEnrollmentId(), e.getSectionId(), e.getStatus()});
        }
    }

    private void onDrop() {
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
        String err = studentService.dropSection(studentId, sectionId);
        if (err == null) {
            JOptionPane.showMessageDialog(this, "Dropped successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadRegistrations();
        } else {
            JOptionPane.showMessageDialog(this, err, "Drop failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
