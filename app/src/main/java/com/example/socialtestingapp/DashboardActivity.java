package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.socialtestingapp.fragment.ChatListFragment;
import com.example.socialtestingapp.fragment.HomeFragment;
import com.example.socialtestingapp.fragment.ProfileFragment;
import com.example.socialtestingapp.fragment.UserFragment;
import com.example.socialtestingapp.notification.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    String mUid;
    //firebase auth
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //Actionbar and title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();


        //home fragment transaction default
        actionBar.setTitle("Home");
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction tr1 = getSupportFragmentManager().beginTransaction();
        tr1.replace(R.id.content, fragment, "");
        tr1.commit();


        checkUserStatus();
        if (null != mUid) {

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();


                            //update token
                            updatetoken(token);
                        }
                    });
        }


        BottomNavigationView navBottom = findViewById(R.id.navBottom);
        navBottom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        actionBar.setTitle("Home");
                        HomeFragment fragment = new HomeFragment();
                        FragmentTransaction tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_profile:
                        actionBar.setTitle("Profile");
                        ProfileFragment fragment1 = new ProfileFragment();
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment1, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_user:
                        actionBar.setTitle("Users");
                        UserFragment fragment2 = new UserFragment();
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment2, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_chat:
                        actionBar.setTitle("Chat");
                        ChatListFragment fragment3 = new ChatListFragment();
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment3, "");
                        tr1.commit();
                        return true;
                }
                return false;
            }
        });

    }

    private void updatetoken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUid).setValue(mToken);
    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            mUid = user.getUid();

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUid);
            editor.apply();

        } else {
            //user not signed in ,go to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}