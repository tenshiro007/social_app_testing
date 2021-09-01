package com.example.socialtestingapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.socialtestingapp.MainActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.adapter.AdapterChatlist;
import com.example.socialtestingapp.model.ModelChat;
import com.example.socialtestingapp.model.ModelChatlist;
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


public class ChatListFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    RecyclerView recyclerChatlist;
    List<ModelChatlist>chatlists;
    List<ModelUser>userList;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat_list, container, false);


        //init
        recyclerChatlist=view.findViewById(R.id.recyclerChatlist);
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseDatabase =FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        currentUser=FirebaseAuth.getInstance().getCurrentUser();


        chatlists=new ArrayList<>();
        reference=firebaseDatabase.getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                chatlists.clear();
                for(DataSnapshot d:snapshot.getChildren()){
                    ModelChatlist chatlist=d.getValue(ModelChatlist.class);
                    chatlists.add(chatlist);

                    loadChat();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChat() {
        userList=new ArrayList<>();
        reference=firebaseDatabase.getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot d:snapshot.getChildren()){
                    ModelUser user=d.getValue(ModelUser.class);
                    for(ModelChatlist chatlist:chatlists){
                        if(user.getUid()!=null && user.getUid().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist=new AdapterChatlist(getContext(),userList);

                    recyclerChatlist.setAdapter(adapterChatlist);

                    //set last message
                    for(int i=0;i<userList.size();i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String uid) {
        DatabaseReference ref=firebaseDatabase.getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String thelast="default";
                for(DataSnapshot d:snapshot.getChildren()){
                    ModelChat chat=d.getValue(ModelChat.class);
                    if(chat ==null){
                        continue;
                    }
                    String sender=chat.getSender();
                    String receiver=chat.getReceiver();
                    if(sender==null ||receiver==null){
                        continue;
                    }
                    if(chat.getReceiver().equals(currentUser.getUid())&&chat.getSender().equals(uid)||
                            (chat.getReceiver().equals(uid)&& chat.getSender().equals(currentUser.getUid()))){
                        thelast=chat.getMessage();
                    }
                }
                adapterChatlist.setLastMessageMap(uid,thelast);
                adapterChatlist.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
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

        super.onCreateOptionsMenu(menu,inflater);
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