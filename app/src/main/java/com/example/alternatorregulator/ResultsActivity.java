package com.example.alternatorregulator;

import android.os.Bundle;
// In ResultsActivity.java
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis; // <-- New Import
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    // Data
    double ratedV, ratedI, dcV, dcI;
    ArrayList<TestDataPoint> octList;
    ArrayList<TestDataPoint> sctList;

    // Calculated Parameters
    double Ra, Xs, Vph;

    // UI
    TextView tvRaResult, tvXsResult, tvRegResult; // <-- Added tvRegResult
    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Link UI
        tvRaResult = findViewById(R.id.tvRaResult);
        tvXsResult = findViewById(R.id.tvXsResult);
        tvRegResult = findViewById(R.id.tvRegResult); // <-- Link new TextView
        lineChart = findViewById(R.id.lineChart);

        // Get all data from the intent
        Intent intent = getIntent();
        ratedV = intent.getDoubleExtra("RATED_V", 0.0);
        ratedI = intent.getDoubleExtra("RATED_I", 0.0);
        dcV = intent.getDoubleExtra("DC_V", 0.0);
        dcI = intent.getDoubleExtra("DC_I", 0.0);
        octList = (ArrayList<TestDataPoint>) intent.getSerializableExtra("OCT_LIST");
        sctList = (ArrayList<TestDataPoint>) intent.getSerializableExtra("SCT_LIST");

        // --- Run Calculations ---
        calculateParameters();

        // --- Calculate and Display Full Load Regulation ---
        // We'll calculate for the standard case: 100% load, 0.8 PF Lag
        double fullLoadReg = getRegulation(100.0, 0.8, true); // 100% load, 0.8 PF, isLagging=true
        tvRegResult.setText(String.format("Regulation (100%% load, 0.8pf lag): %.2f %%", fullLoadReg));

        // --- Setup the new graph ---
        setupOccSccGraph();
    }

    private void calculateParameters() {
        // 1. Calculate Ra (Armature Resistance)
        double R_dc = dcV / dcI;
        double R_dc_ph = R_dc / 2.0; // Assuming DC test is Line-to-Line
        Ra = R_dc_ph * 1.5; // Apply skin effect factor (1.5 is a common assumption)

        // 2. Calculate Xs (Synchronous Reactance)
        // V_ph = V_L / sqrt(3)
        Vph = ratedV / Math.sqrt(3.0);

        // Find If for rated V_L from OCT data
        double If_for_rated_V = interpolate(octList, ratedV, true);

        // Find Isc for that *same* If from SCT data
        double Isc_L = interpolate(sctList, If_for_rated_V, false);
        double Isc_ph = Isc_L; // For Star connection

        // Finally, find Zs and Xs
        double Zs_ph = Vph / Isc_ph;
        Xs = Math.sqrt(Math.pow(Zs_ph, 2) - Math.pow(Ra, 2));

        // Update the UI
        tvRaResult.setText(String.format("Armature Resistance (Ra): %.4f Ω", Ra));
        tvXsResult.setText(String.format("Synchronous Reactance (Xs): %.4f Ω", Xs));
    }

    // Helper function for Linear Interpolation
    private double interpolate(ArrayList<TestDataPoint> table, double xValue, boolean invert) {
        // Sort the table by Field Current first
        table.sort((p1, p2) -> Double.compare(p1.fieldCurrent, p2.fieldCurrent));

        ArrayList<TestDataPoint> sortedTable = new ArrayList<>(table);

        // If inverting, we are finding If (y) for a given Voc (x)
        // We must sort by value (Voc)
        if (invert) {
            sortedTable.sort((p1, p2) -> Double.compare(p1.value, p2.value));
        }

        for (int i = 0; i < sortedTable.size() - 1; i++) {
            TestDataPoint p1 = sortedTable.get(i);
            TestDataPoint p2 = sortedTable.get(i + 1);

            double x1, y1, x2, y2;
            if (invert) {
                // Find X (If) for a given Y (Voc)
                x1 = p1.value; y1 = p1.fieldCurrent;
                x2 = p2.value; y2 = p2.fieldCurrent;
            } else {
                // Find Y (Isc) for a given X (If)
                x1 = p1.fieldCurrent; y1 = p1.value;
                x2 = p2.fieldCurrent; y2 = p2.value;
            }

            // Check if xValue is within the range [x1, x2]
            if (xValue >= x1 && xValue <= x2) {
                // Linear interpolation formula: y = y1 + (x - x1) * (y2 - y1) / (x2 - x1)
                return y1 + (xValue - x1) * (y2 - y1) / (x2 - x1);
            }
        }

        // If out of range, just return the last value (this is a simple fallback)
        if (invert) {
            return sortedTable.get(sortedTable.size() - 1).fieldCurrent;
        } else {
            return sortedTable.get(sortedTable.size() - 1).value;
        }
    }

    // Main function to calculate a single point of regulation
    private double getRegulation(double loadPercent, double cosPhi, boolean isLagging) {
        double Ia = ratedI * (loadPercent / 100.0);
        double sinPhi = Math.sqrt(1 - Math.pow(cosPhi, 2));

        double E0; // No-Load EMF

        // Vectorial calculation
        if (cosPhi == 1.0) {
            E0 = Math.sqrt(Math.pow(Vph + Ia * Ra, 2) + Math.pow(Ia * Xs, 2));
        } else if (isLagging) {
            // E0 = sqrt( (Vph*cos(phi) + Ia*Ra)^2 + (Vph*sin(phi) + Ia*Xs)^2 )
            E0 = Math.sqrt(Math.pow(Vph * cosPhi + Ia * Ra, 2) + Math.pow(Vph * sinPhi + Ia * Xs, 2));
        } else {
            // Leading PF: (Vph*sin(phi) - Ia*Xs)
            // E0 = sqrt( (Vph*cos(phi) + Ia*Ra)^2 + (Vph*sin(phi) - Ia*Xs)^2 )
            E0 = Math.sqrt(Math.pow(Vph * cosPhi + Ia * Ra, 2) + Math.pow(Vph * sinPhi - Ia * Xs, 2));
        }

        // % Regulation = (E0 - Vph) / Vph * 100
        return ((E0 - Vph) / Vph) * 100.0;
    }

    // --- THIS IS THE NEW GRAPH METHOD ---
    private void setupOccSccGraph() {

        // 1. Create data entries from your OCT and SCT lists
        ArrayList<Entry> occEntries = new ArrayList<>();
        ArrayList<Entry> sccEntries = new ArrayList<>();

        for (TestDataPoint p : octList) {
            occEntries.add(new Entry((float) p.fieldCurrent, (float) p.value));
        }

        for (TestDataPoint p : sctList) {
            sccEntries.add(new Entry((float) p.fieldCurrent, (float) p.value));
        }

        // 2. Create "Data Sets" (style the lines)
        LineDataSet occSet = new LineDataSet(occEntries, "O.C.C. (Voltage)");
        occSet.setColor(Color.RED);
        occSet.setDrawCircles(false);
        occSet.setAxisDependency(YAxis.AxisDependency.LEFT); // Plot on LEFT Y-axis

        LineDataSet sccSet = new LineDataSet(sccEntries, "S.C.C. (Current)");
        sccSet.setColor(Color.BLUE);
        sccSet.setDrawCircles(false);
        sccSet.setAxisDependency(YAxis.AxisDependency.RIGHT); // Plot on RIGHT Y-axis

        // 3. Combine Data Sets into LineData
        LineData lineData = new LineData(occSet, sccSet);

        // 4. Configure the Chart
        lineChart.setData(lineData);
        lineChart.getDescription().setText("Field Current (If)");

        // Configure X-Axis
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);

        // Configure LEFT Y-Axis (for O.C.C.)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.RED);
        leftAxis.setAxisMinimum(0f); // Start at 0

        // Configure RIGHT Y-Axis (for S.C.C.)
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(Color.BLUE);
        rightAxis.setAxisMinimum(0f); // Start at 0

        // Configure Legend
        lineChart.getLegend().setWordWrapEnabled(true);
        lineChart.animateX(1000);
        lineChart.invalidate(); // Refresh the chart
    }
}