package com.example.eduview.ui.profile;

import android.graphics.Bitmap;

import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;

//TODO: Implement feature for ParentState
public class ProfileUIState {

    public final String displayName;
    public final String roleText;
    public final int profilePictureResId;

    // -------- FEATURES (nullable = inactive) -------- //
    public final StudentProfileState studentState;
    public final TeacherProfileState teacherState;
    //public final ParentProfileState parentState;

    public ProfileUIState(
            String displayName,
            String roleText,
            int profilePictureResId,
            StudentProfileState studentState,
            TeacherProfileState teacherState
            //ParentProfileState parentState
    ) {
        this.displayName = displayName;
        this.roleText = roleText;
        this.profilePictureResId = profilePictureResId;
        this.studentState = studentState;
        this.teacherState = teacherState;
        //this.parentState = parentState;
    }

}