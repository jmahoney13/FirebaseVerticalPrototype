package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.MutableData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private DatabaseReference fireRoot = FirebaseDatabase.getInstance().getReference();
    private RecyclerView postsRecyclerView;
    private FirebaseRecyclerAdapter<MountainPost, PostsViewHolder> recyclerAdapater;
    private String mName;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapseLayout;
    private FloatingActionButton fab;
    private DatabaseReference adapterRoot;
    private Query adapterRootQuery;
    private int specialColor;
    private int LIMIT = 15;
    private int postBackgroundColor;


    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        initLayout();

        if (collapseLayout != null) {
            adapterRoot = fireRoot.child("mountain").child(mName).child("posts");
        }

        adapterRootQuery = adapterRoot.limitToLast(LIMIT);

        this.recyclerAdapater = new FirebaseRecyclerAdapter<MountainPost, PostsViewHolder>(MountainPost.class, R.layout.posts_view, PostsViewHolder.class, adapterRootQuery) {
            @Override
            public void populateViewHolder(final PostsViewHolder postsViewHolder, MountainPost mountainPost, int position) {
                final MountainPost currPost = recyclerAdapater.getItem(position);

                postsViewHolder.setCurrPost(currPost);
                postsViewHolder.setFirebaseUser(currUser);
                postsViewHolder.setFirebaseRoot(adapterRoot);
                postsViewHolder.setContext(MainActivity.this);
                postsViewHolder.setMountainName(mName);
                postsViewHolder.postText.setText(mountainPost.getLine());
                postsViewHolder.authorText.setText(mountainPost.getAuthor());
                postsViewHolder.likeGroup.clearCheck();
                postsViewHolder.numLikes.setText(Integer.toString(mountainPost.getLikes()));
                postsViewHolder.mainLayout.setBackgroundColor(postBackgroundColor);

                /*----------------------------------------------------------------------------------------------------------------------------*/
                final DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child("comments");
                commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if (snapshot.getKey().equals(currPost.getID())) {
                                DatabaseReference innerTree = commentRef.child(snapshot.getKey());
                                innerTree.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long commentCount = 0;
                                        commentCount = dataSnapshot.getChildrenCount();
                                        postsViewHolder.numComments.setText(Long.toString(commentCount));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else {
                                postsViewHolder.numComments.setText("0");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                /*----------------------------------------------------------------------------------------------------------------------------*/

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                final DatabaseReference historyRoot = ref.child("history");

                historyRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DatabaseReference newRoot = null;
                        String currEmail = currUser.getEmail();
                        currEmail = currEmail.replace(".", ",");
                        String currKey;

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if (currEmail.equals(snapshot.getKey())) {
                                currKey = snapshot.getKey();
                                newRoot = historyRoot.child(currKey);
                            }
                        }

                        if (newRoot != null) {
                            newRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    Map<String, Object> history = (Map<String, Object>) dataSnapshot.getValue();

                                    for (Map.Entry<String, Object> entry: history.entrySet()) {
                                        if (entry.getKey().equals(currPost.getID())) {
                                            if (((Long)(entry.getValue())) == 1) {
                                                postsViewHolder.likeButton.setChecked(true);
                                                postsViewHolder.likeButton.setEnabled(false);
                                                postsViewHolder.dislikeButton.setEnabled(false);
                                            }
                                            else if (((Long)(entry.getValue())) == 0) {
                                                postsViewHolder.dislikeButton.setChecked(true);
                                                postsViewHolder.likeButton.setEnabled(false);
                                                postsViewHolder.dislikeButton.setEnabled(false);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {

                                }


                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });

                postsViewHolder.setRadioGroupListener();
            }
        };

        this.postsRecyclerView.setAdapter(this.recyclerAdapater);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setupFAB(fab, currUser);
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
        private TextView postText;
        private TextView authorText;
        private TextView numComments;
        private TextView numLikes;
        private RadioGroup likeGroup;
        private RadioButton likeButton;
        private RadioButton dislikeButton;
        private DatabaseReference rootRef;
        private MountainPost currPost;
        private Context context;
        private String mountain;
        private FirebaseUser user;
        private LinearLayout mainLayout;

        public PostsViewHolder(View itemView) {
            super(itemView);
            postText = (TextView) itemView.findViewById(R.id.lineTextView);
            authorText = (TextView) itemView.findViewById(R.id.authorTextView);
            numComments = (TextView) itemView.findViewById(R.id.numCommentsTextView);
            numLikes = (TextView) itemView.findViewById(R.id.numLikesTextView);
            likeGroup = (RadioGroup) itemView.findViewById(R.id.likeRadioGroup);
            likeButton = (RadioButton) itemView.findViewById(R.id.likeRadioButton);
            dislikeButton = (RadioButton) itemView.findViewById(R.id.dislikeRadioButton);
            numComments = (TextView) itemView.findViewById(R.id.numCommentsTextView);
            mainLayout = (LinearLayout) itemView.findViewById(R.id.mainPostView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        public void setRadioGroupListener() {
            likeGroup.setOnCheckedChangeListener(this);
        }

        public void setFirebaseUser(FirebaseUser user) {
            this.user = user;
        }

        public void setFirebaseRoot(DatabaseReference root) {
            this.rootRef = root;
        }

        public void setCurrPost(MountainPost mp) {
            this.currPost = mp;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public void setMountainName(String mName) {
            this.mountain = mName;
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (likeButton.isPressed() || dislikeButton.isPressed()) {
                Log.d("pressed: ", "pressed!!!");
                if (checkedId == R.id.likeRadioButton) {
                    likeTransaction();
                    //likeButton.setEnabled(false); //TODO
                }
                else if (checkedId == R.id.dislikeRadioButton) {
                    dislikeTransaction();
                    //dislikeButton.setEnabled(false); //TODO
                }
            }
        }


        public void likeTransaction() {
            this.rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference updateRef = null;
                    for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                        if (currPost.equals(postSnapshot.getValue(MountainPost.class))) {
                            updateRef = rootRef.child(postSnapshot.getKey()).child("likes");
                            updateHistory(postSnapshot, user, 1);
                        }
                    }

                    assert updateRef != null;
                    updateRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue((Long) mutableData.getValue() + 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e("Cancel:", firebaseError.toString());
                }
            });
        }

        public void dislikeTransaction() {
            this.rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference updateRef = null;
                    for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        if (currPost.equals(postSnapshot.getValue(MountainPost.class))) {
                            updateRef = rootRef.child(postSnapshot.getKey()).child("likes");
                            updateHistory(postSnapshot, user, 0);
                        }
                    }

                    assert updateRef != null;
                    updateRef.runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if (mutableData.getValue() == null) {
                                mutableData.setValue(1);
                            } else {
                                mutableData.setValue((Long) mutableData.getValue() - 1);
                            }

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e("Cancel:", firebaseError.toString());
                }
            });
        }

        public void updateHistory(DataSnapshot postSnapshot, FirebaseUser user, int liked) {
            DatabaseReference mainRef = FirebaseDatabase.getInstance().getReference();
            String email = user.getEmail();
            email = email.replace(".", ",");
            final DatabaseReference historyRef = mainRef.child("history").child(email);

            Map<String, Object> likeStatus = new HashMap<>();
            likeStatus.put(postSnapshot.getKey(), liked);
            historyRef.updateChildren(likeStatus);
        }

        @Override
        public void onClick(View v) {
            //start comments activity
            Intent intent = new Intent(this.context, CommentsActivity.class);
            intent.putExtra("MOUNTAIN_NAME", this.mountain);
            intent.putExtra("MOUNTAIN_POST", currPost);
            this.context.startActivity(intent);
        }
    }


    public void initLayout() {
        setContentView(R.layout.activity_main);

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
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_back_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView header = (ImageView) findViewById(R.id.headerImage);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heavenly);

        assert header != null;
        switch(mName) {

            case "Heavenly":
                Glide.with(this).load(R.drawable.heavenly).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heavenly);
                break;
            case "Kirkwood":
                Glide.with(this).load(R.drawable.kirkwood).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kirkwood);
                break;
            case "Mammoth":
                Glide.with(this).load(R.drawable.mammoth).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mammoth);
                break;
            case "Northstar":
                Glide.with(this).load(R.drawable.northstar).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.northstar);
                break;
            case "Sierra-at-Tahoe":
                Glide.with(this).load(R.drawable.sierra).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sierra);
                break;
            case "Squaw Valley":
                Glide.with(this).load(R.drawable.squaw).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.squaw);
                break;
            case "Alpine Meadows":
                Glide.with(this).load(R.drawable.alpinemeadows).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.alpinemeadows);
                break;
            case "Sugar Bowl":
                Glide.with(this).load(R.drawable.sugarbowl).into(header);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sugarbowl);
                break;
            default:
                break;
        }
/*
        AppBarLayout.OnOffsetChangedListener mListener;
        mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(collapseLayout.getHeight() + verticalOffset >= 2 * ViewCompat.getMinimumHeight(collapseLayout)) {
                    getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                }
                else {
                    getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.primary_700));
                }
            }
        };
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.postsAppBar);
        assert appBar != null;
        appBar.addOnOffsetChangedListener(mListener);

*/



        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //int mutedColor = palette.getMutedColor(ContextCompat.getColor(MainActivity.this, R.color.primary_500));
                if (palette != null) {
                    List<Palette.Swatch> swatchList = palette.getSwatches();
                    int maxPop = 0;
                    int maxPosition = 0;
                    int count = 0;
                    for (Palette.Swatch s : swatchList) {
                        if (s.getPopulation() > maxPop) {
                            maxPop = s.getPopulation();
                            maxPosition = count;
                        }
                        count++;
                    }

                    collapseLayout.setExpandedTitleColor(swatchList.get(maxPosition).getTitleTextColor());
                    collapseLayout.setContentScrimColor(palette.getDarkVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.primary_500)));
                    //postBackgroundColor = palette.getDarkVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.primary_500));
                    postBackgroundColor = ContextCompat.getColor(MainActivity.this, R.color.postBackground);
                    //collapseLayout.setContentScrimColor(swatchList.get(maxPosition).getRgb());

                    collapseLayout.setTitle(mName);

                    //specialColor = swatchList.get(maxPosition).getRgb();
                    specialColor = palette.getDarkVibrantColor(ContextCompat.getColor(MainActivity.this, R.color.primary_500));
                }
            }
        });


        AppBarLayout.OnOffsetChangedListener mListener;
        mListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(collapseLayout.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collapseLayout)) {
                    getWindow().setStatusBarColor(specialColor);
                }
                else {
                    getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, android.R.color.transparent));
                }
            }
        };
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.postsAppBar);
        assert appBar != null;
        appBar.addOnOffsetChangedListener(mListener);

    }


    public void setupFAB(FloatingActionButton fab, final FirebaseUser currUser) {
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
                                if (!postInput.getText().toString().trim().equals("")) {
                                    DatabaseReference newRef = adapterRoot.push();
                                    MountainPost mp = new MountainPost(postInput.getText().toString().trim(), currUser.getEmail(), newRef.getKey());
                                    newRef.setValue(mp);
                                    postsRecyclerView.smoothScrollToPosition(LIMIT);
                                }
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
            FirebaseAuth.getInstance().signOut();
            startLogin();
        }

        return super.onOptionsItemSelected(item);
    }
}
