package com.example.eduview.Repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.exists()).thenReturn(false);

        Task<DataSnapshot> task = mockSuccessGetTask(snapshot);
        when(publishedNode.get()).thenReturn(task);

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

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.exists()).thenReturn(false);

        Task<DataSnapshot> task = mockSuccessGetTask(snapshot);
        when(announcementNode.get()).thenReturn(task);

        LiveData<List<FeedItem>> result = repository.fetchAnnouncements(classroomId);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPublishedPosts_emptyChildren_returnsEmptyList() {
        String classroomId = "classEmpty";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        DataSnapshot snapshot = mock(DataSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.getChildren()).thenReturn(Collections.emptyList());

        Task<DataSnapshot> task = mockSuccessGetTask(snapshot);
        when(publishedNode.get()).thenReturn(task);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
    }

    @Test
    public void fetchPublishedPosts_authorMissing_createsItemWithoutAuthorInfo() {
        String classroomId = "class10";
        String postId = "post1";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);
        DatabaseReference postNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
        when(postRefSnapshot.getKey()).thenReturn(postId);
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));

        Task<DataSnapshot> publishedTask = mockSuccessGetTask(publishedSnapshot);
        when(publishedNode.get()).thenReturn(publishedTask);

        when(postsRef.child(postId)).thenReturn(postNode);

        DataSnapshot postSnapshot = mock(DataSnapshot.class);
        mockStringChild(postSnapshot, "authorId", null);
        mockStringChild(postSnapshot, "caption", "Hello world");
        mockStringChild(postSnapshot, "imageUrl", "img.png");
        mockLongChild(postSnapshot, "timestamp", 123L);

        Task<DataSnapshot> postTask = mockSuccessGetTask(postSnapshot);
        when(postNode.get()).thenReturn(postTask);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertEquals(1, result.getValue().size());

        FeedItem item = result.getValue().get(0);
        assertEquals("", item.getAuthorName());
        assertEquals("Hello world", item.getCaption());
        assertEquals("post1", item.getPostId());
        assertEquals("img.png", item.getImageUrl());
        assertEquals(123L, item.getTimestamp());
        assertFalse(item.isTeacher());
    }

    @Test
    public void fetchPublishedPosts_authorExists_loadsAuthorNameAndTeacherFlag() {
        String classroomId = "class11";
        String postId = "post2";
        String authorId = "user1";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);
        DatabaseReference postNode = mock(DatabaseReference.class);
        DatabaseReference authorNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
        when(postRefSnapshot.getKey()).thenReturn(postId);
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));

        Task<DataSnapshot> publishedTask = mockSuccessGetTask(publishedSnapshot);
        when(publishedNode.get()).thenReturn(publishedTask);

        when(postsRef.child(postId)).thenReturn(postNode);

        DataSnapshot postSnapshot = mock(DataSnapshot.class);
        mockStringChild(postSnapshot, "authorId", authorId);
        mockStringChild(postSnapshot, "caption", "Post text");
        mockStringChild(postSnapshot, "imageUrl", null);
        mockLongChild(postSnapshot, "timestamp", 200L);

        Task<DataSnapshot> postTask = mockSuccessGetTask(postSnapshot);
        when(postNode.get()).thenReturn(postTask);

        when(userRef.child(authorId)).thenReturn(authorNode);

        DataSnapshot authorSnapshot = mock(DataSnapshot.class);
        mockStringChild(authorSnapshot, "first_name", "Sam");
        mockStringChild(authorSnapshot, "last_name", "Smith");
        mockStringChild(authorSnapshot, "pfp", "green_dino");
        mockStringChild(authorSnapshot, "role", "Teacher");

        Task<DataSnapshot> authorTask = mockSuccessGetTask(authorSnapshot);
        when(authorNode.get()).thenReturn(authorTask);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertEquals(1, result.getValue().size());

        FeedItem item = result.getValue().get(0);
        assertEquals("Sam Smith", item.getAuthorName());
        assertEquals("Post text", item.getCaption());
        assertEquals("post2", item.getPostId());
        assertEquals(200L, item.getTimestamp());
        assertEquals("green_dino", item.getAuthorPfpName());
        assertTrue(item.isTeacher());
    }

    @Test
    public void fetchPublishedPosts_authorFetchFails_fallsBackButStillBuildsItem() {
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

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
        when(postRefSnapshot.getKey()).thenReturn(postId);
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));

        Task<DataSnapshot> publishedTask = mockSuccessGetTask(publishedSnapshot);
        when(publishedNode.get()).thenReturn(publishedTask);

        when(postsRef.child(postId)).thenReturn(postNode);

        DataSnapshot postSnapshot = mock(DataSnapshot.class);
        mockStringChild(postSnapshot, "authorId", authorId);
        mockStringChild(postSnapshot, "caption", "Post text");
        mockStringChild(postSnapshot, "imageUrl", "img3.png");
        mockLongChild(postSnapshot, "timestamp", 300L);

        Task<DataSnapshot> postTask = mockSuccessGetTask(postSnapshot);
        when(postNode.get()).thenReturn(postTask);

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
        assertFalse(item.isTeacher());
    }

    @Test
    public void fetchPublishedPosts_postFetchFails_skipsFailedPost() {
        String classroomId = "classFail";

        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference publishedNode = mock(DatabaseReference.class);
        DatabaseReference postNode = mock(DatabaseReference.class);

        when(classroomRef.child(classroomId)).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("published_posts")).thenReturn(publishedNode);

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRefSnapshot = mock(DataSnapshot.class);
        when(postRefSnapshot.getKey()).thenReturn("badPost");
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRefSnapshot));

        Task<DataSnapshot> publishedTask = mockSuccessGetTask(publishedSnapshot);
        when(publishedNode.get()).thenReturn(publishedTask);

        when(postsRef.child("badPost")).thenReturn(postNode);
        Task<DataSnapshot> failedPostTask = mockFailureGetTask();
        when(postNode.get()).thenReturn(failedPostTask);

        LiveData<List<FeedItem>> result = repository.fetchPublishedPosts(classroomId);

        assertNotNull(result.getValue());
        assertTrue(result.getValue().isEmpty());
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

        DataSnapshot publishedSnapshot = mock(DataSnapshot.class);
        when(publishedSnapshot.exists()).thenReturn(true);

        DataSnapshot postRef1 = mock(DataSnapshot.class);
        DataSnapshot postRef2 = mock(DataSnapshot.class);
        when(postRef1.getKey()).thenReturn("postA");
        when(postRef2.getKey()).thenReturn("postB");
        when(publishedSnapshot.getChildren()).thenReturn(Arrays.asList(postRef1, postRef2));

        Task<DataSnapshot> publishedTask = mockSuccessGetTask(publishedSnapshot);
        when(publishedNode.get()).thenReturn(publishedTask);

        DatabaseReference postNodeA = mock(DatabaseReference.class);
        DatabaseReference postNodeB = mock(DatabaseReference.class);

        when(postsRef.child("postA")).thenReturn(postNodeA);
        when(postsRef.child("postB")).thenReturn(postNodeB);

        DataSnapshot snapA = mock(DataSnapshot.class);
        DataSnapshot snapB = mock(DataSnapshot.class);

        mockStringChild(snapA, "authorId", null);
        mockStringChild(snapA, "caption", "Older");
        mockStringChild(snapA, "imageUrl", null);
        mockLongChild(snapA, "timestamp", 100L);

        mockStringChild(snapB, "authorId", null);
        mockStringChild(snapB, "caption", "Newer");
        mockStringChild(snapB, "imageUrl", null);
        mockLongChild(snapB, "timestamp", 500L);

        Task<DataSnapshot> taskA = mockSuccessGetTask(snapA);
        Task<DataSnapshot> taskB = mockSuccessGetTask(snapB);
        when(postNodeA.get()).thenReturn(taskA);
        when(postNodeB.get()).thenReturn(taskB);

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

    @Test
    public void rejectPost_removesPendingPostAndDeletesRealPost() {
        DatabaseReference classNode = mock(DatabaseReference.class);
        DatabaseReference feedNode = mock(DatabaseReference.class);
        DatabaseReference pendingNode = mock(DatabaseReference.class);
        DatabaseReference pendingPostNode = mock(DatabaseReference.class);
        DatabaseReference postNode = mock(DatabaseReference.class);

        @SuppressWarnings("unchecked")
        Task<Void> removePendingTask = mock(Task.class);
        @SuppressWarnings("unchecked")
        Task<Void> removePostTask = mock(Task.class);

        when(classroomRef.child("classB")).thenReturn(classNode);
        when(classNode.child("feed")).thenReturn(feedNode);
        when(feedNode.child("pending")).thenReturn(pendingNode);
        when(pendingNode.child("postY")).thenReturn(pendingPostNode);
        when(postsRef.child("postY")).thenReturn(postNode);

        when(pendingPostNode.removeValue()).thenReturn(removePendingTask);
        when(removePendingTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return removePendingTask;
        });
        when(removePendingTask.addOnFailureListener(any())).thenReturn(removePendingTask);

        when(postNode.removeValue()).thenReturn(removePostTask);
        when(removePostTask.addOnFailureListener(any())).thenReturn(removePostTask);

        repository.rejectPost("classB", "postY");

        verify(pendingPostNode).removeValue();
        verify(postNode).removeValue();
    }

    @Test
    public void postAuthorIsTeacher_teacherRole_returnsTrue() {
        String authorId = "teacher1";

        DatabaseReference authorNode = mock(DatabaseReference.class);
        when(userRef.child(authorId)).thenReturn(authorNode);

        DataSnapshot authorSnapshot = mock(DataSnapshot.class);
        mockStringChild(authorSnapshot, "role", "Teacher");

        Task<DataSnapshot> authorTask = mockSuccessGetTask(authorSnapshot);
        when(authorNode.get()).thenReturn(authorTask);

        AtomicReference<Boolean> result = new AtomicReference<>(false);

        repository.postAuthorIsTeacher(authorId, result::set);

        assertTrue(result.get());
    }

    @Test
    public void postAuthorIsTeacher_nonTeacherRole_returnsFalse() {
        String authorId = "parent1";

        DatabaseReference authorNode = mock(DatabaseReference.class);
        when(userRef.child(authorId)).thenReturn(authorNode);

        DataSnapshot authorSnapshot = mock(DataSnapshot.class);
        mockStringChild(authorSnapshot, "role", "Parent");

        Task<DataSnapshot> authorTask = mockSuccessGetTask(authorSnapshot);
        when(authorNode.get()).thenReturn(authorTask);

        AtomicReference<Boolean> result = new AtomicReference<>(true);

        repository.postAuthorIsTeacher(authorId, result::set);

        assertFalse(result.get());
    }

    @Test
    public void postAuthorIsTeacher_fetchFails_returnsFalse() {
        String authorId = "brokenUser";

        DatabaseReference authorNode = mock(DatabaseReference.class);
        when(userRef.child(authorId)).thenReturn(authorNode);

        Task<DataSnapshot> failedTask = mockFailureGetTask();
        when(authorNode.get()).thenReturn(failedTask);

        AtomicReference<Boolean> result = new AtomicReference<>(true);

        repository.postAuthorIsTeacher(authorId, result::set);

        assertFalse(result.get());
    }

    private Task<DataSnapshot> mockSuccessGetTask(DataSnapshot snapshot) {
        @SuppressWarnings("unchecked")
        Task<DataSnapshot> task = mock(Task.class);

        when(task.getResult()).thenReturn(snapshot);

        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DataSnapshot> listener = invocation.getArgument(0);
            listener.onSuccess(snapshot);
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