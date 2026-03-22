package com.example.eduview.ui.profile.profileStates;

import com.example.eduview.data.model.Student;
import java.util.List;

public class ParentProfileState {

    private final List<Student> children;
    private final boolean isLoading;
    private final String errorMessage;

    public ParentProfileState(List<Student> children, boolean isLoading, String errorMessage) {
        this.children = children;
        this.isLoading = isLoading;
        this.errorMessage = errorMessage;
    }

    public List<Student> getChildren() { return children; }
    public boolean isLoading() { return isLoading; }
    public String getErrorMessage() { return errorMessage; }

    // helpers
    public static ParentProfileState loading() {
        return new ParentProfileState(null, true, null);
    }

    public static ParentProfileState success(List<Student> children) {
        return new ParentProfileState(children, false, null);
    }

    public static ParentProfileState error(String msg) {
        return new ParentProfileState(null, false, msg);
    }
}

