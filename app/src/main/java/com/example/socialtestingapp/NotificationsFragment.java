package com.example.socialtestingapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialtestingapp.adapter.AdaptetNotification;
import com.example.socialtestingapp.model.ModelNotifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    RecyclerView recyclerNoti;
    FirebaseAuth firebaseAuth;

    ArrayList<ModelNotifications>notifications;
    AdaptetNotification adaptetNotification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerNoti=view.findViewById(R.id.recyclerNoti);

        notifications=new ArrayList<>();
        firebaseAuth=FirebaseAuth.getInstance();
        getAllNotifications();

        return  view;
    }

    private void getAllNotifications() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        notifications.clear();
                        for(DataSnapshot d:snapshot.getChildren()){
                            //get data
                            ModelNotifications model=d.getValue(ModelNotifications.class);
                            notifications.add(model);
                        }
                        //adapter
                        adaptetNotification=new AdaptetNotification(getContext(),notifications);
                        //set to recyclerview
                        recyclerNoti.setAdapter(adaptetNotification);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


    }
}