package com.extramoney;

import java.util.Locale;

public class Salesman {
    int no;
    String sNo, barcode, millRate, billNo, date;
    double jappa;

    public Salesman(int no, String sNo, String barcode, String millRate, String billNo, String date, double jappa) {
        this.no = no;
        this.sNo = sNo;
        this.barcode = barcode;
        this.millRate = millRate;
        this.billNo = billNo;
        this.date = date;
        this.jappa = jappa;
    }

    public String[] toArray() {
        return new String[]{
                String.valueOf(no), sNo, barcode, millRate, billNo, date,
                String.format(Locale.getDefault(), "%.2f", jappa)
        };
    }

    public String toDelimitedString() {
        return no + ", " + sNo + ", " + barcode + ", " + millRate + ", " + billNo + ", " + date + ", " +
                String.format(Locale.getDefault(), "%.2f", jappa);
    }
}
