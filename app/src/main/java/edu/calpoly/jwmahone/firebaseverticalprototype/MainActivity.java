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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;



public class MainActivity extends AppCompatActivity {
    public static final String FIREBASEURL = "https://popping-inferno-9423.firebaseio.com/";
    private Firebase fireRoot = new Firebase(FIREBASEURL);

    private RecyclerView postsRecyclerView;
    private FirebaseRecyclerAdapter<MountainPost, PostsViewHolder> recyclerAdapater;

    private String mName;

    private Toolbar toolbar;
    private CollapsingToolbarLayout collapseLayout;
    private FloatingActionButton fab;
    private Firebase adapterRoot;
    private Query adapterRootQuery;

    private int currentPosition;


    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final AuthData currUser = fireRoot.getAuth();

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);

        this.postsRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        assert this.postsRecyclerView != null;
        this.postsRecyclerView.setHasFixedSize(false);
        this.postsRecyclerView.setLayoutManager(lm);

        collapseLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolBarLayout);

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

        //glide to change image
        //ImageView header = (ImageView) findViewById(R.id.headerImage);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.squaw);

        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mutedColor = palette.getMutedColor(ContextCompat.getColor(MainActivity.this, R.color.primary_500));
                collapseLayout.setContentScrimColor(mutedColor);
            }
        });


        if (collapseLayout != null) {
            adapterRoot = fireRoot.child("mountain").child(collapseLayout.getTitle().toString()).child("posts");
        }
        //TODO
        //change limit value to be a variable that is also used in the scroll to function
        adapterRootQuery = adapterRoot.limitToLast(15);

        this.recyclerAdapater = new FirebaseRecyclerAdapter<MountainPost, PostsViewHolder>(MountainPost.class, R.layout.posts_view, PostsViewHolder.class, adapterRootQuery) {
            @Override
            public void populateViewHolder(PostsViewHolder postsViewHolder, MountainPost mountainPost, int position) {
                Log.d("number viewholder: ", "" + recyclerAdapater.getItemCount());
                currentPosition = position;
                postsViewHolder.postText.setText(mountainPost.getLine());
                postsViewHolder.authorText.setText(mountainPost.getAuthor());

                if(mountainPost.getComments() != null) {
                    postsViewHolder.numComments.setText(mountainPost.getComments().size());
                }
                else {
                    postsViewHolder.numComments.setText("0");
                }

                postsViewHolder.numLikes.setText(Integer.toString(mountainPost.getLikes()));
            }
        };

        this.postsRecyclerView.setAdapter(this.recyclerAdapater);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setupFAB(fab, currUser);

    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder implements RadioGroup.OnCheckedChangeListener {
        private TextView postText;
        private TextView authorText;
        private TextView numComments;
        private TextView numLikes;
        private RadioGroup likeGroup;
        private RadioButton likeButton;
        private RadioButton dislikeButton;

        public PostsViewHolder(View itemView) {
            super(itemView);
            postText = (TextView) itemView.findViewById(R.id.lineTextView);
            authorText = (TextView) itemView.findViewById(R.id.authorTextView);
            numComments = (TextView) itemView.findViewById(R.id.numCommentsTextView);
            numLikes = (TextView) itemView.findViewById(R.id.numLikesTextView);
            likeGroup = (RadioGroup) itemView.findViewById(R.id.likeRadioGroup);
            likeButton = (RadioButton) itemView.findViewById(R.id.likeRadioButton);
            dislikeButton = (RadioButton) itemView.findViewById(R.id.dislikeRadioButton);
            likeGroup.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int nLikes = Integer.parseInt(numLikes.getText().toString());
            int newLikes = 0;

            if (likeButton.isChecked() == true) {
                if (checkedId == R.id.likeRadioButton) {
                    likeGroup.clearCheck();
                    newLikes = nLikes - 1;
                }
                else if (checkedId == R.id.dislikeRadioButton) {
                    dislikeButton.toggle();
                    newLikes = nLikes - 2;
                }
            }

            if (checkedId == R.id.likeRadioButton) {
                likeButton.toggle();
                newLikes = nLikes + 1;

            }
            else if (checkedId == R.id.dislikeRadioButton) {
                dislikeButton.toggle();
                newLikes = nLikes - 1;
            }

            numLikes.setText(Integer.toString(newLikes));


        }
    }

    public void setupFAB(FloatingActionButton fab, final AuthData currUser) {
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
                                //TODO
                                postsRecyclerView.smoothScrollToPosition(15); //change this to be whatever the limit value is
                                MountainPost mp = new MountainPost(postInput.getText().toString().trim(), (String)currUser.getProviderData().get("email"));
                                //Log.d("comments: ", "" + mp.getComments().size());
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
