package softmaticbd.com.bdgas.Operation;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import softmaticbd.com.bdgas.Model.Product;
import softmaticbd.com.bdgas.R;
import softmaticbd.com.bdgas.RecyclerView.CustomAdapter;

public class ProductList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Product> products = new ArrayList<>();
    private CustomAdapter adapter;

    //todo for database ref
    private DatabaseReference productRef;
    private FirebaseAuth auth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        getProductDetails();
        recyclerView = findViewById(R.id.recyclerID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = (new CustomAdapter(this, products));
        recyclerView.setAdapter(adapter);

    }

    private void getProductDetails() {
        productRef = FirebaseDatabase.getInstance().getReference()
                .child("Product")
                .child(userId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        products.add(snapshot.getValue(Product.class));
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
