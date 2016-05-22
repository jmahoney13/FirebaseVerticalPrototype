package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private int specialColor;

    private int LIMIT = 15;


    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AuthData currUser = fireRoot.getAuth();
        initLayout();

        if (collapseLayout != null) {
            adapterRoot = fireRoot.child("mountain").child(mName).child("posts");
        }

        adapterRootQuery = adapterRoot.limitToLast(LIMIT);

        this.recyclerAdapater = new FirebaseRecyclerAdapter<MountainPost, PostsViewHolder>(MountainPost.class, R.layout.posts_view, PostsViewHolder.class, adapterRootQuery) {
            @Override
            public void populateViewHolder(PostsViewHolder postsViewHolder, MountainPost mountainPost, int position) {
                Log.d("number viewholder: ", "" + recyclerAdapater.getItemCount());
                MountainPost currPost = recyclerAdapater.getItem(position);

                postsViewHolder.setCurrPost(currPost);
                postsViewHolder.setFirebaseUser(currUser);
                postsViewHolder.setFirebaseRoot(adapterRoot);
                postsViewHolder.setContext(MainActivity.this);
                postsViewHolder.setMountainName(mName);
                postsViewHolder.postText.setText(mountainPost.getLine());
                postsViewHolder.authorText.setText(mountainPost.getAuthor());
                postsViewHolder.likeGroup.clearCheck();
                postsViewHolder.numLikes.setText(Integer.toString(mountainPost.getLikes()));

                Firebase ref = new Firebase(FIREBASEURL);
                final Firebase historyRoot = ref.child("history");

                historyRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Firebase newRoot = null;
                        String currEmail = (String)currUser.getProviderData().get("email");
                        currEmail = currEmail.replace(".", ",");

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if (currEmail.equals(snapshot.getKey())) {
                                newRoot = historyRoot.child(snapshot.getKey());
                            }
                        }

                        if (newRoot != null) {
                            newRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot likeHistory: dataSnapshot.getChildren()) {
                                        Log.d("History: ", likeHistory.toString());
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });


            }
        };

        this.postsRecyclerView.setAdapter(this.recyclerAdapater);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setupFAB(fab, currUser);
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder implements RadioGroup.OnCheckedChangeListener, View.OnLongClickListener {
        private TextView postText;
        private TextView authorText;
        private TextView numComments;
        private TextView numLikes;
        private RadioGroup likeGroup;
        private RadioButton likeButton;
        private RadioButton dislikeButton;
        private Firebase rootRef;
        private MountainPost currPost;
        private Context context;
        private String mountain;
        private AuthData user;

        public PostsViewHolder(View itemView) {
            super(itemView);
            postText = (TextView) itemView.findViewById(R.id.lineTextView);
            authorText = (TextView) itemView.findViewById(R.id.authorTextView);
            //numComments = (TextView) itemView.findViewById(R.id.numCommentsTextView);
            numLikes = (TextView) itemView.findViewById(R.id.numLikesTextView);
            likeGroup = (RadioGroup) itemView.findViewById(R.id.likeRadioGroup);
            likeButton = (RadioButton) itemView.findViewById(R.id.likeRadioButton);
            dislikeButton = (RadioButton) itemView.findViewById(R.id.dislikeRadioButton);

            likeGroup.setOnCheckedChangeListener(this);
            itemView.setClickable(true);
            itemView.setOnLongClickListener(this);





        }

        public void setFirebaseUser(AuthData user) {
            this.user = user;
        }

        public void setFirebaseRoot(Firebase root) {
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
            Log.d("radio: ", "onCheckedChanged");

            if (checkedId == R.id.likeRadioButton) {
                likeTransaction();

            }

            else if (checkedId == R.id.dislikeRadioButton) {
                dislikeTransaction();
            }
        }

        public void likeTransaction() {
            this.rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Firebase updateRef = null;
                    for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Log.d("likedPost: ", postSnapshot.toString());

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
                        public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                            Log.d("Liked: ", "");
                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("Cancel:", firebaseError.toString());
                }
            });
        }

        public void dislikeTransaction() {
            this.rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Firebase updateRef = null;
                    for (final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Log.d("dislikedPost: ", postSnapshot.toString());

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
                        public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                            Log.d("Disliked: ", "");
                        }
                    });
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("Cancel:", firebaseError.toString());
                }
            });
        }

        public void updateHistory(DataSnapshot postSnapshot, AuthData user, int liked) {
            Firebase mainRef = new Firebase(FIREBASEURL);
            String email = (String)user.getProviderData().get("email");
            email = email.replace(".", ",");
            final Firebase historyRef = mainRef.child("history").child(email);

            Map<String, Object> likeStatus = new HashMap<>();
            likeStatus.put(postSnapshot.getKey(), liked);
            historyRef.updateChildren(likeStatus);
        }

        @Override
        public boolean onLongClick(View v) {
            //start comments activity
            Intent intent = new Intent(this.context, CommentsActivity.class);
            intent.putExtra("MOUNTAIN_NAME", this.mountain);
            intent.putExtra("MOUNTAIN_POST", currPost);
            this.context.startActivity(intent);
            return true;
        }
    }

/*
    @Override
    public void onResume() {
        super.onResume();
        View mainView = getWindow().getDecorView();
        mainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        mainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        mainView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

*/

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
        //toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_arrow));
        toolbar.setNavigationIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_back_arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView header = (ImageView) findViewById(R.id.headerImage);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heavenly);

        Log.d("test", "");

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
                    collapseLayout.setContentScrimColor(swatchList.get(maxPosition).getRgb());
                    collapseLayout.setTitle(mName);

                    specialColor = swatchList.get(maxPosition).getRgb();
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
                                postsRecyclerView.smoothScrollToPosition(LIMIT);
                                MountainPost mp = new MountainPost(postInput.getText().toString().trim(), (String)currUser.getProviderData().get("email"));
                                Firebase newRef = adapterRoot.push();

                                newRef.setValue(mp);
                                Log.d("fabAddKey: ", newRef.getKey());
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
