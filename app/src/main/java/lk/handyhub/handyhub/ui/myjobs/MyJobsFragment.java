package lk.handyhub.handyhub.ui.myjobs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.handyhub.handyhub.R;
import lk.handyhub.handyhub.databinding.FragmentMyjobsBinding;
import lk.handyhub.handyhub.models.Booking;
import lk.handyhub.handyhub.models.BookingAdapter;

public class MyJobsFragment extends Fragment {

    private FragmentMyjobsBinding binding;
    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private String workerMobile;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyjobsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.jobRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(requireContext(), bookingList, new BookingAdapter.OnBookingActionListener() {

            @Override
            public void onCallClicked(String customerMobile) {
                String formattedNumber = "tel:" + customerMobile.trim();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse(formattedNumber));
                startActivity(callIntent);
            }

            @Override
            public void onAcceptClicked(Booking booking) {
                db.collection("bookings").whereEqualTo("customerMobile", booking.getCustomerMobile()).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String documentId = document.getId(); // Get the document ID
                                db.collection("bookings").document(documentId)
                                        .update("isAccepted", true)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireContext(), "Booking accepted!", Toast.LENGTH_SHORT).show();
                                            booking.setAccepted(true);
                                            bookingAdapter.notifyItemChanged(bookingList.indexOf(booking));
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to accept booking.", Toast.LENGTH_SHORT).show());
                            }
                        });
            }

            @Override
            public void onCancelClicked(Booking booking) {
                db.collection("bookings").whereEqualTo("customerMobile", booking.getCustomerMobile()).get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String documentId = document.getId();
                                db.collection("bookings").document(documentId)
                                        .update("status", "canceled")
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireContext(), "Booking canceled.", Toast.LENGTH_SHORT).show();
                                            booking.setCanceled(true);
                                            bookingAdapter.notifyItemChanged(bookingList.indexOf(booking));
                                        });
                            }
                        });
            }

            @Override
            public void onsetChargeClicked(Booking booking) {
                // Access the charge EditText correctly from the fragment's view
                EditText chargeEditText = binding.getRoot().findViewById(R.id.setChargeText);
                String chargeText = chargeEditText.getText().toString().trim();

                if (chargeText.isEmpty()) {
                    // Show a message if the charge value is empty
                    Toast.makeText(requireContext(), "Charge value cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {

                    double chargeAmount = Double.parseDouble(chargeText);


                    db.collection("bookings")
                            .whereEqualTo("workerMobile", booking.getWorkerMobile())
                            .whereEqualTo("customerMobile", booking.getCustomerMobile())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    String documentId = document.getId();
                                    db.collection("bookings").document(documentId)
                                            .update("charge", chargeAmount)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(), "Charge successfully set", Toast.LENGTH_SHORT).show();
                                                booking.setCharge(chargeAmount);
                                                bookingAdapter.notifyItemChanged(bookingList.indexOf(booking));
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(requireContext(), "Failed to set charge", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Show an error message if the query fails
                                Toast.makeText(requireContext(), "Error finding booking", Toast.LENGTH_SHORT).show();
                            });

                } catch (NumberFormatException e) {
                    // Show an error message if the charge value is not a valid number
                    Toast.makeText(requireContext(), "Please enter a valid charge amount", Toast.LENGTH_SHORT).show();
                }
            }


        });

        recyclerView.setAdapter(bookingAdapter);

        db = FirebaseFirestore.getInstance();
        workerMobile = getWorkerMobile();
        if (workerMobile != null) {
            loadBookings();
        }

        return root;
    }

    private String getWorkerMobile() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("mobile", null);
    }

    private void loadBookings() {
        CollectionReference bookingsRef = db.collection("bookings");
        Query query = bookingsRef.whereEqualTo("workerMobile", workerMobile);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error fetching bookings", error);
                return;
            }

            List<Booking> tempList = new ArrayList<>(); // Temporary list to avoid UI lag

            for (QueryDocumentSnapshot doc : value) {
                Booking booking = doc.toObject(Booking.class);

                Boolean isAccepted = doc.getBoolean("isAccepted");
                Double charge = doc.contains("charge") ? doc.getDouble("charge") : 0.0; // Handle missing field

                Log.d("Booking", "Charge: " + charge);

                booking.setCharge(charge); // Ensure charge is set
                booking.setAccepted(isAccepted != null && isAccepted);

                tempList.add(booking);
            }

            bookingList.clear();
            bookingList.addAll(tempList);
            bookingAdapter.notifyDataSetChanged();
        });
    }



}
