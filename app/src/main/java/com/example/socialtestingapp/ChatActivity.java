package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private CircularImageView profile;
    private TextView txtHisname,userStatus;
    private EditText message;
    private ImageButton btnSend;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbref;

    String hisUid;
    String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");

        initView();
        //init
        firebaseAuth=FirebaseAuth.getInstance();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit
                String msg=message.getText().toString();
                //check if text is empty or not
                if(TextUtils.isEmpty(msg)){
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                }else{
                    //text not empty
                    sendMessage(msg);
                }
            }
        });
    }

    private void sendMessage(String msg) {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",msg);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reset edittext
        message.setText("");
    }

    private void initView(){
        toolbar=findViewById(R.id.toolbar);
        recyclerView=findViewById(R.id.chatRecyclerView);
        profile=findViewById(R.id.profileAvatar);
        txtHisname=findViewById(R.id.txtHisName);
        userStatus=findViewById(R.id.userStatus);
        message=findViewById(R.id.message);
        btnSend =findViewById(R.id.sendBtn);

        firebaseDatabase=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersDbref=firebaseDatabase.getReference("Users");

        //search user to get that user's info
        Query userQuery=usersDbref.orderByChild("uid").equalTo(hisUid);

        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //check until required info is received
                for(DataSnapshot ds:snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String image=""+ds.child("image").getValue();

                    //set data
                    txtHisname.setText(name);
                    try{
                        Picasso.get().load(image).into(profile);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            //user is signed in stay here
            myUid=user.getUid();
        }else{
            //user not signed in ,go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu,menu);
        //hide searchview ,as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                firebaseAuth.signOut();
                checkUserStatus();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}