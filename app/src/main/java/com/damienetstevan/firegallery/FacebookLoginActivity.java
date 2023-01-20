package com.damienetstevan.firegallery;

import static com.damienetstevan.firegallery.Utils.makeLongErrorToast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FacebookLoginActivity extends Activity {
    private static final String TAG = "FacebookLogin";
    private FirebaseAuth auth;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        // initialize firebase auth & callback manager (to handle facebook login events)
        auth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        // register callbacks for the authentication
        final LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(@NonNull final FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                makeLongErrorToast(FacebookLoginActivity.this, error.getMessage());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // disable button to avoid any conflict
        final Button button = findViewById(R.id.login_button);
        button.setEnabled(false);

        // sign in the user with firebase auth
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // success, start the main activity
                        Log.d(TAG, "signInWithCredential:success");
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        // got an error, make a toast
                        Log.w(TAG, "signInWithCredential:failure", task.getException());

                        assert task.getException() != null;
                        makeLongErrorToast(FacebookLoginActivity.this, task.getException().getMessage());
                    }
                });
    }
}