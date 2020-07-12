package com.example.chatbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button registerBtn;
    private EditText userEmail,userPassword,confirmPassword;
    private TextView haveAnAccount;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializingFields();

        firebaseAuth=FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        haveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

    }
    private void createNewAccount() {

        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        String confirmPass=confirmPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(confirmPass))
        {
            Toast.makeText(this, "Please re-enter your password!", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirmPass))
        {
            Toast.makeText(this, "Passwords doesn't match!", Toast.LENGTH_SHORT).show();
        }
        else{
            progressDialog.setTitle("Creating Your Account");
            progressDialog.setMessage("Please wait! We are creating your account!");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String currentUserId=firebaseAuth.getCurrentUser().getUid();
                                databaseReference.child("Users").child(currentUserId).setValue("");
                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                            else{
                                String errorMsg=task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error: "+errorMsg, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }

                        }
                    });
        }
    }

    private void InitializingFields() {
        registerBtn=(Button)findViewById(R.id.register_button);
        userEmail=(EditText)findViewById(R.id.register_email);
        userPassword=(EditText)findViewById(R.id.register_password);
        confirmPassword=(EditText)findViewById(R.id.confirm_password);
        haveAnAccount=(TextView)findViewById(R.id.already_account);
        progressDialog=new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        Intent authIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(authIntent);
    }

    private void sendUserToMainActivity() {
        Intent authIntent=new Intent(RegisterActivity.this,MainActivity.class);
        authIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(authIntent);
        finish();
    }
}
