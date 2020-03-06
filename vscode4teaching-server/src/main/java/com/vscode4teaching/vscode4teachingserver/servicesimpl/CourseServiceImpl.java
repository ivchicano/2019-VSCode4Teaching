package com.vscode4teaching.vscode4teachingserver.servicesimpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.Min;

import com.vscode4teaching.vscode4teachingserver.model.Course;
import com.vscode4teaching.vscode4teachingserver.model.Exercise;
import com.vscode4teaching.vscode4teachingserver.model.User;
import com.vscode4teaching.vscode4teachingserver.model.repositories.CourseRepository;
import com.vscode4teaching.vscode4teachingserver.model.repositories.ExerciseRepository;
import com.vscode4teaching.vscode4teachingserver.model.repositories.UserRepository;
import com.vscode4teaching.vscode4teachingserver.services.CourseService;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.CantRemoveCreatorException;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.CourseNotFoundException;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.ExerciseNotFoundException;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.NotCreatorException;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.NotInCourseException;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.TeacherNotFoundException;
import com.vscode4teaching.vscode4teachingserver.services.exceptions.UserNotFoundException;

import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepo;
    private final ExerciseRepository exerciseRepo;
    private final UserRepository userRepo;

    public CourseServiceImpl(CourseRepository courseRepo, ExerciseRepository exerciseRepo, UserRepository userRepo) {
        this.courseRepo = courseRepo;
        this.exerciseRepo = exerciseRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Course> getAllCourses() {
        return this.courseRepo.findAll();
    }

    @Override
    public Course registerNewCourse(Course course, String requestUsername) throws TeacherNotFoundException {
        Optional<User> teacherOpt = userRepo.findByUsername(requestUsername);
        User teacher = teacherOpt
                .orElseThrow(() -> new TeacherNotFoundException("Teacher not found: " + requestUsername));
        course.addUserInCourse(teacher);
        course.setCreator(teacher);
        return this.courseRepo.save(course);
    }

    @Override
    public User getCreator(@Min(1) Long courseId) throws CourseNotFoundException {
        Course course = courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        return course.getCreator();
    }

    @Override
    public Exercise addExerciseToCourse(Long courseId, Exercise exercise, String requestUsername)
            throws CourseNotFoundException, NotInCourseException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, true);
        exercise.setCourse(course);
        // Fetching exercises of course (Lazy initialization)
        course.getExercises();
        course.addExercise(exercise);
        Exercise savedExercise = exerciseRepo.save(exercise);
        courseRepo.save(course);
        return savedExercise;
    }

    @Override
    public Course editCourse(Long courseId, Course courseData, String requestUsername)
            throws CourseNotFoundException, NotInCourseException {
        Course courseToEdit = this.courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(courseToEdit, requestUsername, true);
        courseToEdit.setName(courseData.getName());
        return courseRepo.save(courseToEdit);
    }

    @Override
    public void deleteCourse(Long courseId, String requestUsername)
            throws CourseNotFoundException, NotInCourseException, NotCreatorException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, true);
        ExceptionUtil.throwIfNotCreator(course, requestUsername);
        this.courseRepo.delete(course);
    }

    @Override
    public List<Exercise> getExercises(Long courseId, String requestUsername)
            throws CourseNotFoundException, NotInCourseException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, false);
        return course.getExercises();
    }

    @Override
    public Course getCourseWithSharingCode(String uuid) throws CourseNotFoundException {
        Course course = this.courseRepo.findByUuid(uuid).orElseThrow(() -> new CourseNotFoundException(uuid));
        return course;
    }

    @Override
    public Exercise editExercise(Long exerciseId, Exercise exerciseData, String requestUsername)
            throws ExerciseNotFoundException, NotInCourseException {
        Exercise exercise = this.exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        ExceptionUtil.throwExceptionIfNotInCourse(exercise.getCourse(), requestUsername, true);
        exercise.setName(exerciseData.getName());
        return exerciseRepo.save(exercise);
    }

    @Override
    public void deleteExercise(Long exerciseId, String requestUsername)
            throws ExerciseNotFoundException, NotInCourseException {
        Exercise exercise = this.exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        ExceptionUtil.throwExceptionIfNotInCourse(exercise.getCourse(), requestUsername, true);
        this.exerciseRepo.delete(exercise);

    }

    @Override
    public List<Course> getUserCourses(Long userId) throws UserNotFoundException {
        User user = this.userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return user.getCourses();
    }

    @Override
    public Course addUsersToCourse(@Min(1) Long courseId, long[] userIds, String requestUsername)
            throws UserNotFoundException, CourseNotFoundException, NotInCourseException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, true);
        for (Long userId : userIds) {
            User user = this.userRepo.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            course.addUserInCourse(user);
        }
        Course savedCourse = this.courseRepo.save(course);
        return savedCourse;
    }

    @Override
    public Set<User> getUsersInCourse(@Min(1) Long courseId, String requestUsername)
            throws CourseNotFoundException, NotInCourseException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, false);
        return course.getUsersInCourse();

    }

    @Override
    public Course removeUsersFromCourse(@Min(1) Long courseId, long[] userIds, String requestUsername)
            throws UserNotFoundException, CourseNotFoundException, NotInCourseException, CantRemoveCreatorException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, true);
        for (Long userId : userIds) {
            User user = this.userRepo.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            if (course.getCreator().equals(user)) {
                throw new CantRemoveCreatorException();
            }
            course.removeUserFromCourse(user);
        }
        Course savedCourse = this.courseRepo.save(course);
        return savedCourse;
    }

    @Override
    public String getCourseCode(Long courseId, String requestUsername)
            throws UserNotFoundException, CourseNotFoundException, NotInCourseException {
        Course course = this.courseRepo.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        ExceptionUtil.throwExceptionIfNotInCourse(course, requestUsername, true);
        return course.getUuid();
    }

    @Override
    public String getExerciseCode(Long exerciseId, String requestUsername)
            throws UserNotFoundException, ExerciseNotFoundException, NotInCourseException {
        Exercise exercise = this.exerciseRepo.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));
        ExceptionUtil.throwExceptionIfNotInCourse(exercise.getCourse(), requestUsername, true);
        return exercise.getUuid();
    }

}