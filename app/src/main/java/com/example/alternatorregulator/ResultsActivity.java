package com.example.alternatorregulator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    double Vph, ratedI;
    ArrayList<TestDataPoint> octList;
    ArrayList<TestDataPoint> sctList;

    final double Ra = 0.0;
    double Xs;

    TextView tvRaResult, tvXsResult, tvRegResult;
    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        tvRaResult = findViewById(R.id.tvRaResult);
        tvXsResult = findViewById(R.id.tvXsResult);
        tvRegResult = findViewById(R.id.tvRegResult);
        lineChart = findViewById(R.id.lineChart);

        Intent intent = getIntent();
        Vph = intent.getDoubleExtra("V_PHASE_RATED", 0.0);
        ratedI = intent.getDoubleExtra("I_ARMATURE_RATED", 0.0);
        octList = (ArrayList<TestDataPoint>) intent.getSerializableExtra("OCT_LIST");
        sctList = (ArrayList<TestDataPoint>) intent.getSerializableExtra("SCT_LIST");

        if (octList == null || sctList == null || octList.isEmpty() || sctList.isEmpty()) {
            tvRegResult.setText("Error: Insufficient data provided.");
            return;
        }

        calculateParameters();
        double fullLoadReg = getRegulation(100.0, 0.8, true);
        tvRegResult.setText(String.format("Regulation (100%% load, 0.8pf lag): %.2f %%", fullLoadReg));
        setupOccSccGraph();
    }

    private void calculateParameters() {
        tvRaResult.setText(String.format("Armature Resistance (Ra): %.4f Ω (Assumed 0)", Ra));
        double If_for_rated_V = interpolate(octList, Vph, true);
        double Isc_ph = interpolate(sctList, If_for_rated_V, false);

        if (Isc_ph == 0) {
            tvXsResult.setText("Synchronous Reactance (Xs): Error - Division by zero");
            return;
        }

        double Zs_ph = Vph / Isc_ph;
        Xs = Zs_ph;
        tvXsResult.setText(String.format("Synchronous Reactance (Xs): %.4f Ω", Xs));
    }

    private double interpolate(ArrayList<TestDataPoint> table, double xValue, boolean invert) {
        ArrayList<TestDataPoint> sortedTable = new ArrayList<>(table);
        if (invert) {
            sortedTable.sort((p1, p2) -> Double.compare(p1.value, p2.value));
        } else {
            sortedTable.sort((p1, p2) -> Double.compare(p1.fieldCurrent, p2.fieldCurrent));
        }

        if (sortedTable.isEmpty()) return 0.0;

        TestDataPoint firstPoint = sortedTable.get(0);
        double firstX = invert ? firstPoint.value : firstPoint.fieldCurrent;
        if (xValue <= firstX) return invert ? firstPoint.fieldCurrent : firstPoint.value;

        TestDataPoint lastPoint = sortedTable.get(sortedTable.size() - 1);
        double lastX = invert ? lastPoint.value : lastPoint.fieldCurrent;
        if (xValue >= lastX) return invert ? lastPoint.fieldCurrent : lastPoint.value;

        for (int i = 0; i < sortedTable.size() - 1; i++) {
            TestDataPoint p1 = sortedTable.get(i);
            TestDataPoint p2 = sortedTable.get(i + 1);

            double x1, y1, x2, y2;
            if (invert) {
                x1 = p1.value; y1 = p1.fieldCurrent;
                x2 = p2.value; y2 = p2.fieldCurrent;
            } else {
                x1 = p1.fieldCurrent; y1 = p1.value;
                x2 = p2.fieldCurrent; y2 = p2.value;
            }

            if (xValue >= x1 && xValue <= x2) {
                if (x2 - x1 == 0) return y1;
                return y1 + (xValue - x1) * (y2 - y1) / (x2 - x1);
            }
        }
        return invert ? lastPoint.fieldCurrent : lastPoint.value;
    }

    private double getRegulation(double loadPercent, double cosPhi, boolean isLagging) {
        double Ia = ratedI * (loadPercent / 100.0);
        double sinPhi = Math.sqrt(1 - Math.pow(cosPhi, 2));
        double E0;

        if (cosPhi == 1.0) {
            E0 = Math.sqrt(Math.pow(Vph, 2) + Math.pow(Ia * Xs, 2));
        } else if (isLagging) {
            E0 = Math.sqrt(Math.pow(Vph * cosPhi, 2) + Math.pow(Vph * sinPhi + Ia * Xs, 2));
        } else {
            E0 = Math.sqrt(Math.pow(Vph * cosPhi, 2) + Math.pow(Vph * sinPhi - Ia * Xs, 2));
        }

        return ((E0 - Vph) / Vph) * 100.0;
    }

    private void setupOccSccGraph() {
        ArrayList<Entry> occEntries = new ArrayList<>();
        for (TestDataPoint p : octList) occEntries.add(new Entry((float) p.fieldCurrent, (float) p.value));

        ArrayList<Entry> sccEntries = new ArrayList<>();
        for (TestDataPoint p : sctList) sccEntries.add(new Entry((float) p.fieldCurrent, (float) p.value));

        LineDataSet occSet = new LineDataSet(occEntries, "O.C.C. (Voltage)");
        occSet.setColor(Color.RED);
        occSet.setDrawCircles(false);
        occSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet sccSet = new LineDataSet(sccEntries, "S.C.C. (Current)");
        sccSet.setColor(Color.BLUE);
        sccSet.setDrawCircles(false);
        sccSet.setAxisDependency(YAxis.AxisDependency.RIGHT);

        LineData lineData = new LineData(occSet, sccSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setText("O.C.C. & S.C.C. Curves");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new AxisUnitFormatter("A"));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.RED);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new AxisUnitFormatter("V"));

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(Color.BLUE);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setValueFormatter(new AxisUnitFormatter("A"));

        lineChart.getLegend().setWordWrapEnabled(true);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    public class AxisUnitFormatter extends ValueFormatter {
        private final String unit;
        public AxisUnitFormatter(String unit) { this.unit = unit; }
        @Override
        public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
            return String.format("%.1f %s", value, unit);
        }
    }
}
