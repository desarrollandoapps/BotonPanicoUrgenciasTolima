package com.desarrollandoapps.botonpanicourgenciastolima;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    public final static String DIR_PHP_ESCRITURA = "http://gesicom.co/paginas/ambulancias/insertar_dato_app.php";

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
    private RequestQueue requestQueue;
    private JsonRequest jsonRequest;

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

        requestQueue  = Volley.newRequestQueue(this);

        GPSManager gps = new GPSManager(this);
        gps.start();
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
                    if (ubicacion != null)
                    {
                        obtenerNumeroInvolucrados();
                    }
                    else {
                        Toast.makeText(this, R.string.ubicacion_null, Toast.LENGTH_SHORT).show();
                    }
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
        /*
         * Subir datos de accesos
         */
        AsyncHttpClient cliente = new AsyncHttpClient();
        RequestParams parametros = new RequestParams();
        progressBar.setVisibility(View.VISIBLE);

        /*
        Crear Json
         */
        String tipoStr = String.valueOf(tipoEmergencia);
        String longitudStr = String.valueOf(ubicacion.getLongitude());
        String latitudStr = String.valueOf(ubicacion.getLatitude());
        String involucradosStr = String.valueOf(numeroInvolucrados);

        ArrayList<HashMap<String, String>> registro = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("tipo", tipoStr);
        map.put("longitud", longitudStr);
        map.put("latitud", latitudStr);
        map.put("involucrados", involucradosStr);

        Gson gson = new GsonBuilder().create();
        registro.add(map);
        String json = gson.toJson( registro );

        parametros.put("registrosJSON", json);

        cliente.post(DIR_PHP_ESCRITURA, parametros, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println(statusCode);
                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(MainActivity.this, R.string.datos_enviados, Toast.LENGTH_LONG).show();
                enviado = true;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "Error: " + statusCode, Toast.LENGTH_SHORT).show();
            }
        });

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

    public void llamar(View v)
    {
        tipoEmergencia = LLAMAR;
        String telefono = "tel:0382709600";
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(telefono));
        startActivity(intent);
    }

}
