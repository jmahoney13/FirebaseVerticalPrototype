package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    public static final String FIREBASEURL = "https://popping-inferno-9423.firebaseio.com/";
    private Firebase fireRoot = new Firebase(FIREBASEURL);
    private Button addLineButton;
    private EditText postEditTextField;
    private Button commentButton;
    private EditText commentEditTextField;
    private String lastKey;
    private TextView mountainTitle;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapseLayout;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final AuthData currUser = fireRoot.getAuth();
        if (currUser == null) {
            startLogin();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        collapseLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolBarLayout);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String mName = extras.getString("MOUNTAIN_NAME");
            collapseLayout.setTitle(mName);
        }
        else {
            collapseLayout.setTitle("THIS IS COOL");
        }

        /*
        ImageView header = (ImageView) findViewById(R.id.headerImage);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.squaw);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(R.color.colorPrimary);
                collapseLayout.setContentScrimColor(mutedColor);
            }
        });*/

    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fireRoot = new Firebase(FIREBASEURL);
        final AuthData currUser = fireRoot.getAuth();

        if (currUser == null) {
            startLogin();
        }

        setContentView(R.layout.activity_main);

        //final AuthData currUser = fireRoot.getAuth();
        //final String currEmail = (String)currUser.getProviderData().get("email");

        //Log.d("email: ", currEmail);
        addLineButton = (Button) findViewById(R.id.submitLineButton);
        postEditTextField = (EditText) findViewById(R.id.lineEditText);
        commentButton = (Button) findViewById(R.id.commentButton);
        commentEditTextField = (EditText) findViewById(R.id.commentEditText);

        mountainTitle = (TextView) findViewById(R.id.mainActWelcome);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String mName = extras.getString("MOUNTAIN_NAME");
            mountainTitle.setText(mName);
        }

        addLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //how to increment likes here
                String post = postEditTextField.getText().toString();
                postEditTextField.setText("");

                String currEmail = (String)currUser.getProviderData().get("email");
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
    */

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
