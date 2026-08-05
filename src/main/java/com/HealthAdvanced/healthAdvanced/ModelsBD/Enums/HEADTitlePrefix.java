package com.HealthAdvanced.healthAdvanced.ModelsBD.Enums;

public enum HEADTitlePrefix {
    DR("Dr."),
    DRA("Dra."),
    ENF("Enf."),
    ENFA("Enfa."),
    CUID("Cuid."),
    TER("Ter."),
    NONE("");

    private final String text;
    HEADTitlePrefix(String text) { this.text = text; }
    public String text() { return text; }
}
