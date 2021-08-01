package com.example.socialtestingapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText edEmail,edPassword;
    private TextView txthaveAccount;
    private Button btnRegister;
    private ProgressDialog progressDialog;
    //Declare an instance of firebase
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //progressbar to display while register user
        //Actionbar and title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        initView();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=edEmail.getText().toString().trim();
                String password=edPassword.getText().toString().trim();

                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email
                    edEmail.setError("Invalid Email");
                    edEmail.setFocusable(true);
                }else if(password.length()<6){
                    edPassword.setError("Password length at least 6");
                    edPassword.setFocusable(true);
                }else {
                    registerUser(email,password);
                }

            }
        });

//        handle login text click listener
        txthaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String  email=user.getEmail();
                            String uid=user.getUid();
                            HashMap<Object,String >hashMap=new HashMap<>();

                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");//we will add later
                            hashMap.put("phone","");
                            hashMap.put("image","");
                            hashMap.put("cover","");
                            //firebase database instance
                            FirebaseDatabase database=FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/");
                            //path to store user data nameed"Users"
                            DatabaseReference reference=database.getReference("Users");
                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Registered.....\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
//                            updateUI(user);
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
//
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@androidx.annotation.NonNull  Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initView() {
        edEmail=findViewById(R.id.emailEt);
        edPassword=findViewById(R.id.passwordEt);
        btnRegister=findViewById(R.id.btnRegister);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Registering User....");
        txthaveAccount=findViewById(R.id.txthave_accout);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to previous activity
        return super.onSupportNavigateUp();
    }
}