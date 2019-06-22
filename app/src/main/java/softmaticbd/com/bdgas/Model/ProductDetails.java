package softmaticbd.com.bdgas.Model;

import java.io.Serializable;

public class ProductDetails implements Serializable {

    private String size;
    private String quantity;
    private String price;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
