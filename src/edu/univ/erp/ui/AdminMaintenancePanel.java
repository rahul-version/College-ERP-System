package edu.univ.erp.ui;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

public class AdminMaintenancePanel extends JPanel {

    private final JLabel statusLabel = new JLabel();
    private final AdminService adminService = new AdminService();

    public AdminMaintenancePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton onButton = new JButton("Maintenance ON");
        JButton offButton = new JButton("Maintenance OFF");

        onButton.addActionListener(e -> toggle(true));
        offButton.addActionListener(e -> toggle(false));

        add(onButton);
        add(offButton);
        add(statusLabel);

        refreshStatus();
    }

    private void refreshStatus() {
        statusLabel.setText("Current: " + (AccessControl.isMaintenanceOn() ? "ON" : "OFF"));
    }

    private void toggle(boolean on) {
        String err = adminService.toggleMaintenance(on);
        if (err == null) {
            JOptionPane.showMessageDialog(this, "Maintenance " + (on ? "enabled" : "disabled"), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshStatus();
        } else {
            JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
