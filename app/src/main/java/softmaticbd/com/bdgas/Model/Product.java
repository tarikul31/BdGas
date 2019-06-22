package softmaticbd.com.bdgas.Model;

import java.io.Serializable;
import java.util.List;

public class Product implements Serializable {
    //todo: Distributor
    private String productId;
    private String productName;
    private List<ProductDetails> productDetails;
    //todo: Customer
    private String deliveryAddress;

    public Product() {
    }

    //TODO: Distributor
    public Product(String productId, String productName, List<ProductDetails> productDetails) {
        this.productId = productId;
        this.productName = productName;
        this.productDetails = productDetails;
    }

    //TODO: Customer
    public Product(String productId, String productName, List<ProductDetails> productDetails, String deliveryAddress) {
        this.productId = productId;
        this.productName = productName;
        this.productDetails = productDetails;
        this.deliveryAddress = deliveryAddress;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public List<ProductDetails> getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(List<ProductDetails> productDetails) {
        this.productDetails = productDetails;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}