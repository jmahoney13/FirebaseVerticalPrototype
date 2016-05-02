package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class SignUpScreenActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        emailEditText = (EditText) findViewById(R.id.emailField);
        passwordEditText = (EditText) findViewById(R.id.passField);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        final Firebase rootBase = new Firebase(MainActivity.FIREBASEURL);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddr = emailEditText.getText().toString();
                String pass = passwordEditText.getText().toString();

                rootBase.createUser(emailAddr, pass, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(SignUpScreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {

                    }
                });
            }
        });

    }
}
