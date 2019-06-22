package softmaticbd.com.bdgas.Model;


import java.io.Serializable;

public class Customer implements Serializable {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String profileImage;

    public Customer() {
    }

    public Customer(String name, String phone, String email, String address, String profileImage) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}