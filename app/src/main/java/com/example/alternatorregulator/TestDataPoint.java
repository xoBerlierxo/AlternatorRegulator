package com.example.alternatorregulator;

import java.io.Serializable;
import java.io.Serializable;

public class TestDataPoint implements Serializable {
    public double fieldCurrent;
    public double value; // This will hold either Voc or Isc

    // Constructor to create a new data point
    public TestDataPoint(double fieldCurrent, double value) {
        this.fieldCurrent = fieldCurrent;
        this.value = value;
    }

    // This method is important!
    // It's what the ListView will use to display the data
    @Override
    public String toString() {
        return "If = " + fieldCurrent + " A,  Value = " + value;
    }
}
