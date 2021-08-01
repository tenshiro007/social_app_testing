package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //Actionbar and title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth=FirebaseAuth.getInstance();


        //home fragment transaction default
        actionBar.setTitle("Home");
        HomeFragment fragment=new HomeFragment();
        FragmentTransaction tr1=getSupportFragmentManager().beginTransaction();
        tr1.replace(R.id.content,fragment,"");
        tr1.commit();

        BottomNavigationView  navBottom=findViewById(R.id.navBottom);
        navBottom.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        actionBar.setTitle("Home");
                        HomeFragment fragment=new HomeFragment();
                        FragmentTransaction tr1=getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content,fragment,"");
                        tr1.commit();
                        return true;

                    case R.id.nav_profile:
                        actionBar.setTitle("Home");
                        ProfileFragment fragment1=new ProfileFragment();
                         tr1=getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content,fragment1,"");
                        tr1.commit();
                        return true;

                    case R.id.nav_user:
                        actionBar.setTitle("Home");
                        UserFragment fragment2=new UserFragment();
                         tr1=getSupportFragmentManager().beginTransaction();
                        tr1.replace(R.id.content,fragment2,"");
                        tr1.commit();
                        return true;
                }
                return false;
            }
        });

    }


    private void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            //user is signed in stay here
        }else{
            //user not signed in ,go to main activity
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    /*infate options menu*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*handle menu item clicks*/

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}