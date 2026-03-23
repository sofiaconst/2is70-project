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
import com.example.eduview.data.model.FeedType;
import com.example.eduview.data.model.ProfilePicture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<FeedItem> items = new ArrayList<>();
    private final FeedViewModel feedViewModel;

    public FeedAdapter(FeedViewModel feedViewModel) {
        this.feedViewModel = feedViewModel;
    }

    public void setItems(List<FeedItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType().ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == FeedType.POST.ordinal()) {
            View view = inflater.inflate(R.layout.item_post, parent, false);
            return new PostViewHolder(view);

        } else if (viewType == FeedType.ANNOUNCEMENT.ordinal()) {
            View view = inflater.inflate(R.layout.item_announcement, parent, false);
            return new AnnouncementViewHolder(view);

        } else {
            View view = inflater.inflate(R.layout.item_pending, parent, false);
            return new PendingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedItem item = items.get(position);

        switch (item.getType()) {
            case POST:
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

    private void bindPost(@NonNull PostViewHolder holder, @NonNull FeedItem item) {
        holder.tvStudentName.setText(item.getAuthorName());
        holder.tvPostContent.setText(item.getContent());
        holder.tvPostDate.setText(formatTimestamp(item.getTimestamp()));
        setProfilePicture(holder.ivProfilePicture, item.getAuthorPfpName());

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

    private void bindAnnouncement(@NonNull AnnouncementViewHolder holder, @NonNull FeedItem item) {
        holder.tvAnnouncementAuthor.setText(item.getAuthorName());
        holder.tvAnnouncementContent.setText(item.getContent());
        holder.tvAnnouncementDate.setText(formatTimestamp(item.getTimestamp()));
        setProfilePicture(holder.ivProfilePicture, item.getAuthorPfpName());

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

    private void bindPending(@NonNull PendingViewHolder holder, @NonNull FeedItem item) {
        holder.tvPendingName.setText(item.getAuthorName());
        holder.tvPendingContent.setText(item.getContent());
        holder.tvPendingDate.setText(formatTimestamp(item.getTimestamp()));
        setProfilePicture(holder.ivProfilePicture, item.getAuthorPfpName());

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

        holder.btnApprove.setOnClickListener(v -> {
            if (item.getPostId() != null) {
                feedViewModel.approvePost(item.getPostId());
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (item.getPostId() != null) {
                feedViewModel.rejectPost(item.getPostId());
            }
        });
    }

    private void setProfilePicture(@NonNull View profileView, String profilePictureName) {
        ProfilePicture profilePicture;

        if (profilePictureName == null) {
            profilePicture = ProfilePicture.DEFAULT;
        } else {
            try {
                profilePicture = ProfilePicture.valueOf(profilePictureName);
            } catch (IllegalArgumentException e) {
                profilePicture = ProfilePicture.DEFAULT;
            }
        }

        profileView.setBackgroundResource(profilePicture.getDrawableId());
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return DateFormat.format("dd MMM yyyy, HH:mm", new Date(timestamp)).toString();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName;
        TextView tvPostContent;
        TextView tvPostDate;
        ImageView ivPostImage;
        View ivProfilePicture;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvPostContent = itemView.findViewById(R.id.tvPostContent);
            tvPostDate = itemView.findViewById(R.id.tvPostDate);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivProfilePicture = itemView.findViewById(R.id.pfp);
        }
    }

    static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView tvAnnouncementAuthor;
        TextView tvAnnouncementContent;
        ImageView ivAnnouncementImage;
        TextView textViewRole;
        TextView tvAnnouncementDate;
        View ivProfilePicture;

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

    static class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPendingName;
        TextView tvPendingContent;
        TextView tvPendingDate;
        ImageView ivPendingImage;
        ImageButton btnApprove;
        ImageButton btnReject;
        View ivProfilePicture;

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