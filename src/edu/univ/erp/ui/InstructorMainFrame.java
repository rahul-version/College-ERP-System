package edu.univ.erp.ui;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

public class InstructorMainFrame extends JFrame {

    private final JLabel maintenanceLabel = new JLabel("", SwingConstants.CENTER);
    private Timer maintenanceTimer;
    private final AuthService authService = new AuthService();

    public InstructorMainFrame() {
        super("Instructor - University ERP");
        initUi();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> onLogout());
        JMenuItem changePwdItem = new JMenuItem("Change Password");
        changePwdItem.addActionListener(e -> onChangePassword());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        userMenu.add(logoutItem);
        userMenu.add(changePwdItem);
        userMenu.addSeparator();
        userMenu.add(exitItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Sections", new InstructorSectionsPanel());
        tabs.addTab("Gradebook", new InstructorGradebookPanel());
        tabs.addTab("Stats", new InstructorStatsPanel());

        updateMaintenanceBanner();
        maintenanceLabel.setOpaque(true);
        maintenanceLabel.setBackground(Color.YELLOW);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(maintenanceLabel, BorderLayout.NORTH);
        mainPanel.add(tabs, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Poll maintenance flag periodically so banner updates immediately after admin toggles it.
        maintenanceTimer = new Timer(2000, e -> updateMaintenanceBanner());
        maintenanceTimer.start();
    }

    private void updateMaintenanceBanner() {
        if (AccessControl.isMaintenanceOn()) {
            maintenanceLabel.setText("Maintenance Mode is ON - read-only");
        } else {
            maintenanceLabel.setText("");
        }
    }

    private void onLogout() {
        if (maintenanceTimer != null) {
            maintenanceTimer.stop();
        }
        Session.setUser(null);
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        dispose();
    }

    private void onChangePassword() {
        User current = Session.getUser();
        if (current == null) {
            JOptionPane.showMessageDialog(this,
                    "No user in session",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField userField = new JTextField(current.getUsername(), 12);
        userField.setEditable(false);
        JPasswordField oldPassField = new JPasswordField(12);
        JPasswordField newPassField = new JPasswordField(12);
        JPasswordField confirmField = new JPasswordField(12);

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Old password:"));
        panel.add(oldPassField);
        panel.add(new JLabel("New password:"));
        panel.add(newPassField);
        panel.add(new JLabel("Confirm new:"));
        panel.add(confirmField);

        int res = JOptionPane.showConfirmDialog(this, panel,
                "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String oldPwd = new String(oldPassField.getPassword());
        String newPwd = new String(newPassField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPwd.equals(confirm)) {
            JOptionPane.showMessageDialog(this,
                    "New passwords do not match",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String err = authService.changePassword(current.getUsername(), oldPwd, newPwd);
        if (err == null) {
            JOptionPane.showMessageDialog(this,
                    "Password changed successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    err,
                    "Change password failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
