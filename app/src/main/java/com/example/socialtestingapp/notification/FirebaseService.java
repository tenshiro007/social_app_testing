package com.example.socialtestingapp.notification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.socialtestingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.jetbrains.annotations.NotNull;

public class FirebaseService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseService";

    @Override
    public void onNewToken(@NonNull @NotNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String tokenRefresh = task.getResult();

                        // Log
                        Log.d(TAG, "onComplete: token ::" + tokenRefresh);

                        if (user != null) {
                            updateToken(tokenRefresh);
                        }
                    }
                });
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance("https://socialapptesting-f0a1e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Tokens");
        Token token=new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);
    }

}
