package com.vscode4teaching.vscode4teachingserver.controllers;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonView;
import com.vscode4teaching.vscode4teachingserver.model.Course;
import com.vscode4teaching.vscode4teachingserver.model.Exercise;
import com.vscode4teaching.vscode4teachingserver.model.validators.ValidationGroupInterfaces.OnCreate;
import com.vscode4teaching.vscode4teachingserver.model.views.CourseViews;
import com.vscode4teaching.vscode4teachingserver.services.CourseService;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.CourseNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Validated
public class CourseController {

    private final CourseService courseService;
    private final Logger logger = LoggerFactory.getLogger(CourseController.class);

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    @JsonView(CourseViews.GeneralView.class)
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        logger.info("Courses gotten: {}", courses);
        return !courses.isEmpty() ? ResponseEntity.ok(courses) : ResponseEntity.noContent().build();
    }

    @PostMapping("/courses")
    @JsonView(CourseViews.GeneralView.class)
    @Validated(OnCreate.class)
    public ResponseEntity<Course> addCourse(@Valid @RequestBody Course course) {
        Course savedCourse = courseService.registerNewCourse(course);
        logger.info("Course saved: {}", savedCourse);
        return new ResponseEntity<Course>(savedCourse, HttpStatus.CREATED);
    }

    @PostMapping("/courses/{courseId}/exercises")
    @JsonView(CourseViews.ExercisesView.class)
    @Validated(OnCreate.class)
    public ResponseEntity<Course> addExercise(@PathVariable @Min(1) Long courseId,
            @Valid @RequestBody Exercise exercise) throws CourseNotFoundException {
        Course savedCourse = courseService.addExerciseToCourse(courseId, exercise);
        // Fetching exercises of course (Lazy initialization)
        List<Exercise> exercises = savedCourse.getExercises();
        logger.info("Exercises of course: {}", exercises);
        return new ResponseEntity<Course>(savedCourse, HttpStatus.CREATED);
    }

}