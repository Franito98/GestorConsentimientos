package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
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

import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.utilities.DateTimeUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;

public class AgenteActivity extends AppCompatActivity {

    private Button cerrar, nuevasol;
    ImageView imageView;
    ScrollView scrollView;
    LinearLayout lay;

    String login;

    String URL;
    String URL2 = "http://192.168.1.54:8080/TFGREST/agente/hospital/";
    ProgressDialog dlg;

    String hosp;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agente);

        imageView = (ImageView) findViewById(R.id.imagebienvenido);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        lay = (LinearLayout) findViewById(R.id.listaconsent);
        cerrar = (Button) findViewById(R.id.cerrarsesion);
        nuevasol = (Button) findViewById(R.id.nuevasol);

        Intent intent = getIntent();
        login = intent.getExtras().getString("login");

        cerrar.setOnClickListener(new View.OnClickListener ()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(AgenteActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        nuevasol.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(AgenteActivity.this, SolicitudActivity.class);
                intent.putExtra("login", login);
                startActivity(intent);
            }
        });
    }

    public void onClick (View view) {

        imageView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        switch (view.getId()) {

            case R.id.otorgados:
                URL = "http://192.168.1.54:8080/TFGREST/agente/" + login + "?estado=active";
                dlg = ProgressDialog.show(this,
                        "Obteniendo consentimientos aceptados",
                        "Por favor, espere...", true);
                break;

            case R.id.pendientes:
                URL = "http://192.168.1.54:8080/TFGREST/agente/" + login + "?estado=draft";
                dlg = ProgressDialog.show(this,
                        "Obteniendo consentimientos pendientes",
                        "Por favor, espere...", true);
                break;

            case R.id.rechazados:
                URL = "http://192.168.1.54:8080/TFGREST/agente/" + login + "?estado=rejected";
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
                                    consen = a??adirhospital(consen);
                                    a??adirconsen(consen);
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
            consen.setDuracion(new StringDt(extension.getJSONObject(1).getString("valueString")));
            consen.setCond(new StringDt(extension.getJSONObject(2).getString("valueString")));
            consen.setAviso(new BooleanDt(extension.getJSONObject(3).getBoolean("valueBoolean")));

            /*
            Identifier identifier = new Identifier();
            identifier.setId(consentimiento.getJSONArray("identifier")
                    .getJSONObject(0).getString("id"));
            identifier.setSystem(consentimiento.getJSONArray("identifier")
                    .getJSONObject(0).getString("system"));
            identifier.setValue(consentimiento.getJSONArray("identifier")
                    .getJSONObject(0).getString("value"));
            consen.addIdentifier(identifier);
             */

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
            consen.addCategory().setText(consentimiento.getJSONArray("category").getJSONObject(0).getString("text"));

            Reference refprac = new Reference();
            refprac.setReference(consentimiento.getJSONArray("performer").getJSONObject(0).getString("reference"));
            refprac.setType(consentimiento.getJSONArray("performer").getJSONObject(0).getString("type"));
            refprac.setIdentifier(new Identifier().setValue(consentimiento.getJSONArray("performer").getJSONObject(0)
                    .getJSONObject("identifier").getString("value")));
            consen.addPerformer(refprac);

            Reference reference = new Reference();
            reference.setReference(consentimiento.getJSONObject("patient").getString("reference"));
            reference.setType(consentimiento.getJSONObject("patient").getString("type"));
            reference.setIdentifier(new Identifier().setValue(consentimiento.getJSONObject("patient")
                    .getJSONObject("identifier").getString("value")));
            consen.setPatient(reference);

            CodeableConcept codeaccion = new CodeableConcept();
            if(consentimiento.getJSONObject("provision").getJSONArray("action")
                    .getJSONObject(0).getJSONArray("coding").getJSONObject(0).get("code").equals("access")){
                Coding cod = new Coding();
                cod.setCode("access");
                codeaccion.addCoding(cod);
            } else {
                if(consentimiento.getJSONObject("provision").getJSONArray("action")
                        .getJSONObject(0).getJSONArray("coding").getJSONObject(0).get("code").equals("use")){
                    Coding cod = new Coding();
                    cod.setCode("use");
                    codeaccion.addCoding(cod);
                } else {
                    if (consentimiento.getJSONObject("provision").getJSONArray("action")
                            .getJSONObject(0).getJSONArray("coding").getJSONObject(0).get("code").equals("correct")){
                        Coding cod = new Coding();
                        cod.setCode("correct");
                        codeaccion.addCoding(cod);
                    } else {
                        if (consentimiento.getJSONObject("provision").getJSONArray("action")
                                .getJSONObject(0).getJSONArray("coding").getJSONObject(0).get("code").equals("disclose")){
                            Coding cod = new Coding();
                            cod.setCode("disclose");
                            codeaccion.addCoding(cod);
                        }
                    }
                }
            }
            Consent.provisionComponent provision = new Consent.provisionComponent();
            provision.addAction(codeaccion);

            Reference refusudatos = new Reference();
            refusudatos.setReference("http://hapi.fhir.org/Practitioner");
            refusudatos.setType("Practitioner");
            refusudatos.setIdentifier(new Identifier().setValue(consentimiento.getJSONObject("provision")
                    .getJSONArray("actor").getJSONObject(0).getJSONObject("reference")
                    .getJSONObject("identifier").getString("value")));
            Consent.provisionActorComponent actor = new Consent.provisionActorComponent();
            actor.setReference(refusudatos);
            provision.addActor(actor);

            consen.setProvision(provision);

            consen.setId(consentimiento.getString("id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return consen;
    }

    public Consen a??adirhospital(Consen consentimiento){

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL2+login, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dlg.dismiss();
                        try {

                            hosp = (String) response.get("hospital");
                            consentimiento.getOrganizationFirstRep().setDisplay(hosp);

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

        return consentimiento;
    }

    public void a??adirconsen(Consen consentimiento){

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

        TextView textViewusu = new TextView(this);
        SpannableString usu = new SpannableString("   Usuario de datos: " + consentimiento.getProvision().getActor().get(0)
                .getReference().getIdentifier().getValue());
        usu.setSpan(new UnderlineSpan(), 3, 20, 0);
        usu.setSpan(new StyleSpan(Typeface.BOLD), 0, usu.length(), 0);
        usu.setSpan(new AbsoluteSizeSpan(15, true),0, usu.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        usu.setSpan(new BackgroundColorSpan(Color.WHITE), 21, usu.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewusu.setText(usu);
        textViewusu.setPadding(10,10,10,0);

        TextView textViewdatos = new TextView(this);
        SpannableString datos = new SpannableString("   Datos a manejar: " + consentimiento.getDatos().getValue());
        datos.setSpan(new UnderlineSpan(), 3, 19, 0);
        datos.setSpan(new StyleSpan(Typeface.BOLD), 0, datos.length(), 0);
        datos.setSpan(new AbsoluteSizeSpan(15, true),0,datos.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        datos.setSpan(new BackgroundColorSpan(Color.WHITE), 20, datos.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewdatos.setText(datos);
        textViewdatos.setPadding(10,15,10,0);

        TextView textViewaccion = new TextView(this);
        if(consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode() == "access"){
            action = "Acceso";
        } else {
            if(consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode() == "use"){
                action = "Lectura";
            } else {
                if (consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode() == "correct"){
                    action = "Modificaci??n";
                } else {
                    if (consentimiento.getProvision().getAction().get(0).getCoding().get(0).getCode() == "disclose"){
                        action = "Env??o";
                    }
                }
            }
        }
        SpannableString accion = new SpannableString("   Acci??n a realizar: " + action);
        accion.setSpan(new UnderlineSpan(), 3, 21, 0);
        accion.setSpan(new StyleSpan(Typeface.BOLD), 0, accion.length(), 0);
        accion.setSpan(new AbsoluteSizeSpan(15, true),0,accion.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        accion.setSpan(new BackgroundColorSpan(Color.WHITE), 22, accion.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewaccion.setText(accion);
        textViewaccion.setPadding(10,15,10,0);

        TextView textViewdest = new TextView(this);
        SpannableString dest = new SpannableString("   Destinatario: " + consentimiento.getPatient().getIdentifier().getValue());
        dest.setSpan(new UnderlineSpan(), 3, 16, 0);
        dest.setSpan(new StyleSpan(Typeface.BOLD), 0, dest.length(), 0);
        dest.setSpan(new AbsoluteSizeSpan(15, true),0,dest.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        dest.setSpan(new BackgroundColorSpan(Color.WHITE), 17, dest.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        textViewdest.setText(dest);
        textViewdest.setPadding(10,15,10,10);

        layconsentimiento.addView(textViewusu);
        layconsentimiento.addView(textViewdatos);
        layconsentimiento.addView(textViewaccion);
        layconsentimiento.addView(textViewdest);
        layconsentimiento.setClickable(true);
        layconsentimiento.setFocusable(true);
        layconsentimiento.setOnClickListener(new View.OnClickListener ()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(AgenteActivity.this, InfosolActivity.class);
                intent.putExtra("consentimiento", consentimiento);
                intent.putExtra("tipo", "agente");
                intent.putExtra("acceso", login);
                startActivity(intent);
            }
        });

        lay.addView(layconsentimiento,paramconsent);

    }
}
