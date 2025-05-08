package lk.handyhub.handyhub;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class CreateAccountFragment extends Fragment {

    private static final String TAG = CreateAccountFragment.class.getSimpleName();
    private FirebaseAuth mAuth; // Declare mAuth as a class-level variable
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private boolean isPhoneFlipped = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mAuth = FirebaseAuth.getInstance();


        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    // Detect phone flip
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];


                    if (z < -9.5) {
                        if (!isPhoneFlipped) {
                            isPhoneFlipped = true;
                            clearTextFields();
                        }
                    } else {
                        isPhoneFlipped = false;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);


        TextView textView = view.findViewById(R.id.textbtn2);
        textView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new SignInFragment()); // Use the single container ID
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        EditText first_name = view.findViewById(R.id.firstName);
        EditText last_name = view.findViewById(R.id.lastName);
        EditText mobile = view.findViewById(R.id.mobile);
        EditText password = view.findViewById(R.id.password);
        Button btnCreateAccount = view.findViewById(R.id.btnCreateAccount);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();

        btnCreateAccount.setOnClickListener(v -> {
            String firstName = first_name.getText().toString();
            String lastName = last_name.getText().toString();
            String phoneNumber = mobile.getText().toString();
            String pass = password.getText().toString();


            String mobileNumber = Verifications.formatMobileNumber(phoneNumber);


            if (firstName.isEmpty()) {
                Toast.makeText(getContext(), "First name is required", Toast.LENGTH_SHORT).show();
                return;
            }


            if (lastName.isEmpty()) {
                Toast.makeText(getContext(), "Last name is required", Toast.LENGTH_SHORT).show();
                return;
            }


            if (mobileNumber.isEmpty()) {
                Toast.makeText(getContext(), "Mobile number is required", Toast.LENGTH_SHORT).show();
                return;
            }


            if (pass.isEmpty()) {
                Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }


            if (!Verifications.isValidPhoneNumber(mobileNumber)) {
                Toast.makeText(getContext(), "Invalid phone number format. Please use the correct format (0771234567).", Toast.LENGTH_SHORT).show();
                return;
            }


            if (!Verifications.isValidPassword(pass)) {
                Toast.makeText(getContext(), "Password must be at least 8 characters long, including 1 uppercase letter and 1 symbol.", Toast.LENGTH_SHORT).show();
                return;
            }


            checkIfMobileExists(firstName, lastName, mobileNumber, pass);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private void checkIfMobileExists(String firstName, String lastName, String mobileNumber, String pass) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("mobile", mobileNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(getContext(), "Mobile number already registered!", Toast.LENGTH_SHORT).show();
                        } else {
                            MobileVerificationFragment fragment = new MobileVerificationFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("firstName", firstName);
                            bundle.putString("lastName", lastName);
                            bundle.putString("mobile", mobileNumber);
                            bundle.putString("password", pass);
                            fragment.setArguments(bundle);

                            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error checking mobile existence", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearTextFields() {
        EditText first_name = getView().findViewById(R.id.firstName);
        EditText last_name = getView().findViewById(R.id.lastName);
        EditText mobile = getView().findViewById(R.id.mobile);
        EditText password = getView().findViewById(R.id.password);

        first_name.setText("");
        last_name.setText("");
        mobile.setText("");
        password.setText("");
    }
}
