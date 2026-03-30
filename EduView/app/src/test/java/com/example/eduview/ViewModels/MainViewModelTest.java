package com.example.eduview.ViewModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.eduview.R;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.ui.main.MainViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private SessionManager sessionManager;
    private MainViewModel viewModel;

    @Before
    public void setUp() {
        sessionManager = Mockito.mock(SessionManager.class);
        viewModel = new MainViewModel(sessionManager);
    }

    @Test
    public void getMenuResForUser_whenUserNotLoaded_throwsException() {
        when(sessionManager.getCurrentUser()).thenReturn(null);

        try {
            viewModel.getMenuResForUser();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("User not loaded yet. Call startSession() first.", e.getMessage());
        }
    }

    @Test
    public void startSession_success_runsCallback() {
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", java.util.Collections.emptyList());

        final boolean[] callbackRan = {false};

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(parent);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(() -> callbackRan[0] = true);

        verify(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));
        assertEquals(true, callbackRan[0]);
    }

    @Test
    public void startSession_success_withNullCallback_doesNotCrash() {
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-1");

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(teacher);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        verify(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));
    }

    @Test
    public void startSession_error_doesNotRunCallback() {
        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onError(new RuntimeException("Session failed"));
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        final boolean[] callbackRan = {false};

        viewModel.startSession(() -> callbackRan[0] = true);

        verify(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));
        assertEquals(false, callbackRan[0]);
    }

    @Test
    public void getMenuResForUser_parent_returnsParentMenu() {
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", java.util.Collections.emptyList());
        when(sessionManager.getCurrentUser()).thenReturn(parent);

        assertEquals(R.menu.bottom_nav_parent, viewModel.getMenuResForUser());
    }

    @Test
    public void getMenuResForUser_teacher_returnsMainMenu() {
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-1");
        when(sessionManager.getCurrentUser()).thenReturn(teacher);

        assertEquals(R.menu.bottom_nav_main, viewModel.getMenuResForUser());
    }

    @Test
    public void getMenuResForUser_student_returnsMainMenu() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        assertEquals(R.menu.bottom_nav_main, viewModel.getMenuResForUser());
    }

    @Test
    public void getCurrentUser_returnsSessionManagerUser() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        when(sessionManager.getCurrentUser()).thenReturn(student);

        User result = viewModel.getCurrentUser();

        assertNotNull(result);
        assertEquals("s1", result.getUserId());
        assertEquals(student, result);
    }

    @Test
    public void getCurrentUser_whenSessionManagerReturnsNull_returnsNull() {
        when(sessionManager.getCurrentUser()).thenReturn(null);

        assertNull(viewModel.getCurrentUser());
    }

    @Test
    public void getSessionUser_returnsSessionLiveData() {
        MutableLiveData<User> sessionLiveData = new MutableLiveData<>();
        Student student = new Student("s2", "Sue", "Student", "sue@test.com", "class-2");
        sessionLiveData.setValue(student);

        when(sessionManager.getSessionUser()).thenReturn(sessionLiveData);

        androidx.lifecycle.LiveData<User> result = viewModel.getSessionUser();

        assertNotNull(result);
        assertEquals(student, result.getValue());
        verify(sessionManager).getSessionUser();
    }

    @Test
    public void refreshSession_callsSessionManagerReloadWithNullCallback() {
        viewModel.refreshSession();

        verify(sessionManager).reloadSession(null);
    }

    @Test
    public void logout_callsSessionManagerLogoutWithNullCallback() {
        viewModel.logout();

        verify(sessionManager).logoutCurrentUser(null);
    }

    @Test
    public void startSession_error_thenGetMenuStillThrowsWhenNoUser() {
        when(sessionManager.getCurrentUser()).thenReturn(null);

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onError(new RuntimeException("Session failed"));
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        try {
            viewModel.getMenuResForUser();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("User not loaded yet. Call startSession() first.", e.getMessage());
        }
    }

    @Test
    public void startSession_success_doesNotCallReloadOrLogout() {
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", java.util.Collections.emptyList());

        Mockito.doAnswer(invocation -> {
            SessionManager.SessionCallback callback = invocation.getArgument(0);
            callback.onSuccess(parent);
            return null;
        }).when(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));

        viewModel.startSession(null);

        verify(sessionManager).initializeSession(Mockito.any(SessionManager.SessionCallback.class));
        verify(sessionManager, never()).reloadSession(Mockito.any());
        verify(sessionManager, never()).logoutCurrentUser(Mockito.any());
    }
}