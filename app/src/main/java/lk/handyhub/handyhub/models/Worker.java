package lk.handyhub.handyhub.models;

public class Worker {
    private String name;
    private String mobile;
    private String title;
    private float rating;

    private String category;
    private boolean isHourlyRate;
    private CharSequence payment;
    private int profileImageResId;


    public Worker(String name, String mobile, String title, float rating, String category, boolean isHourlyRate, CharSequence payment , int profileImageResId) {

        this.name = name;
        this.mobile = mobile;
        this.title = title;
        this.rating = rating;
        this.category = category;
        this.isHourlyRate = isHourlyRate;
        this.payment = payment;
        this.profileImageResId = profileImageResId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean getPayment() {
        return isHourlyRate;
    }

    public void setPayment(boolean isHourlyRate) {
        this.isHourlyRate = isHourlyRate;
    }

    public CharSequence getHourlyRate() {
        return payment;
    }

    public void setHourlyRate(CharSequence hourlyRate) {
        this.payment = hourlyRate;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public void setProfileImageResId(int profileImageResId) {
        this.profileImageResId = profileImageResId;
    }
}
