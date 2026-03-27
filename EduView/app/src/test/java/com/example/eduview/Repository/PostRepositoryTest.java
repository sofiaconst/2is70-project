package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.FeedItemType;
import com.example.eduview.data.repository.FeedRepository;
import com.example.eduview.data.repository.PostRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class PostRepositoryTest {

    private DatabaseReference postRef;
    private DatabaseReference classRef;
    private DatabaseReference rootRef;

    private PostRepository postRepository;
    private FeedRepository feedRepository;

    @Before
    public void setUp() {
        postRef = mock(DatabaseReference.class);
        classRef = mock(DatabaseReference.class);
        rootRef = mock(DatabaseReference.class);

        postRepository = new PostRepository(postRef, classRef, rootRef);
        feedRepository = new FeedRepository();
    }

    @Test
    public void createPost_announcement_success_returnsPostId() {
        DatabaseReference pushedRef = mock(DatabaseReference.class);
        when(postRef.push()).thenReturn(pushedRef);
        when(pushedRef.getKey()).thenReturn("post999");

        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        when(rootRef.updateChildren(any())).thenReturn(updateTask);

        when(updateTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        });
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);

        FeedItem post = new FeedItem(FeedItemType.ANNOUNCEMENT, "user1", "Caption text");
        post.setImageUrl("img.png");

        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        postRepository.createPost("classA", post, result::set, error::set);

        assertNull(error.get());
        assertEquals("post999", result.get());
        assertTrue(post.getTimestamp() > 0);

        verify(rootRef).updateChildren(any());
    }

    @Test
    public void createPost_pending_success_returnsPostId() {
        DatabaseReference pushedRef = mock(DatabaseReference.class);
        when(postRef.push()).thenReturn(pushedRef);
        when(pushedRef.getKey()).thenReturn("post555");

        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        when(rootRef.updateChildren(any())).thenReturn(updateTask);

        when(updateTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        });
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);

        FeedItem post = new FeedItem(FeedItemType.PENDING, "user2", "Pending post");
        post.setImageUrl("img2.png");

        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        postRepository.createPost("classB", post, result::set, error::set);

        assertNull(error.get());
        assertEquals("post555", result.get());
    }

    @Test
    public void createPost_published_success_returnsPostId() {
        DatabaseReference pushedRef = mock(DatabaseReference.class);
        when(postRef.push()).thenReturn(pushedRef);
        when(pushedRef.getKey()).thenReturn("post777");

        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        when(rootRef.updateChildren(any())).thenReturn(updateTask);

        when(updateTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return updateTask;
        });
        when(updateTask.addOnFailureListener(any())).thenReturn(updateTask);

        FeedItem post = new FeedItem(FeedItemType.PUBLISHED, "user3", "Published post");
        post.setImageUrl("img3.png");

        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        postRepository.createPost("classC", post, result::set, error::set);

        assertNull(error.get());
        assertEquals("post777", result.get());
    }

    @Test
    public void createPost_nullPostId_callsOnError() {
        DatabaseReference pushedRef = mock(DatabaseReference.class);
        when(postRef.push()).thenReturn(pushedRef);
        when(pushedRef.getKey()).thenReturn(null);

        FeedItem post = new FeedItem(FeedItemType.ANNOUNCEMENT, "user1","Caption text");
        post.setImageUrl("img.png");

        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        postRepository.createPost("classA", post, result::set, error::set);

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Could not generate post ID", error.get().getMessage());
    }

    @Test
    public void createPost_updateFails_callsOnError() {
        DatabaseReference pushedRef = mock(DatabaseReference.class);
        when(postRef.push()).thenReturn(pushedRef);
        when(pushedRef.getKey()).thenReturn("post999");

        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        when(rootRef.updateChildren(any())).thenReturn(updateTask);

        when(updateTask.addOnSuccessListener(any())).thenReturn(updateTask);
        when(updateTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new RuntimeException("Update failed"));
            return updateTask;
        });

        FeedItem post = new FeedItem(FeedItemType.ANNOUNCEMENT,"user1", "Caption text");

        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        postRepository.createPost("classA", post, result::set, error::set);

        assertNull(result.get());
        assertNotNull(error.get());
        assertEquals("Update failed", error.get().getMessage());
    }

    @Test
    public void createPost_writesExpectedPaths() {
        DatabaseReference pushedRef = mock(DatabaseReference.class);
        when(postRef.push()).thenReturn(pushedRef);
        when(pushedRef.getKey()).thenReturn("post321");

        @SuppressWarnings("unchecked")
        Task<Void> updateTask = mock(Task.class);
        when(rootRef.updateChildren(any())).thenReturn(updateTask);

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

        FeedItem post = new FeedItem(FeedItemType.PENDING,"user9", "Caption text");

        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        postRepository.createPost("classZ", post, result::set, error::set);

        assertNull(error.get());
        assertEquals("post321", result.get());
        assertNotNull(sentUpdates.get());
        assertTrue(sentUpdates.get().containsKey("/posts/post321"));
        assertTrue(sentUpdates.get().containsKey("/classrooms/classZ/feed/pending/post321"));
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

    private void mockStringChild(DataSnapshot parent, String childName, String value) {
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(parent.child(childName)).thenReturn(childSnapshot);
        when(childSnapshot.getValue(String.class)).thenReturn(value);
    }

    private void mockLongChild(DataSnapshot parent, String childName, Long value) {
        DataSnapshot childSnapshot = mock(DataSnapshot.class);
        when(parent.child(childName)).thenReturn(childSnapshot);
        when(childSnapshot.getValue(Long.class)).thenReturn(value);
    }
}