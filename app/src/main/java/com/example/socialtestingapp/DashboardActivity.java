package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

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

import org.jetbrains.annotations.NotNull;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    String mUid;
    //firebase auth
    FirebaseAuth firebaseAuth;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        swipeRefreshLayout = findViewById(R.id.swipperLayout);

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

                        ProfileFragment fragment1 = new ProfileFragment();
                        UserFragment fragment2 = new UserFragment();
                        ChatListFragment fragment3 = new ChatListFragment();
                        NotificationsFragment fragment4 = new NotificationsFragment();

        BottomNavigationView navBottom = findViewById(R.id.navBottom);
        navBottom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        actionBar.setTitle("Home");
                        FragmentTransaction tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_profile:
                        actionBar.setTitle("Profile");
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment1, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_user:
                        actionBar.setTitle("Users");
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment2, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_chat:
                        actionBar.setTitle("Chat");
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment3, "");
                        tr1.commit();
                        return true;

                    case R.id.nav_noti:
                        actionBar.setTitle("Chat");
                        tr1 = getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content, fragment4, "");
                        tr1.commit();
                        return true;
                }
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                BottomNavigationView navBottom = findViewById(R.id.navBottom);
//                getSupportFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
//                navBottom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
//                        switch (item.getItemId()){
//                            case R.id.nav_home:
//                                Toast.makeText(DashboardActivity.this, "Home", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "onNavigationItemSelected: test::"+"Home");
//                                break;
//                            case R.id.nav_profile:
//                                Toast.makeText(DashboardActivity.this, "profile", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "onNavigationItemSelected: test::"+"profile");
//                                break;
//                            default:
//                                Toast.makeText(DashboardActivity.this, "Other", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "onNavigationItemSelected: test::"+"Other");
//                                break;
//                        }
//                        return false;
//                    }
//                });

                switch (navBottom.getSelectedItemId()) {
                    case R.id.nav_home:
                        actionBar.setTitle("Home");
//                        HomeFragment fragment = new HomeFragment();
                        Toast.makeText(DashboardActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().detach(fragment).attach(fragment).commit();
                        break;
                    case R.id.nav_profile:
                        actionBar.setTitle("Profile");
//                        ProfileFragment fragment1 = new ProfileFragment();
                        Toast.makeText(DashboardActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().detach(fragment1).attach(fragment1).commit();
                        break;

                    case R.id.nav_user:
                        actionBar.setTitle("Users");
//                        UserFragment fragment2 = new UserFragment();
                        Toast.makeText(DashboardActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().detach(fragment2).attach(fragment2).commit();
                        break;

                    case R.id.nav_chat:
                        actionBar.setTitle("Chat");
//                        ChatListFragment fragment3 = new ChatListFragment();
                        Toast.makeText(DashboardActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().detach(fragment3).attach(fragment3).commit();
                        break;

                    case R.id.nav_noti:
                        actionBar.setTitle("Chat");
//                        NotificationsFragment fragment4 = new NotificationsFragment();
                        Toast.makeText(DashboardActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().detach(fragment4).attach(fragment4).commit();
                        break;
                }

                swipeRefreshLayout.setRefreshing(false);
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