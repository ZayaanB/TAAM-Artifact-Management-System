package com.example.b07taam2026.data;

import androidx.annotation.NonNull;
import com.example.b07taam2026.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

// firebase reads and writes for admin accounts
public class AdminManager {
    private final DatabaseReference usersRef;
    private ValueEventListener liveListener;
    private Query liveQuery;

    // connect to the users node
    public AdminManager() {
        FirebaseDatabase database = FirebaseDatabase
                .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com");
        usersRef = database.getReference("users");
    }
    public interface AdminCallback {
        void onResult(List<User> admins);
        void onError(String errorMessage);
    }

    public interface WriteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    // promote existing user to admin by email
    public void addAdmin(String email, WriteCallback callback, String role) {
        usersRef.orderByChild("email").equalTo(email).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onError("User not found");
                        return;
                    }
                    // get the role and update by UID
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String uid = userSnapshot.getKey();
                    usersRef.child(uid).child("role").setValue(role)
                            .addOnSuccessListener(a -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // demote admin back to regular user
    public void removeAdmin(String uid, WriteCallback callback) {
        // change admins role to user
        usersRef.child(uid).child("role").setValue("user")
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // change an admins role by uid
    public void updateAdminRole(String uid, String role, WriteCallback callback){
        usersRef.child(uid).child("role").setValue(role)
                .addOnSuccessListener(a -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // listen for live changes to admin users
    public void startLive(AdminCallback callback) {
        // Find all roles that start with admin (i.e. admin and admin_m)
        liveQuery = usersRef.orderByChild("role").startAt("admin").endAt("admin\uf8ff");
        liveListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> admins = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()){
                    User u = child.getValue(User.class);
                    if(u != null){
                        String role = u.getRole();
                        if("admin".equals(role) || "admin_m".equals(role)){
                            u.setUid(child.getKey());
                            admins.add(u);
                        }
                    }
                }
                callback.onResult(admins);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        };
        liveQuery.addValueEventListener(liveListener);
    }
    public void stopLive() {
        if (liveListener != null && liveQuery != null) {
            liveQuery.removeEventListener(liveListener);
        }
    }

}
