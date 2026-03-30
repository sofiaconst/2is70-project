package com.example.eduview.ui.feed;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.Parent;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.model.Teacher;
import com.example.eduview.data.model.User;
import com.example.eduview.data.repository.ClassroomRepository;
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
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    // Feed items shown on screen
    private final MutableLiveData<List<FeedItem>> publishedPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FeedItem>> announcements = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FeedItem>> pendingPosts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Student>> parentChildren = new MutableLiveData<>(new ArrayList<>());

    // Refresh trigger used by the UI to react to manual reloads
    private final MutableLiveData<Integer> refreshTrigger = new MutableLiveData<>(0);

    private String classroomId;
    private User currentUser;

    // LiveData used to observe a student's classroom in real time
    private LiveData<String> liveStudentClassroom;
    private Observer<String> studentClassroomObserver;

    // Live feed sources currently attached to the ViewModel
    private LiveData<List<FeedItem>> livePublishedSource;
    private LiveData<List<FeedItem>> liveAnnouncementsSource;
    private LiveData<List<FeedItem>> livePendingSource;

    // Observers for the currently attached feed sources
    private Observer<List<FeedItem>> publishedObserver;
    private Observer<List<FeedItem>> announcementsObserver;
    private Observer<List<FeedItem>> pendingObserver;

    /**
     * Creates a FeedViewModel with default repository implementations.
     */
    public FeedViewModel() {
        this(new FeedRepository(), new UserRepository(), new ClassroomRepository());
    }

    /**
     * Creates a FeedViewModel with injected repositories.
     *
     * @param feedRepository repository used for feed-related operations
     * @param userRepository repository used for user-related operations
     * @param classroomRepository repository used for classroom-related operations
     */
    public FeedViewModel(FeedRepository feedRepository,
                         UserRepository userRepository,
                         ClassroomRepository classroomRepository) {
        this.feedRepository = feedRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    /**
     * Determines and stores the classroom ID for the given user.
     * Teachers and students are linked to a classroom, parents are not.
     *
     * For students, the classroom membership is observed in real time so the feed
     * can be cleared immediately when the student is removed from the classroom.
     *
     * @param user current user for whom feed data should be loaded
     */
    public void loadPostsForUser(User user) {
        if (user == null) return;

        currentUser = user;
        classroomId = null;

        // Remove old feed observers before attaching new ones.
        detachFeedListeners();

        if (user instanceof Student) {
            observeStudentClassroom((Student) user);
            return;
        } else if (user instanceof Teacher) {
            classroomId = ((Teacher) user).getClassId();
        }

        Log.d("FeedViewModel", "Class id found for user: " + classroomId);

        if (classroomId != null && !classroomId.isEmpty()) {
            attachFeedListeners();
        } else {
            clearFeed();
        }
    }

    /**
     * Starts observing the student's classroom field in real time.
     * When the classroom becomes empty, the feed is cleared immediately.
     * When the classroom changes, the feed listeners are reattached to the new class.
     *
     * @param student student whose classroom membership should be observed
     */
    private void observeStudentClassroom(Student student) {
        if (student == null || student.getUserId() == null) return;

        // Remove any previous classroom observer to avoid duplicate listeners.
        if (liveStudentClassroom != null && studentClassroomObserver != null) {
            liveStudentClassroom.removeObserver(studentClassroomObserver);
        }

        liveStudentClassroom = classroomRepository.getLiveStudentClassroom(student.getUserId());

        studentClassroomObserver = newClassroomId -> {
            Log.d("FeedViewModel", "Live student classroom changed: " + newClassroomId);

            // If the student no longer belongs to a class, remove all feed content immediately.
            if (newClassroomId == null || newClassroomId.trim().isEmpty()) {
                classroomId = null;
                detachFeedListeners();
                clearFeed();
                return;
            }

            // Only reattach if the classroom actually changed.
            if (!newClassroomId.equals(classroomId)) {
                classroomId = newClassroomId;
                detachFeedListeners();
                attachFeedListeners();
            }
        };

        liveStudentClassroom.observeForever(studentClassroomObserver);
    }

    /**
     * Attaches live feed listeners for the current classroom.
     * Published posts and announcements are loaded for students and teachers.
     * Pending posts are loaded only for teachers.
     */
    private void attachFeedListeners() {
        if (classroomId == null || classroomId.isEmpty()) return;

        // Observe published posts for the classroom.
        livePublishedSource = feedRepository.fetchPublishedPosts(classroomId);
        publishedObserver = items -> publishedPosts.setValue(items != null ? items : new ArrayList<>());
        livePublishedSource.observeForever(publishedObserver);

        // Observe announcements for the classroom.
        liveAnnouncementsSource = feedRepository.fetchAnnouncements(classroomId);
        announcementsObserver = items -> announcements.setValue(items != null ? items : new ArrayList<>());
        liveAnnouncementsSource.observeForever(announcementsObserver);

        // Only teachers should see pending posts.
        if (currentUser instanceof Teacher) {
            livePendingSource = feedRepository.fetchPendingPosts(classroomId);
            pendingObserver = items -> pendingPosts.setValue(items != null ? items : new ArrayList<>());
            livePendingSource.observeForever(pendingObserver);
        } else {
            pendingPosts.setValue(new ArrayList<>());
        }
    }

    /**
     * Removes the currently attached feed listeners.
     * This prevents the ViewModel from continuing to observe an old classroom feed.
     */
    private void detachFeedListeners() {
        if (livePublishedSource != null && publishedObserver != null) {
            livePublishedSource.removeObserver(publishedObserver);
            livePublishedSource = null;
            publishedObserver = null;
        }

        if (liveAnnouncementsSource != null && announcementsObserver != null) {
            liveAnnouncementsSource.removeObserver(announcementsObserver);
            liveAnnouncementsSource = null;
            announcementsObserver = null;
        }

        if (livePendingSource != null && pendingObserver != null) {
            livePendingSource.removeObserver(pendingObserver);
            livePendingSource = null;
            pendingObserver = null;
        }
    }

    /**
     * Clears all feed lists shown in the UI.
     * This is used when a user is not part of a classroom.
     */
    private void clearFeed() {
        publishedPosts.setValue(new ArrayList<>());
        announcements.setValue(new ArrayList<>());
        pendingPosts.setValue(new ArrayList<>());
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

                        // Only show children that are actually assigned to a classroom.
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
     * This method performs a one-time attachment of a live source to the ViewModel state.
     *
     * Prefer using the automatic classroom observer flow where possible.
     */
    public void loadPublishedPosts() {
        if (classroomId == null || classroomId.isEmpty()) return;

        livePublishedSource = feedRepository.fetchPublishedPosts(classroomId);
        publishedObserver = items -> {
            if (items != null) {
                publishedPosts.setValue(items);
            } else {
                publishedPosts.setValue(new ArrayList<>());
            }
        };
        livePublishedSource.observeForever(publishedObserver);
    }

    /**
     * Loads announcements for the current classroom.
     * This method performs a one-time attachment of a live source to the ViewModel state.
     *
     * Prefer using the automatic classroom observer flow where possible.
     */
    public void loadAnnouncements() {
        if (classroomId == null || classroomId.isEmpty()) return;

        liveAnnouncementsSource = feedRepository.fetchAnnouncements(classroomId);
        announcementsObserver = items -> {
            if (items != null) {
                announcements.setValue(items);
            } else {
                announcements.setValue(new ArrayList<>());
            }
        };
        liveAnnouncementsSource.observeForever(announcementsObserver);
    }

    /**
     * Loads pending posts for the current classroom.
     * This is intended for teacher users only.
     */
    public void loadPendingPosts() {
        if (classroomId == null || classroomId.isEmpty()) return;

        livePendingSource = feedRepository.fetchPendingPosts(classroomId);
        pendingObserver = items -> {
            if (items != null) {
                pendingPosts.setValue(items);
            } else {
                pendingPosts.setValue(new ArrayList<>());
            }
        };
        livePendingSource.observeForever(pendingObserver);
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

        if (!(currentUser instanceof Parent)) {
            loadPublishedPosts();
            loadAnnouncements();

            if (currentUser instanceof Teacher) {
                loadPendingPosts();
            }
        }

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

    /**
     * Called when the ViewModel is being destroyed.
     * Removes all observers to avoid leaks and stale listeners.
     */
    @Override
    protected void onCleared() {
        super.onCleared();

        if (liveStudentClassroom != null && studentClassroomObserver != null) {
            liveStudentClassroom.removeObserver(studentClassroomObserver);
        }

        detachFeedListeners();
    }
}