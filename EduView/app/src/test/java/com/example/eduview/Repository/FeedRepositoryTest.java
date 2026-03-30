package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.repository.FeedRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FeedRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private DatabaseReference classroomRef;
    private DatabaseReference postsRef;
    private DatabaseReference userRef;

    private FeedRepository repository;

    @Before
    public void setUp() {
        classroomRef = mock(DatabaseReference.class);
        postsRef = mock(DatabaseReference.class);
        userRef = mock(DatabaseReference.class);

        repository = new FeedRepository(classroomRef, postsRef, userRef);
    }

    @Test
    public void fetchPublishedPosts_nullClassroomId_returnsEmptyList() {
        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(null);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPublishedPosts_emptyClassroomId_returnsEmptyList() {
        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts("");

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPendingPosts_nullClassroomId_returnsEmptyList() {
        LiveData<List<FeedItem>> result = repository.fetchPendingPosts(null);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchAnnouncements_emptyClassroomId_returnsEmptyList() {
        LiveData<List<FeedItem>> result = repository.fetchAnnouncements("");

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPublishedPosts_snapshotDoesNotExist_returnsEmptyList() {
        String classroomId = "class1";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        Task<DataSnapshot> task = mockSuccessGetTask();
        when(publishedNode.get()).thenReturn(task);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(task.getResult()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPendingPosts_getFails_returnsEmptyList() {
        String classroomId = "class2";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference pendingNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("pending")).thenReturn(pendingNode);

        Task<DataSnapshot> failedTask = mockFailureGetTask();
        when(pendingNode.get()).thenReturn(failedTask);

        LiveData<List<FeedItem>> result = repository.fetchPendingPosts(classroomId);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchAnnouncements_snapshotDoesNotExist_returnsEmptyList() {
        String classroomId = "class3";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference announcementNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("announcements")).thenReturn(announcementNode);

        Task<DataSnapshot> task = mockSuccessGetTask();
        when(announcementNode.get()).thenReturn(task);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(task.getResult()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        LiveData<List<FeedItem>> result = repository.fetchAnnouncements(classroomId);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPublishedPosts_authorMissing_createsItemWithBlankAuthor() {
        String classroomId = "class10";
        String postId = "post1";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);
        DatabaseReference postNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        Task<DataSnapshot> publishedTask = mockSuccessGetTask();
        when(publishedNode.get()).thenReturn(publishedTask);

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedTask.getResult()).thenReturn(publishedSnapshot);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
        when(postRefSnapshot.getKey()).thenReturn(postId);
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));

        when(postsRef.child(postId)).thenReturn(postNode);

        Task<DataSnapshot> postTask = mockSuccessGetTask();
        when(postNode.get()).thenReturn(postTask);

        DataSnapshot postSnapshot = mock(DataSnapshot.class);
        when(postTask.getResult()).thenReturn(postSnapshot);

        mockStringChild(postSnapshot, "authorId", null);
        mockStringChild(postSnapshot, "caption", "Hello world");
        mockStringChild(postSnapshot, "imageUrl", "img.png");
        mockLongChild(postSnapshot, "timestamp", 123L);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertEquals(1, result.getValue().size());

        FeedItem item = result.getValue().get(0);
        assertEquals("", item.getAuthorName());
        assertEquals("Hello world", item.getCaption());
        assertEquals("post1", item.getPostId());
        assertEquals("img.png", item.getImageUrl());
        assertEquals(123L, item.getTimestamp());
    }

//    @Test
//    public void fetchPublishedPosts_authorExists_loadsAuthorName() {
//        String classroomId = "class11";
//        String postId = "post2";
//        String authorId = "user1";
//
//        DatabaseReference classNode = mock(DatabaseReference.class);
//        DatabaseReference feedNode = mock(DatabaseReference.class);
//        DatabaseReference publishedNode = mock(DatabaseReference.class);
//        DatabaseReference postNode = mock(DatabaseReference.class);
//        DatabaseReference authorNode = mock(DatabaseReference.class);
//
//        when(classroomRef.child(classroomId)).thenReturn(classNode);
//        when(classNode.child("feed")).thenReturn(feedNode);
//        when(feedNode.child("published_posts")).thenReturn(publishedNode);
//
//        Task<DataSnapshot> publishedTask = mockSuccessGetTask();
//        when(publishedNode.get()).thenReturn(publishedTask);
//
//        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
//        when(publishedTask.getResult()).thenReturn(publishedSnapshot);
//        when(publishedSnapshot.exists()).thenReturn(true);
//
//        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
//        when(postRefSnapshot.getKey()).thenReturn(postId);
//        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));
//
//        when(postsRef.child(postId)).thenReturn(postNode);
//        Task<DataSnapshot> postTask = mockSuccessGetTask();
//        when(postNode.get()).thenReturn(postTask);
//
//        DataSnapshot postSnapshot = mock(DataSnapshot.class);
//        when(postTask.getResult()).thenReturn(postSnapshot);
//        mockStringChild(postSnapshot, "authorId", authorId);
//        mockStringChild(postSnapshot, "caption", "Post text");
//        mockStringChild(postSnapshot, "imageUrl", null);
//        mockLongChild(postSnapshot, "timestamp", 200L);
//
//        when(userRef.child(authorId)).thenReturn(authorNode);
//        Task<DataSnapshot> authorTask = mockSuccessGetTask();
//        when(authorNode.get()).thenReturn(authorTask);
//
//        DataSnapshot authorSnapshot = mock(DataSnapshot.class);
//        when(authorTask.getResult()).thenReturn(authorSnapshot);
//        mockStringChild(authorSnapshot, "first_name", "Sam");
//        mockStringChild(authorSnapshot, "last_name", "Smith");
//        mockStringChild(authorSnapshot, "pfp", "green_dino");
//
//        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);
//
//        assertNotNull(result.getValue());
//        assertEquals(1, result.getValue().size());
//
//        FeedItem item = result.getValue().get(0);
//        assertEquals("Sam Smith", item.getAuthorName());
//        assertEquals("Post text", item.getCaption());
//        assertEquals("post2", item.getPostId());
//        assertEquals(200L, item.getTimestamp());
//        assertEquals("green_dino", item.getAuthorPfpName());
//    }

    @Test
    public void fetchPublishedPosts_authorFetchFails_fallsBackToBlankAuthor() {
        String classroomId = "class12";
        String postId = "post3";
        String authorId = "user2";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);
        DatabaseReference postNode = mock(DatabaseReference.class);
        DatabaseReference authorNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        Task<DataSnapshot> publishedTask = mockSuccessGetTask();
        when(publishedNode.get()).thenReturn(publishedTask);

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedTask.getResult()).thenReturn(publishedSnapshot);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
        when(postRefSnapshot.getKey()).thenReturn(postId);
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));

        when(postsRef.child(postId)).thenReturn(postNode);
        Task<DataSnapshot> postTask = mockSuccessGetTask();
        when(postNode.get()).thenReturn(postTask);

        DataSnapshot postSnapshot = mock(DataSnapshot.class);
        when(postTask.getResult()).thenReturn(postSnapshot);
        mockStringChild(postSnapshot, "authorId", authorId);
        mockStringChild(postSnapshot, "caption", "Post text");
        mockStringChild(postSnapshot, "imageUrl", "img3.png");
        mockLongChild(postSnapshot, "timestamp", 300L);

        when(userRef.child(authorId)).thenReturn(authorNode);
        Task<DataSnapshot> failedAuthorTask = mockFailureGetTask();
        when(authorNode.get()).thenReturn(failedAuthorTask);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertEquals(1, result.getValue().size());

        FeedItem item = result.getValue().get(0);
        assertEquals("", item.getAuthorName());
        assertEquals("Post text", item.getCaption());
        assertEquals("post3", item.getPostId());
        assertEquals("img3.png", item.getImageUrl());
        assertEquals(300L, item.getTimestamp());
    }

    @Test
    public void fetchPublishedPosts_sortsByTimestampDescending() {
        String classroomId = "class13";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        Task<DataSnapshot> publishedTask = mockSuccessGetTask();
        when(publishedNode.get()).thenReturn(publishedTask);

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedTask.getResult()).thenReturn(publishedSnapshot);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRef1 = mock(DataSnapshot.class);
        DataSnapshot postRef2 = mock(DataSnapshot.class);
        when(postRef1.getKey()).thenReturn("postA");
        when(postRef2.getKey()).thenReturn("postB");
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRef1, postRef2));

        DatabaseReference postNodeA = mock(DatabaseReference.class);
        DatabaseReference postNodeB = mock(DatabaseReference.class);

        when(postsRef.child("postA")).thenReturn(postNodeA);
        when(postsRef.child("postB")).thenReturn(postNodeB);

        Task<DataSnapshot> taskA = mockSuccessGetTask();
        Task<DataSnapshot> taskB = mockSuccessGetTask();
        when(postNodeA.get()).thenReturn(taskA);
        when(postNodeB.get()).thenReturn(taskB);

        DataSnapshot snapA = mock(DataSnapshot.class);
        DataSnapshot snapB = mock(DataSnapshot.class);
        when(taskA.getResult()).thenReturn(snapA);
        when(taskB.getResult()).thenReturn(snapB);

        mockStringChild(snapA, "authorId", null);
        mockStringChild(snapA, "caption", "Older");
        mockStringChild(snapA, "imageUrl", null);
        mockLongChild(snapA, "timestamp", 100L);

        mockStringChild(snapB, "authorId", null);
        mockStringChild(snapB, "caption", "Newer");
        mockStringChild(snapB, "imageUrl", null);
        mockLongChild(snapB, "timestamp", 500L);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertEquals(2, result.getValue().size());
        assertEquals("Newer", result.getValue().get(0).getCaption());
        assertEquals("Older", result.getValue().get(1).getCaption());
    }

    @Test
    public void approvePost_movesPostFromPendingToPublished() {
        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference pendingNode = mock(DatabaseReference.class);
        DatabaseReference pendingPostNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);
        DatabaseReference publishedPostNode = mock(DatabaseReference.class);

        @SuppressWarnings("unchecked")
        Task<Void> successTask = mock(Task.class);

        when(classroomRef.child("classA")).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("pending")).thenReturn(pendingNode);
        when(pendingNode.child("postX")).thenReturn(pendingPostNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);
        when(publishedNode.child("postX")).thenReturn(publishedPostNode);

        when(publishedPostNode.setValue(true)).thenReturn(successTask);
        when(successTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return successTask;
        });
        when(successTask.addOnFailureListener(any())).thenReturn(successTask);

        repository.approvePost("classA", "postX");

        verify(publishedPostNode).setValue(true);
        verify(pendingPostNode).removeValue();
    }

//    @Test
//    public void rejectPost_removesPendingPost() {
//        DatabaseReference classNode = mock(DatabaseReference.class);
//        DatabaseReference feedNode = mock(DatabaseReference.class);
//        DatabaseReference pendingNode = mock(DatabaseReference.class);
//        DatabaseReference pendingPostNode = mock(DatabaseReference.class);
//
//        @SuppressWarnings("unchecked")
//        Task<Void> removeTask = mock(Task.class);
//
//        when(classroomRef.child("classB")).thenReturn(classNode);
//        when(classNode.child("feed")).thenReturn(feedNode);
//        when(feedNode.child("pending")).thenReturn(pendingNode);
//        when(pendingNode.child("postY")).thenReturn(pendingPostNode);
//
//        when(pendingPostNode.removeValue()).thenReturn(removeTask);
//        when(removeTask.addOnFailureListener(any())).thenReturn(removeTask);
//
//        repository.rejectPost("classB", "postY");
//
//        verify(pendingPostNode).removeValue();
//    }

    private Task<DataSnapshot> mockSuccessGetTask() {
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> task = mock(Task.class);

        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DataSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(task.getResult());
            return task;
        });

        when(task.addOnFailureListener(any())).thenReturn(task);
        return task;
    }

    private Task<DataSnapshot> mockFailureGetTask() {
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> task = mock(Task.class);

        when(task.addOnSuccessListener(any())).thenReturn(task);
        when(task.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new RuntimeException("Firebase failure"));
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