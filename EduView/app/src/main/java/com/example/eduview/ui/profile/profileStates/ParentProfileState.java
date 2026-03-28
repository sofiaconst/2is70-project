package com.example.eduview.ui.profile.profileStates;

import com.example.eduview.data.model.Student;
import java.util.List;

/**
 * Represents the UI state of a parent's profile screen.
 * Contains information like the children list and their classes.
 */
public class ParentProfileState {

    private final List<Student> children;
    private final boolean isLoading;
    private final String errorMessage;

    /**
     * Creates a new state view for the parent profile screen.
     * @param children a list of children (Student Users)
     * @param isLoading whether data is currently loading
     * @param errorMessage error message if an error occurred, otherwise null
     */
    public ParentProfileState(List<Student> children, boolean isLoading, String errorMessage) {
        this.children = children;
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
    }

    /**
     * Indicates whether the profile data is currently loading.
     * @return true if loading, false otherwise
     */
    public boolean isLoading() { return isLoading; }

    /**
     * Returns the list of children of the parent.
     * @return list of children
     */
    public List<Student> getChildren() { return children; }

    /**
     * Returns the error message if an error occurred.
     * @return error message or null if no error
     */
    public String getErrorMessage() { return errorMessage; }

    // Helpers

    /**
     * Creates a loading state.
     */
    public static ParentProfileState loading() {
        return new ParentProfileState(null, true, null);
    }

    /**
     * Creates a success state with loaded data.
     */
    public static ParentProfileState success(List<Student> children) {
        return new ParentProfileState(children, false, null);
    }

    /**
     * Creates an error state with a message.
     */
    public static ParentProfileState error(String msg) {
        return new ParentProfileState(null, false, msg);
    }
}

