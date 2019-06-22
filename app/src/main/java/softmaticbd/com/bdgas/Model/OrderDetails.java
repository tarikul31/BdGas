package softmaticbd.com.bdgas.Model;

import java.io.Serializable;

public class OrderDetails implements Serializable {
    private String proName;
    private String category;
    private String unitPrice;
    private String quantity;
    private String cusName;
    private String contact;
    private String address;
    private String totalBill;

    public OrderDetails() {
    }

    public OrderDetails(String proName, String category, String unitPrice, String quantity, String cusName, String contact, String address, String totalBill) {
        this.proName = proName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.cusName = cusName;
        this.contact = contact;
        this.address = address;
        this.totalBill = totalBill;
    }

    public String getProName() {
        return proName;
    }

    public void setProName(String proName) {
        this.proName = proName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotalBill() {
        return totalBill;
    }

    public void setTotalBill(String totalBill) {
        this.totalBill = totalBill;
    }
}
