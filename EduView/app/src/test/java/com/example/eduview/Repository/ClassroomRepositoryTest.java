package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.repository.ClassroomRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ClassroomRepositoryTest {

    private DatabaseReference rootRef;
    private DatabaseReference classroomsRef;

    private ClassroomRepository repository;

    @Before
    public void setUp() {
        rootRef = mock(DatabaseReference.class);
        classroomsRef = mock(DatabaseReference.class);

        repository = new ClassroomRepository(rootRef, classroomsRef);
    }

    @Test
    public void getClassroomById_validClassroom_returnsClassroom() {
        String classId = "class1";

        DatabaseReference classNodeRef = mock(DatabaseReference.class);
        when(classroomsRef.child(classId)).thenReturn(classNodeRef);

        Task<DataSnapshot> classTask = mockTaskSuccess();
        when(classNodeRef.get()).thenReturn(classTask);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(classTask.getResult()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);

        Classroom classroom = mock(Classroom.class);
        when(snapshot.getValue(Classroom.class)).thenReturn(classroom);

        AtomicReference<Classroom> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom resultValue) {
                result.set(resultValue);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(classroom, result.get());
    }

    @Test
    public void getClassroomById_missingClassroom_callsOnError() {
        String classId = "missingClass";

        DatabaseReference classNodeRef = mock(DatabaseReference.class);
        when(classroomsRef.child(classId)).thenReturn(classNodeRef);

        Task<DataSnapshot> classTask = mockTaskSuccess();
        when(classNodeRef.get()).thenReturn(classTask);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(classTask.getResult()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        AtomicReference<Classroom> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
            @Override
            public void onSuccess(Classroom resultValue) {
                result.set(resultValue);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Classroom not found", error.get().getMessage());
    }

    @Test
    public void joinClassroom_success_callsOnSuccess() {
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);

        when(rootRef.updateChildren(any())).thenReturn(updateTask);
        when(updateTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        });
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);

        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.joinClassroom("student1", "classA", new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled.set(true);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertTrue(successCalled.get());
        assertNull(error.get());
    }

    @Test
    public void joinClassroom_failure_callsOnError() {
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);

        when(rootRef.updateChildren(any())).thenReturn(updateTask);
        when(updateTask.addOnSuccessListener(any())).thenReturn(updateTask);
        when(updateTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new RuntimeException("Join failed"));
            return updateTask;
        });

        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.joinClassroom("student1", "classA", new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled.set(true);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertEquals(false, successCalled.get());
        assertNotNull(error.get());
        assertEquals("Join failed", error.get().getMessage());
    }

    @Test
    public void joinClassroom_writesExpectedPaths() {
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);

        AtomicReference<Map<String, Object>> sentUpdates = new AtomicReference<>();

        when(rootRef.updateChildren(any())).thenAnswer(invocation -> {
            sentUpdates.set(invocation.getArgument(0));
            return updateTask;
        });

        when(updateTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        });
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);

        repository.joinClassroom("student1", "classA", new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }

            @Override
            public void onError(Exception e) { }
        });

        assertNotNull(sentUpdates.get());
        assertEquals(true, sentUpdates.get().get("classrooms/classA/students/student1"));
        assertEquals("classA", sentUpdates.get().get("students/student1/classroom"));
    }

    @Test
    public void getStudentIdsForClassroom_success_returnsIds() {
        String classId = "classX";

        DatabaseReference classNodeRef = mock(DatabaseReference.class);
        DatabaseReference studentsNodeRef = mock(DatabaseReference.class);

        when(classroomsRef.child(classId)).thenReturn(classNodeRef);
        when(classNodeRef.child("students")).thenReturn(studentsNodeRef);

        Task<DataSnapshot> task = mockTaskSuccess();
        when(studentsNodeRef.get()).thenReturn(task);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(task.getResult()).thenReturn(snapshot);

        DataSnapshot child1 = mock(DataSnapshot.class);
        DataSnapshot child2 = mock(DataSnapshot.class);
        when(child1.getKey()).thenReturn("studentA");
        when(child2.getKey()).thenReturn("studentB");
        when(snapshot.getChildren()).thenReturn(Arrays.asList(child1, child2));

        AtomicReference<List<String>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getStudentIdsForClassroom(classId, new ClassroomRepository.ClassroomCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> ids) {
                result.set(ids);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(error.get());
        assertNotNull(result.get());
        assertEquals(2, result.get().size());
        assertTrue(result.get().contains("studentA"));
        assertTrue(result.get().contains("studentB"));
    }

    @Test
    public void getStudentIdsForClassroom_failure_callsOnError() {
        String classId = "classX";

        DatabaseReference classNodeRef = mock(DatabaseReference.class);
        DatabaseReference studentsNodeRef = mock(DatabaseReference.class);

        when(classroomsRef.child(classId)).thenReturn(classNodeRef);
        when(classNodeRef.child("students")).thenReturn(studentsNodeRef);

        Task<DataSnapshot> task = mockTaskFailure();
        when(studentsNodeRef.get()).thenReturn(task);

        AtomicReference<List<String>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.getStudentIdsForClassroom(classId, new ClassroomRepository.ClassroomCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> ids) {
                result.set(ids);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Failed to fetch classroom students", error.get().getMessage());
    }

    @Test
    public void removeStudentFromClassroom_success_callsOnSuccess() {
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);

        when(rootRef.updateChildren(any())).thenReturn(updateTask);
        when(updateTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        });
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);

        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.removeStudentFromClassroom("classA", "student1", new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled.set(true);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertTrue(successCalled.get());
        assertNull(error.get());
    }

    @Test
    public void removeStudentFromClassroom_failure_callsOnError() {
        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);

        when(rootRef.updateChildren(any())).thenReturn(updateTask);
        when(updateTask.addOnSuccessListener(any())).thenReturn(updateTask);
        when(updateTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new RuntimeException("Remove failed"));
            return updateTask;
        });

        AtomicReference<Boolean> successCalled = new AtomicReference<>(false);
        AtomicReference<Exception> error = new AtomicReference<>();

        repository.removeStudentFromClassroom("classA", "student1", new ClassroomRepository.ClassroomCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                successCalled.set(true);
            }

            @Override
            public void onError(Exception e) {
                error.set(e);
            }
        });

        assertEquals(false, successCalled.get());
        assertNotNull(error.get());
        assertEquals("Remove failed", error.get().getMessage());
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
}