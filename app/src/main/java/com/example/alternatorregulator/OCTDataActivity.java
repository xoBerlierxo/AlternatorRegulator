package com.example.alternatorregulator;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class OCTDataActivity extends AppCompatActivity {

    TextInputEditText etOctIf, etOctValue;
    Button btnAddOct, btnNext2;
    ListView lvOctData;

    ArrayList<TestDataPoint> octList = new ArrayList<>();
    ArrayAdapter<TestDataPoint> octAdapter;

    // Variables to hold data from the previous screen
    double ratedV, ratedI, dcV, dcI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_octdata);

        // Get the data passed from RatedDataActivity
        Intent intent = getIntent();
        ratedV = intent.getDoubleExtra("RATED_V", 0.0);
        ratedI = intent.getDoubleExtra("RATED_I", 0.0);
        dcV = intent.getDoubleExtra("DC_V", 0.0);
        dcI = intent.getDoubleExtra("DC_I", 0.0);

        // Link Java variables to XML
        etOctIf = findViewById(R.id.etOctIf);
        etOctValue = findViewById(R.id.etOctValue);
        btnAddOct = findViewById(R.id.btnAddOct);
        btnNext2 = findViewById(R.id.btnNext2);
        lvOctData = findViewById(R.id.lvOctData);

        // Set up the ListView adapter
        octAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, octList);
        lvOctData.setAdapter(octAdapter);

        // "Add Data" button logic
        btnAddOct.setOnClickListener(v -> {
            String ifValStr = etOctIf.getText().toString();
            String valStr = etOctValue.getText().toString();

            if (TextUtils.isEmpty(ifValStr) || TextUtils.isEmpty(valStr)) {
                Toast.makeText(OCTDataActivity.this, "Please fill all fields to add data", Toast.LENGTH_SHORT).show();
                return;
            }

            double ifVal = Double.parseDouble(ifValStr);
            double val = Double.parseDouble(valStr);

            octList.add(new TestDataPoint(ifVal, val));
            octAdapter.notifyDataSetChanged(); // Refresh the list

            // Clear the input boxes
            etOctIf.setText("");
            etOctValue.setText("");
        });

        // "Next" button logic
        btnNext2.setOnClickListener(v -> {
            if (octList.isEmpty()) {
                Toast.makeText(OCTDataActivity.this, "Please add at least one data point", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create intent for the next screen
            Intent nextIntent = new Intent(OCTDataActivity.this, SCTDataActivity.class);

            // Pass ALL data so far
            nextIntent.putExtra("RATED_V", ratedV);
            nextIntent.putExtra("RATED_I", ratedI);
            nextIntent.putExtra("DC_V", dcV);
            nextIntent.putExtra("DC_I", dcI);
            nextIntent.putExtra("OCT_LIST", octList);

            startActivity(nextIntent);
        });
    }
}
