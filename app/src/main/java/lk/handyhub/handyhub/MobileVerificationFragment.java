package lk.handyhub.handyhub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import lk.handyhub.handyhub.models.LocalDBHandler;
import lk.handyhub.handyhub.models.User;

public class MobileVerificationFragment extends Fragment {

    private static final String handyhubLog = "HandyhubLog";  // Updated tag name
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String verificationId;
    private String firstName, lastName, mobileNumber, password;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            firstName = getArguments().getString("firstName");
            lastName = getArguments().getString("lastName");
            mobileNumber = getArguments().getString("mobile");
            password = getArguments().getString("password");

            Log.d(handyhubLog, "First Name: " + firstName);
            Log.d(handyhubLog, "Last Name: " + lastName);
            Log.d(handyhubLog, "Mobile Number: " + mobileNumber);
            Log.d(handyhubLog, "Password: " + password);

            sendOtpToUser(mobileNumber);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile_verification, container, false);

        otp1 = view.findViewById(R.id.otp1);
        otp2 = view.findViewById(R.id.otp2);
        otp3 = view.findViewById(R.id.otp3);
        otp4 = view.findViewById(R.id.otp4);
        otp5 = view.findViewById(R.id.otp5);
        otp6 = view.findViewById(R.id.otp6);
        Button btnVerifyOtp = view.findViewById(R.id.btnCreateAccount3);

        setOtpFieldListeners();

        btnVerifyOtp.setOnClickListener(v -> {
            String enteredOtp = otp1.getText().toString() +
                    otp2.getText().toString() +
                    otp3.getText().toString() +
                    otp4.getText().toString() +
                    otp5.getText().toString() +
                    otp6.getText().toString();
            Log.d(handyhubLog, "Entered OTP: " + enteredOtp);
            verifyOtp(enteredOtp);
        });

        return view;
    }

    private void setOtpFieldListeners() {
        otp1.addTextChangedListener(new OTPTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OTPTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OTPTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OTPTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new OTPTextWatcher(otp5, otp6));
        otp6.addTextChangedListener(new OTPTextWatcher(otp6, null));  // Last field doesn't move focus
    }

    private class OTPTextWatcher implements TextWatcher {

        private final EditText currentField;
        private final EditText nextField;

        OTPTextWatcher(EditText currentField, EditText nextField) {
            this.currentField = currentField;
            this.nextField = nextField;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            if (currentField.getText().length() == 1 && nextField != null) {

                nextField.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void sendOtpToUser(String phoneNumber) {
        Log.d(handyhubLog, "Sending OTP to phone number: " + phoneNumber);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp(String otp) {
        if (verificationId == null) {
            Log.d(handyhubLog, "OTP verification failed: verificationId is null");
            Toast.makeText(getContext(), "OTP verification failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(handyhubLog, "Verifying OTP: " + otp);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserDataToFirestore();
                    } else {
                        Log.d(handyhubLog, "Sign-in failed: Incorrect OTP");
                        Toast.makeText(getContext(), "Incorrect OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataToFirestore() {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        Log.d(handyhubLog, "Saving user data to Firestore");

        HashMap<String, Object> user = new HashMap<>();
        user.put("first_name", firstName);
        user.put("last_name", lastName);
        user.put("mobile", mobileNumber);
        user.put("isVerified", false);
        user.put("password", hashedPassword);

        db.collection("users").document(mobileNumber)
                .set(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d(handyhubLog, "Account created successfully");

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LocalDBHandler dbHandler = new LocalDBHandler(getContext());
                            User newUser = new User();
                            newUser.setFirstName(firstName);
                            newUser.setLastName(lastName);
                            newUser.setMobile(mobileNumber);
                            dbHandler.insertUserData(newUser);
                            dbHandler.close();

                            SharedPreferences loginPrefs = getContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = loginPrefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("mobile", mobileNumber);
                            editor.apply();

                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();

                    Intent intent = new Intent(getContext(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                })
                .addOnFailureListener(e -> {
                    Log.d(handyhubLog, "Database error: " + e.getMessage());
                    Toast.makeText(getContext(), "Database Error", Toast.LENGTH_SHORT).show();
                });
    }


    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    Log.d(handyhubLog, "Verification completed");
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.d(handyhubLog, "Verification failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    MobileVerificationFragment.this.verificationId = verificationId;
                    Log.d(handyhubLog, "OTP sent to mobile. Verification ID: " + verificationId);
                    Toast.makeText(getContext(), "OTP Sent to your Mobile", Toast.LENGTH_SHORT).show();
                }
            };
}
