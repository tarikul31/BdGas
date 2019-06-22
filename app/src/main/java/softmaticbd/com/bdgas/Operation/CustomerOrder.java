package softmaticbd.com.bdgas.Operation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chivorn.smartmaterialspinner.SmartMaterialSpinner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import softmaticbd.com.bdgas.MapActivity.CustomerMapsActivity;
import softmaticbd.com.bdgas.Model.OrderDetails;
import softmaticbd.com.bdgas.Model.Product;
import softmaticbd.com.bdgas.Model.ProductDetails;
import softmaticbd.com.bdgas.R;
import softmaticbd.com.bdgas.SignActivity.SignUpActivity;

public class CustomerOrder extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private TextView shopTv;
    private String disKey, disShopName;
    private SmartMaterialSpinner msProduct, msCategory;
    private List<String> proNameList = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();
    private List<String> proPriceList = new ArrayList<>();
    private List<String> keyList = new ArrayList<>();
    private List<String> availQuantity = new ArrayList<>();

    //todo firebase implementation
    private DatabaseReference disProfileRef, productRef;
    private FirebaseAuth auth;
    private String userID;
    private DatabaseReference assignDisRef, deliveryRef, findRef;

    //todo for order procedure
    private TextView tvAvailQuantity, tvTotalBill, tvUnitPrice;
    private EditText billName, billAddress, billContact, orderQuantity;
    private String proName, category, unitPrice, quantity, cusName, contact, address, totalBill;
    private Button btnOrder;
    private String pName, pQuantity, orderProPrice;
    private Product productObj;
    private static final String REQUIRED = "Required";
    private OrderDetails orderDetails;
    private ProductDetails productDetails;
    private String proKey;
    private int uPrice, productIndex, categoryIndex, proQuantity;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order);
        shopTv = findViewById(R.id.shopNameID);
        msProduct = findViewById(R.id.sp_Product);
        msCategory = findViewById(R.id.sp_Category);

        // todo for order procedure
        tvAvailQuantity = findViewById(R.id.tvAvailAmount);
        tvTotalBill = findViewById(R.id.tvTotalBill);
        billName = findViewById(R.id.billName);
        billContact = findViewById(R.id.billContact);
        billAddress = findViewById(R.id.billAddress);
        orderQuantity = findViewById(R.id.orderQuantity);
        orderQuantity.addTextChangedListener(this);
        tvUnitPrice = findViewById(R.id.tvUnitPrice);
        btnOrder = findViewById(R.id.btnOrderID);
        btnOrder.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        userID = auth.getUid();

        //todo progress Bar
        pDialog = new ProgressDialog(CustomerOrder.this, R.style.MyAlertDialogStyle);
        pDialog.setTitle("Loading Data.....");
        pDialog.setCancelable(false);

        getDistributorAndProductInfo();
        selectProductAndCategory();

    }

    private void getDistributorAndProductInfo() {
        disKey = getIntent().getExtras().getString("disKey");

        disProfileRef = FirebaseDatabase.getInstance().getReference()
                .child("BdGas").child("Distributor").child("Profile")
                .child(disKey).child("shopName");
        disProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    disShopName = dataSnapshot.getValue().toString();
                    shopTv.setText(disShopName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        productRef = FirebaseDatabase.getInstance().getReference()
                .child("Product").child(disKey);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Log.d("Product", dataSnapshot.getKey());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        proKey = snapshot.getKey();
                        keyList.add(proKey);
                        Product product = snapshot.getValue(Product.class);
                        proNameList.add(product.getProductName());
                        productList.add(product);
                        msProduct.setItems(proNameList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void selectProductAndCategory() {

        msProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryList.clear();
                proName = proNameList.get(position);
                productIndex = position;
                productObj = productList.get(position);
                for (ProductDetails details : productObj.getProductDetails()) {
                    categoryList.add(details.getSize());
                    proPriceList.add(details.getPrice());
                    availQuantity.add(details.getQuantity());
                }
                msCategory.setItems(categoryList);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        msCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = categoryList.get(position);
                categoryIndex = position;
                tvUnitPrice.setText(proPriceList.get(position));
                uPrice = Integer.parseInt(proPriceList.get(position));
                unitPrice = String.valueOf(uPrice);
                tvAvailQuantity.setText(availQuantity.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v == btnOrder) {
            getOrderDetails();
        }
    }

    // todo for runtime text update
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (s.length() == 0) {
            tvTotalBill.setText("");
            quantity = "";
        } else {
            proQuantity = Integer.parseInt(s.toString());
            quantity = s.toString();
            tvTotalBill.setText(proQuantity * uPrice + "/-");
            totalBill = tvTotalBill.getText().toString();
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void getOrderDetails() {
        cusName = billName.getText().toString();
        contact = billContact.getText().toString();
        address = billAddress.getText().toString();

        if (TextUtils.isEmpty(cusName)) {
            billName.setError(REQUIRED);
            billName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            billContact.setError(REQUIRED);
            billContact.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            billAddress.setError(REQUIRED);
            billAddress.requestFocus();
            return;
        }

        // todo for total bill

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Product Confirmation");
        alert.setMessage("Are you Sure to Order this Product ? ");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes", (dialog, which) -> {
            pDialog.show();
            dialog.dismiss();
            orderDetails = new OrderDetails(proName, category, unitPrice, quantity, cusName, contact, address, totalBill);
            sendProduct(disKey);
        });
        alert.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        alert.show();

    }

    private void sendProduct(String key) {
        assignDisRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("CustomerRequest")
                .child(key)
                .child(userID);
        assignDisRef.setValue(orderDetails).addOnSuccessListener(aVoid -> {
            updateDatabase();
        });
    }

    private void updateDatabase() {
        Product product = productList.get(productIndex);
        List<ProductDetails> detailsList = product.getProductDetails();
        ProductDetails details = detailsList.get(categoryIndex);
        int avQuantity = Integer.parseInt(details.getQuantity());
        int total = avQuantity - proQuantity;
        details.setQuantity(String.valueOf(total));
        detailsList.set(categoryIndex, details);
        product.setProductDetails(detailsList);
        FirebaseDatabase.getInstance().getReference()
                .child("Product")
                .child(disKey)
                .child(keyList.get(productIndex))
                .setValue(product).addOnSuccessListener(task -> {
            getDeliveryResult();
            Toast.makeText(CustomerOrder.this, "Success", Toast.LENGTH_SHORT).show();
        });
    }


    private void getDeliveryResult() {
        pDialog.dismiss();
        deliveryRef = FirebaseDatabase.getInstance().getReference()
                .child("Activity").child("Distributor")
                .child(disKey).child(userID);
        deliveryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(CustomerOrder.this);
                    alert.setTitle("Response");
                    alert.setMessage("Your Service Delivery Successful");
                    alert.setCancelable(false);
                    alert.setPositiveButton("Ok", (dialog, which) -> {
                        deliveryRef.removeValue();
                        startActivity(new Intent(CustomerOrder.this, FeedbackActivity.class).putExtra("disKey", disKey));

//                        removeFindRequest();
                        dialog.dismiss();
                    });
                    alert.show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
