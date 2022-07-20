package com.aarav.aaravchat;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aarav.aaravchat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.atomic.AtomicReference;

public class Login extends AppCompatActivity {
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        EditText user = findViewById(R.id.user);
        EditText pass = findViewById(R.id.pass);

        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v->
        {
            String dbRefPath = "users/";
            databaseReference = db.getReference(dbRefPath);
            TextView errorMsg = findViewById(R.id.loginError);
            errorMsg.setText("Loading...");

            databaseReference.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    errorMsg.setText("An internal error occurred. We are sorry for the inconvenience. Please try again after a few minutes.");
                }
                else {
                    String username = user.getText().toString();
                    String password = pass.getText().toString();
                    Boolean loggedin = false;
                    for(DataSnapshot child : task.getResult().getChildren()){

                        String curruser[] = new String[2];

                        int i = 0;

                        for (DataSnapshot childchild: child.getChildren()) {
                            curruser[i] = childchild.getValue().toString();
                            i++;
                        }

                        if(curruser[1].equals(username) && curruser[0].equals(password)){
                            Intent main_activity = new Intent(Login.this, MainActivity.class);
                            main_activity.putExtra("USERNAME", curruser[1]);
                            startActivity(main_activity);
                            errorMsg.setText("Logged In");
                            loggedin = true;
                            break;
                        }
                    }
                    if(!loggedin){
                        errorMsg.setText("Incorrect Username or Password");
                    }
                }
            });
        });

        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(v->
        {
            TextView errorMsg = findViewById(R.id.loginError);
            String dbRefPath = "users/";
            errorMsg.setText("Creating Account, Please Wait...");
            databaseReference = db.getReference(dbRefPath);
            boolean notBlank;
            AtomicReference<Boolean> isNewUser = new AtomicReference<>(true);
            if(!user.getText().toString().equals("") && !pass.getText().toString().equals("")){
                notBlank = true;
            } else {
                notBlank = false;
                errorMsg.setText("Username and Password must not be blank!");
            }

            databaseReference.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    errorMsg.setText("An internal error occurred. We are sorry for the inconvenience. Please try again after a few minutes.");
                }
                else {
                    String username = user.getText().toString();
                    for(DataSnapshot child : task.getResult().getChildren()){

                        String[] curruser = new String[2];

                        int i = 0;

                        for (DataSnapshot childchild: child.getChildren()) {
                            curruser[i] = childchild.getValue().toString();
                            i++;
                        }

                        if(curruser[1].equals(username)){
                            isNewUser.set(false);
                        }
                    }
                    if(notBlank && isNewUser.get()){
                        User usr = new User(user.getText().toString(), pass.getText().toString());
                        add(usr);
                    }
                }
            });
        });
    }

    private void add(User usr){
        databaseReference.push().setValue(usr).addOnCompleteListener(v -> {
            TextView errorMsg = findViewById(R.id.loginError);
            errorMsg.setText("New Account Created, Logging you in...");
            Intent main_activity = new Intent(Login.this, MainActivity.class);
            EditText user = findViewById(R.id.user);
            main_activity.putExtra("USERNAME", user.getText().toString());
            startActivity(main_activity);
        });
    }
}