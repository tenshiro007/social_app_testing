package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private static final String TAG = "AddPostActivity";
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;


    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    //image picked will be samed in this uri
    Uri image_uri = null;


    //views
    EditText etitle, edescrip;
    ImageView image;
    Button btnUpload;

    //user info
    String name, email, uid, dp;

    //for update
    String isUpdateKey;
    String editPostId;

    //progressDialog
    ProgressDialog pd;

    //info fo post to be edited
    String editTitle, editDescription, editImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //ini views
        initViews();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setSubtitle(email);

        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        //init firebaseauth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();



        //get data through intent from previous adapter
        Intent intent = getIntent();

        //get data and its type from intent
        String action=intent.getAction();
        String type=intent.getType();
        if(Intent.ACTION_SEND.equals(action)&&type!=null){
            Log.d(TAG, "onCreate: action ::"+action+" \ntype::"+type+" \nintent ::"+intent);
            if("text/plain".equals(type)){
                //text type data
                Log.d(TAG, "onCreate: type::"+"text");
                handleSendText(intent);
            } else if(type.startsWith("image")){
                Log.d(TAG, "onCreate: type::" + "image2");
                //image type data
                handleSendImage(intent);
            }else{
                Log.d(TAG, "onCreate: type::"+"no match");
            }
        }


        isUpdateKey = "" + intent.getStringExtra("key");
         editPostId = "" + intent.getStringExtra("editPostId");

            //validate if we came here to update post i.e. came from adapterpost
        if (isUpdateKey.equals("editPost")) {
            //update
            actionBar.setTitle("Update Post");
            btnUpload.setText("Update");
            loadPostData(editPostId);
        } else {
            //add
            actionBar.setTitle("Add New Post");
            btnUpload.setText("Upload");
        }

        //progressDialog
        pd = new ProgressDialog(this);

        //get some info of current user to include in post
        userDbRef = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        Query query = userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child("name").getValue();
                    email = "" + ds.child("email").getValue();
                    dp = "" + ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });




        //init permissions arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    private void handleSendImage(Intent intent) {
        Uri imageUri=(Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageUri!=null){
            image_uri=imageUri;
            image.setImageURI(image_uri);
        }
    }

    private void handleSendText(Intent intent) {
        String shareText=intent.getStringExtra(Intent.EXTRA_TEXT);
        if(shareText!=null){
            edescrip.setText(shareText);
        }
    }

    private void loadPostData(String editPostId) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");
        //get detail of post using id of post
        Query fquery = ref.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    //get data
                    editTitle = "" + d.child("pTitle").getValue();
                    editDescription = "" + d.child("pDescr").getValue();
                    editImage = "" + d.child("pImage").getValue();
                    //set data to views
                    etitle.setText(editTitle);
                    edescrip.setText(editDescription);
                    if (!editImage.equals("noImage")) {
                        try {
                            Picasso.get().load(editImage).into(image);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        etitle = findViewById(R.id.eTitle);
        edescrip = findViewById(R.id.eDiscription);
        image = findViewById(R.id.eImage);
        btnUpload = findViewById(R.id.btnUpload);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data from edittext
                String title = etitle.getText().toString().trim();
                String description = edescrip.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddPostActivity.this, "Enter title....", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Enter some description....", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isUpdateKey.equals("editPost")){
                    beginUpdate(title,description,editPostId);
                }else {
                    uploadData(title, description);
                }

            }
        });
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String ,Object>hashMap=new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("uName",name);
        hashMap.put("uEmail",email);
        hashMap.put("uDp",dp);
        hashMap.put("pTitle",title);
        hashMap.put("pDescr",description);
        hashMap.put("pImage","noImage");

        DatabaseReference ref=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");
        ref.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Post....");
        pd.show();

        if(!editImage.equals("noImage")){
            //without image
            updataWasWithImage(title,description,editPostId);
        }else if(image.getDrawable()!=null){
            //with image
            updateWithNowImage(title,description,editPostId);
        }else{
            //without image
            updateWithoutImage(title,description,editPostId);
        }
    }

    private void updateWithNowImage(String title, String description, String editPostId) {
        String timeStamp=String.valueOf(System.currentTimeMillis());
        String filePathAndName="Posts/"+"post_"+timeStamp;

        //get image from imageView
        Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();

        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();

        StorageReference ref=FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get its url;
                        Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri=uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            //uri is received ,upload to firebase database

                            HashMap<String ,Object>hashMap=new HashMap<>();
                            hashMap.put("uid",uid);
                            hashMap.put("uName",name);
                            hashMap.put("uEmail",email);
                            hashMap.put("uDp",dp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescr",description);
                            hashMap.put("pImage",downloadUri);

                            DatabaseReference ref=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");
                            ref.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //image not upload
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void updataWasWithImage(String title, String description, String editPostId) {
        //post is with image,delete previous image firest
        StorageReference mPicRef=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPicRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    //image deleted ,upload new image
                        //for post-image,post-id,publish-time
                        String timeStamp=String.valueOf(System.currentTimeMillis());
                        String filePathAndName="Posts/"+"post_"+timeStamp;

                        //get image from imageView
                        Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos=new ByteArrayOutputStream();

                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                        byte[] data=baos.toByteArray();

                        StorageReference ref=FirebaseStorage.getInstance().getReference().child(filePathAndName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        //image uploaded get its url;
                                        Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri=uriTask.getResult().toString();
                                        if(uriTask.isSuccessful()){
                                            //uri is received ,upload to firebase database

                                            HashMap<String ,Object>hashMap=new HashMap<>();
                                            hashMap.put("uid",uid);
                                            hashMap.put("uName",name);
                                            hashMap.put("uEmail",email);
                                            hashMap.put("uDp",dp);
                                            hashMap.put("pTitle",title);
                                            hashMap.put("pDescr",description);
                                            hashMap.put("pImage",downloadUri);

                                            DatabaseReference ref=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");
                                            ref.child(editPostId)
                                                    .updateChildren(hashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updated...", Toast.LENGTH_SHORT).show();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull @NotNull Exception e) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });


                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        //image not upload
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(String title, String description) {
        pd.setMessage("Publishing post...");
        pd.show();

        //for post-image name,post-id,post-publish-time
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;

        if (image.getDrawable()!=null) {
            //get image from imageView
            Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();

            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data=baos.toByteArray();

            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image is uploaded to firebase storeage,now get it's url
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;

                    String downloadUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()) {
                        //url is received upload post ot firebase database;
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("uid", uid);
                        hashMap.put("uName", name);
                        hashMap.put("uEmail", email);
                        hashMap.put("uDp", dp);
                        hashMap.put("pId", timeStamp);
                        hashMap.put("pTitle", title);
                        hashMap.put("pDescr", description);
                        hashMap.put("pImage", downloadUri);
                        hashMap.put("pTime", timeStamp);
                        hashMap.put("pLikes", "0");
                        hashMap.put("pComments", "0");

                        //path to store post data
                        DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");
                        ref.child(timeStamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                                        //reset views
                                        etitle.setText("");
                                        edescrip.setText("");
                                        image.setImageURI(null);
                                        image_uri = null;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    //failed uploading image
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            hashMap.put("pLikes", "0");
            hashMap.put("pComments", "0");

            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();

                            //reset views
                            etitle.setText("");
                            edescrip.setText("");
                            image.setImageURI(null);
                            image_uri = null;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    private boolean checkStoragePermission() {
        /*check if storage permission is enabled or not
         * return true if enabled
         * return false if not enabled
         * */
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        /*check if camera permission is enabled or not
         * return true if enabled
         * return false if not enabled
         * */
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showImagePickDialog() {
        //option (camera,gallery) to show in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from ");
        //set option to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //camera clicked
                        //check camera permission
                        if (!checkCameraPermission()) {
                            requestCameraPermission();
                        } else {
                            pickFromCamera();
                        }

                        break;
                    case 1:
                        //gallery clicked
                        if (!checkStoragePermission()) {
                            requestStoragePermission();
                        } else {
                            pickFromGallery();
                        }
                        break;
                }
            }
        });
        builder.create().show();
    }

    //handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*
         * this method is called when user press allow or deny from permission request dialog
         * here we will handle permision cases (allowed or denied)
         * */
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage both permissions are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission are necessary", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void pickFromCamera() {
        //intent to pick image from camera
        //ContentValues is a maplike class that matches a value to a String key,value
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        //this method will call after picking image from camera and gallery
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_PICK_GALLERY_CODE:
                    //image is picked from gallery ,get uri of image
                    image_uri = data.getData();

                    //set image
                    image.setImageURI(image_uri);
                    break;
                case IMAGE_PICK_CAMERA_CODE:
                    //image is picked from camera ,get uri of image
                    image.setImageURI(image_uri);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu, menu);

        //hind add post menu item
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                firebaseAuth.signOut();
                checkUserStatus();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            email = user.getEmail();
            uid = user.getUid();
            name = user.getDisplayName();

        } else {
            //user not signed in ,go to main activity
            startActivity(new Intent(AddPostActivity.this, MainActivity.class));
            finish();
        }
    }

}