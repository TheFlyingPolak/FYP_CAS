package com.gajewski.structures;

import java.util.List;
import lombok.Data;

@Data
public class AgentResponse {
    private long id;
    private String timestamp;
    private String hostname;
    private String osName;
    private String osVersion;
    private List<Package> packages;

    public AgentResponse() {}
}
