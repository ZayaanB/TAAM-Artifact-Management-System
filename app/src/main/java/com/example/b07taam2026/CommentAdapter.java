package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Collections;
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
    private boolean isAdmin;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void setUserContext(String uid, String lotNumber, CommentManager commentManager, boolean isAdmin) {
        this.uid = uid;
        this.lotNumber = lotNumber;
        this.commentManager = commentManager;
        this.isAdmin = isAdmin;
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
        final TextView textAuthor, textTime, textBody;
        final ImageButton buttonLike, buttonDelete;
        final TextView textLikeCount;
        final Button buttonReply, buttonSubmitReply;
        final EditText editReplyText;
        final LinearLayout layoutReplyInput, layoutReplies;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textAuthor = itemView.findViewById(R.id.textCommentAuthor);
            textTime = itemView.findViewById(R.id.textCommentTime);
            textBody = itemView.findViewById(R.id.textCommentText);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteComment);
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
            textLikeCount.setVisibility(comment.getLikeCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            buttonLike.setOnClickListener(v -> {
                if (commentManager != null && uid != null && lotNumber != null)
                    commentManager.toggleLike(lotNumber, comment.getId(), uid, liked);
            });

            // admin delete
            buttonDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            buttonDelete.setOnClickListener(v -> confirmDelete(() -> {
                if (commentManager != null && lotNumber != null) {
                    commentManager.deleteComment(lotNumber, comment.getId());
                }
            }));

            // Reply toggle
            buttonReply.setOnClickListener(v -> {
                boolean visible = layoutReplyInput.getVisibility() != View.VISIBLE;
                layoutReplyInput.setVisibility(visible ? View.VISIBLE : View.GONE);
                if (visible) editReplyText.requestFocus();
            });

            // Submit reply — use comment.getAuthor() to avoid "TFix" issue
            buttonSubmitReply.setOnClickListener(v -> {
                String text = editReplyText.getText().toString().trim();
                if (text.isEmpty()) return;
                if (commentManager != null && lotNumber != null)
                    commentManager.addReply(lotNumber, comment.getId(), comment.getAuthor(), text);
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

                    ((TextView) replyView.findViewById(R.id.textCommentAuthor))
                            .setText(entry.getValue().getAuthor());
                    ((TextView) replyView.findViewById(R.id.textCommentTime))
                            .setText(DATE_FORMAT.format(new Date(entry.getValue().getTimestamp())));
                    ((TextView) replyView.findViewById(R.id.textCommentText))
                            .setText(entry.getValue().getText());

                    replyView.findViewById(R.id.buttonLike).setVisibility(View.GONE);
                    replyView.findViewById(R.id.buttonReply).setVisibility(View.GONE);
                    replyView.findViewById(R.id.layoutReplyInput).setVisibility(View.GONE);
                    replyView.findViewById(R.id.layoutReplies).setVisibility(View.GONE);

                    View replyDelete = replyView.findViewById(R.id.buttonDeleteComment);
                    if (isAdmin) {
                        replyDelete.setVisibility(View.VISIBLE);
                        replyDelete.setOnClickListener(v -> confirmDelete(() -> {
                            if (commentManager != null && lotNumber != null) {
                                commentManager.deleteReply(lotNumber, comment.getId(), entry.getKey());
                            }
                        }));
                    }

                    layoutReplies.addView(replyView);
                }
            }
        }

        // confirm before removing a comment
        private void confirmDelete(Runnable onConfirm) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle(R.string.delete_comment_dialog_title)
                    .setMessage(R.string.delete_comment_dialog_message)
                    .setPositiveButton(R.string.delete_dialog_confirm, (dialog, which) -> {
                        onConfirm.run();
                        Toast.makeText(itemView.getContext(), R.string.toast_comment_deleted, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(R.string.delete_dialog_cancel, null)
                    .show();
        }

        private int dp(int dp) {
            return (int) (dp * itemView.getResources().getDisplayMetrics().density + 0.5f);
        }
    }
}
