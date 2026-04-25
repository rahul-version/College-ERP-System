package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        super("University ERP");
        initUi();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Menu bar with Logout / Exit
        JMenuBar menuBar = new JMenuBar();
        JMenu userMenu = new JMenu("User");

        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> onLogout());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));

        userMenu.add(logoutItem);
        userMenu.addSeparator();
        userMenu.add(exitItem);
        menuBar.add(userMenu);
        setJMenuBar(menuBar);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Dashboard", new JLabel("Welcome to University ERP", SwingConstants.CENTER));
        tabs.addTab("Students", new StudentsPanel());
        tabs.addTab("Courses", new CoursesPanel());

        setContentPane(tabs);
    }

    private void onLogout() {
        // Clear session and return to login screen
        Session.setUser(null);
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        dispose();
    }
}
