package com.example.socialtestingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.ChatActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.model.ModelChatlist;
import com.example.socialtestingapp.model.ModelUser;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends  RecyclerView.Adapter<AdapterChatlist.MyHolder> {
    Context context;
    List<ModelUser>userList;
    private HashMap<String,String>lastMessageMap;

    public AdapterChatlist(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        this.lastMessageMap =new HashMap<>();
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder holder, int position) {
        String hisUid=userList.get(position).getUid();
        String userImage=userList.get(position).getImage();
        String userName=userList.get(position).getName();
        String lastmessage=lastMessageMap.get(hisUid);

        holder.nameTv.setText(userName);
        if(lastmessage==null||lastmessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else{
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastmessage);
        }
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_avatar_default).into(holder.profileTv);
        }catch(Exception e){
            e.printStackTrace();
        }
        //set online status
        if(userList.get(position).getOnlineStatus().equals("online")){

            //imageview
//            holder.onlineStatusTv.setImageResource(R.drawable.circle_online);
            Picasso.get().load(R.drawable.circle_online).into(holder.onlineStatusTv);
        }else{
            Picasso.get().load(R.drawable.circle_offline).into(holder.onlineStatusTv);
        }

        //handle click of user in chatlist
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity with that user
                Intent intent =new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });

    }
    public void setLastMessageMap(String userId,String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        CircularImageView profileTv,onlineStatusTv;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMessageTv=itemView.findViewById(R.id.lastMessageTv);
            profileTv=itemView.findViewById(R.id.profileTv);
            onlineStatusTv=itemView.findViewById(R.id.onlineStatusTv);
        }
    }
}
