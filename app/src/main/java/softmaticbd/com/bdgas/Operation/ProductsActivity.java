package softmaticbd.com.bdgas.Operation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import softmaticbd.com.bdgas.MapActivity.DistributorMapsActivity;
import softmaticbd.com.bdgas.Model.Product;
import softmaticbd.com.bdgas.Model.ProductDetails;
import softmaticbd.com.bdgas.R;

public class ProductsActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private EditText pName, pPrice, pQuantity, pSize;
    private Button btnProSubmit,btnProUpdate;
    private String name, price, quantity;
    private static final String REQUIRED ="Required";

    private FirebaseAuth auth;
    private DatabaseReference reference,productRef;
    private String userId;
    private ProgressDialog pDialog;

    // todo ProductDetailsAction
    private List<ProductDetails> productDetailsList= new ArrayList<>();
    private Button btnAdd, btnUpdate;

    //todo getProduct
    private Product product;
    private String pKey;
    private TextView proName,proPrice,proQuantity,emptyTv;
    private LinearLayout detailsLayoutID;
    private int index = -1;
    private Product productDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        toolbar = findViewById(R.id.toolBarId);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        product = new Product();

        //todo: Product Details
        pName = findViewById(R.id.productName);
        pSize = findViewById(R.id.sizeID);
        pQuantity = findViewById(R.id.quantityID);
        pPrice = findViewById(R.id.priceID);
        btnAdd = findViewById(R.id.btnAdd);
        btnProSubmit= findViewById(R.id.btnSubmit);
        detailsLayoutID = findViewById(R.id.detailsLayoutID);
        emptyTv= findViewById(R.id.emptyTV);
        btnUpdate = findViewById(R.id.btnUpdate);

        //todo: OnClick Listener
        btnAdd.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnProSubmit.setOnClickListener(this);

        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Loading data....");
        pDialog.setCancelable(false);

        if (getIntent().getExtras()!=null){
            productDetails = (Product) getIntent().getSerializableExtra("pObj");
        }

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Product").child(userId);
        getProductDetails();
        bindDataToView();

    }

    private void bindDataToView() {
        if (productDetails != null ){
            pName.setText(productDetails.getProductName());
            for (ProductDetails proDetails: productDetails.getProductDetails()){
                final View view = LayoutInflater.from(this).inflate(R.layout.product_details_layout, null);
                final TextView sizeTV= view.findViewById(R.id.dSizeTV);
                final TextView quantityTv= view.findViewById(R.id.dQuantityTV);
                final TextView priceTV= view.findViewById(R.id.dPriceTV);
                final ImageButton btnEdit= view.findViewById(R.id.dBtnEdit);
                final ImageButton btnDelete = view.findViewById(R.id.dBtnDelete);

                ProductDetails productList2 = new ProductDetails();
                productList2.setSize(proDetails.getSize());
                productList2.setQuantity(proDetails.getQuantity());
                productList2.setPrice(proDetails.getPrice());
                productDetailsList.add(productList2);

                sizeTV.setText(productList2.getSize());
                quantityTv.setText(productList2.getQuantity());
                priceTV.setText(productList2.getPrice());
                btnEdit.setOnClickListener(v -> {
                    pSize.setText(sizeTV.getText().toString());
                    pQuantity.setText(quantityTv.getText().toString());
                    pPrice.setText(priceTV.getText().toString());
                    btnAdd.setVisibility(View.GONE);
                    btnUpdate.setVisibility(View.VISIBLE);
                    index = detailsLayoutID.indexOfChild(view)-1;
                });

                btnDelete.setOnClickListener(v -> {
                    detailsLayoutID.removeView(view);
                });
                detailsLayoutID.addView(view);
                if (detailsLayoutID.getChildCount() > 0) {
                    emptyTv.setVisibility(View.GONE);
                } else {
                    emptyTv.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void getProductDetails(){
        productRef = FirebaseDatabase.getInstance().getReference().child("Product").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        pKey = snapshot.getKey();
                        product = snapshot.getValue(Product.class);
                        product.setProductId(pKey);
//                        proName.setText("Product Name : "+ product.getProductName());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == btnProSubmit) {
            pDialog.show();
            addProduct();
        }

        if (v == btnAdd){
            addOrUpdateProductToDetailsList();
        }
        if (v == btnUpdate){
            addOrUpdateProductToDetailsList();
        }
    }

    private void addOrUpdateProductToDetailsList() {
        String size= pSize.getText().toString();
        String quantity = pQuantity.getText().toString();
        String price = pPrice.getText().toString();

        if (TextUtils.isEmpty(pSize.getText())){
            pSize.setError(REQUIRED);
            pSize.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pQuantity.getText())){
            pQuantity.setError(REQUIRED);
            pQuantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pPrice.getText())){
            pPrice.setError(REQUIRED);
            pPrice.requestFocus();
            return;
        }

        final View view = LayoutInflater.from(this).inflate(R.layout.product_details_layout, null);
        final TextView sizeTV= view.findViewById(R.id.dSizeTV);
        final TextView quantityTv= view.findViewById(R.id.dQuantityTV);
        final TextView priceTV= view.findViewById(R.id.dPriceTV);
        final ImageButton btnEdit= view.findViewById(R.id.dBtnEdit);
        final ImageButton btnDelete = view.findViewById(R.id.dBtnDelete);

        ProductDetails productDetails = new ProductDetails();
        productDetails.setSize(size);
        productDetails.setQuantity(quantity);
        productDetails.setPrice(price);
        if (index > -1){
            productDetailsList.set(index, productDetails);
        }else {
            productDetailsList.add(productDetails);
        }

        sizeTV.setText(size);
        quantityTv.setText(quantity);
        priceTV.setText(price);

        //todo for clear field
        pSize.setText("");
        pQuantity.setText("");
        pPrice.setText("");

        btnEdit.setOnClickListener(v -> {
            pSize.setText(sizeTV.getText().toString());
            pQuantity.setText(quantityTv.getText().toString());
            pPrice.setText(priceTV.getText().toString());
            btnAdd.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
            index = detailsLayoutID.indexOfChild(view)-1;
        });

        btnDelete.setOnClickListener(v->{
            detailsLayoutID.removeView(view);
        });

        if (index > -1){
            detailsLayoutID.removeViewAt(index+1);
            detailsLayoutID.addView(view,index+1);
            btnAdd.setVisibility(View.VISIBLE);
            btnUpdate.setVisibility(View.GONE);
            index = -1;
        }else {
            detailsLayoutID.addView(view);
        }

        if (detailsLayoutID.getChildCount() > 0) {
            emptyTv.setVisibility(View.GONE);
        } else {
            emptyTv.setVisibility(View.VISIBLE);
        }
    }

    private void addProduct() {
        name = pName.getText().toString();

        if (name.isEmpty()) {
            pDialog.dismiss();
            pName.setError("Please Provide a Product Name");
            pName.requestFocus();
            return;
        }
        if (productDetails != null) {

            product = new Product(productDetails.getProductId(), name, productDetailsList);
            reference.child(productDetails.getProductId()).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        pDialog.dismiss();
                        startActivity(new Intent(ProductsActivity.this, DistributorMapsActivity.class));
                        Toast.makeText(ProductsActivity.this, "Product Updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        pDialog.dismiss();
                        Toast.makeText(ProductsActivity.this, "Error " + e, Toast.LENGTH_SHORT).show();
                    });

        } else {
            pKey = reference.push().getKey();
            product = new Product(pKey, name, productDetailsList);
            reference.child(pKey).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        pDialog.dismiss();
                        startActivity(new Intent(ProductsActivity.this, DistributorMapsActivity.class));
                        Toast.makeText(ProductsActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        pDialog.dismiss();
                        Toast.makeText(ProductsActivity.this, "Error " + e, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
