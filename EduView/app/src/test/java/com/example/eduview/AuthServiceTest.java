package com.example.eduview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AuthServiceTest {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;
    private AuthService authService;

    @Before
    public void setUp() {
        firebaseAuth = mock(FirebaseAuth.class);
        rootRef = mock(DatabaseReference.class);

        authService = new AuthService(firebaseAuth, rootRef);
    }

    @Test
    public void signOut_callsFirebaseSignOut() {
        // Basic test: signing out should just call Firebase signOut.
        authService.signOut();

        verify(firebaseAuth).signOut();
    }

    @Test
    public void checkEmailExists_whenMatchExists_returnsTrue() {
        // If Firebase finds a matching user, callback should get true.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("test@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> task = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(task);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(task.getResult()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);

        AtomicReference<Boolean> result = new AtomicReference<>();

        authService.checkEmailExists("test@eduview.com", result::set);

        assertEquals(Boolean.TRUE, result.get());
    }

    @Test
    public void checkEmailExists_whenNoMatch_returnsFalse() {
        // No match in Firebase should return false.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("test@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> task = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(task);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(task.getResult()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        AtomicReference<Boolean> result = new AtomicReference<>();

        authService.checkEmailExists("test@eduview.com", result::set);

        assertEquals(Boolean.FALSE, result.get());
    }

    @Test
    public void checkEmailExists_whenQueryFails_returnsFalse() {
        // Failing the query should fall back to false.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("test@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> task = mockTaskFailure();
        when(emailQuery.get()).thenReturn(task);

        AtomicReference<Boolean> result = new AtomicReference<>();

        authService.checkEmailExists("test@eduview.com", result::set);

        assertEquals(Boolean.FALSE, result.get());
    }

    @Test
    public void signUpUser_whenEmailAlreadyExists_callsFailure() {
        // If username/email is already taken, it should fail before creating auth user.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("taken@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(true);

        AtomicReference<Exception> error = new AtomicReference<>();
        AtomicReference<Boolean> success = new AtomicReference<>(false);

        authService.signUpUser(
                "Sam",
                "Smith",
                "taken@eduview.com",
                "password123",
                "Student",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertEquals(false, success.get());
        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Username already taken"));
    }

    @Test
    public void signUpUser_whenAuthCreateFails_sanitizesError() {
        // If Firebase auth creation fails, the error message should be sanitized.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("new@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Task<AuthResult> authTask = mock(Task.class);
        when(firebaseAuth.createUserWithEmailAndPassword("new@eduview.com", "password123")).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(false);
        when(authTask.getException()).thenReturn(new Exception("Email address is badly formatted"));

        when(authTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(authTask);
            return authTask;
        });

        AtomicReference<Exception> error = new AtomicReference<>();

        authService.signUpUser(
                "Sam",
                "Smith",
                "new@eduview.com",
                "password123",
                "Student",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Username"));
    }

    @Test
    public void signUpUser_whenDbWriteFails_sanitizesError() {
        // Auth user gets created, but saving to DB fails.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);
        DatabaseReference userIdRef = mock(DatabaseReference.class);
        DatabaseReference finalUserRef = mock(DatabaseReference.class);
        FirebaseUser firebaseUser = mock(FirebaseUser.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("new@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Task<AuthResult> authTask = mock(Task.class);
        when(firebaseAuth.createUserWithEmailAndPassword("new@eduview.com", "password123")).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(true);
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("user123");

        when(authTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(authTask);
            return authTask;
        });

        when(usersRef.child("user123")).thenReturn(finalUserRef);

        @SuppressWarnings("unchecked")
        Task<Void> dbTask = mock(Task.class);
        when(finalUserRef.setValue(any())).thenReturn(dbTask);
        when(dbTask.isSuccessful()).thenReturn(false);
        when(dbTask.getException()).thenReturn(new Exception("email address already used"));

        when(dbTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(dbTask);
            return dbTask;
        });

        AtomicReference<Exception> error = new AtomicReference<>();

        authService.signUpUser(
                "Sam",
                "Smith",
                "new@eduview.com",
                "password123",
                "Student",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("username"));
    }

    @Test
    public void signUpUser_success_callsSuccessAndSignsOut() {
        // Happy path: email not taken, auth succeeds, DB write succeeds.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);
        DatabaseReference finalUserRef = mock(DatabaseReference.class);
        FirebaseUser firebaseUser = mock(FirebaseUser.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("new@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Task<AuthResult> authTask = mock(Task.class);
        when(firebaseAuth.createUserWithEmailAndPassword("new@eduview.com", "password123")).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(true);
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("user123");

        when(authTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(authTask);
            return authTask;
        });

        when(usersRef.child("user123")).thenReturn(finalUserRef);

        @SuppressWarnings("unchecked")
        Task<Void> dbTask = mock(Task.class);
        when(finalUserRef.setValue(any())).thenReturn(dbTask);
        when(dbTask.isSuccessful()).thenReturn(true);

        when(dbTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(dbTask);
            return dbTask;
        });

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        authService.signUpUser(
                "Sam",
                "Smith",
                "new@eduview.com",
                "password123",
                "Student",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertTrue(success.get());
        assertNull(error.get());
        verify(firebaseAuth).signOut();
    }

    @Test
    public void childInfo_constructor_setsFields() {
        // Simple data class check.
        AuthService.ChildInfo child = new AuthService.ChildInfo("Amy", "Brown", "amy@eduview.com");

        assertEquals("Amy", child.firstName);
        assertEquals("Brown", child.lastName);
        assertEquals("amy@eduview.com", child.email);
    }

    @Test
    public void nestedUser_constructor_setsFields() {
        // Simple nested user model.
        AuthService.User user = new AuthService.User("Sam", "Smith", "sam@eduview.com", "Student");

        assertEquals("Sam", user.first_name);
        assertEquals("Smith", user.last_name);
        assertEquals("sam@eduview.com", user.email);
        assertEquals("Student", user.role);
    }

    @Test
    public void nestedParent_setChildren_storesMap() {
        // Parent helper model should store the children map.
        AuthService.Parent parent = new AuthService.Parent("Pat", "Brown", "pat@eduview.com", "Parent");
        Map<String, String> children = new java.util.HashMap<>();
        children.put("student_1", "childA");

        parent.setChildren(children);

        assertNotNull(parent.children);
        assertEquals("childA", parent.children.get("student_1"));
    }

    @Test
    public void nestedStudent_setParentId_storesValue() {
        // Student helper model should store parent id.
        AuthService.Student student = new AuthService.Student("Sam", "Smith");
        student.setParentId("parent123");

        assertEquals("parent123", student.parentId);
    }

    @Test
    public void nestedTeacher_constructor_setsClassroom() {
        // Teacher helper model should store classroom id.
        AuthService.Teacher teacher = new AuthService.Teacher("classA");

        assertEquals("classA", teacher.classroom);
    }

    @Test
    public void nestedClassroom_constructor_setsDefaults() {
        // Classroom helper model should start with empty feed and student list.
        AuthService.Classroom classroom = new AuthService.Classroom("Math", "teacher123");

        assertEquals("Math", classroom.name);
        assertEquals("teacher123", classroom.teacher);
        assertNotNull(classroom.students);
        assertNotNull(classroom.feed);
        assertTrue(classroom.students.isEmpty());
        assertTrue(classroom.feed.announcements.isEmpty());
        assertTrue(classroom.feed.pending.isEmpty());
        assertTrue(classroom.feed.published.isEmpty());
    }

    private Task<DataSnapshot> mockTaskSuccess() {
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> task = mock(Task.class);

        when(task.isSuccessful()).thenReturn(true);

        when(task.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<DataSnapshot> listener = invocation.getArgument(0);
            listener.onComplete(task);
            return task;
        });

        return task;
    }

    private Task<DataSnapshot> mockTaskFailure() {
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> task = mock(Task.class);

        when(task.isSuccessful()).thenReturn(false);

        when(task.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<DataSnapshot> listener = invocation.getArgument(0);
            listener.onComplete(task);
            return task;
        });

        return task;
    }
    @Test
    public void signUpTeacher_whenEmailAlreadyExists_callsFailure() {
        // If the teacher username already exists, it should fail before auth creation.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("taken@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(true);

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        authService.signUpTeacher(
                "Tina",
                "Jones",
                "taken@eduview.com",
                "password123",
                "Math",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertEquals(false, success.get());
        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Username already taken"));
    }

    @Test
    public void signUpTeacher_whenAuthFails_sanitizesError() {
        // If Firebase auth fails, the error message should be sanitized.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("newteacher@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Task<AuthResult> authTask = mock(Task.class);
        when(firebaseAuth.createUserWithEmailAndPassword("newteacher@eduview.com", "password123")).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(false);
        when(authTask.getException()).thenReturn(new Exception("Email address is badly formatted"));

        when(authTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(authTask);
            return authTask;
        });

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        authService.signUpTeacher(
                "Tina",
                "Jones",
                "newteacher@eduview.com",
                "password123",
                "Math",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertEquals(false, success.get());
        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Username"));
    }

    @Test
    public void signUpTeacher_success_callsSuccessAndSignsOut() {
        // Happy path for teacher signup:
        // email is free, auth succeeds, user write succeeds, teacher write succeeds, classroom write succeeds.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        DatabaseReference teachersRef = mock(DatabaseReference.class);
        DatabaseReference classroomsRef = mock(DatabaseReference.class);

        Query emailQuery = mock(Query.class);
        DatabaseReference pushedClassRef = mock(DatabaseReference.class);
        DatabaseReference finalUserRef = mock(DatabaseReference.class);
        DatabaseReference finalTeacherRef = mock(DatabaseReference.class);
        DatabaseReference finalClassRef = mock(DatabaseReference.class);
        FirebaseUser firebaseUser = mock(FirebaseUser.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(rootRef.child("teachers")).thenReturn(teachersRef);
        when(rootRef.child("classrooms")).thenReturn(classroomsRef);

        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("newteacher@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Task<AuthResult> authTask = mock(Task.class);
        when(firebaseAuth.createUserWithEmailAndPassword("newteacher@eduview.com", "password123")).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(true);

        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("teacher123");

        when(authTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(authTask);
            return authTask;
        });

        when(classroomsRef.push()).thenReturn(pushedClassRef);
        when(pushedClassRef.getKey()).thenReturn("class123");

        when(usersRef.child("teacher123")).thenReturn(finalUserRef);
        when(teachersRef.child("teacher123")).thenReturn(finalTeacherRef);
        when(classroomsRef.child("class123")).thenReturn(finalClassRef);

        @SuppressWarnings("unchecked")
        Task<Void> userTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<Void> teacherTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<Void> classroomTask = mock(Task.class);

        when(finalUserRef.setValue(any())).thenReturn(userTask);
        when(finalTeacherRef.setValue(any())).thenReturn(teacherTask);
        when(finalClassRef.setValue(any())).thenReturn(classroomTask);

        when(userTask.isSuccessful()).thenReturn(true);
        when(teacherTask.isSuccessful()).thenReturn(true);
        when(classroomTask.isSuccessful()).thenReturn(true);

        when(userTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(userTask);
            return userTask;
        });

        when(teacherTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(teacherTask);
            return teacherTask;
        });

        when(classroomTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<Void> listener = invocation.getArgument(0);
            listener.onComplete(classroomTask);
            return classroomTask;
        });

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        authService.signUpTeacher(
                "Tina",
                "Jones",
                "newteacher@eduview.com",
                "password123",
                "Math",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertTrue(success.get());
        assertNull(error.get());
        verify(firebaseAuth).signOut();
    }

    @Test
    public void addChildToParent_whenChildEmailAlreadyExists_callsFailure() {
        // If the child username already exists, it should fail before sign-in happens.
        FirebaseUser currentUser = mock(FirebaseUser.class);
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(firebaseAuth.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getEmail()).thenReturn("parent@eduview.com");

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("child1@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(true);

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        AuthService.ChildInfo child = new AuthService.ChildInfo("Amy", "Brown", "child1@eduview.com");

        authService.addChildToParent(
                "parent123",
                child,
                "password123",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertEquals(false, success.get());
        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Username already taken"));
    }

    @Test
    public void addChildToParent_whenParentPasswordIncorrect_callsFailure() {
        // Child email is free, but signing the parent in again fails, so it should say incorrect password.
        FirebaseUser currentUser = mock(FirebaseUser.class);
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query emailQuery = mock(Query.class);

        when(firebaseAuth.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getEmail()).thenReturn("parent@eduview.com");

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(emailQuery);
        when(emailQuery.equalTo("child2@eduview.com")).thenReturn(emailQuery);

        Task<DataSnapshot> emailTask = mockTaskSuccess();
        when(emailQuery.get()).thenReturn(emailTask);

        DataSnapshot emailSnapshot = mock(DataSnapshot.class);
        when(emailTask.getResult()).thenReturn(emailSnapshot);
        when(emailSnapshot.exists()).thenReturn(false);

        @SuppressWarnings("unchecked")
        Task<AuthResult> signInTask = mock(Task.class);
        when(firebaseAuth.signInWithEmailAndPassword("parent@eduview.com", "wrongPassword")).thenReturn(signInTask);
        when(signInTask.isSuccessful()).thenReturn(false);

        when(signInTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(signInTask);
            return signInTask;
        });

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        AuthService.ChildInfo child = new AuthService.ChildInfo("Amy", "Brown", "child2@eduview.com");

        authService.addChildToParent(
                "parent123",
                child,
                "wrongPassword",
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertEquals(false, success.get());
        assertNotNull(error.get());
        assertEquals("Incorrect parent password", error.get().getMessage());
    }

    @Test
    public void signUpParent_whenAnyEmailAlreadyExists_callsFailure() {
        // If either the parent email or one of the child emails already exists,
        // signup should fail before creating any auth users.
        DatabaseReference usersRef = mock(DatabaseReference.class);
        Query parentEmailQuery = mock(Query.class);
        Query childEmailQuery = mock(Query.class);

        when(rootRef.child("users")).thenReturn(usersRef);
        when(usersRef.orderByChild("email")).thenReturn(parentEmailQuery, childEmailQuery);

        when(parentEmailQuery.equalTo("parent@eduview.com")).thenReturn(parentEmailQuery);
        when(childEmailQuery.equalTo("child1@eduview.com")).thenReturn(childEmailQuery);

        Task<DataSnapshot> parentEmailTask = mockTaskSuccess();
        Task<DataSnapshot> childEmailTask = mockTaskSuccess();

        when(parentEmailQuery.get()).thenReturn(parentEmailTask);
        when(childEmailQuery.get()).thenReturn(childEmailTask);

        DataSnapshot parentEmailSnapshot = mock(DataSnapshot.class);
        DataSnapshot childEmailSnapshot = mock(DataSnapshot.class);

        when(parentEmailTask.getResult()).thenReturn(parentEmailSnapshot);
        when(childEmailTask.getResult()).thenReturn(childEmailSnapshot);

        when(parentEmailSnapshot.exists()).thenReturn(false);
        when(childEmailSnapshot.exists()).thenReturn(true);

        AtomicReference<Boolean> success = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        AuthService.ChildInfo child = new AuthService.ChildInfo("Amy", "Brown", "child1@eduview.com");

        authService.signUpParent(
                "Pat",
                "Brown",
                "parent@eduview.com",
                "password123",
                java.util.Arrays.asList(child),
                new AuthService.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.set(e);
                    }
                }
        );

        assertEquals(false, success.get());
        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Username already taken"));
    }
}