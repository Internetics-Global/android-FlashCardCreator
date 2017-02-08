package com.flipflash.android_ffc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by internetics on 17/1/17.
 */

public class FirebaseSignInActivity extends Activity implements
        View.OnClickListener {

    public ProgressDialog mProgressDialog;

    private static final String TAG = "EmailPassword";
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signup(String email, String password) {
        Log.d(TAG, "createcreateAccountAccount:" + email);
        if (!validateForm()) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    FirebaseSignInActivity.this);
            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
            alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
            alertDialogBuilder
                    .setMessage("Email or password could not be empty").show();

            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        hideProgressDialog();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    FirebaseSignInActivity.this);
                            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                            alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                            alertDialogBuilder
                                    .setMessage(R.string.FIREBASE_FAIL_TO_CREATE_ACCOUNT).show();

                        } else {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    FirebaseSignInActivity.this);
                            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                            alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                            alertDialogBuilder
                                    .setMessage(R.string.AWS_DRIVE_LOGIN_SUCCESS).show();

                            finish();
                        }

                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                e.printStackTrace();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        FirebaseSignInActivity.this);
                alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                alertDialogBuilder
                        .setMessage(e.getMessage()).show();

            }
        })
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideProgressDialog();
            }
        });

    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        hideProgressDialog();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(FirebaseSignInActivity.this, "failed todo",
                                    Toast.LENGTH_SHORT).show();
                        }  else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    FirebaseSignInActivity.this);
                            alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                            alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                            alertDialogBuilder
                                    .setMessage(R.string.AWS_DRIVE_LOGIN_SUCCESS).show();

                            finish();
                        }

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        FirebaseSignInActivity.this);
                alertDialogBuilder.setTitle(R.string.DIALOG_AlERT);
                alertDialogBuilder.setPositiveButton(R.string.DIALOG_CLOSE,null);
                alertDialogBuilder
                        .setMessage(e.getMessage()).show();
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            signup(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }
}