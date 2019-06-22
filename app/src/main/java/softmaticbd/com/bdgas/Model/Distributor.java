package softmaticbd.com.bdgas.Model;

import java.io.Serializable;

public class Distributor implements Serializable {
    private String name;
    private String phone;
    private String email;
    private String shopName;
    private String shopAddress;
    private String profileImage;
    private String cusReview;

    public Distributor() {
    }

    public Distributor(String name, String phone, String email, String shopName, String shopAddress, String profileImage) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
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

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getCusReview() {
        return cusReview;
    }

    public void setCusReview(String cusReview) {
        this.cusReview = cusReview;
    }
}
