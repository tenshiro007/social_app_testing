package com.example.socialtestingapp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.service.autofill.Dataset;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.AddPostActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.ThereProfileActivity;
import com.example.socialtestingapp.model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.myHolder> {
    private static final String TAG = "AdapterPosts";
    Context context;
    List<ModelPost> postList;
    String myUid;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @NotNull
    @Override
    public myHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull myHolder holder, int position) {
        //get data
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDesc = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();


        //convert timeStamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //set data
        holder.uName.setText(uName);
        holder.pTime.setText(pTime);
        holder.pTitle.setText(pTitle);
        holder.pDescrip.setText(pDesc);

        //set user dp
        Log.d(TAG, "onBindViewHolder: uDp " + uDp);
        try {
            Picasso.get().load(uDp)
                    .placeholder(R.drawable.ic_avatar_default)
                    .into(holder.uPicture);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // set post image
        try {
            if (pImage.equals("noImage")) {
                //hide imageView
                holder.pImage.setVisibility(View.GONE);
            } else {
                //show imageView
                holder.pImage.setVisibility(View.VISIBLE);
                Picasso.get().load(pImage).into(holder.pImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //handle btn click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn, uid, myUid, pId, pImage);
//                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
            }
        });
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*click to go to thereProfileActivity with uid,this uid is of clicked user
                 * which willl be used to show user specific data/posts */
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        //create popup menu currently having opting Delete,we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if (uid.equals(myUid)) {

            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
            //item click listener
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();

                    switch (item.getItemId()){
                        case 0:
                            beginDelete(pId,pImage);
                            break;
                        case 1:
                            /*Edit is clicked
                            * start activity with key "editPost" and the id of the post clicked*/
                            Intent intent=new Intent(context, AddPostActivity.class);
                            intent.putExtra("key","editPost");
                            intent.putExtra("editPostId",pId);
                            context.startActivity(intent);
                            break;
                    }
                    return false;
                }
            });
            //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image
        if(pImage.equals("noImage")){
            //post is without image;
            deleteWithoutImage(pId);
        }else{
            //post is with image
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting...");
        //image delete ,now delete database
        Query fquery= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot d:snapshot.getChildren()){
                    //remove values from firebase where pid matches
                    d.getRef().removeValue();
                }
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void deleteWithImage(String pId, String pImage) {
        //progress bar
        ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting...");

        /*steps:
             1.delete immage using url
             2.delete from database using post id
         */
        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //image delete ,now delete database
                        Query fquery= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for(DataSnapshot d:snapshot.getChildren()){
                                    //remove values from firebase where pid matches
                                    d.getRef().removeValue();
                                }
                                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class myHolder extends RecyclerView.ViewHolder {
        //views from row_post
        CircularImageView uPicture;
        ImageView pImage;
        TextView uName, pTime, pTitle, pDescrip, pLike;
        ImageButton moreBtn;
        Button btnLike, btnComment, btnShare;
        LinearLayout profileLayout;

        public myHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            //init Views
            profileLayout = itemView.findViewById(R.id.profileLayout);
            uPicture = itemView.findViewById(R.id.uPicture);
            pImage = itemView.findViewById(R.id.pImage);
            uName = itemView.findViewById(R.id.uName);
            pTime = itemView.findViewById(R.id.pTime);
            pTitle = itemView.findViewById(R.id.pTitle);
            pDescrip = itemView.findViewById(R.id.pDesc);
            pLike = itemView.findViewById(R.id.plike);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);

        }
    }
}
