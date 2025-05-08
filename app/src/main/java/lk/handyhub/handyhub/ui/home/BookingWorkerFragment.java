package lk.handyhub.handyhub.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lk.handyhub.handyhub.R;
import lk.handyhub.handyhub.models.LocalDBHandler;
import lk.handyhub.handyhub.models.User;

public class BookingWorkerFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private EditText dateEditText, timeEditText;
    private Button searchPlaceButton, bookButton;
    private FirebaseFirestore db;
    private Bundle bundle;
    private String workerMobile;
    private String customerMobile;
    private String workerName;
    private String workerTitle;
    private String workerCategory;
    private String workerHourlyRate;
    private float workerRating;
    private User user;
    private boolean isAccepted;
    private boolean isJobCompleted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking_worker, container, false);

        // Initialize views
        mapView = rootView.findViewById(R.id.mapView);
        searchPlaceButton = rootView.findViewById(R.id.searchPlace);
        bookButton = rootView.findViewById(R.id.bookButton);
        dateEditText = rootView.findViewById(R.id.date_editText);
        timeEditText = rootView.findViewById(R.id.time_editText);

        // Map setup
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.google_maps_key));
        }

        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());
        searchPlaceButton.setOnClickListener(v -> startAutocompleteActivity());

        bundle = getArguments();
        if(bundle != null){
            workerMobile = bundle.getString("workerMobile");
            workerName = bundle.getString("workerName");
            workerTitle = bundle.getString("workerTitle");
            workerCategory = bundle.getString("workerCategory");
            workerHourlyRate = bundle.getString("workerHourlyRate");
            workerRating = bundle.getFloat("workerRating");
        }
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        customerMobile = sharedPreferences.getString("mobile", null);

        LocalDBHandler dbHandler = new LocalDBHandler(getContext());
        user = new User();
        user = dbHandler.getUserByMobile(customerMobile);

        bookButton.setOnClickListener(view -> saveBooking(customerMobile, workerMobile));

        return rootView;
    }

    private void saveBooking(String customerMobile, String workerMobile) {

        // Create a map to store booking data
        HashMap<String, Object> bookingData = new HashMap<>();
        bookingData.put("customerMobile", customerMobile);
        bookingData.put("customerName", user.getFirstName() + " " + user.getLastName());
        bookingData.put("workerMobile", workerMobile);
        bookingData.put("workerName", workerName);
        bookingData.put("workerTitle", workerTitle);
        bookingData.put("workerCategory", workerCategory);
        bookingData.put("workerHourlyRate", workerHourlyRate);
        bookingData.put("workerRating", workerRating);
        bookingData.put("date", dateEditText.getText().toString());
        bookingData.put("time", timeEditText.getText().toString());
        bookingData.put("location", selectedLocation != null ? selectedLocation.toString() : "Unknown");
        bookingData.put("isAccepted", isAccepted);
        bookingData.put("isJobCompleted", isJobCompleted);

        db = FirebaseFirestore.getInstance();
        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Snackbar snackbar = Snackbar.make(getView(), "Booking successful", Snackbar.LENGTH_LONG);
                    snackbar.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to save booking", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (getActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            selectedLocation = currentLocation;
                        } else {
                            Toast.makeText(getActivity(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Permission not granted for location", Toast.LENGTH_SHORT).show();
        }


        mMap.setOnMapClickListener(point -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(point).title("Selected Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            selectedLocation = point;
        });
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    dateEditText.setText(selectedDate);
                    dateEditText.clearFocus(); // Remove focus after selection
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Show Time Picker
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (TimePicker view, int hourOfDay, int minute) -> {
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                    timeEditText.setText(selectedTime);
                    timeEditText.clearFocus(); // Remove focus after selection
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }


    private void startAutocompleteActivity() {
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
                .setCountry("LK")
                .build(getActivity());

        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            selectedLocation = place.getLatLng();
            if (selectedLocation != null) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Log.e("Autocomplete", "Error: " + status.getStatusMessage());
        }
    }
}
