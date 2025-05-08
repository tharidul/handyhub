package lk.handyhub.handyhub.models;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import java.util.List;
import lk.handyhub.handyhub.R;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;

public class HireWorkerAdapter extends RecyclerView.Adapter<HireWorkerAdapter.HireViewHolder> {

    private final List<Booking> bookingList;
    private final Context context;
    private final ActivityResultLauncher<Intent> paymentResultLauncher;
    private final PaymentCallback paymentCallback;


    public interface PaymentCallback {
        void onPaymentInitiated(Booking booking);
    }

    public HireWorkerAdapter(List<Booking> bookingList, Context context,
                             ActivityResultLauncher<Intent> paymentResultLauncher,
                             PaymentCallback paymentCallback) {
        this.bookingList = bookingList;
        this.context = context;
        this.paymentResultLauncher = paymentResultLauncher;
        this.paymentCallback = paymentCallback;
    }

    @NonNull
    @Override
    public HireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hireviewsitem, parent, false);
        return new HireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HireViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.workerName.setText(booking.getWorkerName());
        holder.workerTitle.setText(booking.getWorkerTitle());
        holder.workerMobile.setText(booking.getWorkerMobile());

        if (booking.getCharge() == 0) {
            holder.charge.setText("Charge not set.");
        } else {
            holder.charge.setText("Charge: LKR " + booking.getCharge());
        }

        holder.payNowButton.setOnClickListener(v -> initiatePayment(booking));
        holder.callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + booking.getWorkerMobile()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    private void initiatePayment(Booking booking) {

        if (paymentCallback != null) {
            paymentCallback.onPaymentInitiated(booking);
        }


        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof ContextWrapper) {
            activity = (Activity) ((ContextWrapper) context).getBaseContext();
        }
        if (activity == null) {
            Toast.makeText(context, "Error: Unable to process payment", Toast.LENGTH_SHORT).show();
            return;
        }


        InitRequest req = new InitRequest();
        req.setMerchantId("1221727");
        req.setCurrency("LKR");
        req.setAmount(booking.getCharge());
        req.setOrderId("HANDYHUB-" + booking.getCustomerMobile() + "-" + System.currentTimeMillis());
        req.setItemsDescription(booking.getWorkerTitle());
        req.getCustomer().setFirstName("Customer");
        req.getCustomer().setLastName("User");
        req.getCustomer().setEmail("customer@example.com");
        req.getCustomer().setPhone(booking.getWorkerMobile());
        req.getCustomer().getAddress().setAddress("No.1, Colombo");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");
        req.setNotifyUrl("https://your-notify-url.com");
        req.getItems().add(new Item(null, booking.getWorkerTitle(), 1, booking.getCharge()));


        Intent intent = new Intent(activity, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);


        paymentResultLauncher.launch(intent);
    }

    public static class HireViewHolder extends RecyclerView.ViewHolder {
        TextView workerName, workerTitle, workerMobile, charge;
        Button payNowButton;
        ImageView callButton;

        public HireViewHolder(View itemView) {
            super(itemView);
            workerName = itemView.findViewById(R.id.worker_name);
            workerTitle = itemView.findViewById(R.id.worker_title);
            workerMobile = itemView.findViewById(R.id.worker_mobile);
            charge = itemView.findViewById(R.id.charge_view);
            payNowButton = itemView.findViewById(R.id.btnPayNow);
            callButton = itemView.findViewById(R.id.call);


        }
    }
}
