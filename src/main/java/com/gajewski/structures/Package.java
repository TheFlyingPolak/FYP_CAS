package com.gajewski.structures;

import lombok.Data;

@Data
public class Package {
    private String name;
    private String version;
    private String architecture;

    public Package() {}
}
