package com.example.b07taam2026;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManageAdminsAdapter extends RecyclerView.Adapter<ManageAdminsAdapter.ManageViewHolder> {

    public interface Listener {
        void onRemoveClicked(User user);
    }
    private List<User> adminList;
    private final Listener listener;
    private final AdminFilter filter = new AdminFilter(this::notifyDataSetChanged);

    public ManageAdminsAdapter(List<User> adminList, Listener listener) {
        this.filter.submit(adminList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_manage_admin, parent, false);
        return new ManageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageViewHolder holder, int position){
        holder.bind(filter.get(position));
    }

    @Override
    public int getItemCount(){
        return filter.size();
    }

    public void submitList(List<User> admins){
        filter.submit(admins);
    }

    public void setQuery(String q){
        filter.setQuery(q);
    }

    class ManageViewHolder extends RecyclerView.ViewHolder{
        TextView textName, textEmail;
        ImageButton buttonRemove;


        ManageViewHolder(@NonNull View itemView){
            super(itemView);
            textName = itemView.findViewById(R.id.textRowName);
            textEmail = itemView.findViewById(R.id.textRowEmail);
            buttonRemove = itemView.findViewById(R.id.buttonRowRemove);
        }
        void bind(User user){
            textName.setText(user.getUsername());
            textEmail.setText("Email: " + user.getEmail());
            buttonRemove.setOnClickListener(v -> {
                // TODO: Add code to remove user's admin perms from database
                listener.onRemoveClicked(user);
            });
        }
    }
}
