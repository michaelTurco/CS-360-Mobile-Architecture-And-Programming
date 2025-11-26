package com.turco_michael_weight_tracking;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.turco_michael_weight_tracking.ui.view_list.WeightEntry;

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
                .update(WEIGHT_ENTRIES_FIELD, newEntries)
                .addOnSuccessListener(unused -> callback.accept(true))
                .addOnFailureListener(e -> {
                    userData.weightEntries = oldEntries;
                    callback.accept(false);
                });
    }
}
