package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorSectionsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final InstructorService instructorService = new InstructorService();

    public InstructorSectionsPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"SectionId", "CourseCode", "Day/Time", "Room", "Capacity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSections());
        add(refreshButton, BorderLayout.SOUTH);

        loadSections();
    }

    private void loadSections() {
        model.setRowCount(0);
        User u = Session.getUser();
        if (u == null) return;
        int instructorId = u.getUserId();
        List<Section> sections = instructorService.getInstructorSections(instructorId);
        if (sections == null) return;
        for (Section s : sections) {
            model.addRow(new Object[]{s.getSectionId(), s.getCourseCode(), s.getDayTime(), s.getRoom(), s.getCapacity()});
        }
    }
}
