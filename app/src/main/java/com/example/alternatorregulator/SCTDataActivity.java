package com.example.alternatorregulator;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class SCTDataActivity extends AppCompatActivity {

    TextInputEditText etSctIf, etSctValue;
    Button btnAddSct, btnCalculate;
    ListView lvSctData;

    ArrayList<TestDataPoint> sctList = new ArrayList<>();
    ArrayAdapter<TestDataPoint> sctAdapter;

    double ratedV, ratedI, dcV, dcI;
    ArrayList<TestDataPoint> octList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sctdata);

        // Get all data passed from OCTDataActivity
        Intent intent = getIntent();
        ratedV = intent.getDoubleExtra("RATED_V", 0.0);
        ratedI = intent.getDoubleExtra("RATED_I", 0.0);
        dcV = intent.getDoubleExtra("DC_V", 0.0);
        dcI = intent.getDoubleExtra("DC_I", 0.0);
        octList = (ArrayList<TestDataPoint>) intent.getSerializableExtra("OCT_LIST");

        // Link Java variables to XML
        etSctIf = findViewById(R.id.etSctIf);
        etSctValue = findViewById(R.id.etSctValue);
        btnAddSct = findViewById(R.id.btnAddSct);
        btnCalculate = findViewById(R.id.btnCalculate);
        lvSctData = findViewById(R.id.lvSctData);

        // Set up the ListView adapter
        sctAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sctList);
        lvSctData.setAdapter(sctAdapter);

        // "Add Data" button logic
        btnAddSct.setOnClickListener(v -> {
            double ifVal = Double.parseDouble(etSctIf.getText().toString());
            double val = Double.parseDouble(etSctValue.getText().toString());

            sctList.add(new TestDataPoint(ifVal, val));
            sctAdapter.notifyDataSetChanged();

            etSctIf.setText("");
            etSctValue.setText("");
        });

        // "Calculate" button logic
        btnCalculate.setOnClickListener(v -> {
            Intent resultsIntent = new Intent(SCTDataActivity.this, ResultsActivity.class);

            // Pass ALL data (the full dataset) to the final screen
            resultsIntent.putExtra("RATED_V", ratedV);
            resultsIntent.putExtra("RATED_I", ratedI);
            resultsIntent.putExtra("DC_V", dcV);
            resultsIntent.putExtra("DC_I", dcI);
            resultsIntent.putExtra("OCT_LIST", octList);
            resultsIntent.putExtra("SCT_LIST", sctList);

            startActivity(resultsIntent);
        });
    }
}