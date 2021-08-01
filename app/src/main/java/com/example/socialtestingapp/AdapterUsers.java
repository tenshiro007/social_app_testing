package com.example.socialtestingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    private Context context;
    List<ModelUser>userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout(row_user.xml)
        View view= LayoutInflater.from(context).inflate(R.layout.row_user,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder holder, int position) {
        //set data from list
        String userImage=userList.get(position).getImage();
        String name=userList.get(position).getName();
        String email=userList.get(position).getEmail();

        //set data
        holder.txtName.setText(name);
        holder.txtEmail.setText(email);
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_avatar_default)
                    .into(holder.avatar);
        }catch (Exception e){
            e.printStackTrace();
        }


        //handle item clisked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, ""+email, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{
        private ImageView avatar;
        private TextView txtName,txtEmail;
        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            //initView
            avatar=itemView.findViewById(R.id.image);
            txtName=itemView.findViewById(R.id.txtName);
            txtEmail=itemView.findViewById(R.id.txtEmail);
        }
    }
}
