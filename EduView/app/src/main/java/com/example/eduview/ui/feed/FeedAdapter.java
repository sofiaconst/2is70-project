package com.example.eduview.ui.feed;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eduview.R;
import com.example.eduview.data.model.FeedItem;
import com.example.eduview.data.model.FeedItemType;
import com.example.eduview.data.model.ProfilePicture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Uses the information on posts from the database (through the FeedViewModel) to fill the
 * RecyclerView with published posts, announcements or pending posts.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<FeedItem> items = new ArrayList<>();
    private final FeedViewModel feedViewModel;

    /**
     * Creates a new FeedAdapter with an instance of FeedViewModel.
     * @param feedViewModel the link between the fragment and repository
     */
    public FeedAdapter(FeedViewModel feedViewModel) {
        this.feedViewModel = feedViewModel;
    }

    /**
     * Adds all FeedItems into the ArrayList initialized.
     * @param newItems list of FeedItems
     */
    public void setItems(List<FeedItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * Gets the FeedItemType for the given position in the ArrayList.
     * @param position position to query
     * @return the FeedItemType for the given position in the ArrayList
     */
    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType().ordinal();
    }

    /**
     * Determines the ViewHolder to be used according to the FeedItemType.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return the type of ViewHolder which is going to be used
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == FeedItemType.PUBLISHED.ordinal()) {
            // Inflate layout for published posts
            View view = inflater.inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);

        } else if (viewType == FeedItemType.ANNOUNCEMENT.ordinal()) {
            // Inflate layout for announcements
            View view = inflater.inflate(R.layout.item_announcement, parent, false);
            return new AnnouncementViewHolder(view);

        } else {
            // Inflate layout for pending
            View view = inflater.inflate(R.layout.item_pending, parent, false);
            return new PendingViewHolder(view);
        }
    }

    /**
     * Determines the type of FeedItem that the components are bound to according to the
     * FeedItemType.
     * @param holder ViewHolder class
     * @param position position of the item that is going to be bound
     *
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedItem item = items.get(position);

        switch (item.getType()) {
            case PUBLISHED:
                bindPost((PostViewHolder) holder, item);
                break;

            case ANNOUNCEMENT:
                bindAnnouncement((AnnouncementViewHolder) holder, item);
                break;

            case PENDING:
                bindPending((PendingViewHolder) holder, item);
                break;
        }
    }

    /**
     * Sets up a Post object with its components to be viewed in the RecyclerView.
     * @param holder ViewHolder for post
     * @param item FeedItem that is going to be bound with post components
     */
    private void bindPost(@NonNull PostViewHolder holder, @NonNull FeedItem item) {
        holder.tvStudentName.setText(item.getAuthorName());
        holder.tvPostContent.setText(item.getCaption());
        holder.tvPostDate.setText(formatTimestamp(item.getTimestamp()));
        setProfilePicture(holder.ivProfilePicture, item.getAuthorPfpName());

        // Assigns the image from Cloudinary and the image URL to the post item.
        if (item.getImageUrl() == null || item.getImageUrl().isEmpty()) {
            holder.ivPostImage.setVisibility(View.GONE);
        } else {
            Log.d("FeedAdapter", "Url " + item.getImageUrl());
            holder.ivPostImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .dontAnimate()
                    .centerCrop()
                    .into(holder.ivPostImage);
        }
    }

    /**
     * Sets up a Announcement object with its components to be viewed in the RecyclerView.
     * @param holder ViewHolder for post
     * @param item FeedItem that is going to be bound with post components
     */
    private void bindAnnouncement(@NonNull AnnouncementViewHolder holder, @NonNull FeedItem item) {
        holder.tvAnnouncementAuthor.setText(item.getAuthorName());
        holder.tvAnnouncementContent.setText(item.getCaption());
        holder.tvAnnouncementDate.setText(formatTimestamp(item.getTimestamp()));
        setProfilePicture(holder.ivProfilePicture, item.getAuthorPfpName());

        // Assigns the image from Cloudinary and the image URL to the announcement item.
        if (item.getImageUrl() == null || item.getImageUrl().isEmpty()) {
            holder.ivAnnouncementImage.setVisibility(View.GONE);
        } else {
            holder.ivAnnouncementImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .dontAnimate()
                    .centerCrop()
                    .into(holder.ivAnnouncementImage);
        }
    }

    /**
     * Sets up a Pending object with its components to be viewed in the RecyclerView.
     * @param holder ViewHolder for post
     * @param item FeedItem that is going to be bound with post components
     */
    private void bindPending(@NonNull PendingViewHolder holder, @NonNull FeedItem item) {
        holder.tvPendingName.setText(item.getAuthorName());
        holder.tvPendingContent.setText(item.getCaption());
        holder.tvPendingDate.setText(formatTimestamp(item.getTimestamp()));
        setProfilePicture(holder.ivProfilePicture, item.getAuthorPfpName());

        // Assigns the image from Cloudinary and the image URL to the pending item.
        if (item.getImageUrl() == null || item.getImageUrl().isEmpty()) {
            holder.ivPendingImage.setVisibility(View.GONE);
        } else {
            holder.ivPendingImage.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .dontAnimate()
                    .centerCrop()
                    .into(holder.ivPendingImage);
        }
        // Sets a listener for whether the approve button is pressed.
        holder.btnApprove.setOnClickListener(v -> {
            if (item.getPostId() != null) {
                feedViewModel.approvePost(item.getPostId());
            }
        });

        // Sets a listener for whether the reject button is pressed.
        holder.btnReject.setOnClickListener(v -> {
            if (item.getPostId() != null) {
                feedViewModel.rejectPost(item.getPostId());
            }
        });
    }

    /**
     * Assigns the profile view according to the name.
     * @param profileView the view component of the profile
     * @param profilePictureName the name of the profile picture that the user currently uses
     */
    private void setProfilePicture(@NonNull View profileView, String profilePictureName) {
        ProfilePicture profilePicture;

        if (profilePictureName == null) {
            profilePicture = ProfilePicture.DEFAULT;
        } else {
            try {
                // Convert the stored string into the corresponding enum value
                profilePicture = ProfilePicture.valueOf(profilePictureName);
            } catch (IllegalArgumentException e) {
                profilePicture = ProfilePicture.DEFAULT;
            }
        }

        // Apply the resolved profile picture to the view
        profileView.setBackgroundResource(profilePicture.getDrawableId());
    }

    /**
     * Formats the timestamp given to dd/mm/yyyy, hh:mm.
     * @param timestamp the timestamp of when the feed item was created
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return DateFormat.format("dd MMM yyyy, HH:mm", new Date(timestamp)).toString();
    }

    /**
     * Returns the number of items in the items ArrayList
     * @return the number of items in the items ArrayList
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Keeps the components that creates a post.
     */
    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        TextView tvPostContent;
        TextView tvPostDate;
        ImageView ivPostImage;
        View ivProfilePicture;

        /**
         * Creates a ViewHolder for published posts by fetching the components of the item xml.
         * @param itemView the xml of the feed item
         */
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivProfilePicture = itemView.findViewById(R.id.pfp);
        }
    }

    /**
     * Keeps the components that creates an announcement.
     */
    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView tvAnnouncementAuthor;
        TextView tvAnnouncementContent;
        ImageView ivAnnouncementImage;
        TextView textViewRole;
        TextView tvAnnouncementDate;
        View ivProfilePicture;

        /**
         * Creates a ViewHolder for announcements by fetching the components of the item xml.
         * @param itemView the xml of the feed item
         */
        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAnnouncementAuthor = itemView.findViewById(R.id.tvAnnouncementAuthor);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            tvAnnouncementContent = itemView.findViewById(R.id.tvAnnouncementContent);
            tvAnnouncementDate = itemView.findViewById(R.id.tvAnnouncementDate);
            ivAnnouncementImage = itemView.findViewById(R.id.ivAnnouncementImage);
            ivProfilePicture = itemView.findViewById(R.id.pfp);
        }
    }

    /**
     * Keeps the components that creates a pending post.
     */
    static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPendingName;
        TextView tvPendingContent;
        TextView tvPendingDate;
        ImageView ivPendingImage;
        ImageButton btnApprove;
        ImageButton btnReject;
        View ivProfilePicture;

        /**
         * Creates a ViewHolder for pending posts by fetching the components of the item xml.
         * @param itemView the xml of the feed item
         */
        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPendingName = itemView.findViewById(R.id.tvPendingName);
            tvPendingContent = itemView.findViewById(R.id.tvPendingContent);
            tvPendingDate = itemView.findViewById(R.id.tvPendingDate);
            ivPendingImage = itemView.findViewById(R.id.PendingImage);
            btnApprove = itemView.findViewById(R.id.btnAcceptPost);
            btnReject = itemView.findViewById(R.id.btnRejectPost);
            ivProfilePicture = itemView.findViewById(R.id.pfp);
        }
    }
}