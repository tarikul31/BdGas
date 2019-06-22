package softmaticbd.com.bdgas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import softmaticbd.com.bdgas.MapActivity.CustomerMapsActivity;
import softmaticbd.com.bdgas.MapActivity.DistributorMapsActivity;
import softmaticbd.com.bdgas.Operation.FeedbackActivity;
import softmaticbd.com.bdgas.SignActivity.SignInActivity;
import softmaticbd.com.bdgas.SignActivity.SignUpActivity;

public class MainActivity extends AppCompatActivity {
    private ImageView ivLogo;
    private FirebaseUser user;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivLogo = findViewById(R.id.ivLogoId);
        ivLogo.setVisibility(View.INVISIBLE);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        onCheckUser();
    }

    private void onCheckUser() {
        if (user != null) {
            if (user.isEmailVerified()) {
                startUserWiseActivity();
            } else {
                checkEmailVerify();
            }
        } else {
            new Handler().postDelayed(() -> {
                ivLogo.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                }, 500);
            }, 2000);
        }
    }

    private void checkEmailVerify() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Alert");
        alert.setMessage("Please Check Your Email To Verify You Account");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes", (dialog, which) -> user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                onCheckUser();
            } else {
                checkEmailVerify();
            }
        }));
        alert.setNegativeButton("Cancel", (dialog, which) -> {
            auth.signOut();
        });
        alert.show();
    }

    private void startUserWiseActivity() {
        String id = auth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User");

        Query mQuery = reference.orderByChild(id);
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String key = data.getKey();
                        if (key.equals("Customer")) {
                            new Handler().postDelayed(() -> {
                                ivLogo.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, CustomerMapsActivity.class)), 500);
                            }, 2000);
                        } else {
                            new Handler().postDelayed(() -> {
                                ivLogo.setVisibility(View.VISIBLE);
                                new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, DistributorMapsActivity.class)), 500);
                            }, 2000);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
