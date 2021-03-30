package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;

public class CiudActivity extends AppCompatActivity {

    ScrollView scrollView;
    ImageView imageView;
    LinearLayout lay;

    private Button salir;

    String dni;
    String name;

    String URL;
    ProgressDialog dlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ciud);

        imageView = (ImageView) findViewById(R.id.imagebienvenido);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        lay = (LinearLayout) findViewById(R.id.listaconsent);
        salir = (Button) findViewById(R.id.cerrarsesion);

        Intent intent = getIntent();
        dni = intent.getExtras().getString("dni");

        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CiudActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }

    public void onClick (View view) {

        imageView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        switch (view.getId()) {

            case R.id.otorgados:
                URL = "http://192.168.1.108:8080/TFGREST/ciud/" + dni + "?estado=active";
                dlg = ProgressDialog.show(this,
                        "Obteniendo consentimientos aceptados",
                        "Por favor, espere...", true);
                break;

            case R.id.pendientes:
                URL = "http://192.168.1.108:8080/TFGREST/ciud/" + dni + "?estado=draft";
                dlg = ProgressDialog.show(this,
                        "Obteniendo consentimientos pendientes",
                        "Por favor, espere...", true);
                break;

            case R.id.rechazados:
                URL = "http://192.168.1.108:8080/TFGREST/ciud/" + dni + "?estado=rejected";
                dlg = ProgressDialog.show(this,
                        "Obteniendo consentimientos rechazados",
                        "Por favor, espere...", true);
                break;

        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dlg.dismiss();
                        try {
                            lay.removeAllViewsInLayout();
                            JSONArray consentimientos = response.getJSONArray("consentimientos");

                            if(consentimientos.length() == 0){
                                imageView.setImageResource(R.mipmap.sinconsentimientos);
                                imageView.setVisibility(View.VISIBLE);
                                scrollView.setVisibility(View.GONE);
                            } else {
                                for (int i = 0; i < consentimientos.length(); i++) {
                                    JSONObject consent = consentimientos.getJSONObject(i);
                                    Consen consen = generarconsentimiento(consent);
                                    obteneragente(consen.getIdentifier().get(0).getId());
                                    new CountDownTimer(500, 100) {

                                        @Override
                                        public void onTick(long arg0) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Estos son sus consentimientos", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFinish() {
                                            añadirconsen(consen);
                                        }
                                    }.start();
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

    public Consen generarconsentimiento (JSONObject consentimiento){

        Consen consen = new Consen();

        try {

            JSONArray extension = consentimiento.getJSONArray("extension");
            consen.setDatos(new StringDt(extension.getJSONObject(0).getString("valueString")));
            consen.setAccion(new StringDt(extension.getJSONObject(1).getString("valueString")));
            consen.setDuracion(new StringDt(extension.getJSONObject(2).getString("valueString")));
            consen.setCond(new StringDt(extension.getJSONObject(3).getString("valueString")));
            consen.setAlerta(new BooleanDt(extension.getJSONObject(4).getBoolean("valueBoolean")));

            consen.addIdentifier((Identifier) new Identifier().setId(consentimiento.getJSONArray("identifier").getJSONObject(0).getString("id")));

            if(consentimiento.getString("status").equals("draft")) {
                consen.setStatus(Consent.ConsentState.DRAFT);
            } else {
                if(consentimiento.getString("status").equals("rejected")) {
                    consen.setStatus(Consent.ConsentState.REJECTED);
                } else {
                    if(consentimiento.getString("status").equals("active")) {
                        consen.setStatus(Consent.ConsentState.ACTIVE);
                    }
                }
            }

            consen.setScope(new CodeableConcept().setText(consentimiento.getJSONObject("scope").getString("text")));
            String cat = consentimiento.getJSONArray("category").getString(0);
            consen.addCategory().setText(cat.substring(9,cat.length()-2));
            consen.setPatient(new Reference().setReference(consentimiento.getJSONObject("patient").getString("reference")));
            String usu = consentimiento.getJSONArray("performer").getString(0);
            consen.addPerformer(new Reference().setReference(usu.substring(14,usu.length()-2)));
            String ubi = consentimiento.getJSONArray("organization").getString(0);
            consen.addOrganization(new Reference().setReference(ubi.substring(14,ubi.length()-2)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return consen;
    }

    public void obteneragente(String agente){

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://192.168.1.108:8080/TFGREST/agente/acceder?contra=" + agente, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dlg.dismiss();
                        try {

                            JSONArray JSONnombre = response.getJSONArray("name");
                            JSONObject objnombre = JSONnombre.getJSONObject(0);
                            name = objnombre.getString("text");

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

    public void añadirconsen(Consen consentimiento){

        LinearLayout layconsentimiento = new LinearLayout(this);
        layconsentimiento.setOrientation(LinearLayout.VERTICAL);

        if(consentimiento.getStatus().toString() == "DRAFT") {
            layconsentimiento.setBackgroundColor(getResources().getColor(R.color.coloramarillo));
        }
        if(consentimiento.getStatus().toString() == "ACTIVE") {
            layconsentimiento.setBackgroundColor(getResources().getColor(R.color.colorazul));
        }
        if(consentimiento.getStatus().toString() == "REJECTED") {
            layconsentimiento.setBackgroundColor(getResources().getColor(R.color.colorrojoclaro));
        }

        LinearLayout.LayoutParams paramconsent = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        paramconsent.setMargins(10,50,10,0);

        TextView textViewsol = new TextView(this);
        SpannableString solicitante = new SpannableString("   Solicitante: " + name);
        solicitante.setSpan(new UnderlineSpan(), 3, 15, 0);
        solicitante.setSpan(new StyleSpan(Typeface.BOLD), 0, solicitante.length(), 0);
        solicitante.setSpan(new AbsoluteSizeSpan(15, true),0, solicitante.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        solicitante.setSpan(new BackgroundColorSpan(Color.WHITE), 16, solicitante.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewsol.setText(solicitante);
        textViewsol.setPadding(10,10,10,0);

        TextView textViewusu = new TextView(this);
        SpannableString usu = new SpannableString("   Usuario de datos: " + consentimiento.getPerformerFirstRep().getReference());
        usu.setSpan(new UnderlineSpan(), 3, 20, 0);
        usu.setSpan(new StyleSpan(Typeface.BOLD), 0, usu.length(), 0);
        usu.setSpan(new AbsoluteSizeSpan(15, true),0, usu.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        usu.setSpan(new BackgroundColorSpan(Color.WHITE), 21, usu.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewusu.setText(usu);
        textViewusu.setPadding(10,15,10,0);

        TextView textViewdatos = new TextView(this);
        SpannableString datos = new SpannableString("   Datos a manejar: " + consentimiento.getDatos().getValue());
        datos.setSpan(new UnderlineSpan(), 3, 19, 0);
        datos.setSpan(new StyleSpan(Typeface.BOLD), 0, datos.length(), 0);
        datos.setSpan(new AbsoluteSizeSpan(15, true),0,datos.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        datos.setSpan(new BackgroundColorSpan(Color.WHITE), 20, datos.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewdatos.setText(datos);
        textViewdatos.setPadding(10,15,10,0);

        TextView textViewaccion = new TextView(this);
        SpannableString accion = new SpannableString("   Acción a realizar: " + consentimiento.getAccion().getValue());
        accion.setSpan(new UnderlineSpan(), 3, 21, 0);
        accion.setSpan(new StyleSpan(Typeface.BOLD), 0, accion.length(), 0);
        accion.setSpan(new AbsoluteSizeSpan(15, true),0,accion.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        accion.setSpan(new BackgroundColorSpan(Color.WHITE), 22, accion.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewaccion.setText(accion);
        textViewaccion.setPadding(10,15,10,10);

        layconsentimiento.addView(textViewsol);
        layconsentimiento.addView(textViewusu);
        layconsentimiento.addView(textViewdatos);
        layconsentimiento.addView(textViewaccion);
        layconsentimiento.setClickable(true);
        layconsentimiento.setFocusable(true);
        layconsentimiento.setOnClickListener(new View.OnClickListener ()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(CiudActivity.this, InfosolActivity.class);
                intent.putExtra("consentimiento", consentimiento);
                intent.putExtra("tipo", "ciud");
                intent.putExtra("acceso", dni);
                intent.putExtra("agente", name);
                startActivity(intent);
            }
        });

        lay.addView(layconsentimiento,paramconsent);

    }
}
