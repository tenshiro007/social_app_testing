package com.example.socialtestingapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {
    private static final String TAG = "UserFragment";
    private RecyclerView recycerView;
    private AdapterUsers adapterUsers;
    private List<ModelUser>usersList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_user, container, false);

        initView(view);

        //get all user
        getAllUser();

        return view;
    }

    private void getAllUser() {
        //get current user
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        //get path of database name "Users" containing users info
        DatabaseReference reference= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        //get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUser modelUser=ds.getValue(ModelUser.class);


                    //get all users except currently signed in user
                    if(!modelUser.getUid().equals(user.getUid())){
                        usersList.add(modelUser);
                    }
                }
                Log.d(TAG, "onDataChange: allUser"+usersList);
                //adapter
                adapterUsers =new AdapterUsers(getContext(),usersList);
                //set adater to recyclerview
                recycerView.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initView(View view) {
        recycerView =view.findViewById(R.id.
                recyclerViewUser);
        //set it's properties
        recycerView.setHasFixedSize(true);
        recycerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //init user list
        usersList=new ArrayList<>();

    }
}