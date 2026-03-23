//package com.example.eduview.Repository;
//
//import static org.junit.Assert.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//import com.example.eduview.data.model.Parent;
//import com.example.eduview.data.model.Student;
//import com.example.eduview.data.model.Teacher;
//import com.example.eduview.data.model.User;
//import com.example.eduview.data.repository.UserRepository;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseReference;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Arrays;
//import java.util.concurrent.atomic.AtomicReference;
//
//public class UserRepositoryTest {
//
//    private DatabaseReference usersRef;
//    private DatabaseReference studentsRef;
//    private DatabaseReference teachersRef;
//    private DatabaseReference parentsRef;
//
//    private UserRepository repository;
//
//    @Before
//    public void setUp() {
//        usersRef = mock(DatabaseReference.class);
//        studentsRef = mock(DatabaseReference.class);
//        teachersRef = mock(DatabaseReference.class);
//        parentsRef = mock(DatabaseReference.class);
//
//        repository = new UserRepository(usersRef, parentsRef, studentsRef, teachersRef);
//    }
//
//    @Test
//    public void getUser_ById_leo_validStudent_returnsStudent() {
//        String userId = "student123";
//
//        DatabaseReference userNodeRef = mock(DatabaseReference.class);
//        when(usersRef.child(userId)).thenReturn(userNodeRef);
//
//        Task<DataSnapshot> userTask = mockTaskSuccess();
//        when(userNodeRef.get()).thenReturn(userTask);
//
//        DataSnapshot userSnapshot = mock(DataSnapshot.class);
//        when(userTask.getResult()).thenReturn(userSnapshot);
//        when(userSnapshot.exists()).thenReturn(true);
//
//        mockStringChild(userSnapshot, "first_name", "Sam");
//        mockStringChild(userSnapshot, "last_name", "Smith");
//        mockStringChild(userSnapshot, "role", "STUDENT");
//        mockStringChild(userSnapshot, "pfp", "pfp-url");
//
//        DatabaseReference studentNodeRef = mock(DatabaseReference.class);
//        when(studentsRef.child(userId)).thenReturn(studentNodeRef);
//
//        Task<DataSnapshot> studentTask = mockTaskSuccess();
//        when(studentNodeRef.get()).thenReturn(studentTask);
//
//        DataSnapshot studentSnapshot = mock(DataSnapshot.class);
//        when(studentTask.getResult()).thenReturn(studentSnapshot);
//        mockStringChild(studentSnapshot, "classroom", "classA");
//
//        AtomicReference<User> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchUser_leo(userId, result::set, error::set);
//
//        assertNull(error.get());
//        assertNotNull(result.get());
//        assertTrue(result.get() instanceof Student);
//
//        Student student = (Student) result.get();
//        assertEquals("student123", student.getUserId());
//        assertEquals("Sam", student.getFirstName());
//        assertEquals("Smith", student.getLastName());
//        assertEquals("classA", student.getClassId());
//        assertEquals("pfp-url", student.getProfileImageURL());
//    }
//
//    @Test
//    public void getUser_ById_leo_validTeacher_returnsTeacher() {
//        String userId = "teacher123";
//
//        DatabaseReference userNodeRef = mock(DatabaseReference.class);
//        when(usersRef.child(userId)).thenReturn(userNodeRef);
//
//        Task<DataSnapshot> userTask = mockTaskSuccess();
//        when(userNodeRef.get()).thenReturn(userTask);
//
//        DataSnapshot userSnapshot = mock(DataSnapshot.class);
//        when(userTask.getResult()).thenReturn(userSnapshot);
//        when(userSnapshot.exists()).thenReturn(true);
//
//        mockStringChild(userSnapshot, "first_name", "Tina");
//        mockStringChild(userSnapshot, "last_name", "Jones");
//        mockStringChild(userSnapshot, "role", "TEACHER");
//        mockStringChild(userSnapshot, "pfp", null);
//
//        DatabaseReference teacherNodeRef = mock(DatabaseReference.class);
//        when(teachersRef.child(userId)).thenReturn(teacherNodeRef);
//
//        Task<DataSnapshot> teacherTask = mockTaskSuccess();
//        when(teacherNodeRef.get()).thenReturn(teacherTask);
//
//        DataSnapshot teacherSnapshot = mock(DataSnapshot.class);
//        when(teacherTask.getResult()).thenReturn(teacherSnapshot);
//        mockStringChild(teacherSnapshot, "classroom", "classB");
//        mockStringChild(teacherSnapshot, "email", "classB@emial.com");
//
//        AtomicReference<User> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchUser_leo(userId, result::set, error::set);
//
//        assertNull(error.get());
//        assertNotNull(result.get());
//        assertTrue(result.get() instanceof Teacher);
//
//        Teacher teacher = (Teacher) result.get();
//        assertEquals("teacher123", teacher.getUserId());
//        assertEquals("Tina", teacher.getFirstName());
//        assertEquals("Jones", teacher.getLastName());
//        assertEquals("classB@emial.com", teacher.getEmail());
//        assertEquals("classB", teacher.getClassId());
//    }
//
//    @Test
//    public void getUser_ById_leo_validParent_returnsParent() {
//        String userId = "parent123";
//
//        DatabaseReference userNodeRef = mock(DatabaseReference.class);
//        when(usersRef.child(userId)).thenReturn(userNodeRef);
//
//        Task<DataSnapshot> userTask = mockTaskSuccess();
//        when(userNodeRef.get()).thenReturn(userTask);
//
//        DataSnapshot userSnapshot = mock(DataSnapshot.class);
//        when(userTask.getResult()).thenReturn(userSnapshot);
//        when(userSnapshot.exists()).thenReturn(true);
//
//        mockStringChild(userSnapshot, "first_name", "Jane");
//        mockStringChild(userSnapshot, "last_name", "Brown");
//        mockStringChild(userSnapshot, "role", "PARENT");
//        mockStringChild(userSnapshot, "pfp", "parent-pfp");
//
//        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
//        when(parentsRef.child(userId)).thenReturn(parentNodeRef);
//
//        Task<DataSnapshot> parentTask = mockTaskSuccess();
//        when(parentNodeRef.get()).thenReturn(parentTask);
//
//        DataSnapshot parentSnapshot = mock(DataSnapshot.class);
//        when(parentTask.getResult()).thenReturn(parentSnapshot);
//
//
//        mockStringChild(parentSnapshot, "email", "parent@email.com");
//
//        DataSnapshot childrenSnapshot = mock(DataSnapshot.class);
//        when(parentSnapshot.child("children")).thenReturn(childrenSnapshot);
//
//        DataSnapshot child1 = mock(DataSnapshot.class);
//        DataSnapshot child2 = mock(DataSnapshot.class);
//        when(child1.getKey()).thenReturn("childA");
//        when(child2.getKey()).thenReturn("childB");
//        when(childrenSnapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));
//
//        AtomicReference<User> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        assertNotNull(parentsRef.child(userId));
//        repository.fetchUser_leo(userId, result::set, error::set);
//
//        assertNull(error.get());
//        assertNotNull(result.get());
//        assertTrue(result.get() instanceof Parent);
//
//        Parent parent = (Parent) result.get();
//        assertEquals("parent123", parent.getUserId());
//        assertEquals("Jane", parent.getFirstName());
//        assertEquals("Brown", parent.getLastName());
//        assertEquals(2, parent.getChildrenIDs().size());
//        assertTrue(parent.getChildrenIDs().contains("childA"));
//        assertTrue(parent.getChildrenIDs().contains("childB"));
//        assertEquals("parent-pfp", parent.getProfileImageURL());
//    }
//
//    @Test
//    public void fetchUser_topLevelGetFails_callsOnErrorLeoById() {
//        String userId = "brokenUser";
//
//        DatabaseReference userNodeRef = mock(DatabaseReference.class);
//        when(usersRef.child(userId)).thenReturn(userNodeRef);
//
//        Task<DataSnapshot> failedTask = mockTaskFailure();
//        when(userNodeRef.get()).thenReturn(failedTask);
//
//        AtomicReference<User> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchUser_leo(userId, result::set, error::set);
//
//        assertNull(result.get());
//        assertNotNull(error.get());
//        assertEquals("Failed to fetch user", error.get().getMessage());
//    }
//
//    @Test
//    public void getUser_userByIdLeoDoesNotExist_callsOnError() {
//        String userId = "missingUser";
//
//        DatabaseReference userNodeRef = mock(DatabaseReference.class);
//        when(usersRef.child(userId)).thenReturn(userNodeRef);
//
//        Task<DataSnapshot> userTask = mockTaskSuccess();
//        when(userNodeRef.get()).thenReturn(userTask);
//
//        DataSnapshot userSnapshot = mock(DataSnapshot.class);
//        when(userTask.getResult()).thenReturn(userSnapshot);
//        when(userSnapshot.exists()).thenReturn(false);
//
//        AtomicReference<User> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchUser_leo(userId, result::set, error::set);
//
//        assertNull(result.get());
//        assertNotNull(error.get());
//        assertEquals("No user with that ID", error.get().getMessage());
//    }
//
//    @Test
//    public void updateProfilePicture_writesToCorrectFirebasePath() {
//        String userId = "user123";
//        String imageUrl = "https://example.com/pfp.png";
//
//        DatabaseReference userNodeRef = mock(DatabaseReference.class);
//        DatabaseReference pfpRef = mock(DatabaseReference.class);
//
//        when(usersRef.child(userId)).thenReturn(userNodeRef);
//        when(userNodeRef.child("pfp")).thenReturn(pfpRef);
//
//        repository.updateProfilePicture(userId, imageUrl);
//
//        verify(usersRef).child(userId);
//        verify(userNodeRef).child("pfp");
//        verify(pfpRef).setValue(imageUrl);
//    }
//
//    @Test
//    public void updateClass_writesToCorrectFirebasePath() {
//        String userId = "student123";
//        String classId = "classA";
//
//        DatabaseReference studentNodeRef = mock(DatabaseReference.class);
//        DatabaseReference classroomRef = mock(DatabaseReference.class);
//
//        when(studentsRef.child(userId)).thenReturn(studentNodeRef);
//        when(studentNodeRef.child("classroom")).thenReturn(classroomRef);
//
//        repository.updateClass(userId, classId);
//
//        verify(studentsRef).child(userId);
//        verify(studentNodeRef).child("classroom");
//        verify(classroomRef).setValue(classId);
//    }
//
//    @Test
//    public void fetchChildrenOfParent_validParent_returnsChildrenIds() {
//        String parentId = "parent123";
//
//        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
//        when(parentsRef.child(parentId)).thenReturn(parentNodeRef);
//
//        Task<DataSnapshot> parentTask = mockTaskSuccess();
//        when(parentNodeRef.get()).thenReturn(parentTask);
//
//        DataSnapshot parentSnapshot = mock(DataSnapshot.class);
//        when(parentTask.getResult()).thenReturn(parentSnapshot);
//        when(parentSnapshot.exists()).thenReturn(true);
//
//        DataSnapshot childrenSnapshot = mock(DataSnapshot.class);
//        when(parentSnapshot.child("children")).thenReturn(childrenSnapshot);
//
//        DataSnapshot child1 = mock(DataSnapshot.class);
//        DataSnapshot child2 = mock(DataSnapshot.class);
//        when(child1.getKey()).thenReturn("childA");
//        when(child2.getKey()).thenReturn("childB");
//        when(childrenSnapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));
//
//        AtomicReference<java.util.List<String>> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchChildrenOfParent(parentId, result::set, error::set);
//
//        assertNull(error.get());
//        assertNotNull(result.get());
//        assertEquals(2, result.get().size());
//        assertTrue(result.get().contains("childA"));
//        assertTrue(result.get().contains("childB"));
//    }
//
//    @Test
//    public void fetchChildrenOfParent_fetchFails_callsOnError() {
//        String parentId = "parent123";
//
//        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
//        when(parentsRef.child(parentId)).thenReturn(parentNodeRef);
//
//        Task<DataSnapshot> failedTask = mockTaskFailure();
//        when(parentNodeRef.get()).thenReturn(failedTask);
//
//        AtomicReference<java.util.List<String>> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchChildrenOfParent(parentId, result::set, error::set);
//
//        assertNull(result.get());
//        assertNotNull(error.get());
//        assertEquals("Failed to fetch parent data", error.get().getMessage());
//    }
//
//    @Test
//    public void fetchChildrenOfParent_parentDoesNotExist_callsOnError() {
//        String parentId = "missingParent";
//
//        DatabaseReference parentNodeRef = mock(DatabaseReference.class);
//        when(parentsRef.child(parentId)).thenReturn(parentNodeRef);
//
//        Task<DataSnapshot> parentTask = mockTaskSuccess();
//        when(parentNodeRef.get()).thenReturn(parentTask);
//
//        DataSnapshot parentSnapshot = mock(DataSnapshot.class);
//        when(parentTask.getResult()).thenReturn(parentSnapshot);
//        when(parentSnapshot.exists()).thenReturn(false);
//
//        AtomicReference<java.util.List<String>> result = new AtomicReference<>();
//        AtomicReference<Exception> error = new AtomicReference<>();
//
//        repository.fetchChildrenOfParent(parentId, result::set, error::set);
//
//        assertNull(result.get());
//        assertNotNull(error.get());
//        assertEquals("No user with that ID", error.get().getMessage());
//    }
//
//    private Task<DataSnapshot> mockTaskSuccess() {
//        @SuppressWarnings("unchecked")
//        Task<DataSnapshot> task = mock(Task.class);
//
//        when(task.isSuccessful()).thenReturn(true);
//
//        when(task.addOnCompleteListener(any())).thenAnswer(invocation -> {
//            OnCompleteListener<DataSnapshot> listener = invocation.getArgument(0);
//            listener.onComplete(task);
//            return task;
//        });
//
//        return task;
//    }
//
//    private Task<DataSnapshot> mockTaskFailure() {
//        @SuppressWarnings("unchecked")
//        Task<DataSnapshot> task = mock(Task.class);
//
//        when(task.isSuccessful()).thenReturn(false);
//
//        when(task.addOnCompleteListener(any())).thenAnswer(invocation -> {
//            OnCompleteListener<DataSnapshot> listener = invocation.getArgument(0);
//            listener.onComplete(task);
//            return task;
//        });
//
//        return task;
//    }
//
//    private void mockStringChild(DataSnapshot parent, String childName, String value) {
//        DataSnapshot childSnapshot = mock(DataSnapshot.class);
//        when(parent.child(childName)).thenReturn(childSnapshot);
//        when(childSnapshot.getValue(String.class)).thenReturn(value);
//    }
//}