package edu.univ.erp.access;

import edu.univ.erp.data.SettingsData;
import edu.univ.erp.domain.User;

public class AccessControl {

    private static boolean maintenanceMode;

    static {
        // Initialize from settings storage so maintenance survives restarts
        maintenanceMode = SettingsData.isMaintenanceOn();
    }

    public static boolean isMaintenanceOn() {
        return maintenanceMode;
    }

    public static void enableMaintenance() {
        maintenanceMode = true;
        SettingsData.set("maintenance", "true");
    }

    public static void disableMaintenance() {
        maintenanceMode = false;
        SettingsData.set("maintenance", "false");
    }

    public static boolean isAllowed(User user, String action) {

        if (maintenanceMode && !"admin".equals(user.getRole())) {
            return false;
        }

        switch (action) {

            case "register":
            case "drop":
            case "viewTranscript":
            case "viewTimetable":
            case "exportTranscript":
                return "student".equals(user.getRole());

            case "enterMarks":
            case "gradeSection":
            case "viewInstructorSections":
            case "exportGrades":
                return "instructor".equals(user.getRole());

            case "createUser":
            case "createCourse":
            case "createSection":
            case "assignInstructor":
            case "toggleMaintenance":
                return "admin".equals(user.getRole());

            default:
                return false;
        }
    }
}
