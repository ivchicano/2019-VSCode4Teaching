package com.vscode4teaching.vscode4teachingserver.services.exceptions;

public class TeacherNotFoundException extends UserNotFoundException {
    private static final long serialVersionUID = 9849813144662442L;

    public TeacherNotFoundException(String username) {
        super("Teacher not found: " + username);
    }
}