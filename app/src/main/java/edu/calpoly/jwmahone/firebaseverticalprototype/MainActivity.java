package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static final String FIREBASEURL = "https://popping-inferno-9423.firebaseio.com/";
    private Firebase fireRoot;
    private Button addLineButton;
    private EditText postEditTextField;
    private Button commentButton;
    private EditText commentEditTextField;
    private String lastKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fireRoot = new Firebase(FIREBASEURL);
        if (fireRoot.getAuth() == null) {
            startLogin();
        }

        final AuthData currUser = fireRoot.getAuth();
        final String currEmail = (String)currUser.getProviderData().get("email");

        Log.d("email: ", currEmail);
        addLineButton = (Button) findViewById(R.id.submitLineButton);
        postEditTextField = (EditText) findViewById(R.id.lineEditText);
        commentButton = (Button) findViewById(R.id.commentButton);
        commentEditTextField = (EditText) findViewById(R.id.commentEditText);

        addLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //how to increment likes here
                String post = postEditTextField.getText().toString();
                postEditTextField.setText("");

                final MountainPost mp = new MountainPost(post, currEmail);
                mp.addComment("test comment 1");
                mp.addComment("test comment 2");
                mp.like();
                final Firebase postRef = fireRoot.child("mountainPosts");

                postRef.push().setValue(mp);

                postRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String key = dataSnapshot.getKey();
                        mp.setPostKey(key);
                        Map<String, Object> vals = new HashMap<>();
                        vals.put("postKey", key);
                        postRef.child(key).updateChildren(vals);

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Firebase postRef = fireRoot.child("mountainPosts").child(lastKey);
                String comment = commentEditTextField.getText().toString();
                commentEditTextField.setText("");
                Map<String, Object> comments = new HashMap<>();
                comments.put("comments", comment);
                postRef.updateChildren(comments);

            }
        });
    }

    public void startLogin() {
        Intent intent = new Intent(this, LoginScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuItemID = item.getItemId();

        if (menuItemID == R.id.logout) {
            fireRoot.unauth();
            startLogin();
        }

        return super.onOptionsItemSelected(item);
    }
}
