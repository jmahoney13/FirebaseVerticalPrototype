package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;



public class MainActivity extends AppCompatActivity {
    public static final String FIREBASEURL = "https://popping-inferno-9423.firebaseio.com/";
    private Firebase fireRoot = new Firebase(FIREBASEURL);
    private Button addLineButton;
    private EditText postEditTextField;
    private Button commentButton;
    private EditText commentEditTextField;
    private String lastKey;
    private TextView mountainTitle;

    private RecyclerView postsRecyclerView;
    private FirebaseRecyclerAdapter<MountainPost, PostsViewHolder> recyclerAdapater;
    private String mName;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapseLayout;
    private FloatingActionButton fab;
    private Firebase adapterRoot;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final AuthData currUser = fireRoot.getAuth();
        if (currUser == null) {
            startLogin();
        }

        this.postsRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        collapseLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolBarLayout);
        this.postsRecyclerView.setHasFixedSize(true);
        this.postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //LinearLayoutManager lm = new LinearLayoutManager(this);
        //lm.setReversedLayout(true);
        //postsRecyclerView.setLayoutManager(lm);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString("MOUNTAIN_NAME");
            collapseLayout.setTitle(mName);

        }
        else {
            collapseLayout.setTitle("THIS IS COOL");
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //ImageView header = (ImageView) findViewById(R.id.headerImage);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.squaw);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(ContextCompat.getColor(MainActivity.this, R.color.primary_500));
                collapseLayout.setContentScrimColor(mutedColor);
            }
        });


        adapterRoot = fireRoot.child("mountain").child(collapseLayout.getTitle().toString()).child("posts");

        this.recyclerAdapater = new FirebaseRecyclerAdapter<MountainPost, PostsViewHolder>(MountainPost.class, android.R.layout.two_line_list_item, PostsViewHolder.class, adapterRoot) {
            @Override
            public void populateViewHolder(PostsViewHolder postsViewHolder, MountainPost mountainPost, int position) {
                Log.d("POPULATEVIEWHOLDER", " MADE IT");
                Log.d("number viewholder: ", "" + recyclerAdapater.getItemCount());
                postsViewHolder.postText.setText(mountainPost.getLine());
                postsViewHolder.authorText.setText(mountainPost.getAuthor());
            }
        };

        this.postsRecyclerView.setAdapter(this.recyclerAdapater);
        Log.d("number of items: ", "" + this.recyclerAdapater.getItemCount());



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View addView = li.inflate(R.layout.activity_add_post, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(addView);

                final EditText postInput = (EditText) addView.findViewById(R.id.addPostEditText);

                alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MountainPost mp = new MountainPost(postInput.getText().toString(), (String)currUser.getProviderData().get("email"));
                            adapterRoot.push().setValue(mp);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }
/*
    @Override
    public void onStart() {
        super.onStart();

        adapterRoot = fireRoot.child("mountain").child(collapseLayout.getTitle().toString()).child("posts");

        this.recyclerAdapater = new FirebaseRecyclerAdapter<MountainPost, PostsViewHolder>(MountainPost.class, android.R.layout.two_line_list_item, PostsViewHolder.class, adapterRoot) {
            @Override
            public void populateViewHolder(PostsViewHolder postsViewHolder, MountainPost mountainPost, int position) {
                Log.d("POPULATEVIEWHOLDER", " MADE IT");
                Log.d("number viewholder: ", "" + recyclerAdapater.getItemCount());
                postsViewHolder.postText.setText(mountainPost.getLine());
                postsViewHolder.authorText.setText(mountainPost.getAuthor());
            }
        };

        this.postsRecyclerView.setAdapter(this.recyclerAdapater);
        Log.d("number of items: ", "" + this.recyclerAdapater.getItemCount());
    }
*/

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        private TextView postText;
        private TextView authorText;

        public PostsViewHolder(View itemView) {
            super(itemView);
            postText = (TextView) itemView.findViewById(android.R.id.text1);
            authorText = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MainActivity.this, MountainSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.recyclerAdapater.cleanup();
    }

    public void populateFirebaseDb(String mountainName, AuthData auth) {
        Firebase ref = fireRoot.child("mountain").child(mountainName).child("posts");
        MountainPost mp = new MountainPost("test line", (String)auth.getProviderData().get("email"));
        ref.push().setValue(mp);
        MountainPost mp2 = new MountainPost("yay is this working?", (String)auth.getProviderData().get("email"));
        ref.push().setValue(mp2);
        MountainPost mp3 = new MountainPost("yay  working?", (String)auth.getProviderData().get("email"));
        ref.push().setValue(mp3);
        MountainPost mp4 = new MountainPost("weird line is this?", (String)auth.getProviderData().get("email"));
        ref.push().setValue(mp4);
        MountainPost mp5 = new MountainPost("number 5 baby", (String)auth.getProviderData().get("email"));
        ref.push().setValue(mp5);

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
