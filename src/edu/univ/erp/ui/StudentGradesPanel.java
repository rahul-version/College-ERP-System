package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.data.EnrollmentData;
import edu.univ.erp.data.GradeData;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class StudentGradesPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final StudentService studentService = new StudentService();

    public StudentGradesPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"SectionId", "Quiz", "Midsem", "Endsem", "FinalScore"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadGrades());
        add(refreshButton, BorderLayout.SOUTH);

        loadGrades();
    }

    private void loadGrades() {
        model.setRowCount(0);
        User u = Session.getUser();
        if (u == null) return;
        int studentId = u.getUserId();

        for (Enrollment e : EnrollmentData.listAll()) {
            if (e.getStudentId() != studentId) continue;
            if (!"enrolled".equals(e.getStatus())) continue;

            double quiz = Double.NaN;
            double midsem = Double.NaN;
            double endsem = Double.NaN;

            for (Grade g : GradeData.findByEnrollment(e.getEnrollmentId())) {
                String comp = g.getComponent().toLowerCase();
                if ("quiz".equals(comp)) {
                    quiz = g.getScore();
                } else if ("midsem".equals(comp) || "midterm".equals(comp)) {
                    midsem = g.getScore();
                } else if ("endsem".equals(comp) || "endterm".equals(comp)) {
                    endsem = g.getScore();
                }
            }

            double finalScore = GradeData.computeFinalForEnrollment(e.getEnrollmentId());

            model.addRow(new Object[]{
                    e.getSectionId(),
                    Double.isNaN(quiz) ? "" : quiz,
                    Double.isNaN(midsem) ? "" : midsem,
                    Double.isNaN(endsem) ? "" : endsem,
                    finalScore
            });
        }
    }
}
