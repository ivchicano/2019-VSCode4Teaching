package com.vscode4teaching.vscode4teachingserver.model.views;

public class CourseViews {
    public static interface GeneralView {
    }

    public static interface ExercisesView extends GeneralView, ExerciseViews.GeneralView {
    }
}