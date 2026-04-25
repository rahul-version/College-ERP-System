package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentTimetablePanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final StudentService studentService = new StudentService();

    public StudentTimetablePanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"SectionId", "CourseCode", "Day/Time", "Room"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadTimetable());
        add(refreshButton, BorderLayout.SOUTH);

        loadTimetable();
    }

    private void loadTimetable() {
        model.setRowCount(0);
        User u = Session.getUser();
        if (u == null) return;
        int studentId = u.getUserId();
        List<Section> sections = studentService.viewTimetable(studentId);
        for (Section s : sections) {
            model.addRow(new Object[]{s.getSectionId(), s.getCourseCode(), s.getDayTime(), s.getRoom()});
        }
    }
}
