package com.example.firebaseuploadretrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class ImageActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private DatabaseReference databaseReference;
    private List<Upload> mUpload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mRecyclerView=findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ImageActivity.this));
        mRecyclerView.setHasFixedSize(true);


        mUpload=new ArrayList<>();
        databaseReference= FirebaseDatabase.getInstance().getReference("uploads");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    Upload upload=postSnapshot.getValue(Upload.class);
                    mUpload.add(upload);
                }
                mAdapter=new ImageAdapter(ImageActivity.this,mUpload);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ImageActivity.this, "Error:"+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
