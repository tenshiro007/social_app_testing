package com.example.socialtestingapp.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialtestingapp.AddPostActivity;
import com.example.socialtestingapp.PostDetailActivity;
import com.example.socialtestingapp.R;
import com.example.socialtestingapp.ThereProfileActivity;
import com.example.socialtestingapp.model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.myHolder> {
    private static final String TAG = "AdapterPosts";
    Context context;
    List<ModelPost> postList;
    String myUid;

    private DatabaseReference likeRef;//for like database node
    private DatabaseReference postRef;//reference of post

    boolean mProcessLike =false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        Log.d(TAG, "AdapterPosts: postlist "+postList.toString());
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Likes");
        postRef=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Posts");
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
        String pLikes = postList.get(position).getpLikes();
        String pComments = postList.get(position).getpComments();

        //convert timeStamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //set data
        holder.uName.setText(uName);
        holder.pTime.setText(pTime);
        holder.pTitle.setText(pTitle);
        holder.pDescrip.setText(pDesc);
        holder.pLike.setText(pLikes+" Likes");
        holder.pComment.setText(pComments+" Comments");

        //set likes for each post
        setLikes(holder,pId);

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
               /*get total number of likes for the post ,whose like button clicked
               * if currently signed in user has not  liked it before
               * increase value by  1,otherwise decrease value by 1
               * */
                int pLikes =Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike=true;

                //get id of post clicked
                String postIde=postList.get(position).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                       if(mProcessLike){
                           if(snapshot.child(postIde).hasChild(myUid)){

                        //already liked ,so remove like
                               postRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1));
                               likeRef.child(postIde).child(myUid).removeValue();
                               mProcessLike=false;
                           }else{
                               //not liked, like it
                               postRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                               likeRef.child(postIde).child(myUid).setValue("Liked");//set any value
                               mProcessLike=false;
                           }
                       }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start postDetailActivity
                Intent intent=new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);

            }
        });
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*some posts contains only text,and some contains image and text so,
                we will handle them both
                 */
                //get image from imageview
                BitmapDrawable bitmapDrawable= (BitmapDrawable) holder.pImage.getDrawable();
                if(bitmapDrawable==null){
                    //post without image
                    shareTextOnly(pTitle,pDesc);
                }else{
                    //post with image

                    //convert image to bitmap
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    ShareImageAndText(pTitle,pDesc,bitmap);
                }
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

    private void ShareImageAndText(String pTitle, String pDesc, Bitmap bitmap) {
         //concatenate title and description to share
        String sharebody=pTitle+"\n"+pDesc;

        //first we will save this image in catche,get the saved image uri
        Uri uri=saveImageToShare(bitmap);

        //share intent
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,sharebody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        // TODO: 9/2/2021 debug share with image

        File imageFolder=new File(context.getCacheDir(),"images");
        Uri uri=null;
        try{
            imageFolder.mkdirs();//create if not exists
            File file=new File(imageFolder,"shared_image.png");

            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.example.socialtestingapp.fileprovider",file);

        }catch(Exception e){
            e.printStackTrace();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDesc) {
        //concatenate title and description to share
        String sharebody=pTitle+"\n"+pDesc;

        //share intent
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here"); //in case you share via an email app
        intent.putExtra(Intent.EXTRA_TEXT,sharebody);//text to share
        context.startActivity(Intent.createChooser(intent,"Share Via"));//message to show in share
    }

    private void setLikes(myHolder holder, String postkey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child(postkey).hasChild(myUid)){
                    /*User has liked this post
                    * To indicate that the post is liked by this (signedIn) user
                    * Change drawable left icon of like button
                    * Change text of like button fron "like" to "liked" */
                    holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_color,0,0,0);
                    holder.btnLike.setText("Liked");
                }else{
                    /*User has liked this post
                     * To indicate that the post is liked by this (signedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button fron "like" to "liked" */
                    holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    holder.btnLike.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

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
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");
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
                        case 2:
                            //start postDetailActivity
                            intent=new Intent(context, PostDetailActivity.class);
                            intent.putExtra("postId",pId);
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
        TextView uName, pTime, pTitle, pDescrip, pLike,pComment;
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
            pComment=itemView.findViewById(R.id.pCommentTv);

        }
    }
}
