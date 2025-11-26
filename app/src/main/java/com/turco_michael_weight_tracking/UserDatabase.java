package com.turco_michael_weight_tracking;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class UserDatabase {
    private static final String COLLECTION_NAME = "users";
    private static final String WEIGHT_ENTRIES_FIELD = "weightEntries";

    private static UserDatabase instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FirebaseUserData userData;

    private boolean initialized;

    private UserDatabase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static UserDatabase getInstance() {
        if (instance == null) instance = new UserDatabase();
        return instance;
    }

    public void initialize(Consumer<Boolean> callback) {
        if (initialized) {
            callback.accept(true);
            return;
        }

        // if not authenticated yet, fail initialization
        if (auth.getCurrentUser() == null) {
            callback.accept(false);
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        // read data from firebase firestore
        db.collection(COLLECTION_NAME)
                .document(uid)
                .get()
                .addOnSuccessListener(this::onFetchSuccess)
                .addOnFailureListener(e -> callback.accept(false))
                .addOnCompleteListener(task -> callback.accept(task.isSuccessful() && initialized));
    }

    private void onFetchSuccess(DocumentSnapshot snapshot) {
        userData = snapshot.toObject(FirebaseUserData.class);

        // handle invalid or missing data
        if (userData == null) {
            userData = new FirebaseUserData();
        }

        initialized = true;
    }

    public List<WeightEntry> getWeightEntries() {
        return userData.weightEntries;
    }

    public float getMostRecentWeight() {
        if (userData.weightEntries.isEmpty()) return LocalStorage.UNKNOWN;
        return userData.weightEntries.get(0).getWeight();
    }

    public void setWeightEntries(List<WeightEntry> newEntries, Consumer<Boolean> callback) {
        // if not initialized, can't write data
        if (!initialized) {
            callback.accept(false);
            return;
        }

        // if not authenticated yet, can't write data
        if (auth.getCurrentUser() == null) {
            callback.accept(false);
            return;
        }
        String uid = auth.getCurrentUser().getUid();

        List<WeightEntry> oldEntries = userData.weightEntries;
        userData.weightEntries = newEntries;

        // write to the user's weight entry field
        // if it fails, revert to original data
        db.collection(COLLECTION_NAME)
                .document(uid)
                .set(
                        Collections.singletonMap(WEIGHT_ENTRIES_FIELD, newEntries),
                        SetOptions.merge()
                )
                .addOnSuccessListener(unused -> callback.accept(true))
                .addOnFailureListener(e -> {
                    userData.weightEntries = oldEntries;
                    callback.accept(false);
                });
    }

    public void addWeightEntry(WeightEntry entry, Consumer<Boolean> callback) {
        // if not initialized, can't write data
        if (!initialized) {
            callback.accept(false);
            return;
        }

        List<WeightEntry> newEntries = new ArrayList<>(userData.weightEntries);
        newEntries.add(0, entry);

        setWeightEntries(newEntries, callback);
    }

    public String getUsername() {
        if (auth.getCurrentUser() == null) return null;

        String email = auth.getCurrentUser().getEmail();
        if (email == null) return null;

        int index = email.indexOf('@');
        if (index <= 0) return null;

        // make the first letter uppercase
        String name = email.substring(0, index);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
