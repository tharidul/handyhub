package lk.handyhub.handyhub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mindrot.jbcrypt.BCrypt;

import lk.handyhub.handyhub.models.LocalDBHandler;
import lk.handyhub.handyhub.models.User;

public class SignInFragment extends Fragment {

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private boolean isPhoneFlipped = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize sensor manager and accelerometer listener
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

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

        TextView textView = view.findViewById(R.id.textbtn1);
        textView.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, new CreateAccountFragment());
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        Button signinButton = view.findViewById(R.id.btnSignIn);
        signinButton.setOnClickListener(v -> {
            EditText mobile = view.findViewById(R.id.mobile2);
            EditText password = view.findViewById(R.id.password2);
            String mobileNumber = mobile.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (mobileNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Mobile number is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Verifications.isValidPhoneNumber(mobileNumber)) {
                Toast.makeText(requireContext(), "Invalid phone number format. Please use the correct format (0771234567).", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mobileNumber.startsWith("0")) {
                mobileNumber = "+94" + mobileNumber.substring(1);
            }

            if (pass.isEmpty()) {
                Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            final String finalMobileNumber = mobileNumber;
            final String finalPass = pass;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .whereEqualTo("mobile", finalMobileNumber)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);



                            // Retrieve user details
                            String storedHashedPassword = userDoc.getString("password");
                            String firstName = userDoc.getString("first_name");
                            String lastName = userDoc.getString("last_name");

                            if (storedHashedPassword != null && BCrypt.checkpw(finalPass, storedHashedPassword)) {
                                LocalDBHandler dbHandler = new LocalDBHandler(requireContext());
                                User newUser = new User();
                                newUser.setFirstName(firstName);
                                newUser.setLastName(lastName);
                                newUser.setMobile(finalMobileNumber);
                                newUser.setEmail(userDoc.getString("email"));
                                newUser.setPostal(userDoc.getString("postal"));
                                newUser.setLine1(userDoc.getString("line1"));
                                newUser.setLine2(userDoc.getString("line2"));
                                newUser.setCity(userDoc.getString("city"));
                                dbHandler.insertUserData(newUser);
                                dbHandler.close();


                                SharedPreferences loginPrefs = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = loginPrefs.edit();
                                editor.putString("mobile", finalMobileNumber);
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();


                                Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show();


                                Intent intent = new Intent(requireContext(), HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(requireContext(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register the accelerometer listener when the fragment starts
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the sensor listener to avoid memory leaks
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private void clearTextFields() {
        EditText mobile = getView().findViewById(R.id.mobile2);
        EditText password = getView().findViewById(R.id.password2);

        mobile.setText("");
        password.setText("");
    }
}
