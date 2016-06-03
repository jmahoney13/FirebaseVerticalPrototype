package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CommentsActivity extends AppCompatActivity {
    private DatabaseReference fireRoot = FirebaseDatabase.getInstance().getReference();
    private String mName;
    private MountainPost commentPost;
    private FirebaseRecyclerAdapter<Comment, CommentViewHolder> commentAdapter;
    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private Button addButton;
    private MountainPost mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString("MOUNTAIN_NAME");
            commentPost = (MountainPost)extras.getSerializable("MOUNTAIN_POST");
        }
        actionBar.setTitle(mName);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        TextView lineTV = (TextView) findViewById(R.id.lineView);
        TextView authTV = (TextView) findViewById(R.id.authorView);
        TextView likesTV = (TextView) findViewById(R.id.likesView);
        this.commentsRecyclerView = (RecyclerView) findViewById(R.id.commentsRecyclerView);
        this.commentsRecyclerView.setHasFixedSize(false);
        this.commentsRecyclerView.setLayoutManager(lm);
        this.commentEditText = (EditText) findViewById(R.id.commentEditText);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        this.addButton = (Button) findViewById(R.id.addCommentButton);

        lineTV.setText(commentPost.getLine());
        authTV.setText(commentPost.getAuthor());
        likesTV.setText("Likes: " + commentPost.getLikes());

        final DatabaseReference adapterRef = fireRoot.child("comments").child(commentPost.getID());

        this.commentAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(Comment.class, R.layout.comment_view, CommentViewHolder.class, adapterRef) {
            @Override
            protected void populateViewHolder(CommentViewHolder commentViewHolder, Comment comment, int position) {
                commentViewHolder.comment.setText(comment.getComment());
                commentViewHolder.author.setText(comment.getCommentAuthor());
            }
        };

        this.commentsRecyclerView.setAdapter(this.commentAdapter);

        this.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!commentEditText.getText().toString().trim().equals("")) {
                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("mountain").child(mName).child("posts").child(commentPost.getID());
                    postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mp = dataSnapshot.getValue(MountainPost.class);
                            DatabaseReference pushcommentRef = adapterRef.push();
                            Comment currComment = new Comment(pushcommentRef.getKey(), commentEditText.getText().toString(), mp.getAuthor());
                            pushcommentRef.setValue(currComment);
                            commentEditText.setText("");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
            }
        });
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView comment;
        private TextView author;

        public CommentViewHolder(View itemView) {
            super(itemView);
            this.comment = (TextView) itemView.findViewById(R.id.commentTV);
            this.author = (TextView) itemView.findViewById(R.id.authorCommentTextView);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CommentsActivity.this, MainActivity.class);
        intent.putExtra("MOUNTAIN_NAME", mName);
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

        else if (menuItemID == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void startLogin() {
        Intent intent = new Intent(this, LoginScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
