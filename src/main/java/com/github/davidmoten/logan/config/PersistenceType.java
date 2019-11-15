package com.github.davidmoten.logan.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum PersistenceType {
    MEMORY,H2,BPLUSTREE;
    
    public String value() {
        return name();
    }

    public static PersistenceType fromValue(String v) {
        return valueOf(v);
    }

}
