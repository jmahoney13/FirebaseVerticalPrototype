package edu.calpoly.jwmahone.firebaseverticalprototype;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpScreenActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private EditText confirmPasswordEditText;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Log.d("user: ", user.getEmail() + " logged in");
                }
                else {
                    Log.d("user is logged out", "");
                }
            }
        };

        emailEditText = (EditText) findViewById(R.id.emailField);
        passwordEditText = (EditText) findViewById(R.id.passField);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPassField);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailAddr = emailEditText.getText().toString();
                final String pass = passwordEditText.getText().toString();
                String confirmPass = confirmPasswordEditText.getText().toString();

                if (pass.equals(confirmPass)) {

                    mAuth.createUserWithEmailAndPassword(emailAddr, pass).addOnCompleteListener(SignUpScreenActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("signup: ", "createUserWithEmail:onComplete:" + task.isSuccessful());
                            Intent intent = new Intent(SignUpScreenActivity.this, MountainSelectionActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpScreenActivity.this, "Failed sign up.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }
                else {
                    Toast.makeText(SignUpScreenActivity.this, "Check if passwords match.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
