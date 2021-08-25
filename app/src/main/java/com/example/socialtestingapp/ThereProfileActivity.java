package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialtestingapp.adapter.AdapterPosts;
import com.example.socialtestingapp.model.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {
    RecyclerView postRecycler;
    List<ModelPost> postlist;
    AdapterPosts adapterPosts;
    String uid;
    FirebaseAuth firebaseAuth;

    private ImageView avatar,coverImage;
    private TextView txtName, txtEmail, txtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        initView();
        firebaseAuth = FirebaseAuth.getInstance();

        //get uid of clicked user ot retrieve his post
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


         /*get info of current signed in user. we can get it using user's email or uid
        (this using email) By using orderByChild query we will show the dialog form node
        whose key name email has value equal to currently signed in email. it willl search all nodes,
        where the key matches it will get its detail
         */
        Query query = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users")
                .orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    txtName.setText(name);
                    txtEmail.setText(email);
                    txtPhone.setText(phone);
                    try {
                        if (!image.equals("")) {
                            Picasso.get().load(image).into(avatar);
//                            Glide.with(view).asBitmap().load(image)
//                                    .into(avatar);

                        }
                        if (!cover.equals("")) {
                            Picasso.get().load(cover).into(coverImage);
//                            Glide.with(view).asBitmap().load(cover)
//                                    .into(coverImage);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Picasso.get().load(R.drawable.ic_add_photo_small).into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        postlist = new ArrayList<>();
        checkUserStatus();
        loadHidpost();


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void initView() {
        avatar = findViewById(R.id.avatar);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        coverImage=findViewById(R.id.coverPhoto);
        postRecycler = findViewById(R.id.recyclerView_post);
    }

    private void loadHidpost() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);

        //show newest post first,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecycler.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");

        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);

        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost mypost = ds.getValue(ModelPost.class);

                    postlist.add(mypost);
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postlist);
                    postRecycler.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchHispost(String search) {
//linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //show newest post first,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postRecycler.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");

        //query to load post
        Query query = ref.orderByChild("uid").equalTo(uid);

        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost mypost = ds.getValue(ModelPost.class);
                    if (mypost.getpTitle().toLowerCase().equals(search.toLowerCase()) ||
                            mypost.getpDescr().toLowerCase().equals(search.toLowerCase())) {
                        postlist.add(mypost);
                    }
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postlist);
                    postRecycler.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflating menu
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    searchHispost(query);
                } else {
                    loadHidpost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    searchHispost(newText);
                } else {
                    loadHidpost();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                firebaseAuth.signOut();
                checkUserStatus();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
        } else {
            //user not signed in ,go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}