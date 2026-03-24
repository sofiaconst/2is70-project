//package com.example.eduview.ui.feed;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import androidx.lifecycle.MutableLiveData;
//
//import com.example.eduview.data.model.FeedItem;
//import com.example.eduview.data.model.Parent;
//import com.example.eduview.data.model.Student;
//import com.example.eduview.data.model.Teacher;
//import com.example.eduview.data.model.User;
//import com.example.eduview.data.repository.FeedRepository;
//import com.example.eduview.data.repository.UserRepository;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import sun.misc.Unsafe;
//
//public class FeedViewModelTest {
//
//    private FeedViewModel viewModel;
//    private FeedRepository feedRepository;
//    private UserRepository userRepository;
//
//    @Before
//    public void setUp() throws Exception {
//        // We cannot call new FeedViewModel() here because its repository fields
//        // create real Firebase repository objects right away.
//        viewModel = createViewModelWithoutConstructor();
//
//        feedRepository = org.mockito.Mockito.mock(FeedRepository.class);
//        userRepository = org.mockito.Mockito.mock(UserRepository.class);
//
//        // Manually inject the mocked repos and LiveData fields we need.
//        setPrivateField(viewModel, "feedRepository", feedRepository);
//        setPrivateField(viewModel, "userRepository", userRepository);
//        setPrivateField(viewModel, "parentChildren", new MutableLiveData<>(new ArrayList<Student>()));
//        setPrivateField(viewModel, "refreshTrigger", new MutableLiveData<>(0));
//    }
//
//    @Test
//    public void loadPostsForUser_student_loadsPublishedAndAnnouncementsForStudentClass() {
//        // Student path should use the student's class id for both feed calls.
//        Student student = new Student("s1", "Sam", "Student", "sam@test.com", "class-123");
//
//        MutableLiveData<List<FeedItem>> published = new MutableLiveData<>(Collections.emptyList());
//        MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(Collections.emptyList());
//
//        when(feedRepository.fetchPublishedPosts("class-123")).thenReturn(published);
//        when(feedRepository.fetchAnnouncements("class-123")).thenReturn(announcements);
//
//        viewModel.loadPostsForUser(student);
//        viewModel.loadPublishedPosts();
//        viewModel.loadAnnouncements();
//
//        verify(feedRepository).fetchPublishedPosts("class-123");
//        verify(feedRepository).fetchAnnouncements("class-123");
//        assertEquals(published, viewModel.getPublishedPosts());
//        assertEquals(announcements, viewModel.getAnnouncements());
//    }
//
//    @Test
//    public void reloadAll_teacher_refreshesEverythingIncludingPendingPosts() {
//        // Teacher reload should refresh published, announcements, and pending.
//        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-9");
//
//        MutableLiveData<List<FeedItem>> published = new MutableLiveData<>(Collections.emptyList());
//        MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(Collections.emptyList());
//        MutableLiveData<List<FeedItem>> pending = new MutableLiveData<>(Collections.emptyList());
//
//        when(feedRepository.fetchPublishedPosts("class-9")).thenReturn(published);
//        when(feedRepository.fetchAnnouncements("class-9")).thenReturn(announcements);
//        when(feedRepository.fetchPendingPosts("class-9")).thenReturn(pending);
//
//        viewModel.loadPostsForUser(teacher);
//        viewModel.reloadAll();
//
//        verify(feedRepository).fetchPublishedPosts("class-9");
//        verify(feedRepository).fetchAnnouncements("class-9");
//        verify(feedRepository).fetchPendingPosts("class-9");
//        assertEquals(Integer.valueOf(1), viewModel.getRefreshTrigger().getValue());
//    }
//
//    @Test
//    public void reloadAll_withNoCurrentUser_doesNothingAndDoesNotCrash() {
//        // If no user has been loaded yet, reload should stop early.
//        viewModel.reloadAll();
//
//        assertEquals(Integer.valueOf(0), viewModel.getRefreshTrigger().getValue());
//        verify(feedRepository, never()).fetchPublishedPosts(any());
//        verify(feedRepository, never()).fetchAnnouncements(any());
//        verify(feedRepository, never()).fetchPendingPosts(any());
//    }
//
//    @Test
//    public void loadChildrenForParent_withNoChildIds_setsEmptyList() {
//        // Parent with no children should just get an empty list.
//        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", new ArrayList<>());
//
//        viewModel.loadChildrenForParent(parent);
//
//        assertNotNull(viewModel.getParentChildren().getValue());
//        assertTrue(viewModel.getParentChildren().getValue().isEmpty());
//    }
//
//    @Test
//    public void loadChildrenForParent_onlyKeepsStudentsThatActuallyHaveAClassroom() {
//        // One valid child, one child with blank class id, and one failed lookup.
//        Parent parent = new Parent(
//                "p1",
//                "Pat",
//                "Parent",
//                "pat@test.com",
//                Arrays.asList("child-1", "child-2", "child-3")
//        );
//
//        Student validStudent = new Student("child-1", "Chris", "One", "c1@test.com", "class-1");
//        Student noClassStudent = new Student("child-2", "Chris", "Two", "c2@test.com", "   ");
//
//        org.mockito.stubbing.Answer<Void> answer = invocation -> {
//            String childId = invocation.getArgument(0);
//            UserRepository.UserCallback callback = invocation.getArgument(1);
//
//            if ("child-1".equals(childId)) {
//                callback.onSuccess(validStudent);
//            } else if ("child-2".equals(childId)) {
//                callback.onSuccess(noClassStudent);
//            } else {
//                callback.onError(new RuntimeException("Child lookup failed"));
//            }
//            return null;
//        };
//
//        org.mockito.Mockito.doAnswer(answer)
//                .when(userRepository)
//                .getUserById(any(String.class), any(UserRepository.UserCallback.class));
//
//        viewModel.loadChildrenForParent(parent);
//
//        List<Student> result = viewModel.getParentChildren().getValue();
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("child-1", result.get(0).getUserId());
//        assertEquals("class-1", result.get(0).getClassId());
//    }
//
//    @Test
//    public void approvePost_withClassroom_callsRepositoryAndReloadsFeed() {
//        // Approving should call the repo and then trigger a reload.
//        Teacher teacher = new Teacher("t1", "Tina", "Teacher", "tina@test.com", "class-42");
//
//        when(feedRepository.fetchPublishedPosts("class-42"))
//                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
//        when(feedRepository.fetchAnnouncements("class-42"))
//                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
//        when(feedRepository.fetchPendingPosts("class-42"))
//                .thenReturn(new MutableLiveData<>(Collections.emptyList()));
//
//        viewModel.loadPostsForUser(teacher);
//        viewModel.approvePost("post-99");
//
//        verify(feedRepository).approvePost("class-42", "post-99");
//        verify(feedRepository).fetchPublishedPosts("class-42");
//        verify(feedRepository).fetchAnnouncements("class-42");
//        verify(feedRepository).fetchPendingPosts("class-42");
//        assertEquals(Integer.valueOf(1), viewModel.getRefreshTrigger().getValue());
//    }
//
//    @Test
//    public void rejectPost_withoutClassroom_doesNotCallRepository() {
//        // Parent never sets a classroom id, so reject should return immediately.
//        Parent parent = new Parent("p1", "Pat", "Parent", "pat@test.com", Collections.emptyList());
//
//        viewModel.loadPostsForUser(parent);
//        viewModel.rejectPost("post-12");
//
//        verify(feedRepository, never()).rejectPost(any(), any());
//        assertEquals(Integer.valueOf(0), viewModel.getRefreshTrigger().getValue());
//    }
//
//    private FeedViewModel createViewModelWithoutConstructor() throws Exception {
//        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
//        unsafeField.setAccessible(true);
//        Unsafe unsafe = (Unsafe) unsafeField.get(null);
//        return (FeedViewModel) unsafe.allocateInstance(FeedViewModel.class);
//    }
//
//    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
//        Field field = FeedViewModel.class.getDeclaredField(fieldName);
//        field.setAccessible(true);
//        field.set(target, value);
//    }
//}