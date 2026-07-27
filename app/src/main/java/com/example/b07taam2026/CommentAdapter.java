package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    private final List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
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

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textAuthor;
        private final TextView textTime;
        private final TextView textBody;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textAuthor = itemView.findViewById(R.id.textCommentAuthor);
            textTime = itemView.findViewById(R.id.textCommentTime);
            textBody = itemView.findViewById(R.id.textCommentText);
        }

        void bind(Comment comment) {
            textAuthor.setText(comment.getAuthor());
            textTime.setText(DATE_FORMAT.format(new Date(comment.getTimestamp())));
            textBody.setText(comment.getText());
        }
    }
}
