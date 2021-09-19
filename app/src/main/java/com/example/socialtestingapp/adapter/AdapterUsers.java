package com.example.socialtestingapp.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.ChatActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.ThereProfileActivity;
import com.example.socialtestingapp.model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    private static final String TAG = "AdapterUsers";
    List<ModelUser> userList;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String myUid;
    private Context context;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");
    }

    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder holder, int position) {
        //set data from list
        String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String name = userList.get(position).getName();
        String email = userList.get(position).getEmail();

        //set data
        holder.txtName.setText(name);
        holder.txtEmail.setText(email);
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_avatar_default)
                    .into(holder.avatar);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //handle item clisked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                /*profile clicked*/
                                /*click to go to thereProfileActivity with uid,this uid is of clicked user
                                 * which willl be used to show user specific data/posts */
                                Intent intent = new Intent(context, ThereProfileActivity.class);
                                intent.putExtra("uid", hisUID);
                                context.startActivity(intent);
                                break;
                            case 1:
                                /*chat clicked*/
                                /*  Click user form user list to start chating/messaging
                                    Start activity by putting uid of receiver
                                    we will use that UID to identify the user we are gonna chat
                                 */
                                isBlockedOrnot(hisUID);
                                break;
                        }
                    }
                }).create().show();
            }
        });

        holder.blockTv.setImageResource(R.drawable.ic_unblock_green);
        checkIsBlocked(hisUID, holder, position);

        holder.blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(position).isBoocked()) {
                    unblockUser(hisUID);
                } else {
                    blockUser(hisUID);
                }
            }
        });
    }

    private void isBlockedOrnot(String hisUid) {
        /*
         * check if sender(current user) is blocked by receiver or not
         * logic:if uid of the sender exists in blockedUser of receiver then sender is block,otherwise not
         * if block then display message "you're blocked by that user,cant send message"
         * if not block then start chat activity
         * */
        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    if (d.exists()) {
                        Toast.makeText(context, "you're blocked by that user,cant send message", Toast.LENGTH_SHORT).show();
                        //blocked
                        Log.d(TAG, "onDataChange: Blocked");
                        return;
                    }
                }
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("hisUid", hisUid);
                    Log.d(TAG, "onDataChange: start" +intent);
                    context.startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void checkIsBlocked(String hisUID, MyHolder holder, int position) {
        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    if (d.exists()) {
                        Log.d(TAG, "onDataChange: "+d.getValue());
                        holder.blockTv.setImageResource(R.drawable.ic_block_red);
                        userList.get(position).setBoocked(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void blockUser(String hisUID) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", hisUID);

        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //blocked successfully
                        Toast.makeText(context, "Blocked successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                //failed to block
                Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unblockUser(String hisUID) {
        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot d : snapshot.getChildren()) {
                            if (d.exists()) {
                                d.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(context, "unlocked successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        private ImageView avatar, blockTv;
        private TextView txtName, txtEmail;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            //initView
            blockTv = itemView.findViewById(R.id.blockTv);
            avatar = itemView.findViewById(R.id.image);
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
        }
    }
}
