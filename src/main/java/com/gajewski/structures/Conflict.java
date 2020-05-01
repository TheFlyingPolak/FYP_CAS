package com.gajewski.structures;

import lombok.Data;

import java.util.List;

@Data
public class Conflict {
    private String packageName;
    private List<Long> agentId;
}
