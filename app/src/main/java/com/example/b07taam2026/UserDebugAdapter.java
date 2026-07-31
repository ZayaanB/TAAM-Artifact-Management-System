package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserDebugAdapter extends RecyclerView.Adapter<UserDebugAdapter.UserViewHolder> {

    private List<User> users;

    public UserDebugAdapter(List<User> users) {
        this.users = users;
    }

    public void submitList(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_user_debug, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.textUsername.setText(user.getUsername() != null ? user.getUsername() : "—");
        holder.textEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
        holder.textUid.setText(user.getUid() != null ? user.getUid() : "—");

        String role = user.getRole() != null ? user.getRole() : "user";
        holder.textRole.setText(role);
        int badgeColor = ContextCompat.getColor(holder.itemView.getContext(),
                "admin".equals(role) ? R.color.antique_gold : R.color.jade);
        holder.textRole.setBackgroundColor(badgeColor);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textEmail, textRole, textUid;

        UserViewHolder(View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textEmail = itemView.findViewById(R.id.textEmail);
            textRole = itemView.findViewById(R.id.textRole);
            textUid = itemView.findViewById(R.id.textUid);
        }
    }
}
