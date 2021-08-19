package com.example.socialtestingapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.socialtestingapp.AddPostActivity;
import com.example.socialtestingapp.MainActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.adapter.AdapterUsers;
import com.example.socialtestingapp.model.ModelUser;
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
    FirebaseAuth firebaseAuth;
    private RecyclerView recycerView;
    private AdapterUsers adapterUsers;
    private List<ModelUser>usersList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_user, container, false);

        initView(view);
        //init
        firebaseAuth=FirebaseAuth.getInstance();

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);//show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    /*infate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_menu,menu);

        //hide add post icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //searchView
        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView= (SearchView) MenuItemCompat.getActionView(item);

        //Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*
                    called when user press search button from keyboard
                    if search query is not empty then search
                 */
                if(!TextUtils.isEmpty(query.trim())){
                    //search text contains text,search it
                    searchUser(query);
                }else{
                    //search text empty,get all users
                    getAllUser();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /*
                    called when user press search button from keyboard
                    if search query is not empty then search
                 */
                if(!TextUtils.isEmpty(newText.trim())){
                    //search text contains text,search it
                    searchUser(newText);
                }else{
                    //search text empty,get all users
                    getAllUser();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu,inflater);
    }

    private void searchUser(String query) {
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

                    /*
                        Conditions to fulfil search
                        1)User not current user
                        2)The user name or email contains text entered in searchView (Case insensitive)
                     */

                    //get all search users except currently signed in user
                    if(!modelUser.getUid().equals(user.getUid())){
                        Log.d(TAG, "onDataChange: query"+query);
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase())||
                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUser);
                        }
                    }
                }
                Log.d(TAG, "onDataChange: Search"+usersList);
                //adapter
                adapterUsers =new AdapterUsers(getContext(),usersList);
                //refresh adapter
                adapterUsers.notifyDataSetChanged();
                //set adater to recyclerview
                recycerView.setAdapter(adapterUsers);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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
    private void checkUserStatus(){
        //get current user
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            //user is signed in stay here
        }else{
            //user not signed in ,go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}