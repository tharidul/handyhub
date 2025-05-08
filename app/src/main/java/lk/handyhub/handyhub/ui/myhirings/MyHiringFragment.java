package lk.handyhub.handyhub.ui.myhirings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import lk.handyhub.handyhub.R;
import lk.handyhub.handyhub.models.HireWorkerAdapter;
import lk.handyhub.handyhub.models.HireWorkerAdapter.PaymentCallback;
import lk.handyhub.handyhub.models.Booking;
import lk.payhere.androidsdk.PHConstants;

public class MyHiringFragment extends Fragment implements PaymentCallback {

    private RecyclerView hireRecyclerView;
    private HireWorkerAdapter hireWorkerAdapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private Booking currentBookingForPayment;

    public static MyHiringFragment newInstance() {
        return new MyHiringFragment();
    }

    ActivityResultLauncher<Intent> paymentResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        String paymentStatus = result.getData().getStringExtra(PHConstants.INTENT_EXTRA_RESULT);
                        Toast.makeText(getContext(), "Payment successful: " + paymentStatus, Toast.LENGTH_SHORT).show();
                        Log.d("PayHereLog", "Payment successful: " + paymentStatus);
                        // Instead of using a booking ID, query using worker and customer mobile numbers.
                        if (currentBookingForPayment != null) {
                            db.collection("bookings")
                                    .whereEqualTo("workerMobile", currentBookingForPayment.getWorkerMobile())
                                    .whereEqualTo("customerMobile", currentBookingForPayment.getCustomerMobile())
                                    .whereEqualTo("isJobCompleted", false)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                document.getReference().update("isJobCompleted", true)
                                                        .addOnSuccessListener(aVoid ->
                                                                Toast.makeText(getContext(), "Booking updated successfully", Toast.LENGTH_SHORT).show()
                                                        )
                                                        .addOnFailureListener(e ->
                                                                Toast.makeText(getContext(), "Failed to update booking", Toast.LENGTH_SHORT).show()
                                                        );
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "No incomplete booking found", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Error searching for booking", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(getContext(), "Payment successful: No data returned", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(getContext(), "Payment was canceled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Payment failed", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_hiring, container, false);

        hireRecyclerView = root.findViewById(R.id.hire_recyclerView);
        bookingList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login_prefs", getContext().MODE_PRIVATE);
        String customerMobile = sharedPreferences.getString("mobile", "");

        if (customerMobile != null && !customerMobile.isEmpty()) {
            loadBookingsFromFirestore(customerMobile);
        } else {
            Toast.makeText(getContext(), "Mobile number not found in preferences", Toast.LENGTH_SHORT).show();
        }

        // Pass the paymentResultLauncher and PaymentCallback (this fragment) to the adapter.
        hireWorkerAdapter = new HireWorkerAdapter(bookingList, getContext(), paymentResultLauncher, this);
        hireRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        hireRecyclerView.setAdapter(hireWorkerAdapter);

        return root;
    }

    private void loadBookingsFromFirestore(String customerMobile) {
        db.collection("bookings")
                .whereEqualTo("customerMobile", customerMobile)
                .whereEqualTo("isJobCompleted", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documentSnapshots = task.getResult();
                        if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                            bookingList.clear();
                            for (QueryDocumentSnapshot document : documentSnapshots) {
                                Booking booking = document.toObject(Booking.class);
                                bookingList.add(booking);
                            }
                            hireWorkerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No incomplete bookings found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error loading bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPaymentInitiated(Booking booking) {
        currentBookingForPayment = booking;
    }
}
