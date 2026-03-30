package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class SessionManagerTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AuthRepository authRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Before
    public void setUp() {
        authRepository = Mockito.mock(AuthRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        sessionManager = new SessionManager(authRepository, userRepository);
    }

    @Test
    public void requireLogin_whenNoUser_throwsException() {
        assertThrows(IllegalStateException.class, () -> sessionManager.requireLogin());
    }

    @Test
    public void getCurrentUser_whenNoUser_returnsNull() {
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    public void setCurrentUserForTest_setsCurrentUserAndLiveData() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");

        sessionManager.setCurrentUserForTest(student);

        assertNotNull(sessionManager.getCurrentUser());
        assertEquals("s1", sessionManager.getCurrentUser().getUserId());
        assertNotNull(sessionManager.getSessionUser().getValue());
        assertEquals("s1", sessionManager.getSessionUser().getValue().getUserId());
    }

    @Test
    public void initializeSession_whenCurrentUserAlreadyExists_returnsItImmediately() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        sessionManager.setCurrentUserForTest(student);

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNotNull(returnedUser[0]);
        assertEquals("s1", returnedUser[0].getUserId());
        assertNull(returnedError[0]);

        verify(authRepository, never()).getCurrentFirebaseUser();
        verify(userRepository, never()).getUserById(any(), any());
    }

    @Test
    public void initializeSession_whenFirebaseUserIsNull_callsError() {
        when(authRepository.getCurrentFirebaseUser()).thenReturn(null);

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNull(returnedUser[0]);
        assertNotNull(returnedError[0]);
        assertEquals("User not logged in", returnedError[0].getMessage());

        verify(userRepository, never()).getUserById(any(), any());
    }

    @Test
    public void initializeSession_whenRepositorySucceeds_setsCurrentUserAndLiveData() {
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");

        when(authRepository.getCurrentFirebaseUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("s1");

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onSuccess(student);
            return null;
        }).when(userRepository).getUserById(Mockito.eq("s1"), any(UserRepository.UserCallback.class));

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNotNull(returnedUser[0]);
        assertEquals("s1", returnedUser[0].getUserId());
        assertNull(returnedError[0]);

        assertNotNull(sessionManager.getCurrentUser());
        assertEquals("s1", sessionManager.getCurrentUser().getUserId());

        assertNotNull(sessionManager.getSessionUser().getValue());
        assertEquals("s1", sessionManager.getSessionUser().getValue().getUserId());
    }

    @Test
    public void initializeSession_whenRepositoryFails_callsError() {
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);

        when(authRepository.getCurrentFirebaseUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("s1");

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onError(new RuntimeException("Failed to load user"));
            return null;
        }).when(userRepository).getUserById(Mockito.eq("s1"), any(UserRepository.UserCallback.class));

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNull(returnedUser[0]);
        assertNotNull(returnedError[0]);
        assertEquals("Failed to load user", returnedError[0].getMessage());
        assertNull(sessionManager.getCurrentUser());
    }

    @Test
    public void logoutCurrentUser_withCallback_logsOutClearsSessionAndReturnsSuccess() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        sessionManager.setCurrentUserForTest(student);

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.logoutCurrentUser(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        verify(authRepository).logout();
        assertNull(returnedUser[0]);
        assertNull(returnedError[0]);
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getSessionUser().getValue());
    }

    @Test
    public void logoutCurrentUser_withoutCallback_stillLogsOutAndClearsSession() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");
        sessionManager.setCurrentUserForTest(student);

        sessionManager.logoutCurrentUser(null);

        verify(authRepository).logout();
        assertNull(sessionManager.getCurrentUser());
        assertNull(sessionManager.getSessionUser().getValue());
    }

    @Test
    public void reloadSession_whenFirebaseUserIsNull_callsError() {
        when(authRepository.getCurrentFirebaseUser()).thenReturn(null);

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.reloadSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNull(returnedUser[0]);
        assertNotNull(returnedError[0]);
        assertEquals("User not logged in", returnedError[0].getMessage());
    }

    @Test
    public void reloadSession_whenRepositorySucceeds_updatesCurrentUserAndLiveData() {
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);
        Student student = new Student("s2", "Sally", "Student", "sally@test.com", "class-2");

        when(authRepository.getCurrentFirebaseUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("s2");

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onSuccess(student);
            return null;
        }).when(userRepository).getUserById(Mockito.eq("s2"), any(UserRepository.UserCallback.class));

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.reloadSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNotNull(returnedUser[0]);
        assertEquals("s2", returnedUser[0].getUserId());
        assertNull(returnedError[0]);
        assertEquals("s2", sessionManager.getCurrentUser().getUserId());
        assertNotNull(sessionManager.getSessionUser().getValue());
        assertEquals("s2", sessionManager.getSessionUser().getValue().getUserId());
    }

    @Test
    public void reloadSession_whenRepositoryFails_callsError() {
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);

        when(authRepository.getCurrentFirebaseUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("s3");

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onError(new RuntimeException("Reload failed"));
            return null;
        }).when(userRepository).getUserById(Mockito.eq("s3"), any(UserRepository.UserCallback.class));

        final User[] returnedUser = new User[1];
        final Exception[] returnedError = new Exception[1];

        sessionManager.reloadSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) {
                returnedUser[0] = user;
            }

            @Override
            public void onError(Exception e) {
                returnedError[0] = e;
            }
        });

        assertNull(returnedUser[0]);
        assertNotNull(returnedError[0]);
        assertEquals("Reload failed", returnedError[0].getMessage());
    }

    @Test
    public void reloadSession_withNullCallback_doesNotCrash() {
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);
        Student student = new Student("s4", "Sue", "Student", "sue@test.com", "class-4");

        when(authRepository.getCurrentFirebaseUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("s4");

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onSuccess(student);
            return null;
        }).when(userRepository).getUserById(Mockito.eq("s4"), any(UserRepository.UserCallback.class));

        sessionManager.reloadSession(null);

        assertNotNull(sessionManager.getCurrentUser());
        assertEquals("s4", sessionManager.getCurrentUser().getUserId());
    }
}