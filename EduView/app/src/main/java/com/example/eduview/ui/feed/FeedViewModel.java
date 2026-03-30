package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.FeedRepository;
import com.example.eduview.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel responsible for managing feed data for different user roles.
 * Handles loading posts, announcements, pending posts,
 * and refresh or moderation actions.
 */
public class FeedViewModel extends ViewModel {

    // Repositories
    private FeedRepository feedRepository;
    private UserRepository userRepository;

    // Feed Items
    private final MutableLiveData<List<FeedItem>> publishedPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FeedItem>> pendingPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Student>> parentChildren = new MutableLiveData<>(new ArrayList<>());

    // Refresh
    private final MutableLiveData<Integer> refreshTrigger = new MutableLiveData<>(0);

    private String classroomId;
    private User currentUser;

    /**
     * Creates a FeedViewModel with default repository implementations.
     */
    public FeedViewModel() {
        this(new FeedRepository(), new UserRepository());
    }

    /**
     * Creates a FeedViewModel with injected repositories.
     *
     * @param feedRepository repository used for feed-related operations
     * @param userRepository repository used for user-related operations
     */
    public FeedViewModel(FeedRepository feedRepository, UserRepository userRepository) {
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
    }

    /**
     * Determines and stores the classroom ID for the given user.
     * Teachers and students are linked to a classroom, parents are not.
     *
     * @param user current user for whom feed data should be loaded
     */
    public void loadPostsForUser(User user) {
        if (user == null) return;
        currentUser = user;
        classroomId = null;

        if (user instanceof Student) {
            classroomId = ((Student) user).getClassId();
        } else if (user instanceof Teacher) {
            classroomId = ((Teacher) user).getClassId();
        }

        Log.d("FeedViewModel", "Class id found for user: " + classroomId);
        
        if (classroomId != null && !classroomId.isEmpty()) {
            loadPublishedPosts();
            loadAnnouncements();
            if (user instanceof Teacher) {
                loadPendingPosts();
            }
        }
    }

    /**
     * Loads the children associated with a parent user.
     * Only children who are assigned to a classroom are included.
     *
     * @param parent parent whose children should be fetched
     */
    public void loadChildrenForParent(Parent parent) {
        List<String> childIds = parent.getChildrenIDs();

        if (childIds == null || childIds.isEmpty()) {
            parentChildren.setValue(new ArrayList<>());
            return;
        }

        List<Student> children = new ArrayList<>();
        final int[] remaining = {childIds.size()};

        for (String childId : childIds) {
            userRepository.getUserById(childId, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user instanceof Student) {
                        Student student = (Student) user;

                        Log.d("FeedViewModel", "Parent child fetched: "
                                + student.getFirstName()
                                + " | userId=" + student.getUserId()
                                + " | classId=" + student.getClassId());

                        if (student.getClassId() != null && !student.getClassId().trim().isEmpty()) {
                            children.add(student);
                        } else {
                            Log.d("FeedViewModel", "Skipping child without classroom: " + student.getFirstName());
                        }
                    }

                    remaining[0]--;
                    if (remaining[0] == 0) {
                        parentChildren.setValue(children);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("FeedViewModel", "Failed to fetch child user", e);
                    remaining[0]--;
                    if (remaining[0] == 0) {
                        parentChildren.setValue(children);
                    }
                }
            });
        }
    }

    /**
     * Loads published posts for the current classroom.
     */
    public void loadPublishedPosts() {
        if (classroomId == null) return;
        feedRepository.fetchPublishedPosts(classroomId).observeForever(items -> {
            if (items != null) publishedPosts.setValue(items);
        });
    }

    /**
     * Loads announcements for the current classroom.
     */
    public void loadAnnouncements() {
        if (classroomId == null) return;
        feedRepository.fetchAnnouncements(classroomId).observeForever(items -> {
            if (items != null) announcements.setValue(items);
        });
    }

    /**
     * Loads pending posts for the current classroom.
     */
    public void loadPendingPosts() {
        if (classroomId == null) return;
        feedRepository.fetchPendingPosts(classroomId).observeForever(items -> {
            if (items != null) pendingPosts.setValue(items);
        });
    }

    /**
     * Returns the published posts LiveData.
     *
     * @return LiveData containing published posts
     */
    public LiveData<List<FeedItem>> getPublishedPosts() {
        return publishedPosts;
    }

    /**
     * Returns the announcements LiveData.
     *
     * @return LiveData containing announcements
     */
    public LiveData<List<FeedItem>> getAnnouncements() {
        return announcements;
    }

    /**
     * Returns the pending posts LiveData.
     *
     * @return LiveData containing pending posts
     */
    public LiveData<List<FeedItem>> getPendingPosts() {
        return pendingPosts;
    }

    /**
     * Returns the list of children associated with the current parent.
     *
     * @return LiveData containing parent children
     */
    public LiveData<List<Student>> getParentChildren() {
        return parentChildren;
    }

    /**
     * Returns a trigger value used to notify observers of manual refresh events.
     *
     * @return LiveData refresh counter
     */
    public LiveData<Integer> getRefreshTrigger() {
        return refreshTrigger;
    }

    /**
     * Reloads all feed data for the current user.
     * Teachers reload published, announcement, and pending posts.
     * Students reload published and announcement posts.
     * Parents do not reload classroom feed posts.
     */
    public void reloadAll() {
        Log.d("FeedViewModel", "Reloading all feed data");

        if (currentUser == null) {
            Log.e("FeedViewModel", "Cannot reload, currentUser is null");
            return;
        }

        loadPostsForUser(currentUser);

        Integer currentValue = refreshTrigger.getValue();
        if (currentValue == null) currentValue = 0;
        refreshTrigger.setValue(currentValue + 1);
    }

    /**
     * Approves a pending post for the current classroom and refreshes the feed.
     *
     * @param postId ID of the post to approve
     */
    public void approvePost(String postId) {
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedViewModel", "Cannot approve post, classroomId is null");
            return;
        }

        feedRepository.approvePost(classroomId, postId);
        reloadAll();
    }

    /**
     * Rejects a pending post for the current classroom and refreshes the feed.
     *
     * @param postId ID of the post to reject
     */
    public void rejectPost(String postId) {
        if (classroomId == null || classroomId.isEmpty()) {
            Log.e("FeedViewModel", "Cannot reject post, classroomId is null");
            return;
        }

        feedRepository.rejectPost(classroomId, postId);
        reloadAll();
    }
}
