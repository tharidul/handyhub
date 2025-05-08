package lk.handyhub.handyhub.models;

public class User {

    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String line1;
    private String line2;
    private String postal;
    private  String city;
    private String profilePicture;
    private String workTitle;
    private String pricing;
    private  String nic;
    private String experience;
    private boolean isVerified;
    private String category;


    public User() {
    }

    public User(String firstName, String lastName, String mobile, String email, String line1,
                String line2, String postal, String city, String profilePicture, String workTitle,
                String pricing, String nic, String experience, boolean isVerified, String category) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.email = email;
        this.line1 = line1;
        this.line2 = line2;
        this.postal = postal;
        this.city = city;
        this.profilePicture = profilePicture;
        this.workTitle = workTitle;
        this.pricing = pricing;
        this.nic = nic;
        this.experience = experience;
        this.isVerified = isVerified;
        this.category = category;

    }



    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(String line2) {
        this.line2 = line2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getWorkTitle() {
        return workTitle;
    }

    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getPricing() {
        return pricing;
    }

    public void setPricing(String pricing) {
        this.pricing = pricing;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
