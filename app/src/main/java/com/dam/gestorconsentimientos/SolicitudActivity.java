package com.dam.gestorconsentimientos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import org.hl7.fhir.r4.model.UriType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;

import static com.dam.gestorconsentimientos.Aplicacion.ctx;

public class SolicitudActivity extends AppCompatActivity {

    LinearLayout layoutciud;
    EditText solicitante, usudatos, ubidatos, catdatos, datos, accion, ciud, motivo, duracion, condiciones;

    Consen consentimiento = new Consen();

    private Button atras;

    JSONObject code;

    Intent intent;
    Bundle extra;
    String contra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud);

        intent = getIntent();
        extra = intent.getExtras();
        contra = extra.getString("contra");

        layoutciud = (LinearLayout) findViewById(R.id.layoutciud);

        solicitante = (EditText) findViewById(R.id.solicitante);
        usudatos = (EditText) findViewById(R.id.usuariodatos);
        ubidatos = (EditText) findViewById(R.id.ubidatos);
        catdatos = (EditText) findViewById(R.id.categoriadatos);
        datos = (EditText) findViewById(R.id.datos);
        accion = (EditText) findViewById(R.id.accion);
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
                intent2.putExtra("contra", contra);
                startActivity(intent2);
            }
        });
    }

    public void crearsol(View v) {

        final String URL = "http://192.168.1.108:8080/TFGREST/agente/solicitud/" + contra;
        final ProgressDialog dlg = ProgressDialog.show(this,
                "Creando solicitud de consentimiento", "Por favor, espere...", true);

        Identifier id = new Identifier();
        id.setId(solicitante.getText().toString());
        id.setSystemElement(new UriType("http://localhost:8080/TFGREST/consentimiento/" + contra));
        id.setValue(contra);
        consentimiento.addIdentifier(id);

        StringDt usuariodatos = new StringDt();
        usuariodatos.setValueAsString(usudatos.getText().toString());
        consentimiento.setUsudatos(usuariodatos);

        StringDt hospital = new StringDt();
        hospital.setValueAsString(ubidatos.getText().toString());
        consentimiento.setUbidatos(hospital);

        consentimiento.addCategory().setText(catdatos.getText().toString());

        StringDt datosmanejar = new StringDt();
        datosmanejar.setValueAsString(datos.getText().toString());
        consentimiento.setDatos(datosmanejar);

        StringDt accionrealizar = new StringDt();
        accionrealizar.setValueAsString(accion.getText().toString());
        consentimiento.setAccion(accionrealizar);

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

                        if (cod == 600) {
                            Toast.makeText(getApplicationContext(),
                                    "Nombre del solicitante incorrecto", Toast.LENGTH_SHORT).show();
                        } else {
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
                                        intent.putExtra("contra", contra);
                                        startActivity(intent);
                                    }
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
