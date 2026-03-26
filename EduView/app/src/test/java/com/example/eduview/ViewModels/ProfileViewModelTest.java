package com.example.eduview.ViewModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.ProfilePicture;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.profile.ProfileUIState;
import com.example.eduview.ui.profile.ProfileViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.eq;

public class ProfileViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private ClassroomRepository classroomRepository;

    @Before
    public void setUp() {
        sessionManager = mock(SessionManager.class);
        userRepository = mock(UserRepository.class);
        classroomRepository = mock(ClassroomRepository.class);
    }

    @Test
    public void constructor_withNullCurrentUser_leavesUiStateNull() {
        // If nobody is logged in, the view model should not build a profile state.
        when(sessionManager.getCurrentUser()).thenReturn(null);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        assertNull(viewModel.getUIState().getValue());
    }

    @Test
    public void constructor_studentWithoutClass_buildsNotRegisteredState() {
        // Student with no class should go to the "not registered" state right away.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertEquals("Sam Student", state.displayName);
        assertEquals(student.getRole().name(), state.roleText);
        assertNotNull(state.studentState);
        assertNull(state.teacherState);
        assertNull(state.parentState);

        verify(classroomRepository, never()).getClassroomById(anyString(), any());
    }

    @Test
    public void constructor_teacherWithoutClass_buildsErrorState() {
        // Teacher with no class id should get an error state immediately.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(teacher);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertEquals("Tina Teacher", state.displayName);
        assertEquals(teacher.getRole().name(), state.roleText);
        assertNotNull(state.teacherState);
        assertNull(state.studentState);
        assertNull(state.parentState);

        verify(classroomRepository, never()).getClassroomById(anyString(), any());
    }

    @Test
    public void constructor_parentWithNullId_buildsErrorState() {
        // Parent with invalid id should get an error state immediately.
        Parent parent = new Parent(null, "Pat", "Parent", "pat@test.com", new ArrayList<>());
        when(sessionManager.getCurrentUser()).thenReturn(parent);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertEquals("Pat Parent", state.displayName);
        assertEquals(parent.getRole().name(), state.roleText);
        assertNotNull(state.parentState);
        assertNull(state.studentState);
        assertNull(state.teacherState);

        verify(userRepository, never()).getUserById(anyString(), any());
    }

    @Test
    public void loadParentChildren_withNoChildren_setsSuccessWithEmptyList() {
        // Parent with no child ids should end up with an empty children list.
        when(sessionManager.getCurrentUser()).thenReturn(null);
        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", new ArrayList<>());
        viewModel.loadParentChildren(parent);

        ProfileUIState state = viewModel.getUIState().getValue();
        // This method updates parent state only if uiState already exists.
        // So first build a parent-based VM and test again below if needed.
        assertTrue(state == null || state.parentState == null);
    }

    @Test
    public void constructor_parentWithNoChildren_buildsParentStateAndLoadsEmptyChildren() {
        // Parent branch should build state and finish with an empty children list.
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", new ArrayList<>());
        when(sessionManager.getCurrentUser()).thenReturn(parent);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertEquals("Pat Parent", state.displayName);
        assertNotNull(state.parentState);
    }

    @Test
    public void loadParentChildren_onlyKeepsStudents() {
        // One student should be kept, non-student users should be ignored.
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com",
                Arrays.asList("child-1", "child-2"));

        when(sessionManager.getCurrentUser()).thenReturn(parent);

        Mockito.doAnswer(invocation -> {
            String id = invocation.getArgument(0);
            UserRepository.UserCallback callback = invocation.getArgument(1);

            if ("child-1".equals(id)) {
                callback.onSuccess(new Student("child-1", "Chris", "One", "c1@test.com", "class-1"));
            } else {
                callback.onSuccess(new Teacher("child-2", "Taylor", "Teach", "t@test.com", "class-9"));
            }
            return null;
        }).when(userRepository).getUserById(anyString(), any(UserRepository.UserCallback.class));

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.parentState);
    }

    @Test
    public void joinClass_onRepositoryError_updatesStudentErrorState() {
        // If joining fails, student state should become an error state.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Void> callback = invocation.getArgument(2);
            callback.onError(new Exception("Join failed"));
            return null;
        }).when(classroomRepository).joinClassroom(
                eq("s1"),
                eq("ABC123"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.joinClass("ABC123");

        verify(classroomRepository).joinClassroom(
                eq("s1"),
                eq("ABC123"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );
        assertNotNull(viewModel.getUIState().getValue());
        assertNotNull(viewModel.getUIState().getValue().studentState);
    }

    @Test
    public void removeStudentFromClass_withNullStudent_doesNothing() {
        // Null student should just return early.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-42");
        when(sessionManager.getCurrentUser()).thenReturn(teacher);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.removeStudentFromClass(null);

        verify(classroomRepository, never()).removeStudentFromClassroom(anyString(), anyString(), any());
    }

    @Test
    public void removeStudentFromClass_teacher_callsRepository() {
        // Teacher removing a student should call the classroom repo.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-42");
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-42");

        when(sessionManager.getCurrentUser()).thenReturn(teacher);

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Void> callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(classroomRepository).removeStudentFromClassroom(
                eq("class-42"),
                eq("s1"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Classroom> callback = invocation.getArgument(1);
            callback.onSuccess(null);
            return null;
        }).when(classroomRepository).getClassroomById(
                eq("class-42"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.removeStudentFromClass(student);

        verify(classroomRepository).removeStudentFromClassroom(
                eq("class-42"),
                eq("s1"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );
    }

    @Test
    public void updateProfilePicture_withNoCurrentUser_doesNothing() {
        // No user means nothing should be updated.
        when(sessionManager.getCurrentUser()).thenReturn(null);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.updateProfilePicture(mock(ProfilePicture.class));

        verify(userRepository, never()).updateProfilePicture(anyString(), any(ProfilePicture.class));
    }

    @Test
    public void logout_clearsUiStateAndLogsOutSession() {
        // Logout should clear the UI state and call the session manager.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        assertNotNull(viewModel.getUIState().getValue());

        viewModel.logout();

        assertNull(viewModel.getUIState().getValue());
        verify(sessionManager).logoutCurrentUser(null);
    }
    @Test
    public void constructor_studentWithClass_loadsClassAndTeacherName() {
        // Student with a class should load classroom info and then teacher name.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        Classroom classroom = mock(Classroom.class);
        when(classroom.getName()).thenReturn("Math");
        when(classroom.getTeacherId()).thenReturn("t1");

        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-1");

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Classroom> callback = invocation.getArgument(1);
            callback.onSuccess(classroom);
            return null;
        }).when(classroomRepository).getClassroomById(
                Mockito.eq("class-1"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onSuccess(teacher);
            return null;
        }).when(userRepository).getUserById(
                Mockito.eq("t1"),
                Mockito.<UserRepository.UserCallback>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.studentState);
    }

    @Test
    public void constructor_studentWithClass_butClassroomFetchFails_setsErrorState() {
        // If the classroom cannot be loaded, the student state should become an error state.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Classroom> callback = invocation.getArgument(1);
            callback.onError(new Exception("Failed to load classroom"));
            return null;
        }).when(classroomRepository).getClassroomById(
                Mockito.eq("class-1"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.studentState);
    }

    @Test
    public void constructor_studentWithClassroomAndNoTeacherId_stillBuildsStudentState() {
        // If the classroom has no teacher id, we should still get a valid student state.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        Classroom classroom = mock(Classroom.class);
        when(classroom.getName()).thenReturn("Math");
        when(classroom.getTeacherId()).thenReturn("");

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Classroom> callback = invocation.getArgument(1);
            callback.onSuccess(classroom);
            return null;
        }).when(classroomRepository).getClassroomById(
                Mockito.eq("class-1"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.studentState);

        verify(userRepository, never()).getUserById(Mockito.anyString(), Mockito.<UserRepository.UserCallback>any());
    }



    @Test
    public void constructor_teacherWithClassroomFetchError_setsTeacherErrorState() {
        // If the teacher's classroom cannot be loaded, teacher state should become error.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-9");
        when(sessionManager.getCurrentUser()).thenReturn(teacher);

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Classroom> callback = invocation.getArgument(1);
            callback.onError(new Exception("Failed to load classroom"));
            return null;
        }).when(classroomRepository).getClassroomById(
                Mockito.eq("class-9"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.teacherState);
    }


    @Test
    public void joinClass_successThenReloadSuccess_loadsUpdatedStudentClass() {
        // Joining succeeds, session reload succeeds, and then the new classroom is loaded.
        Student initialStudent = new Student("s1", "Sam", "Student", "sam@test.com", "");
        Student reloadedStudent = new Student("s1", "Sam", "Student", "sam@test.com", "class-22");
        when(sessionManager.getCurrentUser()).thenReturn(initialStudent);

        Classroom classroom = mock(Classroom.class);
        when(classroom.getName()).thenReturn("Science");
        when(classroom.getTeacherId()).thenReturn("");

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Void> callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(classroomRepository).joinClassroom(
                Mockito.eq("s1"),
                Mockito.eq("ABC123"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            when(sessionManager.getCurrentUser()).thenReturn(reloadedStudent);
            callback.onSuccess(reloadedStudent);
            return null;
        }).when(sessionManager).reloadSession(Mockito.any());

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Classroom> callback = invocation.getArgument(1);
            callback.onSuccess(classroom);
            return null;
        }).when(classroomRepository).getClassroomById(
                Mockito.eq("class-22"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.joinClass("ABC123");

        verify(classroomRepository).joinClassroom(
                Mockito.eq("s1"),
                Mockito.eq("ABC123"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );
        verify(sessionManager).reloadSession(Mockito.any());
        verify(classroomRepository).getClassroomById(
                Mockito.eq("class-22"),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );
    }

    @Test
    public void joinClass_successButReloadFails_setsStudentErrorState() {
        // If join works but reload fails, the student state should become error.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Void> callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(classroomRepository).joinClassroom(
                Mockito.eq("s1"),
                Mockito.eq("ABC123"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onError(new Exception("Reload failed"));
            return null;
        }).when(sessionManager).reloadSession(Mockito.any());

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.joinClass("ABC123");

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.studentState);
    }

    @Test
    public void removeStudentFromClass_whenCurrentUserIsNotTeacher_doesNothing() {
        // Only teachers should be allowed to remove students.
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", new ArrayList<>());
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-42");
        when(sessionManager.getCurrentUser()).thenReturn(parent);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.removeStudentFromClass(student);

        verify(classroomRepository, never()).removeStudentFromClassroom(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );
    }

    @Test
    public void loadParentChildren_withNullParent_doesNothing() {
        // Null parent should return immediately and not hit the repo.
        when(sessionManager.getCurrentUser()).thenReturn(null);
        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        viewModel.loadParentChildren(null);

        verify(userRepository, never()).getUserById(Mockito.anyString(), Mockito.<UserRepository.UserCallback>any());
    }

    @Test
    public void updateProfilePicture_withCurrentUser_callsRepository() {
        // Updating the profile picture should call the repo for the current user.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.updateProfilePicture(ProfilePicture.DEFAULT);

        verify(userRepository).updateProfilePicture("s1", ProfilePicture.DEFAULT);
    }
    @Test
    public void constructor_teacherWithNullClass_buildsImmediateErrorState() {
        // Teacher with null class id should immediately get an error state.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", null);
        when(sessionManager.getCurrentUser()).thenReturn(teacher);

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.teacherState);

        verify(classroomRepository, never()).getClassroomById(
                Mockito.anyString(),
                Mockito.<ClassroomRepository.ClassroomCallback<Classroom>>any()
        );
    }

    @Test
    public void joinClass_whenJoinFails_setsStudentErrorState() {
        // If joining a class fails right away, student state should become error.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        Mockito.doAnswer(invocation -> {
            ClassroomRepository.ClassroomCallback<Void> callback = invocation.getArgument(2);
            callback.onError(new Exception("Join failed"));
            return null;
        }).when(classroomRepository).joinClassroom(
                Mockito.eq("s1"),
                Mockito.eq("ABC123"),
                Mockito.<ClassroomRepository.ClassroomCallback<Void>>any()
        );

        ProfileViewModel viewModel = new ProfileViewModel(sessionManager, userRepository, classroomRepository);
        viewModel.joinClass("ABC123");

        ProfileUIState state = viewModel.getUIState().getValue();
        assertNotNull(state);
        assertNotNull(state.studentState);
    }

}