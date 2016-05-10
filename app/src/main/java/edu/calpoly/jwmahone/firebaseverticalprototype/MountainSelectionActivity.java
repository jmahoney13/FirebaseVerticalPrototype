package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MountainSelectionActivity extends AppCompatActivity {
    private ListView mountainList;
/*
    private final String[] allMountains = {"Bear Valley, CA", "Boreal Mountain Resort, CA", "Donner Ski Rance, CA", "Heavenly Mountain Resort, CA", "Homewood Mountain Resort, CA", "June Mountain, CA",
                                     "Kirkwood, CA", "Mammoth Mountain Ski Area, CA", "Mountain High, CA", "Mt. Baldy, CA", "Northstar, CA", "Sierra-at-Tahoe, CA", "China Peak, CA", "Soda Springs, CA",
                                     "Squaw Valley/Alpine Meadows, CA", "Sugar Bowl Resort, CA", "Tahoe Donner, CA"};
*/
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
                //String test = mountainList.getSelectedItem().toString();
                //Log.d("Mountain: ", test);
                Intent postScreenActivity = new Intent(MountainSelectionActivity.this, MainActivity.class);
                postScreenActivity.putExtra("MOUNTAIN_NAME", mountainName);
                startActivity(postScreenActivity);
            }
        });



    }


}
