package com.gajewski.admin;

import com.gajewski.structures.AgentResponse;
import com.gajewski.structures.Conflict;
import com.gajewski.structures.Package;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConflictFinder {
    private String path;

    public ConflictFinder(String path){
        this.path = path;
    }

    public Map<String, List<Long>> findConflicts(){
        Map<String, Map<String, Value>> packagesMap = readPackages();
        return getConflicts(packagesMap);
    }

    private Map<String, Map<String, Value>> readPackages() {
        Map<String, Map<String, Value>> map = new HashMap<>();
        Gson gson = new Gson();
        File file = new File(path);
        String[] paths = file.list();
        assert paths != null;
        for (String pathname : paths){
            if (pathname.endsWith(".json")){
                try {
                    // TODO optimise this block of code
                    Reader reader = Files.newBufferedReader(Paths.get(path + pathname));
                    AgentResponse agent = gson.fromJson(reader, AgentResponse.class);
                    String os = agent.getOsName();
                    if (!map.containsKey(os)){
                        map.put(os, new HashMap<>());
                    }
                    for (Package p : agent.getPackages()){
                        if (!map.get(os).containsKey(p.getName())){
                            map.get(os).put(p.getName(), new Value(p.getVersion(), agent.getId()));
                        }
                        else{
                            map.get(os).get(p.getName()).installedOn.add(agent.getId());
                            if (!map.get(os).get(p.getName()).packageVersion.equals(p.getVersion())){
                                map.get(os).get(p.getName()).conflict = true;
                            }
                        }
                    }
                }
                catch (IOException e){
                    System.out.println("Cannot read file " + pathname);
                }
            }
        }
        return map;
    }

    private Map<String, List<Long>> getConflicts(Map<String, Map<String, Value>> packages){
        Map<String, List<Long>> conflicts = new HashMap<>();
        for (Map.Entry<String, Map<String, Value>> i: packages.entrySet()){
            for (Map.Entry<String, Value> j: i.getValue().entrySet()){
                if (j.getValue().conflict){
                    conflicts.putIfAbsent(j.getKey(), j.getValue().installedOn);
                }
            }
        }
        return conflicts;
    }

    private class Value{
        String packageVersion;
        List<Long> installedOn;
        boolean conflict = false;

        public Value(String packageVersion, long agentId){
            this.packageVersion = packageVersion;
            installedOn = new ArrayList<>();
            installedOn.add(agentId);
        }
    }
}


