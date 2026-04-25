package edu.univ.erp.ui;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final AuthService authService = new AuthService();

    private final JButton changePasswordButton = new JButton("Change Password"); // currently unused, kept for future
    private final JLabel blockLabel = new JLabel("");
    private int failedAttempts = 0;
    private long blockUntilMillis = 0L;
    private Timer blockTimer;

    public LoginFrame() {
        super("Login - University ERP");
        initUi();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 220);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username label + field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Password label + field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Login button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> onLogin());

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        // Change password button is now only available from main portals, not here after failures.
        // Block / countdown label (for too many failed attempts)
        blockLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(blockLabel, gbc);

        // Timer to update the countdown text when blocked
        blockTimer = new Timer(1000, e -> updateBlockCountdown());

        setContentPane(panel);
    }

    private void onLogin() {
        long now = System.currentTimeMillis();
        if (blockUntilMillis > now) {
            long remainingMs = blockUntilMillis - now;
            long seconds = remainingMs / 1000L;
            long minutes = seconds / 60L;
            long remSec = seconds % 60L;
            String msg = String.format("Too many failed attempts. Please wait %02d:%02d before trying again.", minutes, remSec);
            JOptionPane.showMessageDialog(this,
                    msg,
                    "Login blocked",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter username and password",
                    "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        User logged = authService.login(username, password);
        if (logged != null) {
            // Successful login: clear any block state
            failedAttempts = 0;
            blockUntilMillis = 0L;
            blockLabel.setText("");
            if (blockTimer != null && blockTimer.isRunning()) {
                blockTimer.stop();
            }
            changePasswordButton.setVisible(false);

            Session.setUser(logged);
            JOptionPane.showMessageDialog(this,
                    "Welcome " + logged.getUsername() + " (" + logged.getRole() + ")");

            SwingUtilities.invokeLater(() -> {
                switch (logged.getRole()) {
                    case "student":
                        new StudentMainFrame().setVisible(true);
                        break;
                    case "instructor":
                        new InstructorMainFrame().setVisible(true);
                        break;
                    case "admin":
                    default:
                        new AdminMainFrame().setVisible(true);
                        break;
                }
            });
            dispose();
        } else {
            failedAttempts++;

            if (failedAttempts >= 5) {
                failedAttempts = 0;
                blockUntilMillis = System.currentTimeMillis() + 5L * 60L * 1000L; // 5 minutes
                if (blockTimer != null && !blockTimer.isRunning()) {
                    blockTimer.start();
                }
                JOptionPane.showMessageDialog(this,
                        "Too many failed attempts. You are blocked for 5 minutes.",
                        "Login blocked",
                        JOptionPane.ERROR_MESSAGE);
                updateBlockCountdown();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onChangePassword() {
        JTextField userField = new JTextField(usernameField.getText().trim(), 12);
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

        String username = userField.getText().trim();
        String oldPwd = new String(oldPassField.getPassword());
        String newPwd = new String(newPassField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (username.isEmpty() || oldPwd.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
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

        String err = authService.changePassword(username, oldPwd, newPwd);
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

    private void updateBlockCountdown() {
        if (blockUntilMillis <= 0L) {
            blockLabel.setText("");
            if (blockTimer != null && blockTimer.isRunning()) {
                blockTimer.stop();
            }
            return;
        }
        long now = System.currentTimeMillis();
        if (now >= blockUntilMillis) {
            blockUntilMillis = 0L;
            blockLabel.setText("");
            if (blockTimer != null && blockTimer.isRunning()) {
                blockTimer.stop();
            }
            return;
        }

        long remainingMs = blockUntilMillis - now;
        long seconds = remainingMs / 1000L;
        long minutes = seconds / 60L;
        long remSec = seconds % 60L;
        blockLabel.setText(String.format("Too many failed attempts. Try again in %02d:%02d", minutes, remSec));
    }
}
