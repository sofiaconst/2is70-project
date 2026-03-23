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

public class ParentProfileFeature {

    private View root;

    //--------------------- MY CHILDREN -----------//
    private RecyclerView rvChildren;
    private ChildAdapter adapter;
    private MaterialCardView cardMyChildren;

    public ParentProfileFeature(View root, ChildAdapter adapter) {
        this.root = root;
        root.findViewById(R.id.Teacher_Class_Text).setVisibility(View.VISIBLE);
        root.findViewById(R.id.mcvClassQRCode).setVisibility(View.GONE);

        this.rvChildren = root.findViewById(R.id.rvChildren);
        this.cardMyChildren = root.findViewById(R.id.cardMyChildren);
        this.adapter = adapter;
    }

    public void bind(ProfileUIState uiState) {
        if (uiState == null || uiState.parentState == null) {
            reset();
            return;
        }

        ParentProfileState state = uiState.parentState;
        Context context = root.getContext();
        reset();

        cardMyChildren.setVisibility(View.VISIBLE);

        if (state.isLoading()) {
            return;
        }

        if (state.getErrorMessage() != null) {
            Toast.makeText(context, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        if (state.getChildren() != null && !state.getChildren().isEmpty()) {
            rvChildren.setVisibility(View.VISIBLE);
            adapter.setChildren(state.getChildren());
        } else {
            rvChildren.setVisibility(View.GONE);
        }
    }

    private void reset() {
        cardMyChildren.setVisibility(View.GONE);
        rvChildren.setVisibility(View.GONE);
    }
}
