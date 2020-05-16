package com.gajewski.structures;

import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class AgentResponse {
    @Id
    private long agentId;
    private String timestamp;
    private String hostname;
    private String osName;
    private String osVersion;
    private List<Package> packages;

    public AgentResponse() {}
}
