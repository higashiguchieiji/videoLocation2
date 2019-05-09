package com.lab.locapp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LocationApp extends RealmObject {
    @PrimaryKey
    private long id;
    private String Ido;
    private String Keido;
    private String Koudo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdo() {
        return Ido;
    }

    public void setIdo(String Ido) {
        this.Ido = Ido;
    }

    public String getKeido() {
        return Keido;
    }

    public void setKeido(String Keido) {
        this.Keido = Keido;
    }
/*
    public String getKoudo() {
        return Koudo;
    }

    public void setKoudo(String Koudo) {
        this.Koudo = Koudo;
    }
*/
}