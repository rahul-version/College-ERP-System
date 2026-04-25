package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.DisabledUsers;
import edu.univ.erp.data.InstructorData;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminInstructorsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;

    private final AdminService adminService = new AdminService();

    public AdminInstructorsPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"User ID", "Username", "Department", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deleteButton = new JButton("Delete selected");
        deleteButton.addActionListener(e -> onDeleteSelected());
        JButton disableButton = new JButton("Disable account");
        disableButton.addActionListener(e -> onToggleDisableSelected(true));
        JButton enableButton = new JButton("Enable account");
        enableButton.addActionListener(e -> onToggleDisableSelected(false));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadInstructors());

        bottom.add(deleteButton);
        bottom.add(disableButton);
        bottom.add(enableButton);
        bottom.add(refreshButton);

        add(bottom, BorderLayout.SOUTH);

        loadInstructors();
    }

    private void loadInstructors() {
        model.setRowCount(0);

        // Build a map userId -> username from auth DB for display.
        Map<Integer, String> usernames = new HashMap<>();
        for (User u : AuthDB.loadUsers()) {
            usernames.put(u.getUserId(), u.getUsername());
        }

        List<Instructor> list = InstructorData.listAll();
        for (Instructor i : list) {
            String status = DisabledUsers.isDisabled(i.getUserId()) ? "disabled" : "active";
            String username = usernames.getOrDefault(i.getUserId(), "");
            model.addRow(new Object[]{i.getUserId(), username, i.getDepartment(), status});
        }
    }

    private void onDeleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select an instructor row to delete",
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
                "Delete instructor with userId=" + userId + "?",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = adminService.deleteInstructor(userId);
        if (err == null) {
            loadInstructors();
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
                    "Select an instructor row first",
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
