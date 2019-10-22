package com.desarrollandoapps.botonpanicourgenciastolima;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final static int SIN_ASIGNAR = 0;
    public final static int ACCIDENTE_AUTO = 1;
    public final static int ACCIDENTE_MOTO = 2;
    public final static int CAIDA_DESMAYO = 3;
    public final static int LLAMAR = 4;

    private ImageButton btnSolicitarAmbulancia;
    private ProgressBar progressBar;
    private TextView lblTipoEmergencia;

    private Location ubicacion;
    private int tipoEmergencia;
    private int numeroInvolucrados;
    private boolean enviado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSolicitarAmbulancia = findViewById(R.id.btnSolicitarAmbulancia);
        lblTipoEmergencia = findViewById(R.id.lblTipoEmergencia);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        tipoEmergencia = SIN_ASIGNAR;
        numeroInvolucrados = 0;
        enviado = false;

        int chequeoPermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (chequeoPermiso == PackageManager.PERMISSION_DENIED)
        {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        obtenerUbicacion();

    }

    public void solicitarAmbulancia(View v)
    {
        if (!enviado)
        {
            switch (tipoEmergencia)
            {
                case SIN_ASIGNAR:
                    Toast.makeText(this, "Debe seleccionar el tipo de emergencia", Toast.LENGTH_SHORT).show();
                    break;
                case ACCIDENTE_AUTO:
                case ACCIDENTE_MOTO:
                case CAIDA_DESMAYO:
                    progressBar.setVisibility(View.VISIBLE);
                    obtenerUbicacion();
                    progressBar.setVisibility(View.INVISIBLE);
                    obtenerNumeroInvolucrados();
                    break;
                case LLAMAR:
                    break;
            }
        }
        else
        {
            Toast.makeText(this, "Ya ha enviado la información.", Toast.LENGTH_SHORT).show();
        }
    }

    public void limpiar()
    {
        tipoEmergencia = 0;
        numeroInvolucrados = 0;
    }

    public void enviarDatos()
    {
        String datos = "Emergencia tipo: " + tipoEmergencia + "\n" +
                        "Número de involucrados: " + numeroInvolucrados + "\n" +
                        "Longitud: " + ubicacion.getLongitude() + "\n" +
                        "Latitud: " + ubicacion.getLatitude();
        Toast.makeText(this, datos, Toast.LENGTH_LONG).show();
        enviado = true;
    }

    public void obtenerNumeroInvolucrados()
    {
        final AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle(R.string.dialog_title);
        dialogo.setItems(R.array.num_involucrados, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(MainActivity.this, "Involucrados: " + i, Toast.LENGTH_SHORT).show();
                numeroInvolucrados = i + 1;
                enviarDatos();
                limpiar();
            }
        });

        //Muestra el dialogo
        dialogo.show();
    }

    public void obtenerUbicacion()
    {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                ubicacion = location;
                //Toast.makeText(MainActivity.this, "Latitud: " + ubicacion.getLatitude() + "\nLongitud: " + ubicacion.getLongitude(), Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        int chequeoPermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void cargarAuto(View v)
    {
        tipoEmergencia = ACCIDENTE_AUTO;
        lblTipoEmergencia.setText(getString(R.string.tipo_emergencia) + " " + getString(R.string.btn_auto));
        //Toast.makeText(this, "Ha seleccionado emergencia por accidente de automóviles", Toast.LENGTH_SHORT).show();
    }

    public void cargarMoto(View v)
    {
        tipoEmergencia = ACCIDENTE_MOTO;
        lblTipoEmergencia.setText(getString(R.string.tipo_emergencia) + " " + getString(R.string.btn_moto));
        //Toast.makeText(this, "Ha seleccionado emergencia por accidente de motos", Toast.LENGTH_SHORT).show();
    }

    public void cargarCaida(View v)
    {
        tipoEmergencia = CAIDA_DESMAYO;
        lblTipoEmergencia.setText(getString(R.string.tipo_emergencia) + " " + getString(R.string.btn_desmayo));
        //Toast.makeText(this, "Ha seleccionado emergencia por caída o desmayo", Toast.LENGTH_SHORT).show();
    }

    public void cargarLlamada(View v)
    {
        tipoEmergencia = LLAMAR;
    }

}
