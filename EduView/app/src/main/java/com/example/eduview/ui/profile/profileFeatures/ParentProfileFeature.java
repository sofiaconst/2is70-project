package com.example.eduview.ui.profile.profileFeatures;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.ui.adapters.ChildAdapter;
import com.example.eduview.ui.adapters.StudentManagerAdapter;
import com.example.eduview.ui.profile.ProfileUIState;
import com.example.eduview.ui.profile.profileStates.ParentProfileState;
import com.example.eduview.ui.profile.profileStates.StudentProfileState;
import com.example.eduview.ui.profile.profileStates.TeacherProfileState;
import com.google.android.material.card.MaterialCardView;

/**
 * Handles the parent UI elements shown on the profile fragment.
 * Displays information on student classrooms, and student info where the student
 * is the parent's child.
 */
public class ParentProfileFeature {

    private View root;

    // My Children
    private RecyclerView rvChildren;
    private ChildAdapter adapter;
    private MaterialCardView cardMyChildren;

    /**
     * Creates the parent profile features and initializes all UI components for the parent.
     *
     * @param root root view containing the parent profile layout
     * @param adapter adapter to show children in the view
     */
    public ParentProfileFeature(View root, ChildAdapter adapter) {
        this.root = root;
        root.findViewById(R.id.Teacher_Class_Text).setVisibility(View.VISIBLE);
        root.findViewById(R.id.mcvClassQRCode).setVisibility(View.GONE);

        // Children list UI element initialization
        this.rvChildren = root.findViewById(R.id.rvChildren);
        this.cardMyChildren = root.findViewById(R.id.cardMyChildren);
        this.adapter = adapter;
    }

    /**
     * Updates the parent profile UI based on the provided profile.
     *
     * @param uiState complete profile UI state containing the parent state
     */
    public void bind(ProfileUIState uiState) {
        // Validating whether UI state is null
        if (uiState == null || uiState.parentState == null) {
            reset();
            return;
        }

        // Extract parent state and context for UI operations.
        ParentProfileState state = uiState.parentState;
        Context context = root.getContext();

        // Reset UI to a clean baseline before applying new state.
        reset();

        cardMyChildren.setVisibility(View.VISIBLE);

        // Loading state
        if (state.isLoading()) {
            return;
        }

        // Error State
        if (state.getErrorMessage() != null) {
            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetches the children of the parent
        if (state.getChildren() != null && !state.getChildren().isEmpty()) {
            rvChildren.setVisibility(View.VISIBLE);
            adapter.setChildren(state.getChildren());
        } else {
            rvChildren.setVisibility(View.GONE);
        }
    }

    /**
     * Hides all parent profile sections before showing a new state.
     */
    private void reset() {
        cardMyChildren.setVisibility(View.GONE);
        rvChildren.setVisibility(View.GONE);
    }
}
