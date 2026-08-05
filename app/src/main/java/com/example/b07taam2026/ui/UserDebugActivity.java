package com.example.b07taam2026.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b07taam2026.R;
import com.example.b07taam2026.adapter.UserDebugAdapter;
import com.example.b07taam2026.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UserDebugActivity extends AppCompatActivity {

    private DatabaseReference usersRef;
    private UserDebugAdapter adapter;
    private TextView textUserCount;
    private TextView textEmpty;
    private List<User> userList;

    // set up user list and load users
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_debug);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        textUserCount = findViewById(R.id.textUserCount);
        textEmpty = findViewById(R.id.textEmpty);
        RecyclerView recycler = findViewById(R.id.recyclerUsers);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new UserDebugAdapter(userList);
        recycler.setAdapter(adapter);

        loadUsers();
    }

    // load users from firebase db
    private void loadUsers() {
        try {
            usersRef = FirebaseDatabase
                    .getInstance("https://taam-artifact-management-default-rtdb.firebaseio.com")
                    .getReference("users"); //users key
        } catch (Exception e) {
            Toast.makeText(this, "Firebase not available", Toast.LENGTH_LONG).show();
            textEmpty.setVisibility(View.VISIBLE);
            textEmpty.setText("Firebase not available");
            return;
        }

        // updates to db
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user != null) {
                        user.setUid(child.getKey());
                        userList.add(user);
                    }
                }
                adapter.submitList(new ArrayList<>(userList));

                int count = userList.size();
                textUserCount.setText(getString(R.string.debug_users_count, count));
                textEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDebugActivity.this,
                        "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
                textEmpty.setVisibility(View.VISIBLE);
                textEmpty.setText(error.getMessage());
            }
        });
    }
}
