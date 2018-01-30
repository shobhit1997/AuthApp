package com.authapp.shobhit.authapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    EditText username,email,password;
    Button signIn;
    private FirebaseAuth mAuth;
    String usernameV,passwordV,emailV;
    ProgressDialog progressDialog;
    Menu menu1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
    }



    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        updateUI(currentUser);

    }
    private void updateUI(FirebaseUser user)
    {

        if(user==null)
        {
            setContentView(R.layout.activity_main);
            username=(EditText)findViewById(R.id.username);
            email=(EditText)findViewById(R.id.email);
            password=(EditText)findViewById(R.id.password);
            signIn=(Button)findViewById(R.id.button2);

            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    progressDialog=new ProgressDialog(MainActivity.this);


                    emailV=email.getText().toString();
                    passwordV=String.valueOf(password.getText());
                    usernameV=String.valueOf(username.getText());

                    Log.i("email",emailV+passwordV+usernameV);

                    progressDialog.setTitle("Please Wait... ");
                    progressDialog.setMessage("Signing In...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    progressDialog.setCancelable(false);

                    mAuth.createUserWithEmailAndPassword(emailV,passwordV)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful())
                                    {

                                        progressDialog.setMessage("Sending Verfication Email... ");
                                        FirebaseUser firebaseUser=mAuth.getCurrentUser();
                                        firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        progressDialog.dismiss();

                                                        if(task.isSuccessful())
                                                        {
                                                            SharedPreferences sharedPreferences=getSharedPreferences("User", Context.MODE_PRIVATE);
                                                            SharedPreferences.Editor editor=sharedPreferences.edit();
                                                            editor.putString("username",usernameV);
                                                            editor.apply();
                                                            Toast.makeText(MainActivity.this,"Verfication email sent",Toast.LENGTH_SHORT).show();
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(MainActivity.this,"Invalid Email",Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                    }
                                    else
                                    {
                                        if(task.getException().getMessage().equalsIgnoreCase("The email address is already in use by another account."))
                                        {
                                            mAuth.signInWithEmailAndPassword(emailV,passwordV)
                                                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            progressDialog.dismiss();
                                                            if(task.isSuccessful())
                                                            {

                                                                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                                                                SharedPreferences sharedPreferences=getSharedPreferences("User", Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                                                editor.putString("username",usernameV);
                                                                editor.apply();
                                                                updateUI(firebaseUser.getDisplayName(),firebaseUser.isEmailVerified());
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(MainActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });

                                        }
                                        else
                                        {
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                            });


                }
            });

        }
        else
        {
            Log.i("Verified",user.isEmailVerified()+"");
            updateUI(user.getDisplayName(),user.isEmailVerified());
        }
    }

    private void updateUI(String name,boolean verified)
    {
        if(verified)
        {
            setContentView(R.layout.name_layout);
            TextView textView=(TextView)findViewById(R.id.textView);
            SharedPreferences sharedPreferences=getSharedPreferences("User", Context.MODE_PRIVATE);
            name=sharedPreferences.getString("username","User");
            textView.setText("Hello " +name);

            Button button=(Button)findViewById(R.id.button3);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    updateUI(null);
                }
            });
        }
        else
        {
            Toast.makeText(this,"Pease Verify your email and SignIn",Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }

    }
}
