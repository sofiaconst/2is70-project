package com.example.eduview.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.ProfilePicture;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Arrays;
import java.util.List;

public class PfpAdapter extends RecyclerView.Adapter<PfpAdapter.PfpViewHolder> {

    public interface OnPfpClickListener {
        void onPfpClick(ProfilePicture pfp);
    }

    private final List<ProfilePicture> pfps = Arrays.asList(ProfilePicture.values());
    private final OnPfpClickListener listener;

    public PfpAdapter(OnPfpClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PfpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pfp_choice, parent, false);
        return new PfpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PfpViewHolder holder, int position) {
        holder.bind(pfps.get(position));
    }

    @Override
    public int getItemCount() {
        return pfps.size();
    }

    class PfpViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivPfp;

        public PfpViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPfp = itemView.findViewById(R.id.ivPfpChoice);
        }

        void bind(ProfilePicture pfp) {
            ivPfp.setImageResource(pfp.getDrawableId());
            itemView.setOnClickListener(v -> listener.onPfpClick(pfp));
        }
    }
}
