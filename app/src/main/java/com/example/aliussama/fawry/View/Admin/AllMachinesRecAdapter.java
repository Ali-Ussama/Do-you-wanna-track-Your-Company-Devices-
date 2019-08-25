package com.example.aliussama.fawry.View.Admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aliussama.fawry.Model.Callbacks.SearchActivityCallback;
import com.example.aliussama.fawry.Model.GPSTracker;
import com.example.aliussama.fawry.Model.MachineModel;
import com.example.aliussama.fawry.R;

import com.example.aliussama.fawry.View.User.UserSearchActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.MODE_PRIVATE;

public class AllMachinesRecAdapter extends RecyclerView.Adapter<AllMachinesRecAdapter.viewHolder> {

    private ArrayList<MachineModel> data;
    private boolean mSpinnerCreated = false;
    private SearchActivityCallback mCallback;

    private String userType;

    private final String USER = "USER";
    private final String ADMIN = "ADMIN";

    public AllMachinesRecAdapter(ArrayList<MachineModel> mData, SearchActivityCallback callback, String type) {
        data = mData;
        mCallback = callback;
        userType = type;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_machines_recycler_view_row_item, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        try {
            holder.machineID.setText(data.get(position).getmMachineId());
            holder.clientName.setText(data.get(position).getmClientName());
            holder.mClientPhone.setText(data.get(position).getmClientPhone());
            holder.mAddress.setText(data.get(position).getmAddress());
            holder.mRepresentative.setText(data.get(position).getmRepresentativeName());

            holder.machineCodeEditText.setText(data.get(position).getmMachineId());
            holder.clientNameEditText.setText(data.get(position).getmClientName());
            holder.clientPhoneEditText.setText(data.get(position).getmClientPhone());
            holder.currentAddressEditText.setText(data.get(position).getmAddress());
            holder.mCurrentAddressName = data.get(position).getmAddress();

            holder.mCurrentLocation = new Location("selected_place");
            holder.mCurrentLocation.setLatitude(Double.parseDouble(data.get(position).getmLatitude()));
            holder.mCurrentLocation.setLongitude(Double.parseDouble(data.get(position).getmLongitude()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NotifyAdapter(ArrayList<MachineModel> machines) {
        data = machines;
        notifyDataSetChanged();
        Log.i("AllMachinesAdapter", "NotifyAdapter: adapter is notified");
    }

    public void onPlaceSelected(@NonNull Place place, viewHolder holder) {
        try {
            if (place.getAddress() != null) {
                //declaring location var to hold latitude and longitude
                holder.mCurrentLocation = new Location("selected location");
                //assigning selected place latitude
                holder.mCurrentLocation.setLatitude(place.getLatLng().latitude);
                //assigning selected place longitude
                holder.mCurrentLocation.setLongitude(place.getLatLng().longitude);

                Log.i("onPlaceSelected", "place Name = " + place.getName());

                data.get(holder.getAdapterPosition()).setmLatitude(String.valueOf(place.getLatLng().latitude));
                data.get(holder.getAdapterPosition()).setmLongitude(String.valueOf(place.getLatLng().longitude));

                Log.i("onPlaceSelected", "place Address = " + place.getAddress());
                Log.i("onPlaceSelected", "place Name = " + place.getName());

                Log.i("onPlaceSelected", "current Adapter Position = " + holder.getAdapterPosition());

                holder.mCurrentAddressName = place.getAddress().toString();
                holder.mCurrentAddressTextView.setText(place.getAddress().toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

        EditText machineCodeEditText;

        public TextView machineID, clientName, mClientPhone, mAddress, mRepresentative;
        Spinner mOptionsSpinner;
        ConstraintLayout row_item_layout, update_layout;

        EditText clientNameEditText, clientPhoneEditText;
        EditText currentAddressEditText;
        FloatingActionButton addLocationFab;
        Button addMachineButton;

        TextView mCurrentAddressTextView;
        String mCurrentAddressName;

        //declare current location var
        private Location mCurrentLocation;

        //declare Place Pick Builder request code var
//        private int PLACE_PICKER_REQUEST = 1;
//        FloatingActionButton mScanMachineSerialNumberFab;
        //declare Place Pick Builder reference
//        private PlacePicker.IntentBuilder builder;

        PlacesClient placesClient;

        private GPSTracker mGpsTracker;

        private MachineModel mMachine;

        viewHolder(View v) {
            super(v);
            init(v);
            mOptionsSpinner.setOnTouchListener(this);
            mOptionsSpinner.setOnItemSelectedListener(this);
        }

        private void init(View v) {
            try {
                Places.initialize(v.getContext(), "AIzaSyAAPX5De3aThZZMntG0Gz0wKd9xeGp8aAQ");
                placesClient = Places.createClient(v.getContext());

                //normal row item view vars
                machineID = v.findViewById(R.id.AMRVRI_machine_ID_TextView);
                clientName = v.findViewById(R.id.AMRVRI_clientName_textView);
                mClientPhone = v.findViewById(R.id.AMRVRI_phone_textView);
                mAddress = v.findViewById(R.id.AMRVRI_clientAddress_textView);
                mOptionsSpinner = v.findViewById(R.id.AMRVRI_options_spinner);
                mRepresentative = v.findViewById(R.id.AMRVRI_representative_textView);
//                mScanMachineSerialNumberFab = v.findViewById(R.id.AMRVRI_machine_code_fab);//TODO
                row_item_layout = v.findViewById(R.id.AMRVRI_row_item_layout);
                update_layout = v.findViewById(R.id.AMRVRI_update_layout);

                ArrayAdapter<CharSequence> adapter;
                String userType = CheckUserType();
                if (!userType.isEmpty() && userType.matches("user")) {
                    adapter = ArrayAdapter.createFromResource(v.getContext(),
                            R.array.user_machines_row_item_options_spinner, android.R.layout.simple_spinner_item);
                } else {
                    adapter = ArrayAdapter.createFromResource(v.getContext(),
                            R.array.machines_row_item_options_spinner, android.R.layout.simple_spinner_item);
                }
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // Apply the adapter to the spinner
                mOptionsSpinner.setAdapter(adapter);
                mOptionsSpinner.setSelection(getAdapterPosition(), false);
                //----------------------------------------------------------------------------------
                // the update view vars

                machineCodeEditText = v.findViewById(R.id.AMRVRI_machine_code_edit_text);
                clientNameEditText = v.findViewById(R.id.AMRVRI_client_name_edit_text);
                clientPhoneEditText = v.findViewById(R.id.AMRVRI_client_phone_edit_text);
                addLocationFab = v.findViewById(R.id.AMRVRI_add_location_fab);
                addMachineButton = v.findViewById(R.id.AMRVRI_update_machine_button);
                currentAddressEditText = v.findViewById(R.id.AMRVRI_add_location_edit_text);

                mGpsTracker = new GPSTracker(v.getContext());
                mGpsTracker.getLocation();

                addMachineButton.setOnClickListener(this);
                addLocationFab.setOnClickListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String CheckUserType() {
            String type = "";
            try {
                SharedPreferences mPreferences = itemView.getContext().getSharedPreferences(itemView.getContext().getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
                type = mPreferences.getString(itemView.getContext().getString(R.string.type_key), "None");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return type;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.getId() == R.id.AMRVRI_options_spinner) {
                Log.i("onTouch", "touched");
                mSpinnerCreated = true;
            }
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                Log.i("onItemSelected", "selected item position = " + i);
                Log.i("onItemSelect", "mSpinnerCreated flag = " + mSpinnerCreated);
                Log.i("onItemSelected", "userType" + userType);

                //Admin actions on machines
                if (userType.matches(ADMIN)) {
                    Log.i("onItemSelected", "User Machine Action");
                    if (mSpinnerCreated) {
                        Log.i("onItemSelected", "item position" + i);
                        if (i == 0) {
                            Log.i("onItemSelected", "الغاء");
                            update_layout.setVisibility(View.GONE);
                        } else if (i == 1) {
                            displayAlertDialog(view);
                        } else if (i == 2) {
                            updateMachine();
                        } else if (i == 3) {
                            //share current location
                            shareCurrentLocation(view);
                            mOptionsSpinner.setSelection(getAdapterPosition(), false);
                            mOptionsSpinner.setSelection(0, true);
                        }
                    }
                } else {//User actions on machines
                    Log.i("onItemSelected", "User Machine Action");
                    if (mSpinnerCreated) {
                        Log.i("onItemSelected", "item position" + i);
                        if (i == 0) {
                            Log.i("onItemSelected", "الغاء");
                            update_layout.setVisibility(View.GONE);
                        } else if (i == 1) {
                            //share current location
                            displayAlertDialog(view);

                        } else if (i == 2) {
                            //share current location
                            shareCurrentLocation(view);
                            mOptionsSpinner.setSelection(getAdapterPosition(), false);
                            mOptionsSpinner.setSelection(0, true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void displayAlertDialog(final View view) {
            try {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteMachine(view);
                                mOptionsSpinner.setSelection(getAdapterPosition(), false);
                                mOptionsSpinner.setSelection(0, true);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                mOptionsSpinner.setSelection(getAdapterPosition(), false);
                                mOptionsSpinner.setSelection(0, true);
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setMessage(itemView.getContext().getString(R.string.are_you_sure))
                        .setPositiveButton(itemView.getContext().getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(itemView.getContext().getString(R.string.cancel_updating), dialogClickListener)
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

        @Override
        public void onClick(View view) {
            try {
                if (R.id.AMRVRI_update_machine_button == view.getId()) {
                    Log.i("onClick", String.valueOf(view.getId()));
                    Log.i("onClick", String.valueOf(R.id.AMRVRI_update_machine_button));
                    updateMachineOnClick(view);
                } else if (R.id.AMRVRI_add_location_fab == view.getId()) {
                    Log.i("onClick", String.valueOf(view.getId()));
                    Log.i("onClick", String.valueOf(R.id.AMRVRI_add_location_fab));
                    addCurrentLocationOnClick(view);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateMachine() {
            try {
                Log.i("onItemSelected", "تعديل");
                update_layout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void deleteMachine(View view) {
            try {
                Log.i("onItemSelected", "حذف");
                //delete current selected machine
                if (mCallback != null) {
                    mCallback.onMachineItemDelete(data.get(getAdapterPosition()));
                    mOptionsSpinner.setSelection(getAdapterPosition(), false);
                    mOptionsSpinner.setSelection(0, true);

                } else {
                    //Handle Callback
                    if (view.getContext() != null)
                        Toast.makeText(view.getContext(), view.getContext().getString(R.string.error_message_to_user), Toast.LENGTH_SHORT).show();
                    Log.i("onItemSelected", "Search Activity Callback is null");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void shareCurrentLocation(View view) {
            try {
                String latitude = data.get(getAdapterPosition()).getmLatitude();
                String longitude = data.get(getAdapterPosition()).getmLongitude();

                String clientName = data.get(getAdapterPosition()).getmAddress().trim();

                Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "("+clientName+")&z=20");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                    view.getContext().startActivity(mapIntent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateMachineOnClick(View v) {
            try {
                Log.i("updateMachineOnClick", "is called");

                if (machineCodeEditText.getText() == null || machineCodeEditText.getText().toString().isEmpty()) {
                    Log.i("updateMachineOnClick", "machineCodeEditText is empty");
                    machineCodeEditText.setError(v.getContext().getString(R.string.required));

                } else if (clientNameEditText.getText() == null || clientNameEditText.getText().toString().isEmpty()) {
                    Log.i("updateMachineOnClick", "clientNameEditText is empty");
                    clientNameEditText.setError(v.getContext().getString(R.string.required));

                } else if (clientPhoneEditText.getText() == null || clientPhoneEditText.getText().toString().isEmpty()) {
                    Log.i("updateMachineOnClick", "clientPhoneEditText is empty");
                    clientPhoneEditText.setError(v.getContext().getString(R.string.required));

                } else if (currentAddressEditText.getText() == null || currentAddressEditText.getText().toString().isEmpty()) {
                    currentAddressEditText.setError(v.getContext().getString(R.string.required));
                } else {
                    if (mCurrentLocation == null) {
                        mCurrentLocation = new Location("selected location");
                        mCurrentLocation.setLatitude(Double.parseDouble(data.get(getAdapterPosition()).getmLatitude()));
                        mCurrentLocation.setLongitude(Double.parseDouble(data.get(getAdapterPosition()).getmLongitude()));
                    }

                    String mMachineId = machineCodeEditText.getText().toString();
                    String clientName = clientNameEditText.getText().toString();
                    String clientPhone = clientPhoneEditText.getText().toString();
                    String currentLocationLatitude = String.valueOf(mCurrentLocation.getLatitude());
                    String currentLocationLongitude = String.valueOf(mCurrentLocation.getLongitude());
                    String mCurrentAddressName = currentAddressEditText.getText().toString();

                    Log.i("updateMachineOnClick", "machine ID" + mMachineId);

                    mMachine = new MachineModel(
                            data.get(getAdapterPosition()).getmUID(),
                            mMachineId,
                            clientName,
                            clientPhone,
                            mCurrentAddressName,
                            currentLocationLatitude,
                            currentLocationLongitude,
                            data.get(getAdapterPosition()).getmRepresentativeName());

                    if (mCallback != null) {
                        mCallback.onMachineItemUpdate(mMachine);
                        update_layout.setVisibility(View.GONE);
                        mOptionsSpinner.setSelection(getAdapterPosition(), false);
                        mOptionsSpinner.setSelection(0, true);
                    } else {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_message_to_user), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getUsername() {
            SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences(itemView.getContext().getString(R.string.shared_preferences_file_name), MODE_PRIVATE);
            String username = "none";
            try {
                username = sharedPreferences.getString(itemView.getContext().getString(R.string.username), "none");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return username;
        }

        private void addCurrentLocationOnClick(View v) {
            try {
//                starting Place Picker Builder
                if (mGpsTracker.canGetLocation()) {
                    Log.i("addCurrentLocation", "GPSTracker is enabled");
                    LatLng currentLocationLatLng = new LatLng(mGpsTracker.getLatitude(), mGpsTracker.getLongitude());

                    Log.i("addCurrentLocation", "current location Latitude = " + currentLocationLatLng.latitude);
                    Log.i("addCurrentLocation", "current location Longitude = " + currentLocationLatLng.longitude);

                    LatLngBounds currentLocation = new LatLngBounds(currentLocationLatLng, currentLocationLatLng);
                    mCurrentLocation = new Location("");
                    mCurrentLocation.setLatitude(currentLocationLatLng.latitude);
                    mCurrentLocation.setLongitude(currentLocationLatLng.longitude);
                    mCurrentAddressName = "none";
//                    // Use fields to define the data types to return.
//                    List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG,Place.Field.ADDRESS,Place.Field.NAME);
//
//                    // Use the builder to create a FindCurrentPlaceRequest.
//                    FindCurrentPlaceRequest request =
//                            FindCurrentPlaceRequest.builder(placeFields).build();
//
//                    // Call findCurrentPlace and handle the response (first check that the user has granted permission).
//                    if (ContextCompat.checkSelfPermission(v.getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        placesClient.findCurrentPlace(request).addOnSuccessListener(new OnSuccessListener<FindCurrentPlaceResponse>() {
//                            @Override
//                            public void onSuccess(FindCurrentPlaceResponse response) {
//                                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
//                                    Log.i("addCurrentLocation", String.format("Place '%s' has likelihood: %f",
//                                            placeLikelihood.getPlace().getName(),
//                                            placeLikelihood.getLikelihood()));
//                                    Place place = placeLikelihood.getPlace();
//                                    onPlaceSelected(place,viewHolder.this);
//
//                                }
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@androidx.annotation.NonNull Exception e) {
//                                e.printStackTrace();
//                                if (e instanceof ApiException) {
//                                    ApiException apiException = (ApiException) e;
//                                    Log.i("addCurrentLocation", "Place not found: " + apiException.getStatusCode());
//                                }
//                            }
//                        });
//                    }

                } else {
                    mGpsTracker.showSettingsAlert();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void saveCurrentAdapterPosition(View v) {
            SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("AllMachinesRecAdapterPosition", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("position", getAdapterPosition());
            editor.apply();
        }
    }

}
