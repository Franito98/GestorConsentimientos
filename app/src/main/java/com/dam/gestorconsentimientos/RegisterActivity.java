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
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.UriType;
import org.json.JSONException;
import org.json.JSONObject;

import ca.uhn.fhir.model.primitive.IntegerDt;
import ca.uhn.fhir.model.primitive.StringDt;

import static com.dam.gestorconsentimientos.Aplicacion.ctx;

public class RegisterActivity extends AppCompatActivity {

    private Button atras;
    EditText nombre, user, password, dni, tarjsanitaria, tlf, hospital, departamento, codigo;
    LinearLayout layoutdni, layouttarj, layouttlf, layouthosp, layoutdepart, layoutcod;

    Paciente paciente = new Paciente();
    Practicante practicante = new Practicante();
    Organization hosp = new Organization();
    PractitionerRole rol = new PractitionerRole();

    Boolean acceso = false;

    String tipo = "ciud";

    JSONObject code;
    JSONObject code2;
    JSONObject code3;

    JSONObject objeto;

    ProgressDialog dlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nombre = (EditText) findViewById(R.id.nombre);
        user = (EditText) findViewById(R.id.usuario);
        password = (EditText) findViewById(R.id.password);
        dni = (EditText) findViewById(R.id.dni);
        tarjsanitaria = (EditText) findViewById(R.id.tarjsanitaria);
        tlf = (EditText) findViewById(R.id.telefono);
        hospital = (EditText) findViewById(R.id.hospital);
        departamento = (EditText) findViewById(R.id.departamento);
        codigo = (EditText) findViewById(R.id.codigo);

        layoutdni = (LinearLayout) findViewById(R.id.layoutdni);
        layouttarj = (LinearLayout) findViewById(R.id.layouttarjsanitaria);
        layouttlf = (LinearLayout) findViewById(R.id.layouttelefono);
        layouthosp = (LinearLayout) findViewById(R.id.layouthospital);
        layoutdepart = (LinearLayout) findViewById(R.id.layoutdepart);
        layoutcod = (LinearLayout) findViewById(R.id.layoutcod);

        atras = (Button) findViewById(R.id.atras);

        atras.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    public void register(View v) {
        final String URL1 = "http://192.168.1.108:8080/TFGREST/" + tipo + "/reg/" + password.getText().toString();
        dlg = ProgressDialog.show(
                this,
                "Actualizando datos",
                "Por favor, espere...", true);

        if (tipo == "ciud") {

            HumanName name = new HumanName();
            name.setText(nombre.getText().toString());
            paciente.addName(name);

            StringDt usu = new StringDt();
            usu.setValueAsString(user.getText().toString());
            paciente.setUsu(usu);

            Identifier id = new Identifier();
            id.setId(password.getText().toString());
            id.setSystemElement(new UriType("http://localhost:8080/TFGREST/ciud/" + password.getText().toString()));
            paciente.addIdentifier(id);

            IntegerDt tarj = new IntegerDt();
            tarj.setValue(Integer.parseInt(tarjsanitaria.getText().toString()));
            paciente.setTarjsanitaria(tarj);

            ContactPoint com = new ContactPoint();
            com.setSystem(ContactPoint.ContactPointSystem.PHONE);
            com.setValue(tlf.getText().toString());
            com.setUse(ContactPoint.ContactPointUse.MOBILE);
            paciente.addTelecom(com);

            try{
                objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(paciente));
            } catch (JSONException exc){
                Toast.makeText(getApplicationContext(),"Error en objeto JSON de Paciente", Toast.LENGTH_SHORT).show();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL1, objeto,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            dlg.dismiss();
                            try {

                                code = response.getJSONObject("codigo");
                                Integer cod = code.getInt("valueInteger");

                                if (cod == 400) {
                                    Toast.makeText(getApplicationContext(),
                                            "Persona inexistente", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (cod == 200) {
                                        Toast.makeText(getApplicationContext(),
                                                "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (cod == 500){
                                                Toast.makeText(getApplicationContext(),
                                                        "Número de tarjeta sanitaria incorrecto", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (cod == 300) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                                acceso = true;
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Error al registrarse", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }

                                if (acceso) {
                                    acceso = false;
                                    Intent intent = new Intent(RegisterActivity.this, CiudActivity.class);
                                    intent.putExtra("dni", password.getText().toString());
                                    startActivity(intent);
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
                    Toast.makeText(getApplicationContext(), "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Aplicacion.getInstance().getRequestQueue().add(request);

        } else {

            Identifier id = new Identifier();
            id.setId(dni.getText().toString());
            id.setSystemElement(new UriType("http://localhost:8080/TFGREST/agente/" + dni.getText().toString()));
            practicante.addIdentifier(id);

            HumanName name = new HumanName();
            name.setText(nombre.getText().toString());
            practicante.addName(name);

            StringDt contrasena = new StringDt();
            contrasena.setValueAsString(password.getText().toString());
            practicante.setContra(contrasena);

            StringDt usu = new StringDt();
            usu.setValueAsString(user.getText().toString());
            practicante.setUsu(usu);

            StringDt cod = new StringDt();
            cod.setValueAsString(codigo.getText().toString());
            practicante.setCodigo(cod);

            try {
                objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(practicante));
            } catch (JSONException exc) {
                Toast.makeText(getApplicationContext(), "Error en objeto JSON de Practicante", Toast.LENGTH_SHORT).show();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL1, objeto,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                code = response.getJSONObject("codigo");
                                Integer cod = code.getInt("valueInteger");

                                if (cod == 400) {
                                    dlg.dismiss();
                                    Toast.makeText(getApplicationContext(),
                                            "Persona inexistente", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (cod == 200) {
                                        dlg.dismiss();
                                        Toast.makeText(getApplicationContext(),
                                                "Usuario ya registrado", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (cod == 500) {
                                            dlg.dismiss();
                                            Toast.makeText(getApplicationContext(),
                                                    "Código de agente incorrecto", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (cod == 300) {
                                                registerhospital();
                                            } else {
                                                dlg.dismiss();
                                                Toast.makeText(getApplicationContext(),
                                                        "Error al registrarse", Toast.LENGTH_SHORT).show();
                                            }
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
                    dlg.dismiss();
                    VolleyLog.e("Error: ", error.getMessage());
                    Toast.makeText(getApplicationContext(), "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
                }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Aplicacion.getInstance().getRequestQueue().add(request);
        }
    }

    public void registerhospital() {
        final String URL2 = "http://192.168.1.108:8080/TFGREST/" + tipo + "/reg/hospital/" + password.getText().toString();

        Identifier idhosp = new Identifier();
        idhosp.setId(dni.getText().toString());
        idhosp.setSystemElement(new UriType("http://localhost:8080/TFGREST/hospital/" + dni.getText().toString()));
        hosp.addIdentifier(idhosp);

        hosp.setName(hospital.getText().toString());

        try {
            objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(hosp));
        } catch (JSONException exc) {
            Toast.makeText(getApplicationContext(), "Error en objeto JSON de Hospital", Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.PUT, URL2, objeto,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            code2 = response.getJSONObject("codigo");
                            Integer cod = code2.getInt("valueInteger");

                            if (cod == 600) {
                                dlg.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Hospital incorrecto para contraseña y código",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if (cod == 200) {
                                    registerdepart();
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
                Toast.makeText(getApplicationContext(), "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
            }
        });
        request2.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Aplicacion.getInstance().getRequestQueue().add(request2);
    }

    public void registerdepart() {
        final String URL3 = "http://192.168.1.108:8080/TFGREST/" + tipo + "/reg/depart/" + password.getText().toString();

        Identifier idrol = new Identifier();
        idrol.setId(dni.getText().toString());
        idrol.setSystemElement(new UriType("http://localhost:8080/TFGREST/pracrol/" + dni.getText().toString()));
        rol.addIdentifier(idrol);

        CodeableConcept coderol = new CodeableConcept();
        coderol.setText(departamento.getText().toString());
        rol.addCode(coderol);

        Reference ref1 = new Reference();
        ref1.setReference("http://hapi.fhir.org/Organization");
        ref1.setType("Organization");
        ref1.setIdentifier(new Identifier().setValue((dni.getText().toString())));
        rol.setOrganization(ref1);

        Reference ref2 = new Reference();
        ref2.setReference("http://hapi.fhir.org/Practitioner");
        ref2.setType("Practitioner");
        ref2.setIdentifier(new Identifier().setValue((dni.getText().toString())));
        rol.setPractitioner(ref2);

        try {
            objeto = new JSONObject(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(rol));
        } catch (JSONException exc) {
            Toast.makeText(getApplicationContext(), "Error en objeto JSON de Departamento", Toast.LENGTH_SHORT).show();
        }

        JsonObjectRequest request3 = new JsonObjectRequest(Request.Method.PUT, URL3, objeto,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dlg.dismiss();
                        try {

                            code3 = response.getJSONObject("codigo");
                            Integer cod = code3.getInt("valueInteger");

                            if (cod == 600) {
                                Toast.makeText(getApplicationContext(),
                                        "Departamento incorrecto para contraseña y código",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if (cod == 200) {
                                    Toast.makeText(getApplicationContext(),
                                            "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, AgenteActivity.class);
                                    intent.putExtra("login", user.getText().toString());
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
                Toast.makeText(getApplicationContext(), "No se ha recibido respuesta", Toast.LENGTH_SHORT).show();
            }
        });
        request3.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 3,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Aplicacion.getInstance().getRequestQueue().add(request3);
    }

    public void onClick (View view) {

        switch (view.getId()) {

            case R.id.buttonciud:
                password.setHint("DNI");
                layouttarj.setVisibility(View.VISIBLE);
                layouttlf.setVisibility(View.VISIBLE);
                layoutdni.setVisibility(View.GONE);
                layouthosp.setVisibility(View.GONE);
                layoutdepart.setVisibility(View.GONE);
                layoutcod.setVisibility(View.GONE);
                tipo = "ciud";
                break;

            case R.id.buttonagente:
                password.setHint("Contraseña");
                layouttarj.setVisibility(View.GONE);
                layouttlf.setVisibility(View.GONE);
                layoutdni.setVisibility(View.VISIBLE);
                layouthosp.setVisibility(View.VISIBLE);
                layoutdepart.setVisibility(View.VISIBLE);
                layoutcod.setVisibility(View.VISIBLE);
                tipo = "agente";
                break;
        }
    }
}