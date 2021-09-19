package com.example.socialtestingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.socialtestingapp.adapter.AdapterChat;
import com.example.socialtestingapp.adapter.AdapterUsers;
import com.example.socialtestingapp.model.ModelChat;
import com.example.socialtestingapp.model.ModelUser;
import com.example.socialtestingapp.notification.Data;
import com.example.socialtestingapp.notification.Sender;
import com.example.socialtestingapp.notification.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
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
import com.google.gson.Gson;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    //image picked will be samed in this uri
    Uri image_uri = null;


    private static final String TAG = "ChatActivity";
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbref;
    //for checking if use has seen message or not
    ValueEventListener seenListener;
    AdapterChat adapterChat;
    DatabaseReference userRefForSeen;
    String hisUid;
    String myUid;
    String hisImage;
    List<ModelChat> chatlist;

    //volley request quque for notification
    private RequestQueue requestQueue;
    boolean notify = false;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private CircularImageView profile;
    private TextView txtHisname, userStatus;
    private EditText message;
    private ImageView blockTv;
    private ImageButton btnSend ,attachbtn;
    boolean isblock=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        initView();
        checkUserStatus();

        requestQueue= Volley.newRequestQueue(getApplicationContext());


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;

                //get text from edit
                String msg = message.getText().toString();
                //check if text is empty or not
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    //text not empty
                    sendMessage(msg);
                }
                //reset edittext
                message.setText("");
            }
        });

        attachbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //copy the code of pick image and handle permissions(storage/camera) from add post activity
                // TODO: 9/7/2021 add permission and image
                //show image pick dialog
                showImagePickDialog();
            }
        });

        //check edit text change listener
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(hisUid); //uid of receiver
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        blockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isblock) {
                    unblockUser(hisUid);
                } else {
                    blockUser(hisUid);
                }
            }
        });

        readMessages();

        checkIsBlocked();
        seenMessage();
    }

    private void checkIsBlocked() {
        Log.d(TAG, "checkIsBlocked: myUid::"+myUid +" hisUid::"+hisUid);
        DatabaseReference ref = firebaseDatabase.getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    if (d.exists()) {
                        blockTv.setImageResource(R.drawable.ic_block_red);
                        isblock=true;
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
                        blockTv.setImageResource(R.drawable.ic_block_red);
                        Toast.makeText(ChatActivity.this, "Blocked successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                //failed to block
                Toast.makeText(ChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                blockTv.setImageResource(R.drawable.ic_unblock_green);
                                                Toast.makeText(ChatActivity.this, "unlocked successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(ChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        //this method will call after picking image from camera and gallery
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMAGE_PICK_GALLERY_CODE:
                    //image is picked from gallery ,get uri of image
                    image_uri = data.getData();

                    //set image
                    //use this image uri to upload to firebase storage
                    try {
                        sendImageMessage(image_uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case IMAGE_PICK_CAMERA_CODE:
                    //image is picked from camera ,get uri of image
                    //use this image uri to upload to firebase storage
                    try {
                        sendImageMessage(image_uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        notify=true;

        //progress dialog
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending Message...");
        progressDialog.show();

        String timeStamp=""+System.currentTimeMillis();

        String fileNameAndPath="ChatImages/"+"post_"+timeStamp;

        /*chat node will be created that will contain all images send via chat*/

        //get bitmap form image uri
        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_uri);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[]data=baos.toByteArray(); //cover image to byte

        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                //image upload
                //get url of uploaded image
                Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                    String downloadUri=uriTask.getResult().toString();

                    if(uriTask.isSuccessful()){
                        //add image uri and other info to database
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

                        //setup required data
                        HashMap<String ,Object>hashMap=new HashMap<>();
                        hashMap.put("sender", myUid);
                        hashMap.put("receiver", hisUid);
                        hashMap.put("message",downloadUri);
                        hashMap.put("timeStamp",timeStamp);
                        hashMap.put("type","image");
                        hashMap.put("isSeen",false);

                        //send this data to firebase
                        databaseReference.child("Chats").push().setValue(hashMap);


                        //send notification
                        DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                        database.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                ModelUser user=snapshot.getValue(ModelUser.class);
                                if(notify){
                                    senNotification(hisUid,user.getName(),"Sent you a photo....");
                                notify=false;
                                }
                                // TODO: 9/7/2021 next version is to add notification successfully

                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                        //create chatlist node/child in firebase database
                        DatabaseReference chatRef=firebaseDatabase.getReference("Chatlist")
                                .child(myUid).child(hisUid);
                        chatRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(!snapshot.exists()){
                                    chatRef.child("id").setValue(hisUid);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

                    }
                }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressDialog.dismiss();
            }
        });
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    ModelChat chat = d.getValue(ModelChat.class);
                    if (null != chat) {
                        if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)) {
                            HashMap<String, Object> hasSeenHash = new HashMap<>();
                            hasSeenHash.put("isSeen", true);
                            d.getRef().updateChildren(hasSeenHash);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatlist = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                chatlist.clear();
                for (DataSnapshot d : snapshot.getChildren()) {
                    ModelChat chat = d.getValue(ModelChat.class);

                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatlist.add(chat);
                    }

                    for (ModelChat i : chatlist) {

                    }
                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatlist, hisImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);


                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String msg) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", msg);
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("isSeen", false);
        hashMap.put("type", "text");
        databaseReference.child("Chats").push().setValue(hashMap);

        String messg = msg;
        DatabaseReference database = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);

                if (notify) {
                    senNotification(hisUid, user.getName(), messg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //create chatlist node/child in firebase database
        DatabaseReference chatRef=firebaseDatabase.getReference("Chatlist")
                .child(myUid).child(hisUid);
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2=firebaseDatabase.getReference("Chatlist")
                .child(hisUid).child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void senNotification(String hisUid, String name, String msg) {
        DatabaseReference alltoken = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Tokens");
        Query query = alltoken.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token=ds.getValue(Token.class);
                    Data data = new Data(myUid, name + ":" + msg, "New Message", hisUid, R.drawable.ic_avatar_default);

                    Sender sender = new Sender(data, token.getToken());
                    //from json object request
                    try {
                        JSONObject senderJsonObj=new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //response of the request
                                        Log.d("JSON_RESPONSE", "onResponse: "+response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                        Log.d("JSON_RESPONSE", "onResponse: "+error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put params
                                Map<String,String >headers=new HashMap<>();
                                headers.put("Content-Type","application/json");
                                headers.put("Authorization","key=AAAAtNCDcyg:APA91bEHGOwQYgVa_RbAdubBlFZgaoc8ekw1EzhzKL0_W56gEnfTmhKmy7QArMLnot9byQuHoKThdxq0PBq_JHzVNgLXYN6T6sSyWPX0x6-rLsOXNP0TJApkwnHSvgODaIPTC6G7S45c");
                                return super.getHeaders();
                            }
                        };
                        //add this request to queue
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        blockTv=findViewById(R.id.blockTv);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.chatRecyclerView);
        profile = findViewById(R.id.profileAvatar);
        txtHisname = findViewById(R.id.txtHisName);
        userStatus = findViewById(R.id.userStatus);
        message = findViewById(R.id.message);
        btnSend = findViewById(R.id.sendBtn);
        attachbtn=findViewById(R.id.attachbtn);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");

        //init permissions arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //layout (linearLayout for recyclerview)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recyclerView properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseDatabase = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersDbref = firebaseDatabase.getReference("Users");

        //search user to get that user's info
        Query userQuery = usersDbref.orderByChild("uid").equalTo(hisUid);

        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //check until required info is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    hisImage = "" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();

                    //check typing status
                    if (typingStatus.equals(myUid)) {
                        userStatus.setText("Typing....");
                    } else {
                        //get value of onlineStatus
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")) {
                            userStatus.setText(onlineStatus);
                        } else {
                            //convert timestamp to proper time date
                            //conver time stamp to dd/mm/yyyy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = (String) DateFormat.format("dd/MM/yyyy hh:mm:aa", cal);
                            userStatus.setText("Last seen at: " + dateTime);
                        }
                    }
                    //set data
                    txtHisname.setText(name);

                    try {
                        Picasso.get().load(hisImage).into(profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set ofline with last seen time stamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("onOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            myUid = user.getUid();
            Log.d(TAG, "checkUserStatus: MyUid::"+myUid);
        } else {
            //user not signed in ,go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        //hide searchview ,add post ,as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

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

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        //update value of onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        //update value of onlineStatus of current user
        dbRef.updateChildren(hashMap);
    }
}