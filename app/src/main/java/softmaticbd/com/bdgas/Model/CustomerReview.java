package softmaticbd.com.bdgas.Model;

public class CustomerReview {
    private String name;
    private String address;
    private String review;
    private String userRating;

    public CustomerReview() {
    }

    public CustomerReview(String name, String address, String review, String userRating) {
        this.name = name;
        this.address = address;
        this.review = review;
        this.userRating = userRating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }
}
