package com.aarav.aaravchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.aarav.aaravchat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NewChatDialog.ChatDialogListener {
    private Handler handler = new Handler();
    private DatabaseReference databaseReference;
    private DataSnapshot currentChats;
    final private String TAG = "AaravChat";
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    List<String> dropdownItems = new ArrayList<>();
    String selectedDropdownItem;
    String selectedDropdownChatId;
    String username;
    LinearLayout llMain;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String dbRefPath = "chats/"+selectedDropdownChatId;
            databaseReference = db.getReference(dbRefPath);
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if(currentChats != null){
                        if(currentChats.getValue() != null && task.getResult().getValue() != null){
                            if(!currentChats.getValue().toString().equals(task.getResult().getValue().toString())){
                                currentChats = task.getResult();
                                getChats();
                            }
                        }
                    }
                }
            });

            handler.postDelayed(runnable, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.post(runnable);

        llMain = findViewById(R.id.container_chats);

        final EditText message = findViewById(R.id.message);
        username = getIntent().getStringExtra("USERNAME");

        Button btn = findViewById(R.id.btn_send);
        btn.setOnClickListener(v->
        {
            String dbRefPath = "chatConnect/" + username;
            databaseReference = db.getReference(dbRefPath);
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for(DataSnapshot child : task.getResult().getChildren()){
                        String[] currValue = new String[2];

                        int i = 0;

                        for (DataSnapshot childChild : child.getChildren()) {
                            currValue[i] = Objects.requireNonNull(childChild.getValue()).toString();
                            i++;
                        }

                        if(currValue[1].equals(selectedDropdownItem)){
                            String dbRefPathSend;
                            dbRefPathSend = "chats/"+ currValue[0];
                            Message msg = new Message(message.getText().toString(), username);
                            databaseReference = db.getReference(dbRefPathSend);
                            addMessage(msg);
                        }
                    }
                }
            });
        });
        populateDropdown();
    }

    private void addMessage(Message msg){
        databaseReference.push().setValue(msg).addOnCompleteListener(task -> getChats());
    }

    private void addUserChat(UserChat usrChat){
        databaseReference.push().setValue(usrChat);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selectedItem = dropdownItems.get(i);
        selectedDropdownItem = selectedItem;
        if(selectedItem.equals("New Chat")){
            llMain.removeAllViews();
            Button button = new Button(this);
            button.setText("Create New Chat");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.gravity = Gravity.CENTER;

            Typeface typeface = ResourcesCompat.getFont(this, R.font.euclid_regular);
            button.setTypeface(typeface);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDialog();
                }
            });

            button.setLayoutParams(params);
            int padding_in_dp = 15;
            final float scale = getResources().getDisplayMetrics().density;
            button.setPadding((int) (padding_in_dp * scale + 0.5), (int) (padding_in_dp * scale + 0.5), (int) (padding_in_dp * scale + 0.5), (int) (padding_in_dp * scale + 0.5));

            llMain.addView(button);
        }else{
            String dbRefPath = "chatConnect/" + username;
            databaseReference = db.getReference(dbRefPath);
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for(DataSnapshot child : task.getResult().getChildren()){
                        String[] currValue = new String[2];

                        int j = 0;

                        for (DataSnapshot childChild : child.getChildren()) {
                            currValue[j] = Objects.requireNonNull(childChild.getValue()).toString();
                            j++;
                        }

                        if(currValue[1].equals(selectedDropdownItem)){
                            selectedDropdownChatId = currValue[0];
                        }
                    }
                }
                getChats();
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void populateDropdown(){
        String dbRefPath = "chatConnect/" + username;
        databaseReference = db.getReference(dbRefPath);
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Spinner spinner = (Spinner)findViewById(R.id.spinner_chats);
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);
                for(DataSnapshot child : task.getResult().getChildren()){
                    String[] currValue = new String[2];

                    int i = 0;

                    for (DataSnapshot childChild : child.getChildren()) {
                        currValue[i] = Objects.requireNonNull(childChild.getValue()).toString();
                        i++;
                    }

                    spinnerAdapter.add(currValue[1]);
                    dropdownItems.add(currValue[1]);
                }
                spinnerAdapter.add("New Chat");
                dropdownItems.add("New Chat");
                spinnerAdapter.notifyDataSetChanged();
                spinner.setOnItemSelectedListener(this);
            }
        });
    }

    public void openDialog(){
        NewChatDialog chatDialog = new NewChatDialog();
        chatDialog.show(getSupportFragmentManager(), "newChatDialog");
    }

    @Override
    public void applyTexts(String givenUsername) {
        String dbRefPath = "users/";
        databaseReference = db.getReference(dbRefPath);
        AtomicReference<Boolean> isRealUser = new AtomicReference<>(false);
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for(DataSnapshot child : task.getResult().getChildren()){

                    String[] curruser = new String[2];

                    int i = 0;

                    for (DataSnapshot childchild: child.getChildren()) {
                        curruser[i] = childchild.getValue().toString();
                        i++;
                    }

                    if(curruser[1].equals(givenUsername)){
                        isRealUser.set(true);
                    }
                }
                String dbRefPathNew = "chatConnect/"+username;
                databaseReference = db.getReference(dbRefPathNew);
                AtomicReference<Boolean> isNewUser = new AtomicReference<>(true);
                databaseReference.get().addOnCompleteListener(taskN -> {
                    if (taskN.isSuccessful()) {
                        for(DataSnapshot child : taskN.getResult().getChildren()){

                            String[] curruser = new String[2];

                            int i = 0;

                            for (DataSnapshot childchild: child.getChildren()) {
                                curruser[i] = childchild.getValue().toString();
                                i++;
                            }

                            if(curruser[1].equals(givenUsername)){
                                isNewUser.set(false);
                            }
                        }
                    }
                    if(isRealUser.get() && isNewUser.get()){
                        UserChat usrChat;
                        String chatId = UUID.randomUUID().toString();
                        usrChat = new UserChat(givenUsername, chatId);
                        String dbRefPathI = "chatConnect/"+username;
                        databaseReference = db.getReference(dbRefPathI);
                        addUserChat(usrChat);
                        usrChat = new UserChat(username, chatId);
                        String dbRefPathJ = "chatConnect/"+givenUsername;
                        databaseReference = db.getReference(dbRefPathJ);
                        addUserChat(usrChat);
                        populateDropdown();
                    } else{
                        Context context = getApplicationContext();
                        CharSequence text = "New Chat Failed! Make sure user is real and not duplicate.";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
            }
        });
    }

    private void getChats(){
        String dbRefPath = "chats/"+selectedDropdownChatId;
        databaseReference = db.getReference(dbRefPath);
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentChats = task.getResult();
                llMain.removeAllViews();
                for(DataSnapshot child : task.getResult().getChildren()){
                    String[] currValue = new String[2];

                    int i = 0;

                    for (DataSnapshot childChild : child.getChildren()) {
                        currValue[i] = Objects.requireNonNull(childChild.getValue()).toString();
                        i++;
                    }

                    TextView textView = new TextView(this);
                    textView.setText(currValue[1]);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    if(currValue[0].equals(username)){
                        params.gravity = Gravity.END;
                        textView.setBackgroundResource(R.color.fromMe);
                        textView.setTextColor(Color.parseColor("#FFFFFF"));
                    } else{
                        textView.setBackgroundResource(R.color.fromOther);
                    }
                    textView.setLayoutParams(params);
                    int padding_in_dp = 15;
                    final float scale = getResources().getDisplayMetrics().density;
                    textView.setPadding((int) (padding_in_dp * scale + 0.5), (int) (padding_in_dp * scale + 0.5), (int) (padding_in_dp * scale + 0.5), (int) (padding_in_dp * scale + 0.5));

                    llMain.addView(textView);
                }
                final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }
}