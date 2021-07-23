package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import static com.dam.gestorconsentimientos.Aplicacion.ctx;

public class InfosolActivity extends AppCompatActivity {

    TextView solicitante;
    TextView usudatos;
    TextView ubidatos;
    TextView catdatos;
    TextView datos;
    TextView accion;
    TextView dest;
    TextView motivo;
    TextView duracion;
    TextView cond;
    TextView estado;

    Consen consentimiento;
    String tipo;
    String acceso;
    String name;

    ScrollView scroll;
    RelativeLayout botonesagente;
    RelativeLayout botonesciud;

    Button posponer;
    Button aceptar;
    Button rechazar;
    Button atras;

    Intent intent;

    JSONObject objeto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infosol);

        solicitante = (TextView) findViewById(R.id.solicitante);
        usudatos = (TextView) findViewById(R.id.usuariodatos);
        ubidatos = (TextView) findViewById(R.id.ubidatos);
        catdatos = (TextView) findViewById(R.id.categoriadatos);
        datos = (TextView) findViewById(R.id.datos);
        accion = (TextView) findViewById(R.id.accion);
        dest = (TextView) findViewById(R.id.destinatario);
        motivo = (TextView) findViewById(R.id.motivo);
        duracion = (TextView) findViewById(R.id.duracion);
        cond = (TextView) findViewById(R.id.cond);
        estado = (TextView) findViewById(R.id.estado);

        scroll = (ScrollView) findViewById(R.id.scrollView);
        botonesagente = (RelativeLayout) findViewById(R.id.botonesagente);
        botonesciud = (RelativeLayout) findViewById(R.id.botonesciud);
        posponer = (Button) findViewById(R.id.posponer);
        aceptar = (Button) findViewById(R.id.aceptar);
        rechazar = (Button) findViewById(R.id.rechazar);
        atras = (Button) findViewById(R.id.atras);

        intent = getIntent();
        consentimiento = (Consen) intent.getExtras().get("consentimiento");
        tipo = intent.getExtras().getString("tipo");
        acceso = intent.getExtras().getString("acceso");

        if(tipo.equals("ciud")){
            solicitante.setText(intent.getExtras().getString("sol"));
            botonesagente.setVisibility(View.GONE);
            botonesciud.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 735);
            lay.setMargins(0,25,0,0);
            scroll.setLayoutParams(lay);
        } else{
            solicitante.setText(consentimiento.getPerformer().get(0).getIdentifier().getValue());
        }
        usudatos.setText(intent.getExtras().getString("agente"));
        ubidatos.setText(consentimiento.getOrganizationFirstRep().getDisplay());
        catdatos.setText(consentimiento.getCategoryFirstRep().getText());
        datos.setText(consentimiento.getDatos().getValue());
        if(consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode().equals("access")){
            accion.setText("Acceso");
        } else {
            if(consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode().equals("use")){
                accion.setText("Lectura");
            } else {
                if (consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode().equals("correct")){
                    accion.setText("Modificación");
                } else {
                    if (consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode().equals("disclose")){
                        accion.setText("Envío");
                    }
                }
            }
        }
        dest.setText(consentimiento.getPatient().getIdentifier().getValue());
        motivo.setText(consentimiento.getScope().getText());
        duracion.setText(consentimiento.getDuracion().getValue());
        cond.setText(consentimiento.getCond().getValue());
        if(consentimiento.getStatus().equals(Consent.ConsentState.DRAFT)) {
            estado.setText("Pendiente");
        } else {
            if(consentimiento.getStatus().equals(Consent.ConsentState.REJECTED)) {
                estado.setText("Rechazado");
                posponer.setVisibility(View.GONE);
            } else {
                if(consentimiento.getStatus().equals(Consent.ConsentState.ACTIVE)) {
                    estado.setText("Otorgado");
                    posponer.setVisibility(View.GONE);
                }
            }
        }

        try{
            objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(consentimiento));
        } catch (JSONException exc){
            Toast.makeText(getApplicationContext(),"Error en objeto JSON de Consentimiento", Toast.LENGTH_SHORT).show();
        }

        if(consentimiento.getAviso().getValue() == true) {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, "http://192.168.1.54:8080/TFGREST/ciud/consentimiento/actualizaralerta", objeto,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                JSONObject code = response.getJSONObject("codigo");
                                Integer cod = code.getInt("valueInteger");

                                if (cod == 400) {
                                    Toast.makeText(getApplicationContext(),
                                            "Error al actualizar la alerta del consentimiento", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            VolleyLog.v("Response:%n %s", response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // add the request object to the queue to be executed
            Aplicacion.getInstance().getRequestQueue().add(request);
        }

        posponer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "Se mantiene en pendiente", Toast.LENGTH_SHORT).show();
                if(intent.getExtras().getBoolean("alerta") == true) {
                    Intent intent = new Intent(InfosolActivity.this, AlertasActivity.class);
                    intent.putExtra("dni", acceso);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(InfosolActivity.this, CiudActivity.class);
                    intent.putExtra("dni", acceso);
                    startActivity(intent);
                }
            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, "http://192.168.1.54:8080/TFGREST/ciud/consentimiento/modificar?estado=active", objeto,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    JSONObject code = response.getJSONObject("codigo");
                                    Integer cod = code.getInt("valueInteger");

                                    if (cod == 400) {
                                        Toast.makeText(getApplicationContext(),
                                                "Error al aceptar el consentimiento", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (cod == 200) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Consentimiento aceptado", Toast.LENGTH_SHORT).show();

                                            if(intent.getExtras().getBoolean("alerta") == true) {
                                                Intent intent = new Intent(InfosolActivity.this, AlertasActivity.class);
                                                intent.putExtra("dni", acceso);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(InfosolActivity.this, CiudActivity.class);
                                                intent.putExtra("dni", acceso);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                VolleyLog.v("Response:%n %s", response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
                    }
                });
                request.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // add the request object to the queue to be executed
                Aplicacion.getInstance().getRequestQueue().add(request);
            }
        });

        rechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, "http://192.168.1.54:8080/TFGREST/ciud/consentimiento/modificar?estado=rejected", objeto,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    JSONObject code = response.getJSONObject("codigo");
                                    Integer cod = code.getInt("valueInteger");

                                    if (cod == 400) {
                                        Toast.makeText(getApplicationContext(),
                                                "Error al rechazar el consentimiento", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (cod == 200) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Consentimiento rechazado", Toast.LENGTH_SHORT).show();

                                            if(intent.getExtras().getBoolean("alerta") == true) {
                                                Intent intent = new Intent(InfosolActivity.this, AlertasActivity.class);
                                                intent.putExtra("dni", acceso);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(InfosolActivity.this, CiudActivity.class);
                                                intent.putExtra("dni", acceso);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                VolleyLog.v("Response:%n %s", response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
                    }
                });
                request.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                // add the request object to the queue to be executed
                Aplicacion.getInstance().getRequestQueue().add(request);
            }
        });

        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(intent.getExtras().getBoolean("alerta") == true) {
                    Intent intent = new Intent(InfosolActivity.this, AlertasActivity.class);
                    intent.putExtra("dni", acceso);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(InfosolActivity.this, CiudActivity.class);
                    intent.putExtra("dni", acceso);
                    startActivity(intent);
                }
            }
        });
    }

    public void onClickizq (View view) {

        String URL = "http://192.168.1.54:8080/TFGREST/agente/consentimiento/eliminar";
        ProgressDialog dlg = ProgressDialog.show(this,
                "Eliminando consentimiento",
                "Por favor, espere...", true);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, objeto,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dlg.dismiss();
                        try {

                            JSONObject code = response.getJSONObject("codigo");
                            Integer cod = code.getInt("valueInteger");

                            if (cod == 400) {
                                Toast.makeText(getApplicationContext(),
                                        "Error al eliminar el consentimiento", Toast.LENGTH_SHORT).show();
                            } else {
                                if (cod == 200) {
                                    Toast.makeText(getApplicationContext(),
                                            "Consentimiento eliminado", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(InfosolActivity.this, AgenteActivity.class);
                                    intent.putExtra("login", acceso);
                                    startActivity(intent);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        VolleyLog.v("Response:%n %s", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dlg.dismiss();
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // add the request object to the queue to be executed
        Aplicacion.getInstance().getRequestQueue().add(request);
    }

    public void onClickder (View view) {
        Intent intentatras = new Intent(InfosolActivity.this, AgenteActivity.class);
        intentatras.putExtra("login", acceso);
        startActivity(intentatras);
    }
}
