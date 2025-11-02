package com.example.alternatorregulator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import for validation
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; // Import for validation

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class SCTDataActivity extends AppCompatActivity {

    TextInputEditText etSctIf, etSctValue;
    Button btnAddSct, btnCalculate;
    ListView lvSctData;

    ArrayList<TestDataPoint> sctList = new ArrayList<>();
    SctAdapter sctAdapter; // Use our new custom adapter

    ArrayList<TestDataPoint> octList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sctdata); // Use the correct layout name

        // Get all data passed from OCTDataActivity
        Intent intent = getIntent();
        double Vph_rated = intent.getDoubleExtra("V_PHASE_RATED", 0.0);
        double Ia_rated = intent.getDoubleExtra("I_ARMATURE_RATED", 0.0);
        octList = (ArrayList<TestDataPoint>) intent.getSerializableExtra("OCT_LIST");

        // Link Java variables to XML
        etSctIf = findViewById(R.id.etSctIf);
        etSctValue = findViewById(R.id.etSctValue);
        btnAddSct = findViewById(R.id.btnAddSct);
        btnCalculate = findViewById(R.id.btnCalculate);
        lvSctData = findViewById(R.id.lvSctData);

        // Set up the custom adapter
        sctAdapter = new SctAdapter(this, sctList);
        lvSctData.setAdapter(sctAdapter);

        // "Add Data" button logic
        btnAddSct.setOnClickListener(v -> {
            // --- Validation logic ---
            String ifValStr = etSctIf.getText().toString();
            String valStr = etSctValue.getText().toString();

            if (TextUtils.isEmpty(ifValStr) || TextUtils.isEmpty(valStr)) {
                Toast.makeText(SCTDataActivity.this, "Please fill all fields to add data", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- End validation ---

            double ifVal = Double.parseDouble(ifValStr);
            double val = Double.parseDouble(valStr);

            // Create and add the new point
            TestDataPoint newPoint = new TestDataPoint(ifVal, val);
            sctAdapter.add(newPoint); // This automatically notifies the adapter

            etSctIf.setText("");
            etSctValue.setText("");
        });

        // "Calculate" button logic
        btnCalculate.setOnClickListener(v -> {
            // --- Validation logic ---
            if (sctList.isEmpty()) {
                Toast.makeText(SCTDataActivity.this, "Please add at least one data point", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- End validation ---

            Intent resultsIntent = new Intent(SCTDataActivity.this, ResultsActivity.class);

            // Pass ALL data (the full dataset) to the final screen
            resultsIntent.putExtra("V_PHASE_RATED", Vph_rated);
            resultsIntent.putExtra("I_ARMATURE_RATED", Ia_rated);
            resultsIntent.putExtra("OCT_LIST", octList);
            resultsIntent.putExtra("SCT_LIST", sctList);

            startActivity(resultsIntent);
        });
    }

    // --- Our Custom Adapter Class ---
    private class SctAdapter extends ArrayAdapter<TestDataPoint> {
        public SctAdapter(Context context, ArrayList<TestDataPoint> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            TestDataPoint point = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_card, parent, false);
            }

            // Lookup view for data population
            TextView tvIf = convertView.findViewById(R.id.tvFieldCurrent);
            TextView tvVal = convertView.findViewById(R.id.tvValue);

            // Populate the data into the template view
            tvIf.setText(String.format("If = %.2f A", point.fieldCurrent));
            // --- IMPORTANT CHANGE ---
            tvVal.setText(String.format("Isc = %.2f A", point.value)); // Use "Isc" for this screen

            // Return the completed view to render on screen
            return convertView;
        }
    }
}
