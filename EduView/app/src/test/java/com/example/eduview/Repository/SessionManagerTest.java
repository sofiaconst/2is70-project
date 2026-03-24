package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.AuthRepository;
import com.example.eduview.data.repository.SessionManager;
import com.example.eduview.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SessionManagerTest {

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
        // If nobody is logged in, requireLogin should throw.
        assertThrows(IllegalStateException.class, () -> sessionManager.requireLogin());
    }

    @Test
    public void getCurrentUser_whenNoUser_throwsException() {
        // getCurrentUser also depends on requireLogin, so it should throw too.
        assertThrows(IllegalStateException.class, () -> sessionManager.getCurrentUser());
    }

    @Test
    public void initializeSession_whenCurrentUserAlreadyExists_returnsItImmediately() {
        // First call loads and caches the user.
        // Second call should return the cached user without hitting the repo again.
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

        // First call: fills currentUser cache
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

        // Clear previous callback results
        returnedUser[0] = null;
        returnedError[0] = null;

        // Second call: should use cached currentUser directly
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

        // Repo fetch should only have happened once total
        verify(userRepository).getUserById(Mockito.eq("s1"), any(UserRepository.UserCallback.class));
    }

    @Test
    public void initializeSession_whenFirebaseUserIsNull_callsError() {
        // If Firebase has no logged-in user, session init should fail.
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

        verify(userRepository, never()).getUserById(Mockito.anyString(), any(UserRepository.UserCallback.class));
    }

    @Test
    public void initializeSession_whenRepositorySucceeds_setsCurrentUser() {
        // Normal success path: Firebase user exists and repo returns the app user.
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

        // getCurrentUser should now work because currentUser was set.
        assertEquals("s1", sessionManager.getCurrentUser().getUserId());
    }

    @Test
    public void initializeSession_whenRepositoryFails_callsError() {
        // If loading the user fails, the callback should get that error.
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
    }

    @Test
    public void logoutCurrentUser_withCallback_logsOutAndReturnsSuccess() {
        // Logout should clear the session, call auth logout, and notify callback.
        FirebaseUser firebaseUser = Mockito.mock(FirebaseUser.class);
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-1");

        when(authRepository.getCurrentFirebaseUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("s1");

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onSuccess(student);
            return null;
        }).when(userRepository).getUserById(Mockito.eq("s1"), any(UserRepository.UserCallback.class));

        sessionManager.initializeSession(new SessionManager.SessionCallback() {
            @Override
            public void onSuccess(User user) { }

            @Override
            public void onError(Exception e) { }
        });

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
        assertThrows(IllegalStateException.class, () -> sessionManager.getCurrentUser());
    }

    @Test
    public void logoutCurrentUser_withoutCallback_stillLogsOut() {
        // Null callback should still be allowed.
        sessionManager.logoutCurrentUser(null);

        verify(authRepository).logout();
    }

    @Test
    public void reloadSession_whenFirebaseUserIsNull_callsError() {
        // Reload should fail if Firebase has no logged-in user.
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
    public void reloadSession_whenRepositorySucceeds_updatesCurrentUser() {
        // Reload should refresh the cached user.
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
    }

    @Test
    public void reloadSession_whenRepositoryFails_callsError() {
        // Reload should pass repo errors back to the caller.
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
}