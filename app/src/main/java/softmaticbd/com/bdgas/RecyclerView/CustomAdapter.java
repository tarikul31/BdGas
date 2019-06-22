package softmaticbd.com.bdgas.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import softmaticbd.com.bdgas.Model.Product;
import softmaticbd.com.bdgas.Model.ProductDetails;
import softmaticbd.com.bdgas.Operation.ProductsActivity;
import softmaticbd.com.bdgas.R;

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private Context context;
    private List<Product> productList;
    private int total = 0;

    public CustomAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.sample_view_request, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder rvHolder, int i) {
        final Product product = productList.get(i);
        rvHolder.pName.setText(product.getProductName());
        for (ProductDetails details : product.getProductDetails()) {
            total += Integer.parseInt(details.getQuantity());
            rvHolder.pQuantity.setText(String.valueOf(total));
        }

        rvHolder.pLayout.setOnClickListener(v -> {
            context.startActivity(new Intent(context, ProductsActivity.class).putExtra("pObj", product));
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
