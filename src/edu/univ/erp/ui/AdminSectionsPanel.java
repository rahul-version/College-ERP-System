package edu.univ.erp.ui;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.data.SectionData;
import edu.univ.erp.data.SettingsData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminSectionsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;

    // Use ~20 columns so each text box is comfortably wide (about 20 characters)
    // while keeping the same layout order.
    private final JTextField courseCodeField = new JTextField(20);
    private final JTextField instructorIdField = new JTextField(20);
    private final JTextField dayTimeField = new JTextField(20);
    private final JTextField roomField = new JTextField(20);
    private final JTextField capacityField = new JTextField(20);
    private final JTextField semesterField = new JTextField(20);
    private final JTextField yearField = new JTextField(20);

    // Admin-configurable registration and drop timelines (in minutes).
    private final JTextField registerMinutesField = new JTextField(10);
    private final JTextField dropMinutesField = new JTextField(10);

    private final JRadioButton sectionAButton = new JRadioButton("Section A", true);
    private final JRadioButton sectionBButton = new JRadioButton("Section B");

    private final AdminService adminService = new AdminService();

    public AdminSectionsPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"SectionId", "CourseCode", "InstructorId", "Day/Time", "Room", "Cap", "Sem", "Year"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Use GridBagLayout so labels align with their input fields,
        // and keep Section A/B radio buttons close together.
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        // Do not stretch components horizontally; keep them at their preferred size
        // so text boxes remain compact, aligned roughly under the Section A/B controls.
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Section choice A/B in a small sub-panel to reduce gap between them
        ButtonGroup sectionGroup = new ButtonGroup();
        sectionGroup.add(sectionAButton);
        sectionGroup.add(sectionBButton);
        JPanel sectionRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sectionRadioPanel.add(sectionAButton);
        sectionRadioPanel.add(sectionBButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Section"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(sectionRadioPanel, gbc);
        row++;

        // CourseCode
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("CourseCode"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(courseCodeField, gbc);
        row++;

        // Instructor Id
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("InstrId"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(instructorIdField, gbc);
        row++;

        // Day/Time
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Day/Time"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(dayTimeField, gbc);
        row++;

        // Room
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Room"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(roomField, gbc);
        row++;

        // Capacity
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Cap"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(capacityField, gbc);
        row++;

        // Semester
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Sem"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(semesterField, gbc);
        row++;

        // Year
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Year"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(yearField, gbc);
        row++;

        // Registration timeline (minutes)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Reg timeline (min)"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(registerMinutesField, gbc);
        row++;

        // Drop timeline (minutes)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Drop timeline (min)"), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        form.add(dropMinutesField, gbc);
        row++;

        // Buttons row
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton createButton = new JButton("Save / Assign section");
        createButton.addActionListener(e -> onCreateOrAssignSection());
        buttonsPanel.add(createButton);

        JButton deleteButton = new JButton("Delete section");
        deleteButton.addActionListener(e -> onDeleteSection());
        buttonsPanel.add(deleteButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadSections());
        buttonsPanel.add(refreshButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0;
        form.add(buttonsPanel, gbc);

        add(form, BorderLayout.SOUTH);

        loadSections();
    }

    private void loadSections() {
        model.setRowCount(0);
        List<Section> list = SectionData.listAll();
        for (Section s : list) {
            model.addRow(new Object[]{s.getSectionId(), s.getCourseCode(), s.getInstructorId(), s.getDayTime(), s.getRoom(), s.getCapacity(), s.getSemester(), s.getYear()});
        }
    }

    private void onCreateOrAssignSection() {
        try {
            String code = courseCodeField.getText().trim();
            String secSuffix = sectionAButton.isSelected() ? "-A" : "-B";
            String secId = code + secSuffix;
            int instrId = Integer.parseInt(instructorIdField.getText().trim());
            String dayTime = dayTimeField.getText().trim();
            String room = roomField.getText().trim();
            int cap = Integer.parseInt(capacityField.getText().trim());
            String sem = semesterField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());

            String regMinutesStr = registerMinutesField.getText().trim();
            String dropMinutesStr = dropMinutesField.getText().trim();

            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "CourseCode required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (cap < 0) {
                JOptionPane.showMessageDialog(this, "Capacity cannot be negative", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (regMinutesStr.isEmpty() || dropMinutesStr.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Registration and drop timelines (in minutes) are required",
                        "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!regMinutesStr.matches("\\d+") || !dropMinutesStr.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "Timelines must be numeric minutes",
                        "Invalid timelines",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int regMinutes = Integer.parseInt(regMinutesStr);
            int dropMinutes = Integer.parseInt(dropMinutesStr);
            if (regMinutes < 0 || dropMinutes < 0) {
                JOptionPane.showMessageDialog(this,
                        "Timelines cannot be negative",
                        "Invalid timelines",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If section already exists, just re-assign the instructor.
            Section existing = SectionData.getById(secId);
            String err;
            if (existing != null) {
                err = adminService.assignInstructor(secId, instrId);
            } else {
                Section s = new Section(secId, code, instrId, dayTime, room, cap, sem, year);
                err = adminService.createSection(s);
            }

            if (err == null) {
                // Set register and drop deadlines for this section based on admin timelines.
                long now = System.currentTimeMillis();
                long registerUntil = now + regMinutes * 60L * 1000L;
                long dropUntil = now + dropMinutes * 60L * 1000L;
                SettingsData.set("registerUntil_" + secId, String.valueOf(registerUntil));
                SettingsData.set("dropUntil_" + secId, String.valueOf(dropUntil));

                JOptionPane.showMessageDialog(this, "Section saved/assigned", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSections();
            } else {
                JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "InstructorId, capacity, year, and timelines must be numbers", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteSection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a section row to delete",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0); // SectionId column
        String secId = val == null ? null : val.toString().trim();
        if (secId == null || secId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid section id in table",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete section " + secId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = adminService.deleteSection(secId);
        if (err == null) {
            loadSections();
        } else {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
