package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public enum SortMode { NEWEST, OLDEST }

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    private final List<Comment> comments;
    private SortMode sortMode = SortMode.NEWEST;
    private String uid;
    private String lotNumber;
    private CommentManager commentManager;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setUserContext(String uid, String lotNumber, CommentManager commentManager) {
        this.uid = uid;
        this.lotNumber = lotNumber;
        this.commentManager = commentManager;
    }

    public void setSortMode(SortMode sortMode) {
        this.sortMode = sortMode;
        sortAndNotify();
    }

    public void submitList(List<Comment> fresh) {
        comments.clear();
        comments.addAll(fresh);
        sortAndNotify();
    }

    private void sortAndNotify() {
        if (sortMode == SortMode.NEWEST) {
            Collections.sort(comments, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        } else {
            Collections.sort(comments, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_row, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        final TextView textAuthor;
        final TextView textTime;
        final TextView textBody;
        final ImageButton buttonLike;
        final TextView textLikeCount;
        final Button buttonReply;
        final LinearLayout layoutReplyInput;
        final EditText editReplyText;
        final Button buttonSubmitReply;
        final LinearLayout layoutReplies;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textAuthor = itemView.findViewById(R.id.textCommentAuthor);
            textTime = itemView.findViewById(R.id.textCommentTime);
            textBody = itemView.findViewById(R.id.textCommentText);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            buttonReply = itemView.findViewById(R.id.buttonReply);
            layoutReplyInput = itemView.findViewById(R.id.layoutReplyInput);
            editReplyText = itemView.findViewById(R.id.editReplyText);
            buttonSubmitReply = itemView.findViewById(R.id.buttonSubmitReply);
            layoutReplies = itemView.findViewById(R.id.layoutReplies);
        }

        void bind(Comment comment) {
            textAuthor.setText(comment.getAuthor());
            textTime.setText(DATE_FORMAT.format(new Date(comment.getTimestamp())));
            textBody.setText(comment.getText());

            // Like button
            boolean liked = comment.isLikedBy(uid);
            buttonLike.setImageResource(liked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            buttonLike.getDrawable().setTint(ContextCompat.getColor(itemView.getContext(),
                    liked ? R.color.cinnabar : R.color.muted_taupe));
            textLikeCount.setText(String.valueOf(comment.getLikeCount()));
            textLikeCount.setVisibility(comment.getLikeCount() > 0 ? View.VISIBLE : View.GONE);
            buttonLike.setOnClickListener(v -> {
                if (commentManager != null && uid != null && lotNumber != null) {
                    commentManager.toggleLike(lotNumber, comment.getId(), uid, liked);
                }
            });

            // Reply button
            buttonReply.setOnClickListener(v -> {
                boolean visible = layoutReplyInput.getVisibility() != View.VISIBLE;
                layoutReplyInput.setVisibility(visible ? View.VISIBLE : View.GONE);
                if (visible) editReplyText.requestFocus();
            });

            // Submit reply
            buttonSubmitReply.setOnClickListener(v -> {
                String text = editReplyText.getText().toString().trim();
                if (text.isEmpty()) return;
                if (commentManager != null && lotNumber != null) {
                    commentManager.addReply(lotNumber, comment.getId(), textAuthor.getText().toString(), text);
                }
                editReplyText.setText("");
                layoutReplyInput.setVisibility(View.GONE);
            });

            // Render replies
            layoutReplies.removeAllViews();
            Map<String, Comment> replies = comment.getReplies();
            if (replies != null && !replies.isEmpty()) {
                for (Map.Entry<String, Comment> entry : replies.entrySet()) {
                    View replyView = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.comment_row, layoutReplies, false);
                    replyView.setPadding(dp(8), 0, 0, 0);

                    TextView rAuthor = replyView.findViewById(R.id.textCommentAuthor);
                    TextView rTime = replyView.findViewById(R.id.textCommentTime);
                    TextView rBody = replyView.findViewById(R.id.textCommentText);
                    replyView.findViewById(R.id.buttonLike).setVisibility(View.GONE);
                    replyView.findViewById(R.id.textLikeCount).setVisibility(View.GONE);
                    replyView.findViewById(R.id.buttonReply).setVisibility(View.GONE);
                    replyView.findViewById(R.id.layoutReplyInput).setVisibility(View.GONE);
                    replyView.findViewById(R.id.layoutReplies).setVisibility(View.GONE);

                    Comment reply = entry.getValue();
                    if (reply != null) {
                        rAuthor.setText(reply.getAuthor());
                        rTime.setText(DATE_FORMAT.format(new Date(reply.getTimestamp())));
                        rBody.setText(reply.getText());
                    }
                    layoutReplies.addView(replyView);
                }
            }
        }

        private int dp(int dp) {
            return (int) (dp * itemView.getResources().getDisplayMetrics().density + 0.5f);
        }
    }
}
