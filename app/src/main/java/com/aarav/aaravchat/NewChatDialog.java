package com.aarav.aaravchat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

    import com.aarav.aaravchat.R;

public class NewChatDialog extends AppCompatDialogFragment {

    private EditText username;
    private ChatDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view  = inflater.inflate(R.layout.layout_dialoug, null);

        builder.setView(view).setTitle("Create a New Chat").setNegativeButton("Cancel", (dialogInterface, i) -> {

        }).setPositiveButton("Ok", (dialogInterface, i) -> {
            String usernameVal = username.getText().toString();
            listener.applyTexts(usernameVal);
        });

        username = view.findViewById(R.id.editUsername);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ChatDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement NewChatDialog");
        }
    }

    public interface ChatDialogListener{
        void applyTexts(String username);
    }
}
