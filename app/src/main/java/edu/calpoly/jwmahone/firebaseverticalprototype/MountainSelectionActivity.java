package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.firebase.client.Firebase;


public class MountainSelectionActivity extends AppCompatActivity {
    private ListView mountainList;
    private Firebase fireRoot = new Firebase(MainActivity.FIREBASEURL);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mountain_selection);

        this.mountainList = (ListView) findViewById(android.R.id.list);
        this.mountainList.setAdapter(new ArrayAdapter<>(this, R.layout.mountain_selection, R.id.mountainTextView, getResources().getStringArray(R.array.mountains)));

        this.mountainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mountainName = (String)parent.getItemAtPosition(position);
                Intent postScreenActivity = new Intent(MountainSelectionActivity.this, MainActivity.class);
                postScreenActivity.putExtra("MOUNTAIN_NAME", mountainName);
                startActivity(postScreenActivity);
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
