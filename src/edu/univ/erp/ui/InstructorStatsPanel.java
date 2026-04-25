package edu.univ.erp.ui;

import edu.univ.erp.data.EnrollmentData;
import edu.univ.erp.data.GradeData;
import edu.univ.erp.data.SectionData;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InstructorStatsPanel extends JPanel {

    private final JTextField sectionIdField = new JTextField(10);

    public InstructorStatsPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        add(new JLabel("Section ID:"));
        add(sectionIdField);
        JButton computeButton = new JButton("Compute avg final");
        computeButton.addActionListener(e -> onCompute());
        add(computeButton);
    }

    private void onCompute() {
        String sectionId = sectionIdField.getText().trim();
        if (sectionId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter section id", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Section s = SectionData.getById(sectionId);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Section not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Enrollment> enrollments = EnrollmentData.listAll();
        double sum = 0;
        int count = 0;
        for (Enrollment e : enrollments) {
            if (sectionId.equals(e.getSectionId()) && "enrolled".equals(e.getStatus())) {
                double finalScore = GradeData.computeFinalForEnrollment(e.getEnrollmentId());
                sum += finalScore;
                count++;
            }
        }
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "No enrollments", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            double avg = sum / count;
            JOptionPane.showMessageDialog(this, "Average final = " + avg, "Stats", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
