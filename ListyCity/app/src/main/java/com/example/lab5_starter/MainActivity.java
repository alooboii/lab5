package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final City city = cityArrayList.get(i);
            new AlertDialog.Builder(this)
                .setTitle("Delete City")
                .setMessage("Are you sure you want to delete " + city.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    citiesRef.document(city.getName()).delete()
                        .addOnSuccessListener(aVoid -> Log.d("MainActivity", "DocumentSnapshot successfully deleted!"))
                        .addOnFailureListener(e -> Log.w("MainActivity", "Error deleting document", e));
                })
                .setNegativeButton("Cancel", null)
                .show();
            return true;
        });

        citiesRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.w("MainActivity", "Listen failed.", e);
                return;
            }

            cityArrayList.clear();
            if (queryDocumentSnapshots != null) {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String cityName = doc.getString("name");
                    String provinceName = doc.getString("province");
                    cityArrayList.add(new City(cityName, provinceName));
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
            Log.d("MainActivity", "Snapshot listener updated list.");
        });
    }

    @Override
    public void updateCity(City city, String name, String province) {
        // This is tricky if the city name is the document ID.
        // For now, we assume city name (the ID) doesn't change, only the province.
        // A better way would be to have a unique ID for each city.
        citiesRef.document(city.getName()).set(city);
    }

    @Override
    public void addCity(City city){
        citiesRef.document(city.getName()).set(city)
                .addOnSuccessListener(aVoid -> Log.d("MainActivity", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("MainActivity", "Error writing document", e));
    }
}
