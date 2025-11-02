package com.example.alternatorregulator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import this
import android.widget.Button;
import android.widget.Toast; // Import this
import com.google.android.material.textfield.TextInputEditText;

public class RatedDataActivity extends AppCompatActivity {

    TextInputEditText etRatedVoltage, etRatedCurrent, etDcVoltage, etDcCurrent;
    Button btnNext1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated_data);

        // Link Java variables to XML components
        etRatedVoltage = findViewById(R.id.etRatedVoltage);
        etRatedCurrent = findViewById(R.id.etRatedCurrent);
        etDcVoltage = findViewById(R.id.etDcVoltage);
        etDcCurrent = findViewById(R.id.etDcCurrent);
        btnNext1 = findViewById(R.id.btnNext1);

        // Set the button's click listener
        btnNext1.setOnClickListener(v -> {
            // Get text from boxes
            String ratedV = etRatedVoltage.getText().toString();
            String ratedI = etRatedCurrent.getText().toString();
            String dcV = etDcVoltage.getText().toString();
            String dcI = etDcCurrent.getText().toString();

            // --- START OF FIX ---
            // Check if any string is empty before parsing
            if (TextUtils.isEmpty(ratedV) || TextUtils.isEmpty(ratedI) || TextUtils.isEmpty(dcV) || TextUtils.isEmpty(dcI)) {
                // Show an error message to the user
                Toast.makeText(RatedDataActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return; // Stop the function here
            }
            // --- END OF FIX ---

            // Create an "Intent" to open the next screen
            Intent intent = new Intent(RatedDataActivity.this, OCTDataActivity.class);

            // Pass the data to the next screen
            // This is now safe because we checked for empty strings
            intent.putExtra("RATED_V", Double.parseDouble(ratedV));
            intent.putExtra("RATED_I", Double.parseDouble(ratedI));
            intent.putExtra("DC_V", Double.parseDouble(dcV));
            intent.putExtra("DC_I", Double.parseDouble(dcI));

            // Start the next screen
            startActivity(intent);
        });
    }
}