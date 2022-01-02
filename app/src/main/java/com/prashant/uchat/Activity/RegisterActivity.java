package com.prashant.uchat.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prashant.uchat.R;
import com.prashant.uchat.ModelClass.Users;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    TextView txt_signin, btn_signUp;
    CircleImageView profile_image;
    EditText reg_email, reg_name , reg_pass , reg_cPass;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    Uri imageUri;
    String imageURI;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        txt_signin = findViewById(R.id.txt_signin);
        profile_image = findViewById(R.id.profile_image);
        reg_email = findViewById(R.id.reg_email);
        reg_name = findViewById(R.id.reg_name);
        reg_pass = findViewById(R.id.reg_pass);
        reg_cPass = findViewById(R.id.reg_cPass);
        btn_signUp = findViewById(R.id.btn_signUp);


        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                String name = reg_name.getText().toString();
                String email = reg_email.getText().toString();
                String pass = reg_pass.getText().toString();
                String cPass = reg_cPass.getText().toString();
                String status = "Hey there ! I'm using uChat !!";


                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(cPass))
                {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this , "Input fields cannot be Empty" , Toast.LENGTH_SHORT).show();
                }
                else if(!email.matches(emailPattern))
                {
                    reg_email.setError("Please enter Valid Email");
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this , "Please enter Valid Email" , Toast.LENGTH_SHORT).show();
                }
                else if(!pass.equals(cPass))
                {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this , "Password fields does not match" , Toast.LENGTH_SHORT).show();
                }
                else if(pass.length()<6)
                {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this , "Password must be of atleast 6 characters" , Toast.LENGTH_SHORT).show();
                }else
                {
                    auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {
                                DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
                                StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

                                if(imageUri != null)
                                {
                                    storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                            if(task.isSuccessful())
                                            {
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String status = "Hey there ! I'm using uChat !!";
                                                        imageURI=uri.toString();
                                                        Users users = new Users(auth.getUid(),name,email,imageURI,status);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    progressDialog.dismiss();
                                                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(RegisterActivity.this, "Error in Creating a New User", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                        }
                                    });
                                }
                                else
                                {
                                    String status = "Hey there ! I'm using uChat !!";
                                    imageURI="https://firebasestorage.googleapis.com/v0/b/uchat-2e8ec.appspot.com/o/profile_image.png?alt=media&token=be03336e-0c41-49b2-8705-8e66619f9c2a";
                                    Users users = new Users(auth.getUid(),name,email,imageURI,status);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                            }
                                            else
                                            {
                                                Toast.makeText(RegisterActivity.this, "Error in Creating a New User", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this , "Something went Wrong" , Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });


        txt_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==10)
        {
            if(data != null)
            {
                imageUri = data.getData();
                profile_image.setImageURI(imageUri);
            }
        }
    }
}