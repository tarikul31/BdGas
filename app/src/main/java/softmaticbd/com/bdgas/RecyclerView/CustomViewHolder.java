package softmaticbd.com.bdgas.RecyclerView;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import softmaticbd.com.bdgas.R;

public class CustomViewHolder extends RecyclerView.ViewHolder {

    public TextView pName,pQuantity;
    RelativeLayout pLayout;

    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);
        pLayout = itemView.findViewById(R.id.productLayoutID);
        pName = itemView.findViewById(R.id.pNameID);
        pQuantity = itemView.findViewById(R.id.pQuantity);
    }
}
