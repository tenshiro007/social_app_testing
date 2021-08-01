package com.example.socialtestingapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.security.Key;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    //storage
    StorageReference storageReference;
    //path where image of user profile and cover will be stored
    String storagePath="Users_Profile_Cover_Imgs/";


    //view
    private ImageView avatar,coverImage;
    private FloatingActionButton floatAction;
    private TextView txtName, txtEmail, txtPhone;
    private ProgressDialog progressDialog;

    //Permission contants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;

    //Array of permission to be requested
    String cameraPermission[];
    String storagePermission[];

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover
    String profileOrCoverPhoto;



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initView(view);

        /*get info of current signed in user. we can get it using user's email or uid
        (this using email) By using orderByChild query we will show the dialog form node
        whose key name email has value equal to currently signed in email. it willl search all nodes,
        where the key matches it will get its detail
         */
        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    txtName.setText(name);
                    txtEmail.setText(email);
                    txtPhone.setText(phone);
                    try {
                        if (!image.equals("")) {
                        Picasso.get().load(image).into(avatar);
//                            Glide.with(view).asBitmap().load(image)
//                                    .into(avatar);

                        }
                        if (!cover.equals("")) {
                            Picasso.get().load(cover).into(coverImage);
//                            Glide.with(view).asBitmap().load(cover)
//                                    .into(coverImage);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Picasso.get().load(R.drawable.ic_add_photo_small).into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        floatAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private void showEditProfileDialog() {
        /*Show Dialog Option edit photo,cover photo,name,phone*/

        //show in dialog
        String options[]={"Edit Profile picture","Edit Cover Photo","Edit Name","Edit Phone"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                switch (which){
                    case 0:
                        progressDialog.setMessage("Updating Profile Picture");
                        profileOrCoverPhoto ="image";
                        showImagePicDialog();
                        break;
                    case 1:
                        progressDialog.setMessage("Updating Cover Photo");
                        profileOrCoverPhoto ="cover";
                        showImagePicDialog();
                        break;
                    case 2:
                        progressDialog.setMessage("Updating Name");
                        showNamePhoneUpdateDialog("name");
                        break;
                    case 3:
                        progressDialog.setMessage("Updating Phone");
                        showNamePhoneUpdateDialog("phone");
                        break;
                }
            }
        }).create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        /*
            key will contain value:
        "name" which is key in user's database which is used to update user's name
        "phone" which is key in user's database which is used to update user's phone
         */
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);
        //set layout of dialog
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //add edit text
        EditText editText=new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text form edit text
                String value=editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    progressDialog.show();
                    HashMap<String,Object>result=new HashMap<>();
                    result.put(key,value);

                    reference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated.... " , Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage() , Toast.LENGTH_SHORT).show();

                        }
                    });
                }else{
                    Toast.makeText(getActivity(), "Please enter "+key , Toast.LENGTH_SHORT).show();
                }

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog containing option camere and galery to pick the image
        //show in dialog
        String options[]={"Camera","Gallery"};
        //alert dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog item clicks
                switch (which){
                    case 0:
                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        }else{
                            pickFromCamera();
                        }
                       //camera clicked
                        break;
                    case 1:
                        if(!checkStoragePermission()){
                            requestStoragePermission();
                        }else{
                            pickFromGallery();
                        }
                        //gallery clicked
                        break;

                }
            }
        }).create().show();
        /*
        *for picking image from :
        * camera [camera and storage permission required]
        * gallery [storage permission required]
        * */
    }

    private void initView(View view) {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");
        reference = firebaseDatabase.getReference("Users");

        storageReference= FirebaseStorage.getInstance().getReference(); //firebase storage reference

        //init Views
        avatar = view.findViewById(R.id.avatar);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        coverImage=view.findViewById(R.id.coverPhoto);
        floatAction=view.findViewById(R.id.btnFloating);

        //init progression
        progressDialog=new ProgressDialog(getContext());


        //init array of permission
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }
    private boolean checkStoragePermission(){
        //check if storage permission is enable or not
        //return true if enabled
        //return false if not enable
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission(){
        //check if storage permission is enable or not
        //return true if enabled
        //return false if not enable
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result &&result1;
    }
    private void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
        * this method called when user press allow or deny from permission request dialog
        * here we will handle permission cases(allowed &denied)
        */

        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                //picking from camera,first check if camera and storage permission allowed or not
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAcceipted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAcceipted){
                        //permission enabled
                        pickFromCamera();
                    }else{
                        //permission denied
                        Toast.makeText(getActivity(), "Plase enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                //picking from gallery,first check if storage permission allowed or not
                if(grantResults.length>0){
                    boolean writeStorageAcceipted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if( writeStorageAcceipted){
                        //permission enabled
                        pickFromGallery();
                    }else{
                        //permission denied
                        Toast.makeText(getActivity(), "Plase enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void  pickFromCamera() {
        //Intent of picking image form device camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        //this method called after picking image from camara or gallery

        if(resultCode==RESULT_OK){
            switch(requestCode){
                case IMAGE_PICK_GALLERY_CODE:
                    //image is picked from gallery,get uri of image
                    image_uri=data.getData();

                    uploadProfileCoverPhoto(image_uri);
                    break;
                case IMAGE_PICK_CAMERA_CODE:
                    //image is picked from camera,get uri of image
//                    image_uri=data.getData();

                    if(null!=image_uri){
                        uploadProfileCoverPhoto(image_uri);
                    }else{
                        Log.d(TAG, "onActivityResult: camera-imgea"+image_uri);
                    }
                    break;
                default:
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri image_uri) {
        progressDialog.show();

        /*Instead of creating separate function for profile picture and cover photo
        * i'm doing in the same function
        *
        * To add check ill add a string variable and assign it value "image" when
        * user clicks "Edit profile pic" ,and assign value "cover" when user clicks
        * "Edit cover photo"
        * Here :image is the key in each user containing url of user's profile picture
        *      cover si the key in each user containing url of user's cover photo*/

        /*The parameter "image_uri" contains the uri of image picked either from camera or gallery
        we will use UID of the currently signed in user as name of the image so there will be only one image
        profile and image for cover for each user
         */


        //path and name of image to be stored in firebase storage
        String filePathAndName=storagePath+""+profileOrCoverPhoto+"_"+user.getUid();
        StorageReference storageReference2nd=storageReference.child(filePathAndName);
        storageReference2nd.putFile(image_uri)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded to storage,now get it's url and store in user's database
                Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                Uri downloadUri=uriTask.getResult();

                //check if image is uploaded or not  and uri is received
                if(uriTask.isSuccessful()){
                    //image uploaded
                    //add/updae url in user's database
                    HashMap<String,Object>results=new HashMap<>();
                    /*First parameter is profileOrCoverPhoto that has value "image or cover"
                      which are keys in user's database where url of image will be saved in one
                      of them

                      Secound paremeter contains the url of the image stored in firebase storage ,
                      this url will be saved as value against key "image" or "cover"
                     */

                    results.put(profileOrCoverPhoto,downloadUri.toString());
                    reference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //url in database of user is added successfully
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull  Exception e) {
                            //error adding url in database of user
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    //error
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Some error occored", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                //there were some error(s) ,get and show message ,dismiss progress dialog
                progressDialog.dismiss();
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}