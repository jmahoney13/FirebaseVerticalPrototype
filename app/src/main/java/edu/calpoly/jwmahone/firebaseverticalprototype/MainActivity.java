package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.firebase.client.ValueEventListener;


//test github comment!!!

public class MainActivity extends AppCompatActivity {
    public static final String FIREBASEURL = "https://popping-inferno-9423.firebaseio.com/";
    private Firebase fireRoot;
    private Button addLineButton;
    private EditText postEditTextField;
    private Button getLineButton;
    private TextView receivedPostTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fireRoot = new Firebase(FIREBASEURL);
        if (fireRoot.getAuth() == null) {
            startLogin();
        }

        final AuthData currUser = fireRoot.getAuth();
        if (currUser != null) {
            Toast.makeText(this, "User: " + currUser.getUid() + " is logged in with " + currUser.getProviderData(), Toast.LENGTH_LONG).show();
        }

        addLineButton = (Button) findViewById(R.id.submitLineButton);
        postEditTextField = (EditText) findViewById(R.id.lineEditText);
        getLineButton = (Button) findViewById(R.id.getLineButton);
        receivedPostTextView = (TextView) findViewById(R.id.receivedPostTextView);

        addLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //HOW DO YOU GRAB THE EMAIL FROM THE USER LOGGED IN RIGHT HERE!!!!!!!!!!!!!!!!!
                //how to increment likes here
                String post = postEditTextField.getText().toString();
                postEditTextField.setText("");

                assert currUser != null;
                MountainPost mp = new MountainPost(post, currUser.getUid());
                Firebase newRef = fireRoot.child("users").child(currUser.getUid()).child("posts");
                newRef.push().setValue(mp);
            }
        });

        getLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Firebase postRef = fireRoot.child("users").child(currUser.getUid()).child("posts");
                Query qRef = postRef.orderByChild("author").equalTo(currUser.getUid());

                qRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        MountainPost recPost = dataSnapshot.getValue(MountainPost.class);
                        receivedPostTextView.setText(recPost.getLine());
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
