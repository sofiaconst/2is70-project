package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.ProfilePicture;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UserRepositoryTest {

    private DatabaseReference usersRef;
    private DatabaseReference studentsRef;
    private DatabaseReference teachersRef;
    private DatabaseReference parentsRef;

    private UserRepository repository;

    @Before
    public void setUp() {
        usersRef = mock(DatabaseReference.class);
        studentsRef = mock(DatabaseReference.class);
        teachersRef = mock(DatabaseReference.class);
        parentsRef = mock(DatabaseReference.class);

        repository = new UserRepository(usersRef, parentsRef, studentsRef, teachersRef);
    }

    @Test
    public void getUserById_validStudent_returnsStudent() {
        String userId = "student123";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        when(usersRef.child(userId)).thenReturn(userNodeRef);

        Task<DataSnapshot> userTask = mockTaskSuccess();
        when(userNodeRef.get()).thenReturn(userTask);

        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        when(userTask.getResult()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);

        mockBaseUser(userSnapshot, "Sam", "Smith", "STUDENT", "pfp-url", "sam@test.com", "hello");

        DatabaseReference studentNodeRef = mock(DatabaseReference.class);
        when(studentsRef.child(userId)).thenReturn(studentNodeRef);

        Task<DataSnapshot> studentTask = mockTaskSuccess();
        when(studentNodeRef.get()).thenReturn(studentTask);

        DataSnapshot studentSnapshot = mock(DataSnapshot.class);
        when(studentTask.getResult()).thenReturn(studentSnapshot);
        when(studentSnapshot.exists()).thenReturn(true);
        mockStringChild(studentSnapshot, "classroom", "classA");

        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                result.set(user);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(error.get());
        assertNotNull(result.get());
        assertTrue(result.get() instanceof Student);

        Student student = (Student) result.get();
        assertEquals("student123", student.getUserId());
        assertEquals("Sam", student.getFirstName());
        assertEquals("Smith", student.getLastName());
        assertEquals("sam@test.com", student.getEmail());
        assertEquals("classA", student.getClassId());
    }

    @Test
    public void getUserById_validTeacher_returnsTeacher() {
        String userId = "teacher123";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        when(usersRef.child(userId)).thenReturn(userNodeRef);

        Task<DataSnapshot> userTask = mockTaskSuccess();
        when(userNodeRef.get()).thenReturn(userTask);

        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        when(userTask.getResult()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);

        mockBaseUser(userSnapshot, "Tina", "Jones", "TEACHER", "teacher-pfp", "tina@test.com", null);

        DatabaseReference teacherNodeRef = mock(DatabaseReference.class);
        when(teachersRef.child(userId)).thenReturn(teacherNodeRef);

        Task<DataSnapshot> teacherTask = mockTaskSuccess();
        when(teacherNodeRef.get()).thenReturn(teacherTask);

        DataSnapshot teacherSnapshot = mock(DataSnapshot.class);
        when(teacherTask.getResult()).thenReturn(teacherSnapshot);
        when(teacherSnapshot.exists()).thenReturn(true);
        mockStringChild(teacherSnapshot, "classroom", "classB");

        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                result.set(user);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(error.get());
        assertNotNull(result.get());
        assertTrue(result.get() instanceof Teacher);

        Teacher teacher = (Teacher) result.get();
        assertEquals("teacher123", teacher.getUserId());
        assertEquals("Tina", teacher.getFirstName());
        assertEquals("Jones", teacher.getLastName());
        assertEquals("tina@test.com", teacher.getEmail());
        assertEquals("classB", teacher.getClassId());
    }

    @Test
    public void getUserById_validParent_returnsParent() {
        String userId = "parent123";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        when(usersRef.child(userId)).thenReturn(userNodeRef);

        Task<DataSnapshot> userTask = mockTaskSuccess();
        when(userNodeRef.get()).thenReturn(userTask);

        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        when(userTask.getResult()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);

        mockBaseUser(userSnapshot, "Jane", "Brown", "PARENT", "parent-pfp", "parent@test.com", null);

        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
        when(parentsRef.child(userId)).thenReturn(parentNodeRef);

        Task<DataSnapshot> parentTask = mockTaskSuccess();
        when(parentNodeRef.get()).thenReturn(parentTask);

        DataSnapshot parentSnapshot = mock(DataSnapshot.class);
        when(parentTask.getResult()).thenReturn(parentSnapshot);
        when(parentSnapshot.exists()).thenReturn(true);

        DataSnapshot childrenSnapshot = mock(DataSnapshot.class);
        when(parentSnapshot.child("children")).thenReturn(childrenSnapshot);
        when(childrenSnapshot.exists()).thenReturn(true);

        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);
        when(child1.getValue(String.class)).thenReturn("childA");
        when(child2.getValue(String.class)).thenReturn("childB");
        when(childrenSnapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));

        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                result.set(user);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(error.get());
        assertNotNull(result.get());
        assertTrue(result.get() instanceof Parent);

        Parent parent = (Parent) result.get();
        assertEquals("parent123", parent.getUserId());
        assertEquals(2, parent.getChildrenIDs().size());
        assertTrue(parent.getChildrenIDs().contains("childA"));
        assertTrue(parent.getChildrenIDs().contains("childB"));
    }

    @Test
    public void getUserById_topLevelGetFails_callsOnError() {
        String userId = "brokenUser";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        when(usersRef.child(userId)).thenReturn(userNodeRef);

        Task<DataSnapshot> failedTask = mockTaskFailure();
        when(userNodeRef.get()).thenReturn(failedTask);

        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                result.set(user);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Failed to fetch user", error.get().getMessage());
    }

    @Test
    public void getUserById_missingBaseUserInfo_callsOnError() {
        String userId = "badUser";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        when(usersRef.child(userId)).thenReturn(userNodeRef);

        Task<DataSnapshot> userTask = mockTaskSuccess();
        when(userNodeRef.get()).thenReturn(userTask);

        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        when(userTask.getResult()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);

        // Important: mock every field parseBaseUser touches.
        // Only first_name is missing on purpose.
        mockStringChild(userSnapshot, "first_name", null);
        mockStringChild(userSnapshot, "last_name", "Smith");
        mockStringChild(userSnapshot, "role", "STUDENT");
        mockStringChild(userSnapshot, "pfp", "pfp-url");
        mockStringChild(userSnapshot, "email", "sam@test.com");
        mockStringChild(userSnapshot, "bio", "hello");

        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                result.set(user);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("User information missing", error.get().getMessage());
    }

    @Test
    public void getUserById_invalidRole_callsOnError() {
        String userId = "badRoleUser";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        when(usersRef.child(userId)).thenReturn(userNodeRef);

        Task<DataSnapshot> userTask = mockTaskSuccess();
        when(userNodeRef.get()).thenReturn(userTask);

        DataSnapshot userSnapshot = mock(DataSnapshot.class);
        when(userTask.getResult()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);

        // Same fix: mock all expected fields, only role is invalid.
        mockStringChild(userSnapshot, "first_name", "Sam");
        mockStringChild(userSnapshot, "last_name", "Smith");
        mockStringChild(userSnapshot, "role", "DINOSAUR");
        mockStringChild(userSnapshot, "pfp", "pfp-url");
        mockStringChild(userSnapshot, "email", "sam@test.com");
        mockStringChild(userSnapshot, "bio", "hello");

        AtomicReference<User> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                result.set(user);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(result.get());
        assertNotNull(error.get());
        assertTrue(error.get().getMessage().contains("Invalid user role"));
    }

    @Test
    public void updateProfilePicture_writesToCorrectFirebasePath() {
        String userId = "user123";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        DatabaseReference pfpRef = mock(DatabaseReference.class);

        when(usersRef.child(userId)).thenReturn(userNodeRef);
        when(userNodeRef.child("pfp")).thenReturn(pfpRef);

        repository.updateProfilePicture(userId, ProfilePicture.DEFAULT);

        verify(usersRef).child(userId);
        verify(userNodeRef).child("pfp");
        verify(pfpRef).setValue(ProfilePicture.DEFAULT.name());
    }

    @Test
    public void updateClass_writesToCorrectFirebasePath() {
        String userId = "student123";
        String classId = "classA";

        DatabaseReference studentNodeRef = mock(DatabaseReference.class);
        DatabaseReference classroomRef = mock(DatabaseReference.class);

        when(studentsRef.child(userId)).thenReturn(studentNodeRef);
        when(studentNodeRef.child("classroom")).thenReturn(classroomRef);

        repository.updateClass(userId, classId);

        verify(studentsRef).child(userId);
        verify(studentNodeRef).child("classroom");
        verify(classroomRef).setValue(classId);
    }

    @Test
    public void updateBio_success_callsOnSuccess() {
        String userId = "user1";
        String bio = "New bio";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        DatabaseReference bioRef = mock(DatabaseReference.class);

        when(usersRef.child(userId)).thenReturn(userNodeRef);
        when(userNodeRef.child("bio")).thenReturn(bioRef);

        @SuppressWarnings("unchecked")
        Task<Void> task = mock(Task.class);
        when(bioRef.setValue(bio)).thenReturn(task);

        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return task;
        });
        when(task.addOnFailureListener(any())).thenReturn(task);

        repository.updateBio(userId, bio, () -> successCalled.set(true), error::set);

        assertTrue(successCalled.get());
        assertNull(error.get());
    }

    @Test
    public void updateBio_failure_callsOnError() {
        String userId = "user1";
        String bio = "New bio";

        DatabaseReference userNodeRef = mock(DatabaseReference.class);
        DatabaseReference bioRef = mock(DatabaseReference.class);

        when(usersRef.child(userId)).thenReturn(userNodeRef);
        when(userNodeRef.child("bio")).thenReturn(bioRef);

        @SuppressWarnings("unchecked")
        Task<Void> task = mock(Task.class);
        when(bioRef.setValue(bio)).thenReturn(task);

        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        when(task.addOnSuccessListener(any())).thenReturn(task);
        when(task.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new RuntimeException("Bio failed"));
            return task;
        });

        repository.updateBio(userId, bio, () -> successCalled.set(true), error::set);

        assertEquals(false, successCalled.get());
        assertNotNull(error.get());
        assertEquals("Bio failed", error.get().getMessage());
    }

    @Test
    public void fetchChildrenOfParent_validParent_returnsChildrenIds() {
        String parentId = "parent123";

        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
        when(parentsRef.child(parentId)).thenReturn(parentNodeRef);

        Task<DataSnapshot> parentTask = mockTaskSuccess();
        when(parentNodeRef.get()).thenReturn(parentTask);

        DataSnapshot parentSnapshot = mock(DataSnapshot.class);
        when(parentTask.getResult()).thenReturn(parentSnapshot);
        when(parentSnapshot.exists()).thenReturn(true);

        DataSnapshot childrenSnapshot = mock(DataSnapshot.class);
        when(parentSnapshot.child("children")).thenReturn(childrenSnapshot);
        when(childrenSnapshot.exists()).thenReturn(true);

        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);
        when(child1.getValue(String.class)).thenReturn("childA");
        when(child2.getValue(String.class)).thenReturn("childB");
        when(childrenSnapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));

        AtomicReference<List<String>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.fetchChildrenOfParent(parentId, result::set, error::set);

        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(2, result.get().size());
        assertTrue(result.get().contains("childA"));
        assertTrue(result.get().contains("childB"));
    }

    @Test
    public void fetchChildrenOfParent_fetchFails_callsOnError() {
        String parentId = "parent123";

        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
        when(parentsRef.child(parentId)).thenReturn(parentNodeRef);

        Task<DataSnapshot> failedTask = mockTaskFailure();
        when(parentNodeRef.get()).thenReturn(failedTask);

        AtomicReference<List<String>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.fetchChildrenOfParent(parentId, result::set, error::set);

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Failed to fetch parent data", error.get().getMessage());
    }

    @Test
    public void getStudentsByIds_nullList_returnsEmptyList() {
        AtomicReference<List<Student>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getStudentsByIds(null, result::set, error::set);

        assertNotNull(result.get());
        assertTrue(result.get().isEmpty());
        assertNull(error.get());
    }

    @Test
    public void getStudentsByIds_allStudents_returnsStudentList() {
        UserRepository spyRepo = Mockito.spy(repository);

        Student student1 = new Student("s1", "Sam", "One", "s1@test.com", "class1");
        Student student2 = new Student("s2", "Sue", "Two", "s2@test.com", "class1");

        Mockito.doAnswer(invocation -> {
            String id = invocation.getArgument(0);
            UserRepository.UserCallback callback = invocation.getArgument(1);

            if ("s1".equals(id)) {
                callback.onSuccess(student1);
            } else {
                callback.onSuccess(student2);
            }
            return null;
        }).when(spyRepo).getUserById(Mockito.anyString(), Mockito.<UserRepository.UserCallback>any());

        AtomicReference<List<Student>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        spyRepo.getStudentsByIds(Arrays.asList("s1", "s2"), result::set, error::set);

        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(2, result.get().size());
    }

    @Test
    public void getStudentsByIds_firstError_callsOnError() {
        UserRepository spyRepo = Mockito.spy(repository);

        Mockito.doAnswer(invocation -> {
            UserRepository.UserCallback callback = invocation.getArgument(1);
            callback.onError(new RuntimeException("Student lookup failed"));
            return null;
        }).when(spyRepo).getUserById(Mockito.anyString(), Mockito.<UserRepository.UserCallback>any());

        AtomicReference<List<Student>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        spyRepo.getStudentsByIds(Arrays.asList("s1", "s2"), result::set, error::set);

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Student lookup failed", error.get().getMessage());
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

    private void mockBaseUser(DataSnapshot snapshot,
                              String firstName,
                              String lastName,
                              String role,
                              String pfp,
                              String email,
                              String bio) {
        mockStringChild(snapshot, "first_name", firstName);
        mockStringChild(snapshot, "last_name", lastName);
        mockStringChild(snapshot, "role", role);
        mockStringChild(snapshot, "pfp", pfp);
        mockStringChild(snapshot, "email", email);
        mockStringChild(snapshot, "bio", bio);
    }

    private void mockStringChild(DataSnapshot parent, String childName, String value) {
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(parent.child(childName)).thenReturn(childSnapshot);
        when(childSnapshot.getValue(String.class)).thenReturn(value);
    }
}