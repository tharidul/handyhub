package lk.handyhub.handyhub.models;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import lk.handyhub.handyhub.R;
import lk.handyhub.handyhub.ui.home.BookingWorkerFragment;

import android.content.SharedPreferences;

public class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {
    private Context context;
    private List<Worker> workerList;
    private FragmentManager fragmentManager;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;


    public WorkerAdapter(Context context, List<Worker> workerList, FragmentManager fragmentManager) {
        this.context = context;
        this.workerList = workerList;
        this.fragmentManager = fragmentManager;
        db = FirebaseFirestore.getInstance();
        sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE); // SharedPreferences instance
    }

    @NonNull
    @Override
    public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workersitem, parent, false);
        return new WorkerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
        Worker worker = workerList.get(position);
        holder.workerName.setText(worker.getName());
        holder.workTitle.setText(worker.getTitle());
        holder.categoryBadge.setText(worker.getCategory());
        holder.ratingBar.setRating(worker.getRating());
        holder.profileImage.setImageResource(worker.getProfileImageResId());

        String customerMobile = sharedPreferences.getString("mobile", "");


        checkIfBooked(customerMobile, worker.getMobile(), holder.hireButton);

        holder.hireButton.setOnClickListener(v -> {
            BookingWorkerFragment bookingWorkerFragment = new BookingWorkerFragment();
            Bundle bundle = new Bundle();
            bundle.putString("workerName", worker.getName());
            bundle.putString("workerMobile", worker.getMobile());
            bundle.putString("workerTitle", worker.getTitle());
            bundle.putString("workerCategory", worker.getCategory());
            bundle.putFloat("workerRating", worker.getRating());

            bookingWorkerFragment.setArguments(bundle);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragmentContainerView2, bookingWorkerFragment);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }


    private void checkIfBooked(String customerMobile, String workerMobile, Button hireButton) {
        db.collection("bookings")
                .whereEqualTo("customerMobile", customerMobile)
                .whereEqualTo("workerMobile", workerMobile)
                .whereEqualTo("isAccepted", true)
                .whereEqualTo("isJobCompleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        hireButton.setText("Hired");
                        hireButton.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {

                });
    }

    @Override
    public int getItemCount() {
        return workerList.size();
    }

    public static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView workerName, workTitle, categoryBadge, reviewCount;
        RatingBar ratingBar;
        ImageView profileImage;
        Button hireButton;

        public WorkerViewHolder(@NonNull View itemView) {
            super(itemView);
            workerName = itemView.findViewById(R.id.workerName);
            workTitle = itemView.findViewById(R.id.workTitle);
            categoryBadge = itemView.findViewById(R.id.categoryBadge);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            profileImage = itemView.findViewById(R.id.profileImage);
            hireButton = itemView.findViewById(R.id.hireButton);
        }
    }
}

