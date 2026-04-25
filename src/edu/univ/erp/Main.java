package edu.univ.erp;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.Session;
import edu.univ.erp.domain.*;
import edu.univ.erp.data.*;

import javax.swing.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // ============================
        // 1. INITIALIZE DATA FILES
        // ============================
        CourseData.init();
        StudentData.init();
        InstructorData.init();
        SectionData.init();
        EnrollmentData.init();
        GradeData.init();
        SettingsData.init();

        
        // ============================
        // 2. START SWING UI (LOGIN WINDOW)
        // ============================
        SwingUtilities.invokeLater(() -> {
            new edu.univ.erp.ui.LoginFrame().setVisible(true);
        });
    }
}
