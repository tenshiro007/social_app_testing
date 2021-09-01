package com.example.socialtestingapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.R;
import com.example.socialtestingapp.model.ModelComment;
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

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{
    private static final String TAG = "AdapterComments";
    Context context;
    List<ModelComment>comments;
    String myUid,postId;

    public AdapterComments(Context context, List<ModelComment> comments, String myUid, String postId) {
        this.context = context;
        this.comments = comments;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder holder, int position) {
        String uid=comments.get(position).getUid();
        String name=comments.get(position).getuName();
        String email=comments.get(position).getuEmail();
        String uDp=comments.get(position).getuDp();
        String cid=comments.get(position).getCid();
        String comment=comments.get(position).getComment();
        String time=comments.get(position).getTimestamp();

        //conver time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(time));
        String dateTime = (String) DateFormat.format("dd/MM/yyyy hh:mm:aa", cal);


        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(dateTime);

        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_avatar_default).into(holder.avatarTv);
        }catch (Exception e){
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if this comment is by currently signed in user or not
                if(myUid.equals(uid)){
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comments?");
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });
                    builder.create().show();
                }else{
                    Toast.makeText(context, "Can't delete other's comment...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteComment(String cid) {
         DatabaseReference ref= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();


        //now update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String comments = "" + snapshot.child("pComments").getValue();
                int newCommetsVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(String.valueOf(newCommetsVal));

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(context, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        CircularImageView avatarTv;
        TextView nameTv,commentTv,timeTv;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            avatarTv=itemView.findViewById(R.id.avatar);
            nameTv=itemView.findViewById(R.id.nameTv);
            commentTv=itemView.findViewById(R.id.commentTv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }
}
