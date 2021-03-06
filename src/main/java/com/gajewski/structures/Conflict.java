package com.gajewski.structures;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Conflict {
    private String packageName;
    private String packageOs;
    private String packageVersion;
    private List<Long> agentId;

    public Conflict(String packageName, String packageOs, String packageVersion){
        this.packageName = packageName;
        this.packageOs = packageOs;
        this.packageVersion = packageVersion;
        this.agentId = new LinkedList<>();
    }
}
