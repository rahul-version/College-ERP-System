package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.DisabledUsers;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows all accounts from auth.csv with role, S/P/A code and status,
 * and lets admin disable/enable accounts.
 */
public class AdminAccountsPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final AdminService adminService = new AdminService();

    public AdminAccountsPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{
                "User ID", "Username", "Role", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton disableButton = new JButton("Disable account");
        disableButton.addActionListener(e -> onToggleDisableSelected(true));
        JButton enableButton = new JButton("Enable account");
        enableButton.addActionListener(e -> onToggleDisableSelected(false));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAccounts());

        bottom.add(disableButton);
        bottom.add(enableButton);
        bottom.add(refreshButton);

        add(bottom, BorderLayout.SOUTH);

        loadAccounts();
    }

    private void loadAccounts() {
        model.setRowCount(0);
        List<User> users = AuthDB.loadUsers();
        for (User u : users) {
            int userId = u.getUserId();
            String role = u.getRole();
            String status = DisabledUsers.isDisabled(userId) ? "disabled" : "active";

            model.addRow(new Object[]{
                    userId,
                    u.getUsername(),
                    role,
                    status
            });
        }

        // Simple numeric sort by User ID
        javax.swing.RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (sorter != null) {
            java.util.List<javax.swing.RowSorter.SortKey> keys = new ArrayList<>();
            keys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
        }
    }

    private void onToggleDisableSelected(boolean disable) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a user row first",
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
                (disable ? "Disable" : "Enable") + " account for userId=" + userId + "?",
                "Confirm " + action,
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String err = disable ? adminService.disableUser(userId) : adminService.enableUser(userId);
        if (err != null) {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            loadAccounts();
        }
    }
}
