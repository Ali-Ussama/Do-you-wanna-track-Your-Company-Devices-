package com.example.aliussama.fawry.Model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.aliussama.fawry.Model.Callbacks.OnAddMachineListener;
import com.example.aliussama.fawry.Model.Callbacks.ReadingAllDatabaseCallback;
import com.example.aliussama.fawry.Model.Callbacks.SearchActivityCallback;
import com.example.aliussama.fawry.Model.Callbacks.UserDatabaseCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by ali Ussama on 7/6/2018.
 */

public class UserDatabase {

    private final String GET_ALL_USERS_TAG = "getAllUsers";
    private final String DELETE_USER_TAG = "deleteUser";
    private final String UPDATE_USER_TAG = "updateUser";


    public void CheckIfUserExists(final String phone, final String email, final UserDatabaseCallback callback) {

        final String TAG = "CheckIfUserExists";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Log.i(TAG, "Firebase reference is initialized");

        reference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Log.i(TAG, "Extracting codes from data Snapshot");
                    GenericTypeIndicator<Map<String, Map<String, String>>> t = new GenericTypeIndicator<Map<String, Map<String, String>>>() {
                    };
                    Map<String, Map<String, String>> codes = dataSnapshot.getValue(t);
                    Log.i(TAG, "Declaring var to check found code");
                    boolean founded = false;
                    if (codes != null) {
                        Log.i(TAG, "username: " + email + " password: " + phone);
                        Log.i(TAG, "returned codes are not null");
                        for (String code : codes.keySet()) {
                            String mPhone = codes.get(code).get("phone").toLowerCase();
                            String id = codes.get(code).get("id").toLowerCase();
                            String type = codes.get(code).get("type").toLowerCase();

                            Log.i(TAG, "current username is : " + id + "\n phone : " + mPhone);
                            if (mPhone.matches(phone.toLowerCase()) && id.matches(email.toLowerCase())) {
                                Log.i(TAG, "User Code is found in Firebase database");
                                founded = true;
                                Log.i(TAG, "Sending callback with true value");
                                if (type.matches("admin")) {
                                    callback.onLoginSuccess(true, "admin");
                                } else if (type.matches("user")) {
                                    callback.onLoginSuccess(true, "user");
                                }
                                break;
                            }
                        }
                        if (!founded) {
                            Log.i(TAG, "User Code is not found in Firebase database");
                            callback.onLoginSuccess(false, null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, databaseError.getMessage());
            }
        });
    }

    public void addUser(final UserModel user, final UserDatabaseCallback callback) {
        try {
            final String TAG = "addUser";
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Log.i(TAG, "Firebase reference is initialized");

            reference.child("users").push().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete() && task.isSuccessful()) {
                        if (callback != null)
                            callback.onAddUserSuccess(true);
                    } else {
                        if (callback != null)
                            callback.onAddUserSuccess(false);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    if (callback != null)
                        callback.onAddUserSuccess(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllUsers(final ReadingAllDatabaseCallback callback) {
        try {

            Log.i(GET_ALL_USERS_TAG, "getAllUsers is called");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Log.i(GET_ALL_USERS_TAG, "Declaring Database reference");
            reference.child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Log.i(GET_ALL_USERS_TAG, "onDataChange is called");
                        GenericTypeIndicator<Map<String, Map<String, String>>> mapGenericTypeIndicator = new GenericTypeIndicator<Map<String, Map<String, String>>>() {
                        };
                        ArrayList<UserModel> users = new ArrayList<>();
                        Log.i(GET_ALL_USERS_TAG, "reading data from dataSnapShot");
                        Map<String, Map<String, String>> result = dataSnapshot.getValue(mapGenericTypeIndicator);
                        Log.i(GET_ALL_USERS_TAG, "data from dataSnapShot has been read");
                        if (result != null) {
                            Log.i(GET_ALL_USERS_TAG, "result is not null");
                            for (String key : result.keySet()) {
                                Log.i(GET_ALL_USERS_TAG, "user = " + result.get(key).get("name"));
                                users.add(new UserModel(key, result.get(key).get("id"), result.get(key).get("name"),
                                        result.get(key).get("phone"), result.get(key).get("type")));
                            }

                            if (callback != null) {
                                Log.i(GET_ALL_USERS_TAG, "sending users to callback");
                                callback.onAllUsersSuccess(users);
                            }
                        } else {

                            if (callback != null) {
                                Log.i(GET_ALL_USERS_TAG, "sending users to callback");
                                callback.onAllUsersFailure(new Exception("reading all users, result is null"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (callback != null) {
                        callback.onAllUsersFailure(databaseError.toException());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(UserModel user) {
        Log.i(DELETE_USER_TAG, "deleteUser is called");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Log.i(DELETE_USER_TAG, "Declaring Database reference");
        reference.child("users").child(user.getUserKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                try {
                    if (task.isComplete() && task.isSuccessful()) {
                        Log.i(DELETE_USER_TAG, "user has been deleted successfully");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateUser(UserModel user) {
        Log.i(UPDATE_USER_TAG, "updateUser is called");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Log.i(UPDATE_USER_TAG, "Declaring Database reference");
        reference.child("users").child(user.getUserKey()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful() && task.isComplete()) {
                    Log.i(UPDATE_USER_TAG, "onComplete: user is updated successfully");
                } else if (task.isCanceled()) {
                    Log.i(UPDATE_USER_TAG, "onComplete: task is Canceled");

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getAllMachines(final ReadingAllDatabaseCallback callback) {
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("machines").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {

                        ArrayList<MachineModel> machines = new ArrayList<>();

                        Map<String, Map<String, String>> machinesResult = (Map<String, Map<String, String>>) dataSnapshot.getValue();

                        if (machinesResult != null) {
                            for (String key : machinesResult.keySet()) {
                                machines.add(new MachineModel(
                                        machinesResult.get(key).get("mUID"),
                                        machinesResult.get(key).get("mMachineId"),
                                        machinesResult.get(key).get("mClientName"),
                                        machinesResult.get(key).get("mClientPhone"),
                                        machinesResult.get(key).get("mAddress"),
                                        machinesResult.get(key).get("mLatitude"),
                                        machinesResult.get(key).get("mLongitude")));
                            }

                            if (callback != null)
                                callback.onAllMachinesSuccess(machines);

                        } else {

                            if (callback != null)
                                callback.onAllMachinesFailure("No machines added in database yet");

                        }
                    } catch (Exception e) {
                        if (callback != null)
                            callback.onAllMachinesFailure(e.getMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (callback != null)
                        callback.onAllMachinesFailure(databaseError.getMessage());
                    else {
                        databaseError.toException().printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMachine(final MachineModel machine, final OnAddMachineListener callback) {
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String key = reference.child("machines").push().getKey();
            machine.setmUID(key);
            if (key != null)
                reference.child("machines").child(key).setValue(machine).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete() && task.isSuccessful()) {
                            if (callback != null)
                                callback.onAddMachineSuccess(true);
                        } else if (callback != null)
                            callback.onAddMachineSuccess(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callback != null)
                            callback.onAddMachineFailure(e);
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkIfMachineExists(final String machineCode, final OnAddMachineListener callback) {
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("machines").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        boolean mMachineIsFounded = false;
                        Map<String, Map<String, String>> result = (Map<String, Map<String, String>>) dataSnapshot.getValue();
                        if (result != null) {
                            for (String key : result.keySet()) {
                                if (result.get(key).get("mMachineId").matches(machineCode)) {
                                    mMachineIsFounded = true;
                                    break;
                                }
                            }
                        }

                        if (mMachineIsFounded) {
                            if (callback != null) {
                                callback.onMachineExists(true);
                            }
                        } else {
                            if (callback != null) {
                                callback.onMachineExists(false);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (callback != null)
                        callback.onAddMachineFailure(databaseError.toException());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMachine(MachineModel machine, final SearchActivityCallback callback) {
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("machines").child(machine.getmUID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful() && task.isComplete()) {
                        if (callback != null) {
                            callback.onMachineDeletedSuccess(true);
                        }
                    } else {
                        if (callback != null) {
                            callback.onMachineDeletedSuccess(false);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (callback != null) {
                        callback.onMachineDeleteFailure(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMachine(MachineModel machine, final SearchActivityCallback callback) {

        Log.i("updateMachine", "machine ID" + machine.getmMachineId());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("machines").child(machine.getmUID()).setValue(machine).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete() && task.isSuccessful()) {
                    Log.i("updateMachine", "machine is updated");
                    if (callback != null)
                        callback.onMachineUpdatedSuccess(true);
                } else {
                    if (callback != null)
                        callback.onMachineUpdatedSuccess(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (callback != null)
                    callback.onMachineUpdatedFailure(e);
            }
        });
    }
}
