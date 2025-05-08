package lk.handyhub.handyhub.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import lk.handyhub.handyhub.R;
import lk.handyhub.handyhub.models.LocalDBHandler;
import lk.handyhub.handyhub.models.User;

public class MeFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText firstNameEditText, lastNameEditText, mobileEditText, emailEditText;
    private EditText postalCodeEditText, streetAddress1EditText, streetAddress2EditText;

    private static final int PICK_IMAGE_REQUEST = 1;
    private StorageReference storageReference;
    private ImageView profilePicture;
    private Uri imageUri;
    private String userMobile;

    private User user;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        Spinner citySpinner = rootView.findViewById(R.id.city);
        firstNameEditText = rootView.findViewById(R.id.firstName);
        lastNameEditText = rootView.findViewById(R.id.lastName);
        mobileEditText = rootView.findViewById(R.id.mobile);
        emailEditText = rootView.findViewById(R.id.email);

        postalCodeEditText = rootView.findViewById(R.id.postalCode);
        streetAddress1EditText = rootView.findViewById(R.id.streetAddress1);
        streetAddress2EditText = rootView.findViewById(R.id.streetAddress2);

        db = FirebaseFirestore.getInstance();

        loadCitiesIntoSpinner(citySpinner, this::fetchUserData);

        Button btnSaveChanges = rootView.findViewById(R.id.btnSaveChanges);
        btnSaveChanges.setOnClickListener(view -> {
            saveUserData(citySpinner);
            uploadImageToFirebase();
        });

        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userMobile = sharedPreferences.getString("mobile", null);

        if (userMobile != null) {
            loadProfileImage();
        }

        profilePicture = rootView.findViewById(R.id.profilepicture);
        profilePicture.setOnClickListener(view -> openFileChooser());

        return rootView;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePicture.setImageURI(imageUri);
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null && userMobile != null) {
            StorageReference fileReference = storageReference.child(userMobile + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveImageUrlToFirestore(uri.toString());
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageUrlToFirestore(String imageUrl) {
        db.collection("users").whereEqualTo("mobile", userMobile).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentReference userDoc = task.getResult().getDocuments().get(0).getReference();
                        userDoc.update("profile_picture", imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                                    }
                                    loadProfileImage();
                                })
                                .addOnFailureListener(e -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Failed to update Firestore", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }


    private void loadProfileImage() {
        db.collection("users").whereEqualTo("mobile", userMobile).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        String imageUrl = userDoc.getString("profile_picture");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(profilePicture);
                        }
                    }
                });
    }

    private void loadCitiesIntoSpinner(Spinner citySpinner, Runnable onCitiesLoadedCallback) {
        db.collection("cities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> cityNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String cityName = document.getString("city");
                            if (cityName != null) {
                                cityNames.add(cityName);
                            }
                        }

                        if (!cityNames.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_item, cityNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            citySpinner.setAdapter(adapter);

                            // After adapter is set, execute the callback
                            if (onCitiesLoadedCallback != null) {
                                onCitiesLoadedCallback.run();
                            }
                        } else {
                            Toast.makeText(getContext(), "No cities found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error getting cities: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserData() {
        LocalDBHandler dbHandler = new LocalDBHandler(getContext());

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        String mobile = sharedPreferences.getString("mobile", null);
        user = dbHandler.getUserByMobile(mobile);

        if (user != null) {
            firstNameEditText.setText(user.getFirstName() != null ? user.getFirstName() : "");
            lastNameEditText.setText(user.getLastName() != null ? user.getLastName() : "");
            emailEditText.setText(user.getEmail() != null ? user.getEmail() : "");
            mobileEditText.setText(user.getMobile() != null ? user.getMobile() : "");
            postalCodeEditText.setText(user.getPostal() != null ? user.getPostal() : "");
            streetAddress1EditText.setText(user.getLine1() != null ? user.getLine1() : "");
            streetAddress2EditText.setText(user.getLine2() != null ? user.getLine2() : "");

            String city = user.getCity();
            if (city != null && !city.isEmpty()) {
                Spinner citySpinner = requireView().findViewById(R.id.city);
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) citySpinner.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(city);
                    citySpinner.setSelection(position);
                }
            } else {
                Spinner citySpinner = requireView().findViewById(R.id.city);
                citySpinner.setSelection(0);
            }

        } else {
            Toast.makeText(getContext(), "User data not found in local database", Toast.LENGTH_SHORT).show();
        }

        dbHandler.close();
    }

    private void saveUserData(Spinner citySpinner) {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String mobile = mobileEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String postalCode = postalCodeEditText.getText().toString();
        String streetAddress1 = streetAddress1EditText.getText().toString();
        String streetAddress2 = streetAddress2EditText.getText().toString();
        String city = citySpinner.getSelectedItem().toString();

        new Thread(() -> {
            LocalDBHandler dbHandler = new LocalDBHandler(getContext());

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setMobile(mobile);
            user.setEmail(email);
            user.setPostal(postalCode);
            user.setLine1(streetAddress1);
            user.setLine2(streetAddress2);
            user.setCity(city);

            User existingUser = dbHandler.getUserByMobile(mobile);

            if (existingUser != null) {
                dbHandler.updateUserData(user);
            } else {
                dbHandler.insertUserData(user);
            }
            dbHandler.close();
        }).start();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(mobile);

        userDocRef.update(
                "first_name", firstName,
                "last_name", lastName,
                "email", email,
                "mobile", mobile,
                "postal", postalCode,
                "line1", streetAddress1,
                "line2", streetAddress2,
                "city", city
        ).addOnSuccessListener(aVoid -> {
            fetchUserData();
            Toast.makeText(getContext(), "Profile Update Successful", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to save user data to Firebase", Toast.LENGTH_SHORT).show();
        });
    }
}
