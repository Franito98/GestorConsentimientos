package com.dam.gestorconsentimientos;

import org.hl7.fhir.r4.model.Consent;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.StringDt;

@ResourceDef(name = "Consent")
public class Consen extends Consent {

    public Consen() { super(); }

    private static final long serialVersionUID = 1L;

    /** * This is a basic extension, with a DataType value (in this case, StringDt) */
    //Atributo extendido Datos
    @Child(name = "datos")
    @Description(shortDefinition = "Indica qué datos se solicitan en el consentimiento")
    @Extension(url = "localhost:8080/extensión/consent/datos", isModifier = false, definedLocally = true)
    private StringDt datos;

    /** * This is a basic extension, with a DataType value (in this case, StringDt) */
    //Atributo extendido Acción
    @Child(name = "accion")
    @Description(shortDefinition = "Indica qué hacer con los datos")
    @Extension(url = "localhost:8080/extensión/consent/accion", isModifier = false, definedLocally = true)
    private StringDt accion;

    /** * This is a basic extension, with a DataType value (in this case, StringDt) */
    //Atributo extendido Duración
    @Child(name = "duracion")
    @Description(shortDefinition = "Indica cuánto tiempo estarán los datos disponibles")
    @Extension(url = "localhost:8080/extensión/consent/duracion", isModifier = false, definedLocally = true)
    private StringDt duracion;

    /** * This is a basic extension, with a DataType value (in this case, StringDt) */
    //Atributo extendido Condiciones
    @Child(name = "cond")
    @Description(shortDefinition = "Condiciones opcionales que el solicitante puede asociar a los datos ")
    @Extension(url = "localhost:8080/extensión/consent/cond", isModifier = false, definedLocally = true)
    private StringDt cond;

    /** * This is a basic extension, with a DataType value (in this case, StringDt) */
    //Atributo extendido Alerta
    @Child(name = "alerta")
    @Description(shortDefinition = "Indica si es un consentimiento nuevo o no para el ciudadano")
    @Extension(url = "localhost:8080/extensión/consent/alerta", isModifier = false, definedLocally = true)
    private BooleanDt alerta;

    public StringDt getDatos() {
        return datos;
    }

    public void setDatos(StringDt datos) {
        this.datos = datos;
    }

    public StringDt getAccion() {
        return accion;
    }

    public void setAccion(StringDt accion) {
        this.accion = accion;
    }

    public StringDt getDuracion() {
        return duracion;
    }

    public void setDuracion(StringDt duracion) {
        this.duracion = duracion;
    }

    public StringDt getCond() {
        return cond;
    }

    public void setCond(StringDt cond) {
        this.cond = cond;
    }

    public BooleanDt getAlerta() {
        return alerta;
    }

    public void setAlerta(BooleanDt alerta) {
        this.alerta = alerta;
    }
}
