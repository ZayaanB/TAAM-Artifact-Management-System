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
        void onEditClicked(User user);
    }
    private List<User> adminList;
    private final Listener listener;
    private final AdminFilter filter = new AdminFilter(this::notifyDataSetChanged);
    private final String currentUid;

    public ManageAdminsAdapter(List<User> adminList, Listener listener, String currentUid) {
        this.filter.submit(adminList);
        this.listener = listener;
        this.currentUid = currentUid;
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
        TextView textName, textEmail, textRole;
        ImageButton buttonRemove;
        ImageButton buttonEdit;


        ManageViewHolder(@NonNull View itemView){
            super(itemView);
            textName = itemView.findViewById(R.id.textRowName);
            textEmail = itemView.findViewById(R.id.textRowEmail);
            textRole = itemView.findViewById(R.id.textRowAdminType);
            buttonRemove = itemView.findViewById(R.id.buttonRowRemove);
            buttonEdit = itemView.findViewById(R.id.buttonRowEdit);
        }
        void bind(User user){
            textName.setText(user.getUsername());
            textEmail.setText(user.getEmail() != null ? ("Email: " + user.getEmail()) : "Email Not found");

            String role = user.getRole();
            if("admin_m".equals(role)){
                textRole.setText("Manager");
            } else {
                textRole.setText("Admin");
            }

            if (user.getUid() != null && user.getUid().equals(currentUid)){
                buttonEdit.setVisibility(View.GONE);
                buttonRemove.setVisibility(View.GONE);
                textName.setText(user.getUsername() + " (You)");
            } else {
                buttonEdit.setVisibility(View.VISIBLE);
                buttonRemove.setVisibility(View.VISIBLE);
            }

            buttonRemove.setOnClickListener(v -> {
                listener.onRemoveClicked(user);
            });

            buttonEdit.setOnClickListener(v -> {
                listener.onEditClicked(user);
            });
        }
    }
}
