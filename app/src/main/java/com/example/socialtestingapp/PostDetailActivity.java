package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialtestingapp.adapter.AdapterComments;
import com.example.socialtestingapp.model.ModelComment;
import com.example.socialtestingapp.notification.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    private static final String TAG = "PostDetailActivity";
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog pd;

    //to get detail of user and post
    String myUid,myEmail,myName,myDp,hisUid,pImage,
    postId,pLikes,hisDp,hisName;

    //views
    CircularImageView uPicture;
    ImageView pImageTv;
    TextView uNameTv,pTimeTv,pTitleTv,pDescrTv,pLikeTv,pCommentTv;
    ImageButton moreBtn;
    Button likeBtn,shareBtn;
    LinearLayout profileLayout;

    //comments views
    RecyclerView recyclerComment;
    List<ModelComment>comments;
    AdapterComments adapterComments;

    boolean mProcessComment =false;
    boolean mProcessLike=false;

    //add comments views
    EditText commentEt;
    CircularImageView cAvatar;
    ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        initViews();

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();

        //actionBar and its properties
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of post using intent
        Intent intent=getIntent();
        postId=intent.getStringExtra("postId");

        firebaseDatabase=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");

        loadPostInfo();
        loadUserInfo();

        setLikes();

        //set subtitle of actionbar
        actionBar.setSubtitle("SignedIn as "+myEmail);

        loadComments();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle =pTitleTv.getText().toString().trim();
                String pDesc=pDescrTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable= (BitmapDrawable) pImageTv.getDrawable();
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
        startActivity(Intent.createChooser(intent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder=new File(this.getCacheDir(),"images");
        Uri uri=null;
        try{
            imageFolder.mkdirs();//create if not exists
            File file=new File(imageFolder,"shared_image.png");

            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(this,"com.example.socialtestingapp.fileprovider",file);

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
        startActivity(Intent.createChooser(intent,"Share Via"));//message to show in share
    }

    private void loadComments() {
        comments=new ArrayList<>();
       //layout(linear) for recyclerview
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());

        //set layout to recyclerview
        recyclerComment.setLayoutManager(layoutManager);

        //init commment list
        DatabaseReference ref=firebaseDatabase.getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                comments.clear();
                for(DataSnapshot d:snapshot.getChildren()){
                    ModelComment modelComment=d.getValue(ModelComment.class);
                    comments.add(modelComment);

                    //pass myuid and postid as paremeter of constructor of comment adapter
                    Log.d(TAG, "onDataChange: commentlist "+modelComment.toString());


                    //setup adapter
                    adapterComments=new AdapterComments(PostDetailActivity.this,comments,myUid,postId);

                    //set adapter
                    recyclerComment.setAdapter(adapterComments);

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        //create popup menu currently having opting Delete,we will add more options later
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        if (hisUid.equals(myUid)) {

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
                        beginDelete();
                        break;
                    case 1:
                        /*Edit is clicked
                         * start activity with key "editPost" and the id of the post clicked*/
                        Intent intent=new Intent(PostDetailActivity.this, AddPostActivity.class);
                        intent.putExtra("key","editPost");
                        intent.putExtra("editPostId",postId);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image
        if(pImage.equals("noImage")){
            //post is without image;
            deleteWithoutImage();
        }else{
            //post is with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        //progress bar
        ProgressDialog pd=new ProgressDialog(this);
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
                                .getReference("Posts").orderByChild("pId").equalTo(postId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                for(DataSnapshot d:snapshot.getChildren()){
                                    //remove values from firebase where pid matches
                                    d.getRef().removeValue();
                                }
                                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting...");
        //image delete ,now delete database
        Query fquery= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot d:snapshot.getChildren()){
                    //remove values from firebase where pid matches
                    d.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        //when the details of post is loading,also check if current user has liked it or not
        DatabaseReference likeRef=firebaseDatabase.getReference("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)){
                    /*User has liked this post
                     * To indicate that the post is liked by this (signedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button fron "like" to "liked" */
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_color,0,0,0);
                    likeBtn.setText("Liked");
                }else{
                    /*User has liked this post
                     * To indicate that the post is liked by this (signedIn) user
                     * Change drawable left icon of like button
                     * Change text of like button fron "like" to "liked" */
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        /*get total number of likes for the post ,whose like button clicked
         * if currently signed in user has not  liked it before
         * increase value by  1,otherwise decrease value by 1
         * */
        mProcessLike=true;

        DatabaseReference likeRef=firebaseDatabase.getReference().child("Likes");
        DatabaseReference postRef=firebaseDatabase.getReference().child("Posts");
        //get id of post clicked
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(mProcessLike){
                    if(snapshot.child(postId).hasChild(myUid)){

                        //already liked ,so remove like
                        postRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes )-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcessLike=false;

//                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_color,0,0,0);
//                        likeBtn.setText("Liked");

                    }else{
                        //not liked, like it
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes )+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");//set any value
                        mProcessLike=false;

//                        likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_color,0,0,0);
//                        likeBtn.setText("Liked");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void postComment() {
        pd=new ProgressDialog(this);
        pd.setMessage("Adding comment....");
        String comment=commentEt.getText().toString().trim();
        //validate
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        pd.show();

        String timeStamp=String.valueOf(System.currentTimeMillis());

        //each post will have a child comments that will contain comment of that post
        DatabaseReference ref=firebaseDatabase.getReference("Posts").child(postId).child("Comments");

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("cid",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        //put this data to db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                       pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });


    }

    private void updateCommentCount() {
        //whenever user add comment incress the comment count as we did for like count
        mProcessComment=true;
        DatabaseReference ref=firebaseDatabase.getReference("Posts").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(mProcessComment){
                    String comments=""+snapshot.child("pComments").getValue();
                    int newCommentVal=Integer.parseInt(comments)+1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment=false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef=firebaseDatabase.getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for(DataSnapshot d:snapshot.getChildren()){
                            myName=""+d.child("name").getValue();
                            myDp=""+d.child("image").getValue();

                            //set data
                            try{
                                //if image is received then set
//                                Picasso.get().load(R.drawable.ic_avatar_default).into(cAvatar);
                                Picasso.get().load(myDp).placeholder(R.drawable.ic_avatar_default).into(cAvatar);
                                Log.d(TAG, "onDataChange: myDp"+myDp);
                            }catch(Exception e){
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadPostInfo() {
        //get post using the id of the post
        DatabaseReference ref= firebaseDatabase.getReference("Posts");
        Query query=ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //keep checking the posts until get the required post
                for(DataSnapshot d:snapshot.getChildren()){
                    //get data
                    String pTitle=""+d.child("pTitle").getValue();
                    String pDescr=""+d.child("pDescr").getValue();
                     pLikes=""+d.child("pLikes").getValue();
                    String pTimeStamp=""+d.child("pTime").getValue();
                     pImage=""+d.child("pImage").getValue();
                    hisDp=""+d.child("uDp").getValue();
                     hisUid=""+d.child("uId").getValue();
                    String uEmail=""+d.child("uEmail").getValue();
                     hisName=""+d.child("uName").getValue();
                     String commentCount=""+d.child("pComments").getValue();

                     //convert timestamp to proper form
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescrTv.setText(pDescr);
                    pLikeTv.setText(pLikes +" Likes");
                    pTimeTv.setText(pTime);
                    pCommentTv.setText(commentCount+ "Comments");

                    uNameTv.setText(hisName);

                    // set image of the user who posted
                    if(pImage.equals("noImage")){
                        //hide imageView
                        pImageTv.setVisibility(View.GONE);
                    }else{
                        try{
                            Picasso.get().load(pImage).into(pImageTv);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                    //set user
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_avatar_default).into(uPicture);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        uPicture=findViewById(R.id.uPicture);
        pImageTv=findViewById(R.id.pImage);
        uNameTv =findViewById(R.id.uName);
        pTitleTv=findViewById(R.id.pTitle);
        pTimeTv=findViewById(R.id.pTime);
        pDescrTv=findViewById(R.id.pDesc);
        pLikeTv=findViewById(R.id.plike);
        moreBtn=findViewById(R.id.moreBtn);
        likeBtn=findViewById(R.id.btnLike);
        shareBtn=findViewById(R.id.btnShare);
        pCommentTv=findViewById(R.id.pCommentTv);
        profileLayout=findViewById(R.id.profileLayout);

        commentEt=findViewById(R.id.btnComment);
        sendBtn=findViewById(R.id.sendBtn);
        cAvatar=findViewById(R.id.cAvatar);
        recyclerComment=findViewById(R.id.recyclerComment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    private void checkUserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            //user is signed in
            myEmail=user.getEmail();
            myUid=user.getUid();
        }else{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu,menu);

        //hide some menu
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

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
}
