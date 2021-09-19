package com.example.socialtestingapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Layout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.R;
import com.example.socialtestingapp.model.ModelChat;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Myholder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private static final String TAG = "AdapterChat";
    String imageUrl;
    FirebaseUser user;
    private Context context;
    private List<ModelChat> chatslist;

    public AdapterChat(Context context, List<ModelChat> chatslist, String imageUrl) {
        this.imageUrl = imageUrl;
        this.context = context;
        this.chatslist = chatslist;
    }

    @NonNull
    @NotNull
    @Override
    public Myholder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new Myholder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new Myholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull Myholder holder, int position) {
        //get data
        String message = chatslist.get(position).getMessage();
        String timeStamp = chatslist.get(position).getTimeStamp();
        String type = chatslist.get(position).getType();

        //conver time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = (String) DateFormat.format("dd/MM/yyyy hh:mm:aa", cal);

        if(type.equals("text")){
            holder.message.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);

            holder.message.setText(message);

        }else{
            holder.message.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageTv);
        }
        //set data
        if (!message.equals("")) {

            holder.message.setText(message);
        }
        holder.time.setText(dateTime);
        try {
            Picasso.get().load(imageUrl).into(holder.profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set seen /deliiverd status of message
        if (position == chatslist.size() - 1) {
            if (chatslist.get(position).isSeen()) {
                holder.isSeen.setText("seen");
            } else {
                holder.isSeen.setText("Delivered");
            }
        } else {
            holder.isSeen.setVisibility(View.GONE);
        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    private void deleteMessage(int position) {
        String myUid = FirebaseAuth.getInstance().getUid();
        /*logic:
         * get timeStamp of clicked message
         * compare the timestamp of the clicked message with all messages in chats
         * where both values matches delete that message
         */
        String msgTimeStamp = chatslist.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Chats");
        Query query = dbRef.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    /*
                     * we can do one of two things here
                     * 1) Remove the message from chats
                     * 2) Set the value of message "this message was deleted..."
                     * so do whatever you want */
                    if (ds.child("sender").getValue().equals(myUid)) {

                        //1) remove the message from chats
//                        ds.getRef().removeValue();

                        //2) set the value of message this message was deleted
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted....");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "You can deleted only your messages...", Toast.LENGTH_SHORT).show();
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
        return chatslist.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user;
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (chatslist.get(position).getSender().equals(user.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
//        return super.getItemViewType(position);
    }

    //view holder class
    protected class Myholder extends RecyclerView.ViewHolder {
        //view
        private CircularImageView profile;
        private ImageView messageTv;
        private TextView message, time, isSeen;
        private LinearLayout messageLayout;

        public Myholder(@NonNull @NotNull View itemView) {
            super(itemView);
            //initView
            messageTv=itemView.findViewById(R.id.imageTv);
            profile = itemView.findViewById(R.id.profile);
            message = itemView.findViewById(R.id.txtMessage);
            time = itemView.findViewById(R.id.txtTime);
            isSeen = itemView.findViewById(R.id.isSeen);
            messageLayout = itemView.findViewById(R.id.messageLayout);

        }
    }
}
