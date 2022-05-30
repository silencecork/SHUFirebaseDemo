package com.example.shufirebasedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class MemoActivity extends AppCompatActivity {

    private EditText mTitle;
    private EditText mContent;
    private Button mSaveButton;
    private DatabaseReference mReference;
    private String TAG = "MemoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        String userId = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mReference = database.getReference("memo").child(userId);

        mTitle = findViewById(R.id.input_title);
        mContent = findViewById(R.id.input_content);
        mSaveButton = findViewById(R.id.save);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = mTitle.getEditableText().toString();
                String content = mContent.getEditableText().toString();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                    return;
                }

                Memo memo = new Memo();
                memo.title = title;
                memo.content = content;

                mReference.setValue(memo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MemoActivity.this, "儲存成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MemoActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                Memo memo = snapshot.getValue(Memo.class);
                mTitle.setText(memo.title);
                mContent.setText(memo.content);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
                Toast.makeText(MemoActivity.this, error.toException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}