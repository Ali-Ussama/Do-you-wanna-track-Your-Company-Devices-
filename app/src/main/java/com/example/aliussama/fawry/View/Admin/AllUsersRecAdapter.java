package com.example.aliussama.fawry.View.Admin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aliussama.fawry.Model.Callbacks.SearchActivityCallback;
import com.example.aliussama.fawry.Model.UserModel;
import com.example.aliussama.fawry.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AllUsersRecAdapter extends RecyclerView.Adapter<AllUsersRecAdapter.viewHolder> {
    private String TAG = "AllUsersRecAdapter";
    private ArrayList<UserModel> users;
    private Context context;
    private SearchActivityCallback mCallback;
    private SearchActivity mActivity;

    AllUsersRecAdapter(ArrayList<UserModel> users, SearchActivityCallback callback, SearchActivity activity) {
        this.users = users;
        this.mCallback = callback;
        this.mActivity = activity;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_recycler_view_row_item, parent, false);
        context = parent.getContext();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(final viewHolder holder, int position) {
        try {
            holder.userName.setText(users.get(position).getName());
            holder.userEmail.setText(users.get(position).getPhone());

            holder.updateUsernameEditText.setText(users.get(position).getName());
            holder.updateEmailEditText.setText(users.get(position).getPhone());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void NotifyAdapter(ArrayList<UserModel> data) {
        users = data;
        notifyDataSetChanged();
        Log.i("NotifyAdapter", "adapter is notified");
    }

    class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            AdapterView.OnItemSelectedListener,
            View.OnTouchListener, View.OnLongClickListener {

        TextView userName, userEmail;
        Spinner options;
        CardView cardView;
        ConstraintLayout row_item_layout;
        ConstraintLayout update_layout;

        EditText updateUsernameEditText, updateEmailEditText;
        Button updateUserButton;

        private boolean mSpinnerCreated = false;
        String userCode = "";
        View v;

        viewHolder(final View v) {
            super(v);
            try {
                this.v = v;
                userName = v.findViewById(R.id.all_users_rec_row_item_username_textView);
                userEmail = v.findViewById(R.id.all_users_rec_row_item_email_textView2);
                options = v.findViewById(R.id.all_users_rec_row_item_options_spinner);
                cardView = v.findViewById(R.id.all_users_rec_row_item_cardView);
                row_item_layout = v.findViewById(R.id.all_users_rec_row_item_layout);
                update_layout = v.findViewById(R.id.all_users_rec_row_item_update_layout);
                updateEmailEditText = v.findViewById(R.id.all_users_rec_row_item_user_phone_edit_text);
                updateUsernameEditText = v.findViewById(R.id.all_users_rec_row_item_user_name_edit_text);
                updateUserButton = v.findViewById(R.id.all_users_rec_row_item_update_user_button);

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                        R.array.users_row_item_options_spinner, android.R.layout.simple_spinner_item);

                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Apply the adapter to the spinner
                options.setAdapter(adapter);
                options.setSelection(getAdapterPosition(), false);
                options.setOnTouchListener(this);
                options.setOnItemSelectedListener(this);
                updateUserButton.setOnClickListener(this);

                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showChoiceDialog(v);
                        return true;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void showChoiceDialog(final View v) {

            Log.i(TAG, "showChoiceDialog() is called");
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(v.getContext());
            builderSingle.setTitle("قائمة الاختيارات");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.select_dialog_singlechoice);
            arrayAdapter.add(v.getContext().getString(R.string.cancel_updating));
            arrayAdapter.add(v.getContext().getString(R.string.delete));
            arrayAdapter.add(v.getContext().getString(R.string.update));

            builderSingle.setNegativeButton(v.getContext().getString(R.string.cancel_updating), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            Log.i(TAG, "showChoiceDialog(): setting Adapter");

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String choice = arrayAdapter.getItem(which);
                    if (choice != null) {
                        if (choice.matches(v.getContext().getString(R.string.cancel_updating))) {
                            Log.i("onItemSelected", "الغاء");
                            update_layout.setVisibility(View.GONE);
                        } else if (choice.matches(v.getContext().getString(R.string.delete))) {
                            Log.i("onItemSelected", "حذف");
                            if (mCallback != null) {
                                options.setSelection(getAdapterPosition(), false);
                                options.setSelection(0, true);
                                deleteUser();
                            }
                        } else if (choice.matches(v.getContext().getString(R.string.update))) {
                            try {
                                Log.i("onItemSelected", "تعديل");
                                update_layout.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            Log.i(TAG, "showChoiceDialog(): displaying the dialog");

            builderSingle.show();
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();

            if (id == R.id.all_users_rec_row_item_update_user_button) {

                if (updateUsernameEditText.getText().toString().isEmpty()) {
                    updateUsernameEditText.setError(context.getResources().getString(R.string.enter_username));
                } else if (updateEmailEditText.getText().toString().isEmpty()) {
                    updateEmailEditText.setError(context.getResources().getString(R.string.enter_user_phone));
                } else {
                    try {


                            users.get(getAdapterPosition()).setPhone(updateEmailEditText.getText().toString());
                            users.get(getAdapterPosition()).setName(updateUsernameEditText.getText().toString());
                            users.get(getAdapterPosition()).setId(updateUsernameEditText.getText().toString());
                            update_layout.setVisibility(View.GONE);
                            row_item_layout.setVisibility(View.VISIBLE);
                            options.setSelection(getAdapterPosition(), false);
                            options.setSelection(0, true);
                            if (mCallback != null) {
                                mCallback.onUserItemUpdate(users.get(getAdapterPosition()));
                            } else {
                                Log.i("AllUsersRecAdapter", "onClick : Home Admin Activity callback is null");
                            }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
            try {
                Log.i("onItemSelected", "selected item position = " + i);
                Log.i("onItemSelect", "flag = " + mSpinnerCreated);

                if (mSpinnerCreated) {
                    Log.i("onItemSelected", "item position" + i);
                    if (i == 0) {
                        Log.i("onItemSelected", "الغاء");
                        update_layout.setVisibility(View.GONE);
                    } else if (i == 1) {
                        Log.i("onItemSelected", "حذف");
                        if (mCallback != null) {
                            options.setSelection(getAdapterPosition(), false);
                            options.setSelection(0, true);
                            deleteUser();
                        }
                    } else if (i == 2) {
                        try {
                            Log.i("onItemSelected", "تعديل");
                            update_layout.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mSpinnerCreated = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void deleteUser() {
            try {
                if (mCallback != null)
                    mCallback.onUserItemDelete(users.get(getAdapterPosition()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.getId() == R.id.all_users_rec_row_item_options_spinner) {
                Log.i("onTouch", "touched");
                mSpinnerCreated = true;
            }
            return false;
        }

        @Override
        public boolean onLongClick(View view) {
            Log.i(TAG, "onLongClick() view id = " + view.getId());
            Log.i(TAG, "onLongClick() row item id = " + R.id.all_users_rec_row_item_cardView);

            if (view.getId() == R.id.all_users_rec_row_item_cardView) {
                showChoiceDialog(view);
                return true;
            }

            return false;
        }
    }
}
