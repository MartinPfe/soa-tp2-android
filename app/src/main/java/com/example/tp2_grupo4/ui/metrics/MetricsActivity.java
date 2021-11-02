package com.example.tp2_grupo4.ui.metrics;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tp2_grupo4.R;
import com.example.tp2_grupo4.data.DbRepository;

import java.lang.reflect.Array;
import java.util.List;

public class MetricsActivity extends AppCompatActivity {

    private Spinner spinner;
    private ListView lvResults;
    DbRepository db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);

        db = new DbRepository(this);

        spinner = (Spinner)findViewById(R.id.spinner);
        lvResults = (ListView)findViewById(R.id.lvResults);

        ArrayAdapter spinAdapter = ArrayAdapter.createFromResource(this, R.array.MetricsParameters, android.R.layout.simple_spinner_item);
        spinner.setAdapter(spinAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> list, View view, int position, long id) {
                if(list.getItemAtPosition(position).equals("Los países más buscados")){
                    List<String> array = db.getCountryMoreVisited();

                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(MetricsActivity.this, android.R.layout.simple_list_item_1, array);
//                    ArrayAdapter listAdapter = (ArrayAdapter) db.getCountryMoreVisited();
                    lvResults.setAdapter(listAdapter);
                }
                else if(list.getItemAtPosition(position).equals("Los países con menos infectados")){
                    List<String> array = db.getCountriesLessInfected();

                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(MetricsActivity.this, android.R.layout.simple_list_item_1, array);
//                    ArrayAdapter listAdapter = (ArrayAdapter) db.getCountriesLessInfected();
                    lvResults.setAdapter(listAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
