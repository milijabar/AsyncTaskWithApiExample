package com.example.asynctaskwithapiexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asynctaskwithapiexample.utilities.ApiDataReader;
import com.example.asynctaskwithapiexample.utilities.AsyncDataLoader;
import com.example.asynctaskwithapiexample.utilities.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    private ListView lvItems;
    private TextView tvStatus;
    private ArrayAdapter listAdapter;
    private Switch swUseAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.lvItems = findViewById(R.id.lv_items);
        this.tvStatus = findViewById(R.id.tv_status);
        this.swUseAsyncTask = findViewById(R.id.sw_use_async_task);

        this.listAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        this.lvItems.setAdapter(this.listAdapter);


        Spinner spinnerCurrencies = findViewById(R.id.spinner_currencies);
        ArrayAdapter<CharSequence> currencyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.currency_array, // Create an array resource with your currency names
                android.R.layout.simple_spinner_item
        );
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrencies.setAdapter(currencyAdapter);

        spinnerCurrencies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle currency selection here
                String selectedCurrency = parentView.getItemAtPosition(position).toString();
                // Call a method to filter and display data for the selected currency
                // For now, let's just show a Toast message with the selected currency
                //Toast.makeText(MainActivity.this, "Selected Currency: " + selectedCurrency, Toast.LENGTH_SHORT).show();
                getDataByAsyncTask(selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here for now
            }
        });

    }

    public void onBtnGetDataClick(View view) {
        this.tvStatus.setText(R.string.loading_data);
        if (this.swUseAsyncTask.isChecked()) {
            Spinner spinnerCurrencies = findViewById(R.id.spinner_currencies);
            String selectedCurrency = spinnerCurrencies.getSelectedItem().toString();

            getDataByAsyncTask(selectedCurrency);
            Toast.makeText(this, R.string.msg_using_async_task, Toast.LENGTH_LONG).show();
        } else {
            Spinner spinnerCurrencies = findViewById(R.id.spinner_currencies);
            String selectedCurrency = spinnerCurrencies.getSelectedItem().toString();

            getDataByThread(selectedCurrency);
            Toast.makeText(this, R.string.msg_using_thread, Toast.LENGTH_LONG).show();
        }
    }

    public void getDataByAsyncTask(String selectedCurrency) {
        new AsyncDataLoader() {
            @Override
            public void onPostExecute(String result) {
                tvStatus.setText(getString(R.string.data_loaded) + result);
            }
            //}.execute(Constants.GUNFIRE_URL);
        }.execute(Constants.FLOATRATES_API_URL, selectedCurrency);
        //}.execute(Constants.METEOLT_API_URL);
    }

    public void getDataByThread(String selectedCurrency) {
        this.tvStatus.setText(R.string.loading_data);
        Runnable getDataAndDisplayRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = ApiDataReader.getValuesFromApi(Constants.FLOATRATES_API_URL, selectedCurrency);
                    //final String result = ApiDataReader.getValuesFromApi(Constants.METEOLT_API_URL);
                    Runnable updateUIRunnable = new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(getString(R.string.data_loaded) + result);
                        }
                    };
                    runOnUiThread(updateUIRunnable);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(getDataAndDisplayRunnable);
        thread.start();

        //with Lambdas --->
        //        new Thread(() -> {
        //            try {
        //                final String result = ApiDataReader.getValuesFromApi(Constants.FLOATRATES_API_URL);
        //                runOnUiThread(() -> tvStatus.setText(getString(R.string.data_loaded) + result));
        //            } catch (IOException ex) {
        //                runOnUiThread(() -> tvStatus.setText("Error occured:" + ex.getMessage()));
        //            }
        //        }).start();
    }
}
