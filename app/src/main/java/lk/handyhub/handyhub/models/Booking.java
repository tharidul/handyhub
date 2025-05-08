package lk.handyhub.handyhub.models;

public class Booking {
    private String customerMobile;
    private String customerName;
    private String workerMobile;
    private String workerName;
    private String workerTitle;
    private String workerCategory;
    private float workerRating;
    private String date;
    private String time;
    private String location;
    private boolean isAccepted;
    private boolean isCanceled;
    private boolean isJobCompleted;
    private double charge;

    public Booking(String customerMobile,String customerName, String workerMobile, String workerName, String workerTitle,
                   String workerCategory, float workerRating, String date,
                   String time, String location, boolean isAccepted,boolean isCanceled, boolean isJobCompleted, double charge) {
        this.customerMobile = customerMobile;
        this.customerName = customerName;
        this.workerMobile = workerMobile;
        this.workerName = workerName;
        this.workerTitle = workerTitle;
        this.workerCategory = workerCategory;
        this.workerRating = workerRating;
        this.date = date;
        this.time = time;
        this.location = location;
        this.isAccepted = isAccepted;
        this.isCanceled = isCanceled;
        this.isJobCompleted = isJobCompleted;
        this.charge = charge;
    }

    // Default Constructor
    public Booking() {}

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getWorkerMobile() {
        return workerMobile;
    }

    public void setWorkerMobile(String workerMobile) {
        this.workerMobile = workerMobile;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerTitle() {
        return workerTitle;
    }

    public void setWorkerTitle(String workerTitle) {
        this.workerTitle = workerTitle;
    }

    public String getWorkerCategory() {
        return workerCategory;
    }

    public void setWorkerCategory(String workerCategory) {
        this.workerCategory = workerCategory;
    }

    public float getWorkerRating() {
        return workerRating;
    }

    public void setWorkerRating(float workerRating) {
        this.workerRating = workerRating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isJobCompleted() {
        return isJobCompleted;
    }

    public void setJobCompleted(boolean jobCompleted) {
        isJobCompleted = jobCompleted;
    }
}
