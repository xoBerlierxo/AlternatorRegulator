package com.example.alternatorregulator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioGroup; // New import
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class RatedDataActivity extends AppCompatActivity {

    TextInputEditText etRatedVoltage, etKVA; // Changed from old inputs
    Button btnNext1;
    RadioGroup rgConnection; // New RadioGroup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rated_data);

        // Link Java variables to XML components
        etRatedVoltage = findViewById(R.id.etRatedVoltage);
        etKVA = findViewById(R.id.etKVA); // New
        btnNext1 = findViewById(R.id.btnNext1);
        rgConnection = findViewById(R.id.rgConnection); // New

        // Set the button's click listener
        btnNext1.setOnClickListener(v -> {
            // Get text from boxes
            String ratedV_str = etRatedVoltage.getText().toString();
            String kva_str = etKVA.getText().toString();

            // Check if any string is empty before parsing
            if (TextUtils.isEmpty(ratedV_str) || TextUtils.isEmpty(kva_str)) {
                Toast.makeText(RatedDataActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return; // Stop the function here
            }

            double ratedLineVoltage = Double.parseDouble(ratedV_str);
            double ratedKVA = Double.parseDouble(kva_str);
            double ratedVA = ratedKVA * 1000; // Convert kVA to VA

            // --- NEW CALCULATIONS ---
            double Vph; // Rated Phase Voltage
            double Ia_rated; // Rated Armature Current

            int selectedId = rgConnection.getCheckedRadioButtonId();
            if (selectedId == R.id.rbStar) {
                // Star Connection
                Vph = ratedLineVoltage / Math.sqrt(3.0);
            } else {
                // Delta Connection
                Vph = ratedLineVoltage;
            }

            // Calculate Rated Armature Current (Ia)
            // S(3-phase) = 3 * V_phase * I_armature
            Ia_rated = ratedVA / (3.0 * Vph);
            // --- END OF NEW CALCULATIONS ---

            // Create an "Intent" to open the next screen
            Intent intent = new Intent(RatedDataActivity.this, OCTDataActivity.class);

            // Pass the *calculated* base values to the next screen
            intent.putExtra("V_PHASE_RATED", Vph);
            intent.putExtra("I_ARMATURE_RATED", Ia_rated);

            // Start the next screen
            startActivity(intent);
        });
    }
}