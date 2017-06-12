package gs.momokun.homeautomationx;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import gs.momokun.homeautomationx.tools.DataLogging;
import gs.momokun.homeautomationx.tools.DatabaseHandler;



class GraphAdapter {

    private DatabaseHandler db;
    private Context context;
    private View view;
    // private LineGraphSeries<DataPoint> serieswatt,seriesTemperature,seriesAmps,seriesEnergy;
    private LineChart mChart;
    private Spinner spinner_data_type,spinner_time_range;
    private List<DataLogging> dataLogging;

    private int verticalTypeData = 0;
    private int verticalTimeData = 0;
    private TextView calculatedEnergy;
    private TextView graphTitle;

    public GraphAdapter(){

    }

    GraphAdapter( Context context, View view, DatabaseHandler db) {
        this.context=context;
        this.view=view;
        this.db = db;
    }

    private void declaration(){
        mChart = (LineChart) view.findViewById(R.id.chart);
        spinner_data_type = (Spinner) view.findViewById(R.id.spinner_date_array);
        spinner_time_range = (Spinner) view.findViewById(R.id.spinner_date_range);
        calculatedEnergy = (TextView) view.findViewById(R.id.calcPrice);
        graphTitle = (TextView) view.findViewById(R.id.graph_name);
        calculatedEnergy = (TextView) view.findViewById(R.id.calcPrice);
        TextView viewedBy = (TextView) view.findViewById(R.id.viewedBy);
        viewedBy.setText("This month :");
    }

    private void graphSetup(){
        leftAxis = mChart.getAxisLeft();
        mChart.getDescription().setEnabled(false); //bottom left text
        mChart.setTouchEnabled(true);
        mChart.setDragDecelerationFrictionCoef(0.9f);
        mChart.setDragEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(true);
        IMarker marker = new CustomMarkerGraph(context,R.layout.marker_graph_custom);
        mChart.setMarker(marker);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);
        calculatedEnergy.setText("Rp." + 0 + ",-");
    }


    private YAxis leftAxis;

    private void checkMonthlyPrice(){
        int ctx=0;
        int ctz=0;
        double calcPrice = 0;
        for (DataLogging ct : db.getByMonthly()) {
            ctx++;
        }
        for (DataLogging getPrice : db.getByMonthly()) {
            ctz++;
            if (ctx == ctz) {
                float z = Float.parseFloat(getPrice.get_energy())*24/1000;
                calcPrice = calcPrice + z * 1467;
                DecimalFormat df = new DecimalFormat("#.##");
                calculatedEnergy.setText("Rp." + df.format(calcPrice) + ",-");
            }
        }
    }

    void viewGraph(){
        declaration();
        graphSetup();
        //setupGraphData(0,0);





        if(db.checkData()) {
            checkMonthlyPrice();


            mChart.animateX(1000);

            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();
            l.setEnabled(false);

            final XAxis xAxis = mChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(Color.BLACK);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(Color.rgb(0, 0, 0));
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f); // one hour step
            xAxis.setGridColor(Color.argb(25, 0, 0, 0));


            leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

            leftAxis.setTextColor(ColorTemplate.getHoloBlue());
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setAxisMinimum(0f);

            leftAxis.setYOffset(-9f);
            leftAxis.setTextColor(Color.rgb(0, 0, 0));
            leftAxis.setGridColor(Color.argb(25, 0, 0, 0));

            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setEnabled(false);

            spinner_data_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    verticalTypeData = i;
                    setupGraphData(verticalTimeData, i);
                    mChart.animateX(1000);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spinner_time_range.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    verticalTimeData = i;
                    if (i == 0) {
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {

                                //long millis = TimeUnit.HOURS.toMillis((long) value);
                                return mFormat.format(new Date((long) value));
                            }
                        });

                    }
                    if (i == 1) {
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat mFormat = new SimpleDateFormat("dd-MMM");

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {

                                //long millis = TimeUnit.HOURS.toMillis((long) value);
                                return mFormat.format(new Date((long) value));
                            }
                        });
                    }
                    if (i == 2) {
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat mFormat = new SimpleDateFormat("'Week'-W");

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {

                                //long millis = TimeUnit.HOURS.toMillis((long) value);
                                return mFormat.format(new Date((long) value));
                            }
                        });
                    }
                    if (i == 3) {
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            private SimpleDateFormat mFormat = new SimpleDateFormat("MMM");

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {

                                //long millis = TimeUnit.HOURS.toMillis((long) value);
                                return mFormat.format(new Date((long) value));
                            }
                        });
                    }
                    setupGraphData(i, verticalTypeData);
                    mChart.animateX(1000);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }
    }

    private void setupGraphData(int dateType, int dataType){
        String titleType = "";
        float y = 0;
        float time = 0;
        if(dateType == 0){
            dataLogging = db.getByHourly();
            time = 1;
        }else if(dateType == 1){
            dataLogging = db.getByDay();
            time = 24;
        }else if(dateType == 2){
            dataLogging = db.getByWeekly();
            time = 24;
        }else if(dateType == 3){
            dataLogging = db.getByMonthly();
            time = 24;
        }

        ArrayList<Entry> values = new ArrayList<>();

        long x;

        for(DataLogging dl : dataLogging){
            x = dl.get_date();
            Log.v("DATE", String.valueOf(dl.get_date()));
            if(dataType==0) {
                y = Float.parseFloat(dl.get_temp());
                Log.v("TEMP",dl.get_temp());
                titleType="Temperature";
                graphTitle.setText("Temperature");
                //leftAxis.setAxisMaximum(50f);
            }else if(dataType==1) {
                y = Float.parseFloat(dl.get_watt()); //watt
                titleType="Power";
                graphTitle.setText("Power");
              /*  if(dateType==0){
                    leftAxis.setAxisMaximum(500f);
                }else if(dateType==1){
                    leftAxis.setAxisMaximum(1250f);
                }else if(dateType==2){
                    leftAxis.setAxisMaximum(1500f);
                }else leftAxis.setAxisMaximum(2000f);*/
            }else if(dataType==2) {
                y = Float.parseFloat(dl.get_amps());
                titleType="Current";
                graphTitle.setText("Current");
            /*    if(dateType==0){
                    leftAxis.setAxisMaximum(2f);
                }else if(dateType==1){
                    leftAxis.setAxisMaximum(5f);
                }else if(dateType==2){
                    leftAxis.setAxisMaximum(5f);
                }else leftAxis.setAxisMaximum(1f);*/
            }else if(dataType==3) {
                y = Float.parseFloat(dl.get_energy())*time/1000;
                titleType="Energy";
                graphTitle.setText("Energy");
                Log.v("ENERGY",dl.get_energy());
              /*  if(dateType==0){
                    leftAxis.setAxisMaximum(1f);
                }else if(dateType==1){
                    leftAxis.setAxisMaximum(150f);
                }else if(dateType==2){
                    leftAxis.setAxisMaximum(250f);
                }else leftAxis.setAxisMaximum(500f);*/
            }


            values.add(new Entry(x*1000, (float) round(y,2)));

        }


        LineDataSet set1 = new LineDataSet(values, titleType);


            set1.setCubicIntensity(0.2f);
            set1.setLineWidth(1.5f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(true);

            set1.setDrawValues(true);
            set1.setValueTextColor(Color.BLACK);
            set1.setFillAlpha(65);

            set1.setHighLightColor(Color.rgb(0, 0, 255));
            set1.setDrawCircleHole(false);
            set1.setDrawHorizontalHighlightIndicator(false);


        if(dataType==0) {
            set1.setColor(Color.rgb(0, 0, 255));
            set1.setFillColor(Color.rgb(0,0,255));
            set1.setCircleColor(Color.rgb(0,0,255));
            set1.setHighLightColor(Color.argb(50,0,0,255));
        }else if(dataType==1) {
            set1.setColor(Color.rgb(255, 0, 0));
            set1.setFillColor(Color.rgb(255,0,0));
            set1.setCircleColor(Color.rgb(255,0,0));
            set1.setHighLightColor(Color.argb(50,255,0,0));
        }else if(dataType==2) {
            set1.setColor(Color.rgb(255, 0, 255));
            set1.setFillColor(Color.rgb(255,0,255));
            set1.setCircleColor(Color.rgb(255,0,255));
            set1.setHighLightColor(Color.argb(50,255,0,255));
        }else if(dataType==3) {
            set1.setColor(Color.rgb(0, 255, 0));
            set1.setFillColor(Color.rgb(0,255,0));
            set1.setCircleColor(Color.rgb(0,255,0));
            set1.setHighLightColor(Color.argb(50,0,255,0));
        }

            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return -10;
                }
            });



            // create a data object with the datasets
            LineData data = new LineData(set1);
            data.setValueTextColor(Color.argb(0,0,0,0));
            data.setValueTextSize(9f);

            // set data

            mChart.setData(data);


    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
