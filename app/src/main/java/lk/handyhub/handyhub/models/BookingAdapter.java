package lk.handyhub.handyhub.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;
import java.util.List;
import lk.handyhub.handyhub.R;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private OnBookingActionListener listener;
    private Context context;

    public BookingAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.jobsviewsitem, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        if (booking.isAccepted()) {
            holder.acceptButton.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.GONE);
            holder.setChargeButton.setVisibility(View.VISIBLE);
            holder.chargeEditText.setVisibility(View.VISIBLE);
        } else {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.setChargeButton.setVisibility(View.GONE);
            holder.chargeEditText.setVisibility(View.GONE);
        }

        holder.customerName.setText(booking.getCustomerName());
        holder.customerMobile.setText(booking.getCustomerMobile());
        holder.dateTime.setText(booking.getDate() + " " + booking.getTime());
        holder.callButton.setOnClickListener(v -> listener.onCallClicked(booking.getCustomerMobile()));
        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClicked(booking));
        holder.cancelButton.setOnClickListener(v -> listener.onCancelClicked(booking));
        holder.setChargeButton.setOnClickListener(v -> listener.onsetChargeClicked(booking));

        // Handle map location (if needed)
        LatLng location = parseLatLng(booking.getLocation());
        if (location != null) {
            holder.mapView.onCreate(null);
            holder.mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.getUiSettings().setAllGesturesEnabled(false); // Disable interaction
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    googleMap.addMarker(new MarkerOptions().position(location).title("Job Location"));
                }
            });
            holder.mapView.onResume();
        }
    }


    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, customerMobile, dateTime;
        MapView mapView;
        ImageView callButton;
        Button acceptButton, setChargeButton, cancelButton;
        EditText chargeEditText;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.worker_name);
            customerMobile = itemView.findViewById(R.id.worker_mobile);
            dateTime = itemView.findViewById(R.id.datetimeView);
            mapView = itemView.findViewById(R.id.mapView2);
            callButton = itemView.findViewById(R.id.call);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            setChargeButton = itemView.findViewById(R.id.btnPayNow);
            cancelButton = itemView.findViewById(R.id.btnCancel);
            chargeEditText = itemView.findViewById(R.id.setChargeText);
        }
    }

    public interface OnBookingActionListener {
        void onCallClicked(String customerMobile);
        void onAcceptClicked(Booking booking);
        void onCancelClicked(Booking booking);
        void onsetChargeClicked(Booking booking);
    }

    private LatLng parseLatLng(String locationString) {
        try {
            String latLngPart = locationString.replace("lat/lng: (", "").replace(")", "");
            String[] latLngArray = latLngPart.split(",");
            double lat = Double.parseDouble(latLngArray[0].trim());
            double lng = Double.parseDouble(latLngArray[1].trim());
            return new LatLng(lat, lng);
        } catch (Exception e) {
            return null;
        }
    }
}
