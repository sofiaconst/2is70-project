package com.example.eduview.ViewModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eduview.R;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.ui.main.MainViewModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class MainViewModelTest {

    private SessionManager sessionManager;
    private MainViewModel viewModel;

    @Before
    public void setUp() {
        sessionManager = Mockito.mock(SessionManager.class);
        viewModel = new MainViewModel(sessionManager);
    }

    @Test
    public void getMenuResForUser_whenUserNotLoaded_throwsException() {
        // Should fail if startSession was never called successfully.
        try {
            viewModel.getMenuResForUser();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("User not loaded yet. Call startSession() first.", e.getMessage());
        }
    }

    @Test
    public void startSession_success_setsCurrentUser_andRunsCallback() {
        // Success path should store the user and run the callback.
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", java.util.Collections.emptyList());

        final boolean[] callbackRan = {false};

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(parent);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(() -> callbackRan[0] = true);

        assertNotNull(viewModel.getCurrentUser());
        assertEquals("p1", viewModel.getCurrentUser().getUserId());
        assertEquals(parent, viewModel.getCurrentUser());
        assertEquals(R.menu.bottom_nav_parent, viewModel.getMenuResForUser());
        assertEquals(true, callbackRan[0]);
    }

    @Test
    public void startSession_success_withNullCallback_setsCurrentUser() {
        // Null callback should be allowed.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-1");

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(teacher);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        assertNotNull(viewModel.getCurrentUser());
        assertEquals("t1", viewModel.getCurrentUser().getUserId());
        assertEquals(R.menu.bottom_nav_main, viewModel.getMenuResForUser());
    }

    @Test
    public void startSession_error_doesNotSetCurrentUser() {
        // Error path should leave currentUser as null.
        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onError(new RuntimeException("Session failed"));
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        final boolean[] callbackRan = {false};

        viewModel.startSession(() -> callbackRan[0] = true);

        assertNull(viewModel.getCurrentUser());
        assertEquals(false, callbackRan[0]);
    }

    @Test
    public void getMenuResForUser_parent_returnsParentMenu() {
        // Parent should get the parent menu.
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", java.util.Collections.emptyList());

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(parent);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        assertEquals(R.menu.bottom_nav_parent, viewModel.getMenuResForUser());
    }

    @Test
    public void getMenuResForUser_teacher_returnsMainMenu() {
        // Teacher should get the main menu.
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-1");

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(teacher);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        assertEquals(R.menu.bottom_nav_main, viewModel.getMenuResForUser());
    }

    @Test
    public void getMenuResForUser_student_returnsMainMenu() {
        // Student should also get the main menu.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(student);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        assertEquals(R.menu.bottom_nav_main, viewModel.getMenuResForUser());
    }

    @Test
    public void logout_clearsCurrentUser_andCallsSessionManager() {
        // Logout should clear cached user and call session logout.
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(student);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);
        assertNotNull(viewModel.getCurrentUser());

        viewModel.logout();

        verify(sessionManager).logoutCurrentUser(null);
        assertNull(viewModel.getCurrentUser());
    }
}