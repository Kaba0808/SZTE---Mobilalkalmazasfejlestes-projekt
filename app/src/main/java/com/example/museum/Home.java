package com.example.museum;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Home extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private final double LOUVRE_LAT = 48.8606;
    private final double LOUVRE_LNG = 2.3376;

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<Ticket> tickets;
    private Toolbar toolbar;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView distanceText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Főoldal");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        setHasOptionsMenu(true);

        recyclerView = view.findViewById(R.id.ticketRecyclerView);
        distanceText = view.findViewById(R.id.distanceText);

        // Jegyek létrehozása
        tickets = new ArrayList<>();
        tickets.add(new Ticket("Gyerek", 200, "Ingyenes látogatás 18 év alattiaknak"));
        tickets.add(new Ticket("Diák", 2000, "Diákjegy 18-26 év között"));
        tickets.add(new Ticket("Diákcsoportra Szóló Jegy", 1200, "Iskolai diákcsoportok számára kedvezményes belépő, tematikus tárlatvezetéssel."));
        tickets.add(new Ticket("Nyugdíjas", 2000, "Nyugdíjas jegy"));
        tickets.add(new Ticket("Felnőtt", 2500, "Felnőtt jegy"));
        tickets.add(new Ticket("Családi Jegy", 6000, "2 felnőtt és 2 gyerek számára, családbarát tárlatvezetéssel."));
        tickets.add(new Ticket("Csoportos Jegy", 10000, "Csoportos látogatók számára, kedvezményes ár és csoportos tárlatvezetés."));
        tickets.add(new Ticket("Szakmai Jegy", 3500, "Szakemberek számára, mélyreható tárlatvezetéssel."));
        tickets.add(new Ticket("Jótékonysági Jegy", 8000, "A múzeum fenntartásához járul hozzá, VIP túrával."));
        tickets.add(new Ticket("VIP Jegy", 5000, "Különleges élmény egyéni tárlatvezetéssel, elsőbbségi belépéssel és ajándékcsomaggal."));
        tickets.add(new Ticket("Múzeum Évjegy", 15000, "Bármikor látogatható egész évben, ingyenes belépés a különböző rendezvényekre."));
        tickets.add(new Ticket("Éjszakai Jegy", 3000, "Éjszakai múzeumi látogatás, zárás után."));
        tickets.add(new Ticket("Digitális Jegy", 1500, "Online virtuális tárlatvezetés, múzeumi tartalomhoz való hozzáférés."));
        tickets.add(new Ticket("Múzeumi Ajándék Jegy", 2500, "Ajándékba adható jegy, bármikor felhasználható a múzeumban."));

        adapter = new TicketAdapter(tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            checkLocationSettings();
        }

        ImageView mapIcon = view.findViewById(R.id.mapIcon);
        mapIcon.setOnClickListener(v -> {
            String location = "Louvre Museum, Paris";
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            startActivity(mapIntent);
        });
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(requireActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnCompleteListener(task1 -> {
            try {
                LocationSettingsResponse response = task1.getResult(ApiException.class);
                // Helymeghatározás be van kapcsolva, elindíthatjuk a helyzetfrissítést
                startLocationUpdates();
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Megjelenít egy dialógust a helymeghatározás bekapcsolására
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            resolvable.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            distanceText.setText("Hiba történt a helymeghatározás beállításánál.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        distanceText.setText("Nem lehet bekapcsolni a helymeghatározást a készüléken.");
                        break;
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (locationManager == null) return;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                updateDistance(location);
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override public void onProviderEnabled(@NonNull String provider) {}
            @Override public void onProviderDisabled(@NonNull String provider) {}
        };

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 10, locationListener);

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown == null) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnown != null) {
                updateDistance(lastKnown);
            }
        }
    }

    private void updateDistance(Location userLocation) {
        Location louvreLocation = new Location("Louvre");
        louvreLocation.setLatitude(LOUVRE_LAT);
        louvreLocation.setLongitude(LOUVRE_LNG);

        float distanceInMeters = userLocation.distanceTo(louvreLocation);

        String distanceStr;
        if (distanceInMeters > 1000) {
            distanceStr = String.format(Locale.getDefault(), "Távolságod a Louvre-tól: %.2f km", distanceInMeters / 1000);
        } else {
            distanceStr = String.format(Locale.getDefault(), "Távolságod a Louvre-tól: %.0f m", distanceInMeters);
        }

        distanceText.setText(distanceStr);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationSettings();
            } else {
                distanceText.setText("Helymeghatározási engedély szükséges a távolság megjelenítéséhez.");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                startLocationUpdates();
            } else {
                distanceText.setText("A helymeghatározás nem lett bekapcsolva.");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setQueryHint("Keresés jegytípus alapján");
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.soft_cream));
        searchEditText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent));
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterList(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterList(newText
                );
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }
}