package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;

import com.example.socialtestingapp.adapter.AdapterUsers;
import com.example.socialtestingapp.model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PostLikeByActivity extends AppCompatActivity {
    String postId;
    RecyclerView recyclerView;
    List<ModelUser>userList;
    AdapterUsers adapterUsers;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_like_by);

        recyclerView=findViewById(R.id.recyclerlike);

        firebaseAuth=FirebaseAuth.getInstance();
        //actionbar
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Post Liked By");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());


        //get the postid
        Intent intent=getIntent();
        postId=intent.getStringExtra("postId");

        userList=new ArrayList<>();

        DatabaseReference ref= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
               userList.clear();
                for(DataSnapshot d:snapshot.getChildren()){

                    String hisUid=""+d.getRef().getKey();
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void getUsers(String hisUid) {
        DatabaseReference ref= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for(DataSnapshot d:snapshot.getChildren()){

                            userList.add(d.getValue(ModelUser.class));
                        }

                        adapterUsers=new AdapterUsers(PostLikeByActivity.this,userList);

                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}