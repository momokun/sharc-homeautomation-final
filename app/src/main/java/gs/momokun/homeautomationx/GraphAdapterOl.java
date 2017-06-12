package gs.momokun.homeautomationx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.TimeZone;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import gs.momokun.homeautomationx.tools.DataLogging;
import gs.momokun.homeautomationx.tools.DatabaseHandler;

import static android.graphics.Color.argb;
import static java.lang.Math.round;

class GraphAdapterOl {
    private DatabaseHandler db;
    private Context context;
    private View view;
    private LineGraphSeries<DataPoint> serieswatt,seriesTemperature,seriesAmps,seriesEnergy;
    private GraphView graph;
    private Spinner spinner_data_type,spinner_time_range;
    private List<DataLogging> dataLogging;
    private double verticalValue;
    private int verticalTypeData = 0;
    private int verticalTimeData = 0;
    private double calcPrice;
    private TextView calculatedEnergy, graphTitle;
    //empty constructor, leave it be
    public GraphAdapterOl() {

    }

    GraphAdapterOl( Context context, View view, DatabaseHandler db) {
        this.context=context;
        this.view=view;
        this.db = db;
    }

    private void declaration(){
        //db = new DatabaseHandler(context);
        graph = new GraphView(context);
        graph = (GraphView) view.findViewById(R.id.graph);
        spinner_data_type = (Spinner) view.findViewById(R.id.spinner_date_array);
        spinner_time_range = (Spinner) view.findViewById(R.id.spinner_date_range);
        calculatedEnergy = (TextView) view.findViewById(R.id.calcPrice);
        graphTitle = (TextView) view.findViewById(R.id.graph_name);

        if(!db.getLatestTemp().equals("25")) {
            seriesTemperature = new LineGraphSeries<>(generateData(0,1));
            serieswatt = new LineGraphSeries<>(generateData(0,2));
            seriesAmps = new LineGraphSeries<>(generateData(0,3));
            seriesEnergy = new LineGraphSeries<>(generateData(3,4));
        }
    }

    private void graphSetup(){
        graph.getViewport().setScalable(true);
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHumanRounding(true);
     //   graph.getLegendRenderer().setVisible(true);
      //  graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
      //  graph.getLegendRenderer().setSpacing(15);


    }

    public void viewGraph(){
        declaration();
        graphSetup();

        final SimpleDateFormat hour = new SimpleDateFormat("HH");
        final SimpleDateFormat day = new SimpleDateFormat("dd-MMM");
        final SimpleDateFormat week = new SimpleDateFormat("W-1");
        final SimpleDateFormat month = new SimpleDateFormat("MMMM");
        //sdfz.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));

        /*graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis((long) value);
                    return sdfz.format(mCalendar.getTimeInMillis());
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + " €";
                }
            }
        });*/

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context,hour));

       /* StaticLabelsFormatter slf = new StaticLabelsFormatter(graph);
        slf.setHorizontalLabels(new String[] {"1","2","3","4","5","6","7"});
        graph.getGridLabelRenderer().setLabelFormatter(slf);*/

        //when data tapped, it will show something


        //choose range date of data that will be shown
        spinner_time_range.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                verticalTimeData = spinner_time_range.getSelectedItemPosition();
                verticalTypeData = spinner_data_type.getSelectedItemPosition();


                if(i==0){
                    graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context,hour));
                }
                if(i==1){
                    graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context,day));

                }
                if(i==2){
                    graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context,week));
                }
                if(i==3){
                    graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context,month));
                }


                if(verticalTypeData==0){
                    seriesTemperature.resetData(generateData(verticalTimeData,1));
                }
                if(verticalTypeData==1) {

                    serieswatt.resetData(generateData(verticalTimeData, 2));
                }
                if(verticalTypeData==2) {

                    seriesAmps.resetData(generateData(verticalTimeData, 3));
                }
                if(verticalTypeData==3) {

                    seriesEnergy.resetData(generateData(verticalTimeData, 4));

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //choose data type that will be shown
        spinner_data_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(i == 0){
                    graphTitle.setText("Temperature Graph");
                    graph.addSeries(seriesTemperature);

                    seriesTemperature.resetData(generateData(verticalTimeData,i+1));
                    seriesTemperature.setTitle("Temperature");
                //    seriesTemperature.setColor(argb(255,0,0,255));
                    seriesTemperature.setDrawBackground(true);
                    seriesTemperature.setBackgroundColor(argb(255,200,200,255));
                    seriesTemperature.setThickness(7);
                    seriesTemperature.setDrawDataPoints(true);
                    seriesTemperature.setDataPointsRadius(15);
                    seriesTemperature.setAnimated(true);
                    graph.removeSeries(serieswatt);
                    graph.removeSeries(seriesAmps);
                    graph.removeSeries(seriesEnergy);

                }else if(i == 1){
                    graphTitle.setText("Electricity Power Graph");
                    graph.addSeries(serieswatt);
                    serieswatt.resetData(generateData(verticalTimeData,i+1));
                    serieswatt.setTitle("Watt");
                    serieswatt.setColor(Color.RED);
                    serieswatt.setThickness(5);
                    graph.removeSeries(seriesTemperature);
                    graph.removeSeries(seriesAmps);
                    graph.removeSeries(seriesEnergy);
                }else if(i==2){
                    graphTitle.setText("Electricity Current Graph");
                    graph.addSeries(seriesAmps);
                    seriesAmps.resetData(generateData(verticalTimeData,i+1));
                    seriesAmps.setTitle("Current");
                    seriesAmps.setColor(Color.MAGENTA);
                    seriesAmps.setThickness(5);
                    graph.removeSeries(seriesTemperature);
                    graph.removeSeries(serieswatt);
                    graph.removeSeries(seriesEnergy);
                }else if(i==3){
                    graphTitle.setText("Energy Usage Graph");
                    graph.addSeries(seriesEnergy);
                    seriesEnergy.resetData(generateData(verticalTimeData,i+1));
                    seriesEnergy.setTitle("Energy");
                    seriesEnergy.setColor(Color.GREEN);
                    seriesEnergy.setThickness(5);
                    graph.removeSeries(seriesTemperature);
                    graph.removeSeries(serieswatt);
                    graph.removeSeries(seriesAmps);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private DataPoint[] generateData(int range,int type) {

        Log.v("VertType", String.valueOf(type));
        Log.v("VertRange", String.valueOf(range));
        int count = 0;
        if(range==0){
            dataLogging = db.getByHourly();
        }else if(range==1){
            dataLogging = db.getByDay();
        }else if(range==2){
            dataLogging = db.getByWeekly();
        }else if(range==3){
            dataLogging = db.getByMonthly();
        }
        Date date;

        for(DataLogging dlForCount : dataLogging){
            count++;
            if(count==1) {
                date = new Date(dlForCount.get_date()*1000L);
                graph.getViewport().setMinX(date.getTime());
            }

        }

        graph.getGridLabelRenderer().setNumHorizontalLabels(6);



        int i = 0;
        DataPoint[] values = new DataPoint[count];


        DecimalFormat df = new DecimalFormat("#.##");
        for (DataLogging cn : dataLogging) {
            if(type==1){
                verticalValue = round(Double.parseDouble(cn.get_temp()));
                Log.v("dateTo",cn.get_temp());
            }else if(type==2){
                verticalValue = Double.parseDouble(cn.get_watt());
                Log.v("wattTo",cn.get_watt());
            }else if(type==3){
                verticalValue = Double.parseDouble(cn.get_amps());
                Log.v("ampsTo", String.valueOf(verticalValue));
            }else if(type==4){
                verticalValue = Double.parseDouble(cn.get_energy())/3600;
                Log.v("energyTo",cn.get_energy());
                Log.v("calcPriceVert", String.valueOf(verticalValue));
                calcPrice = calcPrice + verticalValue*1350;
                Log.v("calcPriceVert2", String.valueOf(calcPrice));
            }


            calculatedEnergy.setText("Rp."+df.format(calcPrice)+",-");
            calcPrice=0;

            Log.v("DateMinus", String.valueOf(cn.get_date()));
            Log.v("DateMinus", String.valueOf(cn.get_date()*1000L));
            date = new Date(cn.get_date()*1000L);

            Log.v("DateToUnix", String.valueOf(date.getTime()));
            DataPoint v = new DataPoint(date.getTime(), verticalValue);
            values[i] = v;
            i++;
            if(count==i){
                graph.getViewport().setMaxX(date.getTime());
            }
        }
        return values;
    }

}
