package lk.handyhub.handyhub.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.handyhub.handyhub.R;

public class CreateWorkProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private StorageReference storageReference;
    private Spinner categorySpinner;
    private EditText workTitleEditText, workerExperienceEditText, workerPricingEditText, nicEditText;
    private ImageView nicFrontImageView, nicBackImageView;
    private Uri nicFrontUri, nicBackUri;
    private static final int PICK_NIC_FRONT = 1;
    private static final int PICK_NIC_BACK = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_work_profile, container, false);

        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        workTitleEditText = rootView.findViewById(R.id.work_titile);
        workerExperienceEditText = rootView.findViewById(R.id.workerExperience);
        workerPricingEditText = rootView.findViewById(R.id.workerPricing);
        nicEditText = rootView.findViewById(R.id.nic_number);
        nicFrontImageView = rootView.findViewById(R.id.nic_front);
        nicBackImageView = rootView.findViewById(R.id.nic_back);
        Button btnSaveWorkerProfile = rootView.findViewById(R.id.btnSaveWorkerProfile);
        categorySpinner = rootView.findViewById(R.id.serviceCategory);

        loadCategories();

        nicFrontImageView.setOnClickListener(v -> pickImage(PICK_NIC_FRONT));
        nicBackImageView.setOnClickListener(v -> pickImage(PICK_NIC_BACK));

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("login_prefs", MODE_PRIVATE);
        String userMobileNumber = sharedPreferences.getString("mobile", "");

        // Fetch user data from Firestore to check verification status
        db.collection("users")
                .whereEqualTo("mobile", userMobileNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);


                        String verifyStatus = document.getString("worker_profile.v_status"); // "1", "2", "3"


                        ImageView verifiedImageView = rootView.findViewById(R.id.imageView5);


                        if ("1".equals(verifyStatus)) { // Pending verification
                            nicEditText.setEnabled(false);
                            btnSaveWorkerProfile.setEnabled(false);
                            workTitleEditText.setEnabled(false);
                            workerExperienceEditText.setEnabled(false);
                            workerPricingEditText.setEnabled(false);
                            categorySpinner.setEnabled(false);
                            nicFrontImageView.setVisibility(View.GONE);
                            nicBackImageView.setVisibility(View.GONE);

                            verifiedImageView.setImageResource(R.mipmap.inprogress);
                            verifiedImageView.setVisibility(View.VISIBLE);

                        } else if ("2".equals(verifyStatus)) { // Accepted or verified
                            nicEditText.setEnabled(false);
                            nicFrontImageView.setVisibility(View.GONE);
                            nicBackImageView.setVisibility(View.GONE);

                            verifiedImageView.setImageResource(R.mipmap.verified);
                            verifiedImageView.setVisibility(View.VISIBLE);

                        } else if ("3".equals(verifyStatus)) { // Rejected status
                            verifiedImageView.setImageResource(R.mipmap.rejected);
                            verifiedImageView.setVisibility(View.VISIBLE);

                        }



                        Map<String, Object> workerProfile = (Map<String, Object>) document.get("worker_profile");
                        if (workerProfile != null) {
                            String workTitle = (String) workerProfile.get("title");
                            String experience = (String) workerProfile.get("experience");
                            String pricing = (String) workerProfile.get("pricing");
                            String category = (String) workerProfile.get("category");
                            String nic = (String) workerProfile.get("nic");


                            workTitleEditText.setText(workTitle);
                            workerExperienceEditText.setText(experience);
                            workerPricingEditText.setText(pricing);
                            nicEditText.setText(nic);


                            if (category != null) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
                                if (adapter != null) {
                                    int position = adapter.getPosition(category);
                                    categorySpinner.setSelection(position);
                                }
                            }


                            String nicFrontUrl = (String) workerProfile.get("nic_front");
                            String nicBackUrl = (String) workerProfile.get("nic_back");
                            if (nicFrontUrl != null) {

                            }
                            if (nicBackUrl != null) {

                            }
                        }
                    }
                });

        btnSaveWorkerProfile.setOnClickListener(v -> {
            if (nicFrontUri == null || nicBackUri == null) {
                Toast.makeText(getContext(), "Please select both NIC images", Toast.LENGTH_SHORT).show();
                return;
            }

            String frontPath = "user_nic/" + userMobileNumber + "/nic_front.jpg";
            String backPath = "user_nic/" + userMobileNumber + "/nic_back.jpg";

            StorageReference frontRef = storageReference.child(frontPath);
            StorageReference backRef = storageReference.child(backPath);

            frontRef.putFile(nicFrontUri)
                    .continueWithTask(task -> task.isSuccessful() ? frontRef.getDownloadUrl() : null)
                    .addOnFailureListener(e -> Log.e("FirebaseStorage", "Front Image Upload Failed: " + e.getMessage()));

            backRef.putFile(nicBackUri)
                    .continueWithTask(task -> task.isSuccessful() ? backRef.getDownloadUrl() : null)
                    .addOnFailureListener(e -> Log.e("FirebaseStorage", "Back Image Upload Failed: " + e.getMessage()));

            frontRef.getDownloadUrl().addOnSuccessListener(frontUrl ->
                    backRef.getDownloadUrl().addOnSuccessListener(backUrl -> {
                        saveWorkerProfile(userMobileNumber, frontUrl.toString(), backUrl.toString());
                    })
            );
        });

        return rootView;
    }

    private void saveWorkerProfile(String userMobileNumber, String frontUrl, String backUrl) {
        DocumentReference userRef = db.collection("users").document(userMobileNumber);

        String workTitle = workTitleEditText.getText().toString().trim();
        String experience = workerExperienceEditText.getText().toString().trim();
        String pricing = workerPricingEditText.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String nic = nicEditText.getText().toString().trim();

        Map<String, Object> workerProfile = new HashMap<>();
        workerProfile.put("title", workTitle);
        workerProfile.put("experience", experience);
        if (pricing.isEmpty()) {
            workerProfile.put("is_hourly_rate", false);
        } else {
            workerProfile.put("is_hourly_rate", true);
            workerProfile.put("hourly_rate", pricing);
        }
        workerProfile.put("category", selectedCategory);
        workerProfile.put("nic", nic);
        workerProfile.put("nic_front", frontUrl);
        workerProfile.put("nic_back", backUrl);
        workerProfile.put("v_status", "1");
        workerProfile.put("total_ratings", 0);
        workerProfile.put("rating_sum", 0.0);
        workerProfile.put("average_rating", 0.0);
        workerProfile.put("is_available", true);







        userRef.update("worker_profile", workerProfile)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();

                    refreshUI(userMobileNumber);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error updating worker profile: " + e.getMessage());
                });
    }

    private void refreshUI(String userMobileNumber) {
        // Fetch the updated data from Firestore to update the UI
        db.collection("users")
                .whereEqualTo("mobile", userMobileNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);

                        Map<String, Object> workerProfile = (Map<String, Object>) document.get("worker_profile");
                        if (workerProfile != null) {
                            String workTitle = (String) workerProfile.get("title");
                            String experience = (String) workerProfile.get("experience");
                            String pricing = (String) workerProfile.get("pricing");
                            String category = (String) workerProfile.get("category");

                            // Update the UI with the new data
                            workTitleEditText.setText(workTitle);
                            workerExperienceEditText.setText(experience);
                            workerPricingEditText.setText(pricing);

                            // Update category spinner selection
                            if (category != null) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
                                if (adapter != null) {
                                    int position = adapter.getPosition(category);
                                    categorySpinner.setSelection(position);
                                }
                            }
                        }
                    }
                });
    }

    private void loadCategories() {
        db.collection("work_categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> categoryNames = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String categoryName = document.getString("category");
                    if (categoryName != null) {
                        categoryNames.add(categoryName);
                    }
                }
                if (!categoryNames.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);
                }
            }
        });
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_NIC_FRONT) {
                nicFrontUri = data.getData();
                nicFrontImageView.setImageURI(nicFrontUri);
            } else if (requestCode == PICK_NIC_BACK) {
                nicBackUri = data.getData();
                nicBackImageView.setImageURI(nicBackUri);
            }
        }
    }
}
