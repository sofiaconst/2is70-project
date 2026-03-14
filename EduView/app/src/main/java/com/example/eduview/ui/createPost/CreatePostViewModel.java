package com.example.eduview.ui.createpost;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.eduview.data.model.Post;
import com.example.eduview.data.model.PostType;
import com.example.eduview.data.repository.PostRepository;

/**
 * ViewModel for the Create Post screen.
 *
 * Its main job is to remember what the user typed or selected
 * so the data survives screen rotation.
 *
 * It also sends the post to the repository when the user presses "Post".
 */
public class CreatePostViewModel extends ViewModel {

    private final PostRepository postRepository;

    // What the user typed in the caption field
    private final MutableLiveData<String> caption = new MutableLiveData<>("");

    // URL of the image attached to the post (or empty if none)
    private final MutableLiveData<String> imageUrl = new MutableLiveData<>("");

    // Used by the Fragment to know when the post was successfully created
    private final MutableLiveData<Boolean> postCreated = new MutableLiveData<>(false);

    // Used to display an error message if something goes wrong
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    /**
     * Constructor.
     * Creates the repository that actually talks to Firebase through repository.
     */
    public CreatePostViewModel() {
        this.postRepository = new PostRepository();
    }

    /**
     * Allows the postFragment to create and store caption to refill upon screen rotation.
     */
    public LiveData<String> getCaption() {
        return caption;
    }

    /**
     * Allows the postFragment to create and store image URL to refill upon screen rotation.
     */
    public LiveData<String> getImageUrl() {
        return imageUrl;
    }

    /**
     * Lets the Fragment know when a post was successfully uploaded.
     * The Fragment can react by closing the screen or clearing inputs.
     */
    public LiveData<Boolean> getPostCreated() {
        return postCreated;
    }

    /**
     * Lets the Fragment know when something went wrong,
     * so it can show an error message to the user.
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Called when the user types in the caption box.
     * We store the value here so it survives screen rotation.
     */
    public void setCaption(String newCaption) {
        caption.setValue(newCaption != null ? newCaption : "");
    }

    /**
     * Called when the user adds an image.
     * For now this will store the image URL (later CameraX will provide it).
     */
    public void setImageUrl(String newImageUrl) {
        imageUrl.setValue(newImageUrl != null ? newImageUrl : "");
    }

    /**
     * Called when the user presses the "Post" button.
     *
     * This collects the caption and image URL currently stored in the ViewModel,
     * builds a Post object, and asks the repository to upload it to Firebase.
     */
    public void createPost(PostType type, String classId, String authorId) {

        String currentCaption = caption.getValue() != null ? caption.getValue().trim() : "";
        String currentImageUrl = imageUrl.getValue() != null ? imageUrl.getValue().trim() : "";

        // Prevent empty posts (must have text or an image)
        if (currentCaption.isEmpty() && currentImageUrl.isEmpty()) {
            errorMessage.setValue("Post must contain text or an image.");
            return;
        }

        Post post = new Post(authorId, currentImageUrl, currentCaption);

        postRepository.createPost(
                type,
                classId,
                post,
                postId -> postCreated.setValue(true),
                error -> errorMessage.setValue(error.getMessage())
        );
    }

    /**
     * After the Fragment reacts to a successful post,
     * it can reset this flag so it doesn't trigger again.
     */
    public void clearPostCreatedFlag() {
        postCreated.setValue(false);
    }

    /**
     * Clears the stored error after it has been shown to the user.
     */
    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
}