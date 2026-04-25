package edu.univ.erp.ui;

import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.User;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StudentTranscriptPanel extends JPanel {

    private final StudentService studentService = new StudentService();

    public StudentTranscriptPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton exportButton = new JButton("Export Transcript (CSV)");
        exportButton.addActionListener(e -> onExport());
        add(exportButton);
    }

    private void onExport() {
        User u = Session.getUser();
        if (u == null) {
            JOptionPane.showMessageDialog(this, "No user in session", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int studentId = u.getUserId();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save transcript as CSV");
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        String path = f.getAbsolutePath();
        // Ensure the file has a .csv extension so it opens correctly in spreadsheet apps.
        if (!path.toLowerCase().endsWith(".csv")) {
            path = path + ".csv";
        }

        String err = studentService.exportTranscriptCsv(studentId, path);
        if (err == null) {
            JOptionPane.showMessageDialog(this, "Transcript exported", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, err, "Export failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
