package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;

public class CommentsActivity extends AppCompatActivity {
    private Firebase fireRoot = new Firebase(MainActivity.FIREBASEURL);

    private Toolbar toolbar;
    private String mName;
    private MountainPost commentPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //this.toolbar = (Toolbar) findViewById(R.id.toolbarWidget);
        //setSupportActionBar(this.toolbar);
        //toolbar.setNavigationIcon(ContextCompat.getDrawable(CommentsActivity.this, R.drawable.ic_back_arrow));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

/*
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }); */

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString("MOUNTAIN_NAME");
            commentPost = (MountainPost)extras.getSerializable("MOUNTAIN_POST");
        }
        actionBar.setTitle(mName);

        TextView lineTV = (TextView) findViewById(R.id.lineView);
        TextView authTV = (TextView) findViewById(R.id.authorView);
        TextView likesTV = (TextView) findViewById(R.id.likesView);

        lineTV.setText("Line: " + commentPost.getLine());
        authTV.setText("Author: " + commentPost.getAuthor());
        likesTV.setText("Likes: " + commentPost.getLikes());


        //toolbar.setTitle(mName);
        //toolbar.setBackgroundColor(ContextCompat.getColor(CommentsActivity.this, R.color.primary_700));

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
            fireRoot.unauth();
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
