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

/**
 * Adapter for displaying selectable profile pictures in a grid.
 * Uses an enum of ProfilePicture values and notifies a listener when one is selected.
 */
public class PfpAdapter extends RecyclerView.Adapter<PfpAdapter.PfpViewHolder> {

    /**
     * Listener interface for handling profile picture selection.
     */
    public interface OnPfpClickListener {

        /**
         * Called when the profile picture is clicked.
         *
         * @param pfp the profile picture
         */
        void onPfpClick(ProfilePicture pfp);
    }

    private final List<ProfilePicture> pfps = Arrays.asList(ProfilePicture.values());
    private final OnPfpClickListener listener;

    /**
     * Creates the adapter with a click listener.
     *
     * @param listener callback for when a profile picture is selected
     */
    public PfpAdapter(OnPfpClickListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates the profile picture item view.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return new ViewHolder instance
     */
    @NonNull
    @Override
    public PfpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pfp_choice, parent, false);
        return new PfpViewHolder(view);
    }

    /**
     * Binds a profile picture to the ViewHolder.
     *
     * @param holder ViewHolder instance
     * @param position position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull PfpViewHolder holder, int position) {
        holder.bind(pfps.get(position));
    }

    /**
     * Returns total number of profile pictures.
     *
     * @return number of items
     */
    @Override
    public int getItemCount() {
        return pfps.size();
    }

    /**
     * ViewHolder for displaying a single profile picture.
     */
    class PfpViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView ivPfp;

        /**
         * Initializes the ViewHolder.
         *
         * @param itemView item layout view
         */
        public PfpViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPfp = itemView.findViewById(R.id.ivPfpChoice);
        }

        /**
         * Binds a profile picture and sets click behavior.
         *
         * @param pfp profile picture to display
         */
        void bind(ProfilePicture pfp) {
            ivPfp.setImageResource(pfp.getDrawableId());
            itemView.setOnClickListener(v -> listener.onPfpClick(pfp));
        }
    }
}
