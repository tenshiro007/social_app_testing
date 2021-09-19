package com.example.socialtestingapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.PostDetailActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.model.ModelNotifications;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdaptetNotification extends RecyclerView.Adapter<AdaptetNotification.holder>{
    Context context;
    List<ModelNotifications>noti;

    FirebaseAuth firebaseAuth;
    public AdaptetNotification(Context context, List<ModelNotifications> noti) {
        this.context = context;
        this.noti = noti;
    }

    @NonNull
    @NotNull
    @Override
    public holder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_noti,parent,false);

        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull holder holder, int position) {
        firebaseAuth=FirebaseAuth.getInstance();
        ModelNotifications model=noti.get(position);
        String name=noti.get(position).getsName();
        String notification=noti.get(position).getNotification();
        String image=noti.get(position).getsImage();
        String timestamp=noti.get(position).getTimestamp();
        String senderUid=noti.get(position).getsUid();
        String pId=noti.get(position).getpId();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String time= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();


        //we will get the name,email,image of the user of notification form his uid
        DatabaseReference ref= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        ref.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for(DataSnapshot d:snapshot.getChildren()){
                            String name=""+d.child("name").getValue();
                            String image=""+d.child("image").getValue();
                            String email=""+d.child("email").getValue();

                            //add to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set to model
                            try{
                            Picasso.get().load(image).placeholder(R.drawable.ic_avatar_default).into(holder.avataTv);
                            holder.notiTv.setText(notification);
                            holder.nameTv.setText(name);
                            holder.timeTv.setText(time);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        //clicked to open
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start postDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId );
                context.startActivity(intent);
            }
        });

        //delete
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show confirmation dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setMessage("Are you sure to delete this notification?");
                builder.setTitle("Delete");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //delete notification
                        DatabaseReference ref= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(context, "Notification deleted...", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(context, "Failed to deleted..."+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return noti.size();
    }

    class holder extends RecyclerView.ViewHolder{
        private CircularImageView avataTv;
        private TextView nameTv,notiTv,timeTv;
        public holder(@NonNull @NotNull View itemView) {
            super(itemView);
            avataTv=itemView.findViewById(R.id.avatarTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            notiTv=itemView.findViewById(R.id.notificationTv);

        }
    }
}
