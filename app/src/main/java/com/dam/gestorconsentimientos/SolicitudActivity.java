package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;

import static com.dam.gestorconsentimientos.Aplicacion.ctx;

public class SolicitudActivity extends AppCompatActivity {

    LinearLayout layoutciud;
    EditText usudatos, ubidatos, catdatos, datos, ciud, motivo, duracion, condiciones;
    Spinner accion;

    String acselec;

    Consen consentimiento = new Consen();

    private Button atras;

    JSONObject code;

    Intent intent;
    Bundle extra;
    String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud);

        intent = getIntent();
        extra = intent.getExtras();
        login = extra.getString("login");

        layoutciud = (LinearLayout) findViewById(R.id.layoutciud);

        //solicitante = (EditText) findViewById(R.id.solicitante);
        usudatos = (EditText) findViewById(R.id.usuariodatos);
        ubidatos = (EditText) findViewById(R.id.ubidatos);
        catdatos = (EditText) findViewById(R.id.categoriadatos);
        datos = (EditText) findViewById(R.id.datos);
        accion = (Spinner) findViewById(R.id.acciones);
        ciud = (EditText) findViewById(R.id.ciud);
        motivo = (EditText) findViewById(R.id.motivo);
        duracion = (EditText) findViewById(R.id.duracion);
        condiciones = (EditText) findViewById(R.id.cond);
        motivo = (EditText) findViewById(R.id.motivo);

        atras = (Button) findViewById(R.id.atras);

        atras.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v){
                Intent intent2 = new Intent(SolicitudActivity.this, AgenteActivity.class);
                intent2.putExtra("login", login);
                startActivity(intent2);
            }
        });

        ArrayList<String> actions = new ArrayList<>();
        actions.add("Acceso");
        actions.add("Lectura");
        actions.add("Modificación");
        actions.add("Envío");

        ArrayAdapter adaptador = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, actions);
        accion.setAdapter(adaptador);

        accion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                acselec = (String) accion.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void crearsol(View v) {

        final String URL = "http://192.168.1.54:8080/TFGREST/agente/solicitud/" + login;
        final ProgressDialog dlg = ProgressDialog.show(this,
                "Creando solicitud de consentimiento", "Por favor, espere...", true);

        /*
        Identifier id = new Identifier();
        id.setId(solicitante.getText().toString());
        id.setSystemElement(new UriType("http://localhost:8080/TFGREST/consentimiento/" + login));
        id.setValue(contra);
        consentimiento.addIdentifier(id);

        Reference refprac = new Reference();
        refprac.setReference("http://hapi.fhir.org/Practitioner");
        refprac.setType("Practitioner");
        refprac.setIdentifier(new Identifier().setValue(solicitante.getText().toString()));
        consentimiento.addPerformer(refprac);

         */

        Reference refubi = new Reference();
        refubi.setReference("http://hapi.fhir.org/Organization");
        refubi.setType("Organization");
        refubi.setIdentifier(new Identifier().setValue(login));
        refubi.setDisplay(ubidatos.getText().toString());
        consentimiento.addOrganization(refubi);

        consentimiento.addCategory().setText(catdatos.getText().toString());

        StringDt datosmanejar = new StringDt();
        datosmanejar.setValueAsString(datos.getText().toString());
        consentimiento.setDatos(datosmanejar);

        Reference refusudatos = new Reference();
        refusudatos.setReference("http://hapi.fhir.org/Practitioner");
        refusudatos.setType("Practitioner");
        refusudatos.setIdentifier(new Identifier().setValue(usudatos.getText().toString()));
        Consent.provisionActorComponent actor = new Consent.provisionActorComponent();
        actor.setReference(refusudatos);


        Consent.provisionComponent provision = new Consent.provisionComponent();
        provision.addActor(actor);

        CodeableConcept codeaccion = new CodeableConcept();
        if(acselec == "Acceso"){
            Coding cod = new Coding();
            cod.setCode("access");
            codeaccion.addCoding(cod);
            provision.addAction(codeaccion);
        } else {
            if(acselec == "Lectura"){
                Coding cod = new Coding();
                cod.setCode("use");
                codeaccion.addCoding(cod);
                provision.addAction(codeaccion);
            } else {
                if (acselec == "Modificación") {
                    Coding cod = new Coding();
                    cod.setCode("correct");
                    codeaccion.addCoding(cod);
                    provision.addAction(codeaccion);
                } else {
                    if (acselec == "Envío") {
                        Coding cod = new Coding();
                        cod.setCode("disclose");
                        codeaccion.addCoding(cod);
                        provision.addAction(codeaccion);
                    }
                }
            }
        }

        consentimiento.setProvision(provision);

        if (ciud.getText().toString().isEmpty()) {
            consentimiento.setPatient(new Reference("todos"));
        } else {
            Reference referencia = new Reference();
            referencia.setReference("http://hapi.fhir.org/Patient");
            referencia.setType("Patient");
            referencia.setIdentifier(new Identifier().setValue(ciud.getText().toString()));
            consentimiento.setPatient(referencia);
        }

        consentimiento.setScope(new CodeableConcept().setText(motivo.getText().toString()));

        consentimiento.setDateTime(new Date());
        StringDt dur = new StringDt();
        dur.setValueAsString(duracion.getText().toString());
        consentimiento.setDuracion(dur);

        StringDt cond = new StringDt();
        cond.setValueAsString(condiciones.getText().toString());
        consentimiento.setCond(cond);

        consentimiento.setStatus(Consent.ConsentState.DRAFT);

        consentimiento.setAviso(new BooleanDt(Boolean.TRUE));

        try{
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(consentimiento)),
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    dlg.dismiss();
                    try {

                        code = response.getJSONObject("codigo");
                        Integer cod = code.getInt("valueInteger");

                        if (cod == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Error al crear los consentimientos para todos los ciudadanos",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (cod == 400) {
                                Toast.makeText(getApplicationContext(),
                                        "Ciudadano elegido inexistente", Toast.LENGTH_SHORT).show();
                            } else {
                                if (cod == 100) {
                                    Toast.makeText(getApplicationContext(),
                                            "Ciudadano elegido no registrado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Consentimiento/s creado correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SolicitudActivity.this, AgenteActivity.class);
                                    intent.putExtra("login", login);
                                    startActivity(intent);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    VolleyLog.v("Response:%n %s", response);
                }}, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dlg.dismiss();
                    VolleyLog.e("Error: ", error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 5,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // add the request object to the queue to be executed
            Aplicacion.getInstance().getRequestQueue().add(request);
        } catch (JSONException exc){
            Toast.makeText(getApplicationContext(),"Error en objeto JSON de Practicante", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick (View view) {

        switch (view.getId()) {

            case R.id.buttontodos:
                layoutciud.setVisibility(View.GONE);
                break;

            case R.id.buttonuno:
                layoutciud.setVisibility(View.VISIBLE);
                break;
        }
    }
}
