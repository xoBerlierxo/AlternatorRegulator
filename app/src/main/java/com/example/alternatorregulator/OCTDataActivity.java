package com.example.alternatorregulator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Added from your code
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; // Added from your code

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class OCTDataActivity extends AppCompatActivity {

    TextInputEditText etOctIf, etOctValue;
    Button btnAddOct, btnNext2;
    ListView lvOctData;

    ArrayList<TestDataPoint> octList = new ArrayList<>();
    OctSctAdapter octAdapter; // Use our new custom adapter

    double ratedV, ratedI, dcV, dcI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Corrected layout name (was activity_octdata in your code)
        setContentView(R.layout.activity_octdata);

        Intent intent = getIntent();
        ratedV = intent.getDoubleExtra("RATED_V", 0.0);
        ratedI = intent.getDoubleExtra("RATED_I", 0.0);
        dcV = intent.getDoubleExtra("DC_V", 0.0);
        dcI = intent.getDoubleExtra("DC_I", 0.0);

        etOctIf = findViewById(R.id.etOctIf);
        etOctValue = findViewById(R.id.etOctValue);
        btnAddOct = findViewById(R.id.btnAddOct);
        btnNext2 = findViewById(R.id.btnNext2);
        lvOctData = findViewById(R.id.lvOctData);

        // Set up the custom adapter
        octAdapter = new OctSctAdapter(this, octList);
        lvOctData.setAdapter(octAdapter);

        // "Add Data" button logic
        btnAddOct.setOnClickListener(v -> {
            // --- Validation logic from your code ---
            String ifValStr = etOctIf.getText().toString();
            String valStr = etOctValue.getText().toString();

            if (TextUtils.isEmpty(ifValStr) || TextUtils.isEmpty(valStr)) {
                Toast.makeText(OCTDataActivity.this, "Please fill all fields to add data", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- End validation ---

            double ifVal = Double.parseDouble(ifValStr);
            double val = Double.parseDouble(valStr);

            // Create and add the new point
            TestDataPoint newPoint = new TestDataPoint(ifVal, val);
            octAdapter.add(newPoint); // This automatically notifies the adapter

            etOctIf.setText("");
            etOctValue.setText("");
        });

        // "Next" button logic
        btnNext2.setOnClickListener(v -> {
            // --- Validation logic from your code ---
            if (octList.isEmpty()) {
                Toast.makeText(OCTDataActivity.this, "Please add at least one data point", Toast.LENGTH_SHORT).show();
                return;
            }
            // --- End validation ---

            Intent nextIntent = new Intent(OCTDataActivity.this, SCTDataActivity.class);
            nextIntent.putExtra("RATED_V", ratedV);
            nextIntent.putExtra("RATED_I", ratedI);
            nextIntent.putExtra("DC_V", dcV);
            nextIntent.putExtra("DC_I", dcI);
            nextIntent.putExtra("OCT_LIST", octList);
            startActivity(nextIntent);
        });
    }

    // --- Our Custom Adapter Class ---
    private class OctSctAdapter extends ArrayAdapter<TestDataPoint> {
        public OctSctAdapter(Context context, ArrayList<TestDataPoint> list) {
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
            tvVal.setText(String.format("Voc = %.2f V", point.value)); // Use "Voc" for this screen

            // Return the completed view to render on screen
            return convertView;
        }
    }
}