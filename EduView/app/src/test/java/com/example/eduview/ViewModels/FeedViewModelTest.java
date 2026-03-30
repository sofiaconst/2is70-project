package com.example.eduview.ViewModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.data.repository.FeedRepository;
import com.example.eduview.data.repository.UserRepository;
import com.example.eduview.ui.feed.FeedViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeedViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FeedViewModel viewModel;
    private FeedRepository feedRepository;
    private UserRepository userRepository;
    private ClassroomRepository classroomRepository;

    @Before
    public void setUp() {
        feedRepository = Mockito.mock(FeedRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        classroomRepository = Mockito.mock(ClassroomRepository.class);

        viewModel = new FeedViewModel(feedRepository, userRepository, classroomRepository);
    }

    @Test
    public void loadPostsForUser_student_loadsPublishedAndAnnouncementsForLiveClassroom() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "old-class-id");

        MutableLiveData<String> liveClassroom = new MutableLiveData<>("class-123");
        MutableLiveData<List<FeedItem>> published = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(Collections.emptyList());

        when(classroomRepository.getLiveStudentClassroom("s1")).thenReturn(liveClassroom);
        when(feedRepository.fetchPublishedPosts("class-123")).thenReturn(published);
        when(feedRepository.fetchAnnouncements("class-123")).thenReturn(announcements);

        viewModel.loadPostsForUser(student);

        verify(classroomRepository).getLiveStudentClassroom("s1");
        verify(feedRepository).fetchPublishedPosts("class-123");
        verify(feedRepository).fetchAnnouncements("class-123");
        verify(feedRepository, never()).fetchPendingPosts(any());

        assertNotNull(viewModel.getPublishedPosts().getValue());
        assertNotNull(viewModel.getAnnouncements().getValue());
    }

    @Test
    public void reloadAll_teacher_refreshesEverythingIncludingPendingPosts() {
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-9");

        MutableLiveData<List<FeedItem>> published = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> pending = new MutableLiveData<>(Collections.emptyList());

        when(feedRepository.fetchPublishedPosts("class-9")).thenReturn(published);
        when(feedRepository.fetchAnnouncements("class-9")).thenReturn(announcements);
        when(feedRepository.fetchPendingPosts("class-9")).thenReturn(pending);

        viewModel.loadPostsForUser(teacher);
        viewModel.reloadAll();

        verify(feedRepository, times(2)).fetchPublishedPosts("class-9");
        verify(feedRepository, times(2)).fetchAnnouncements("class-9");
        verify(feedRepository, times(2)).fetchPendingPosts("class-9");
        assertEquals(Integer.valueOf(1), viewModel.getRefreshTrigger().getValue());
    }

    @Test
    public void reloadAll_withNoCurrentUser_doesNothing() {
        viewModel.reloadAll();

        assertEquals(Integer.valueOf(0), viewModel.getRefreshTrigger().getValue());
        verify(feedRepository, never()).fetchPublishedPosts(any());
        verify(feedRepository, never()).fetchAnnouncements(any());
        verify(feedRepository, never()).fetchPendingPosts(any());
    }

    @Test
    public void loadChildrenForParent_withNoChildIds_setsEmptyList() {
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", new ArrayList<>());

        viewModel.loadChildrenForParent(parent);

        assertNotNull(viewModel.getParentChildren().getValue());
        assertTrue(viewModel.getParentChildren().getValue().isEmpty());
    }

    @Test
    public void loadChildrenForParent_onlyKeepsStudentsThatHaveAClassroom() {
        Parent parent = new Parent(
                "p1",
                "Pat",
                "Parent",
                "pat@test.com",
                Arrays.asList("child-1", "child-2", "child-3")
        );

        Student validStudent = new Student("child-1", "Chris", "One", "c1@test.com", "class-1");
        Student noClassStudent = new Student("child-2", "Chris", "Two", "c2@test.com", "   ");

        Mockito.doAnswer(invocation -> {
            String childId = invocation.getArgument(0);
            UserRepository.UserCallback callback = invocation.getArgument(1);

            if ("child-1".equals(childId)) {
                callback.onSuccess(validStudent);
            } else if ("child-2".equals(childId)) {
                callback.onSuccess(noClassStudent);
            } else {
                callback.onError(new RuntimeException("Child lookup failed"));
            }
            return null;
        }).when(userRepository).getUserById(any(String.class), any(UserRepository.UserCallback.class));

        viewModel.loadChildrenForParent(parent);

        List<Student> result = viewModel.getParentChildren().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("child-1", result.get(0).getUserId());
        assertEquals("class-1", result.get(0).getClassId());
    }

    @Test
    public void approvePost_withClassroom_callsRepositoryAndReloads() {
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-42");

        when(feedRepository.fetchPublishedPosts("class-42"))
                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(feedRepository.fetchAnnouncements("class-42"))
                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(feedRepository.fetchPendingPosts("class-42"))
                .thenReturn(new MutableLiveData<>(Collections.emptyList()));

        viewModel.loadPostsForUser(teacher);
        viewModel.approvePost("post-99");

        verify(feedRepository).approvePost("class-42", "post-99");
        verify(feedRepository, times(2)).fetchPublishedPosts("class-42");
        verify(feedRepository, times(2)).fetchAnnouncements("class-42");
        verify(feedRepository, times(2)).fetchPendingPosts("class-42");
        assertEquals(Integer.valueOf(1), viewModel.getRefreshTrigger().getValue());
    }

    @Test
    public void rejectPost_withoutClassroom_doesNotCallRepository() {
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", Collections.emptyList());

        viewModel.loadPostsForUser(parent);
        viewModel.rejectPost("post-12");

        verify(feedRepository, never()).rejectPost(any(), any());
        assertEquals(Integer.valueOf(0), viewModel.getRefreshTrigger().getValue());
    }

    @Test
    public void loadPendingPosts_teacher_callsRepository() {
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-55");

        MutableLiveData<List<FeedItem>> published = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> pending = new MutableLiveData<>(Collections.emptyList());

        when(feedRepository.fetchPublishedPosts("class-55")).thenReturn(published);
        when(feedRepository.fetchAnnouncements("class-55")).thenReturn(announcements);
        when(feedRepository.fetchPendingPosts("class-55")).thenReturn(pending);

        viewModel.loadPostsForUser(teacher);
        viewModel.loadPendingPosts();

        verify(feedRepository, atLeastOnce()).fetchPendingPosts("class-55");
        assertNotNull(viewModel.getPendingPosts().getValue());
    }

    @Test
    public void loadPendingPosts_student_callsRepositoryForLiveStudentClassroom() {
        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "ignored-class-id");

        MutableLiveData<String> liveClassroom = new MutableLiveData<>("class-123");
        MutableLiveData<List<FeedItem>> published = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(Collections.emptyList());
        MutableLiveData<List<FeedItem>> pending = new MutableLiveData<>(Collections.emptyList());

        when(classroomRepository.getLiveStudentClassroom("s1")).thenReturn(liveClassroom);
        when(feedRepository.fetchPublishedPosts("class-123")).thenReturn(published);
        when(feedRepository.fetchAnnouncements("class-123")).thenReturn(announcements);
        when(feedRepository.fetchPendingPosts("class-123")).thenReturn(pending);

        viewModel.loadPostsForUser(student);
        viewModel.loadPendingPosts();

        verify(classroomRepository).getLiveStudentClassroom("s1");
        verify(feedRepository).fetchPendingPosts("class-123");
        assertNotNull(viewModel.getPendingPosts().getValue());
    }

    @Test
    public void rejectPost_withClassroom_callsRepositoryAndReloads() {
        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-42");

        when(feedRepository.fetchPublishedPosts("class-42"))
                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(feedRepository.fetchAnnouncements("class-42"))
                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(feedRepository.fetchPendingPosts("class-42"))
                .thenReturn(new MutableLiveData<>(Collections.emptyList()));

        viewModel.loadPostsForUser(teacher);
        viewModel.rejectPost("post-77");

        verify(feedRepository).rejectPost("class-42", "post-77");
        verify(feedRepository, times(2)).fetchPublishedPosts("class-42");
        verify(feedRepository, times(2)).fetchAnnouncements("class-42");
        verify(feedRepository, times(2)).fetchPendingPosts("class-42");
        assertEquals(Integer.valueOf(1), viewModel.getRefreshTrigger().getValue());
    }

    @Test
    public void approvePost_withoutClassroom_doesNotCallRepository() {
        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", Collections.emptyList());

        viewModel.loadPostsForUser(parent);
        viewModel.approvePost("post-44");

        verify(feedRepository, never()).approvePost(any(), any());
        assertEquals(Integer.valueOf(0), viewModel.getRefreshTrigger().getValue());
    }

    @Test
    public void loadPostsForUser_parent_clearsFeedAndDoesNotFetch() {
        Parent parent = new Parent("p2", "Paula", "Parent", "paula@test.com", Collections.emptyList());

        viewModel.loadPostsForUser(parent);

        verify(feedRepository, never()).fetchPublishedPosts(any());
        verify(feedRepository, never()).fetchAnnouncements(any());
        verify(feedRepository, never()).fetchPendingPosts(any());

        assertNotNull(viewModel.getPublishedPosts().getValue());
        assertTrue(viewModel.getPublishedPosts().getValue().isEmpty());
        assertNotNull(viewModel.getAnnouncements().getValue());
        assertTrue(viewModel.getAnnouncements().getValue().isEmpty());
        assertNotNull(viewModel.getPendingPosts().getValue());
        assertTrue(viewModel.getPendingPosts().getValue().isEmpty());
    }

    @Test
    public void loadPostsForUser_student_withBlankLiveClassroom_clearsFeed() {
        Student student = new Student("s2", "Sara", "Student", "sara@test.com", "old-class");

        MutableLiveData<String> liveClassroom = new MutableLiveData<>("   ");
        when(classroomRepository.getLiveStudentClassroom("s2")).thenReturn(liveClassroom);

        viewModel.loadPostsForUser(student);

        verify(classroomRepository).getLiveStudentClassroom("s2");
        verify(feedRepository, never()).fetchPublishedPosts(any());
        verify(feedRepository, never()).fetchAnnouncements(any());
        verify(feedRepository, never()).fetchPendingPosts(any());

        assertNotNull(viewModel.getPublishedPosts().getValue());
        assertTrue(viewModel.getPublishedPosts().getValue().isEmpty());
        assertNotNull(viewModel.getAnnouncements().getValue());
        assertTrue(viewModel.getAnnouncements().getValue().isEmpty());
        assertNotNull(viewModel.getPendingPosts().getValue());
        assertTrue(viewModel.getPendingPosts().getValue().isEmpty());
    }
}