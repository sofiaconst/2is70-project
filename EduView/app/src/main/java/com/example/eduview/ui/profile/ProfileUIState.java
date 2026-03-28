package com.example.eduview.ui.profile;

import android.graphics.Bitmap;

import com.example.eduview.ui.profile.profileStates.ParentProfileState;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;

/**
 * Represents the complete UI state for the profile screen.
 * Combines user information with states specific to roles (student, teacher, or parent).
 */
public class ProfileUIState {

    public final String displayName;
    public final String roleText;
    public final int profilePictureResId;

    public final StudentProfileState studentState;
    public final TeacherProfileState teacherState;
    public final ParentProfileState parentState;

    /**
     * Creates a complete profile UI state.
     *
     * @param displayName name to display in the UI
     * @param roleText role shown in the UI
     * @param profilePictureResId resource ID of the profile image
     * @param studentState state for student UI
     * @param teacherState state for teacher UI
     * @param parentState state for parent UI
     */
    public ProfileUIState(
            String displayName,
            String roleText,
            int profilePictureResId,
            StudentProfileState studentState,
            TeacherProfileState teacherState,
            ParentProfileState parentState
    ) {
        this.displayName = displayName;
        this.roleText = roleText;
        this.profilePictureResId = profilePictureResId;
        this.studentState = studentState;
        this.teacherState = teacherState;
        this.parentState = parentState;
    }

}