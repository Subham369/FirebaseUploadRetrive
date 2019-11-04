package com.example.firebaseuploadretrive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private  static  final int PICK_IMAGE_REQUEST=1;
    private Button mButtonChooseImage;
    private  Button mButtonUpload;
    private TextView mTextViewShowUpload;
    private EditText mEditTextFileName;
    private ProgressBar progressBar;
    private ImageView mImageView;

    private Uri mImageUri;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    private StorageTask storageTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonChooseImage=findViewById(R.id.imgchoose);
        mButtonUpload=findViewById(R.id.btnupload);
        mTextViewShowUpload=findViewById(R.id.showupload);
        mEditTextFileName=findViewById(R.id.filename);
        progressBar=findViewById(R.id.progressbar);
        mImageView=findViewById(R.id.imageview);

        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference("uploads");
        databaseReference= FirebaseDatabase.getInstance().getReference("uploads");


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storageTask!=null && storageTask.isInProgress())
                {
                    Toast.makeText(MainActivity.this, "Upload in progress ....", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    uploadFile();
                }
            }
        });

        mTextViewShowUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openImageActivity();
            }
        });
    }

    private void openFileChooser()
    {
        Intent intent=new Intent();
        intent.setType("image/");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
           mImageUri=data.getData();

            Picasso.with(this).load(mImageUri).into(mImageView);

        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cr=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFile()
    {
        if (mImageUri!=null)
        {
            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));
            storageTask=fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Handler handler= new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                          progressBar.setProgress(0);
                        }
                    },5000);
                   fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                       @Override
                       public void onSuccess(Uri uri) {
                           Upload upload=new Upload(mEditTextFileName.getText().toString().trim(),uri.toString());
                           String uploadId=databaseReference.push().getKey();
                           databaseReference.child(uploadId).setValue(upload);
                           Toast.makeText(MainActivity.this, "Upload successfully", Toast.LENGTH_LONG).show();
                       }
                   });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error1:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double prog=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)prog);
                }
            });
        }
        else
        {
            Toast.makeText(this, "No file selected !!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        firebaseAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
                Toast.makeText(MainActivity.this, "Logged in successfully ....", Toast.LENGTH_LONG).show();
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //Log.e(TAG, "signInAnonymously:FAILURE", exception);
                       // Toast.makeText(MainActivity.this, "Error : "+exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openImageActivity()
    {
        Intent intent=new Intent(MainActivity.this,ImageActivity.class);
        startActivity(intent);
    }
}
