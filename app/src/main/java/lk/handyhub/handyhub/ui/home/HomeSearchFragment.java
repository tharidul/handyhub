package lk.handyhub.handyhub.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import lk.handyhub.handyhub.R;
import lk.handyhub.handyhub.databinding.FragmentHomeSearchBinding;
import lk.handyhub.handyhub.models.Worker;
import lk.handyhub.handyhub.models.WorkerAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeSearchFragment extends Fragment {

    private FragmentHomeSearchBinding binding;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;
    private List<Worker> filteredList;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.workerRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        db = FirebaseFirestore.getInstance();
        workerList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Pass the FragmentManager to the WorkerAdapter
        FragmentManager fragmentManager = getParentFragmentManager();
        workerAdapter = new WorkerAdapter(getContext(), filteredList, fragmentManager); // Pass FragmentManager here
        binding.workerRecyclerView.setAdapter(workerAdapter);

        EditText searchField = root.findViewById(R.id.search_field);

        loadWorkers();

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWorkers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return root;
    }

    private static final String TAG = "HomeFragment";

    private void loadWorkers() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        String loggedInMobile = sharedPreferences.getString("mobile", null);
        CollectionReference usersRef = db.collection("users");

        usersRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            if (value == null || value.isEmpty()) {
                return;
            }

            workerList.clear();

            for (QueryDocumentSnapshot doc : value) {
                if (doc.contains("worker_profile")) {
                    Object workerProfileObj = doc.get("worker_profile");

                    if (workerProfileObj instanceof java.util.Map) {
                        java.util.Map<String, Object> workerProfile = (java.util.Map<String, Object>) workerProfileObj;


                        String mobile = doc.getString("mobile");
                        if (mobile != null && mobile.equals(loggedInMobile)) {
                            continue;
                        }

                        String vStatus = (String) workerProfile.get("v_status");
                        if ("2".equals(vStatus)) {
                            String firstName = doc.getString("first_name");
                            String lastName = doc.getString("last_name");
                            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                            String jobTitle = (String) workerProfile.get("title");
                            String category = (String) workerProfile.get("category");
                            String rate = "Rs. " + (String) workerProfile.get("pricing");
                            double rating = workerProfile.containsKey("average_rating") ?
                                    (double) workerProfile.get("average_rating") : 0.0;

                            Worker worker = new Worker(fullName, mobile, jobTitle, (float) rating, category, true, rate, R.mipmap.user);
                            workerList.add(worker);
                        }
                    }
                }
            }

            // Initially show all workers
            filteredList.clear();
            filteredList.addAll(workerList);
            workerAdapter.notifyDataSetChanged();
        });
    }


    private void filterWorkers(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(workerList);
        } else {
            for (Worker worker : workerList) {
                if (worker.getName().toLowerCase().contains(query.toLowerCase()) ||
                        worker.getCategory().toLowerCase().contains(query.toLowerCase()) ||
                        worker.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(worker);
                }
            }
        }
        workerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}
