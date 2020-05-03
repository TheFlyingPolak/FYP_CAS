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

    public List<Conflict> findConflicts(){
        Map<String, Map<Key, Value>> packagesMap = readPackages();
        return getConflicts(packagesMap);
    }

    // outer map key: OS name
    // inner map key: package name, package version
    // inner map value: list of agents, conflict T/F
    private Map<String, Map<Key, Value>> readPackages() {
        Map<String, Map<Key, Value>> map = new HashMap<>();
        Gson gson = new Gson();
        File file = new File(path);
        String[] paths = file.list();
        assert paths != null;
        for (String pathname : paths){
            if (pathname.endsWith(".json")){
                try {
                    Reader reader = Files.newBufferedReader(Paths.get(path + pathname));
                    AgentResponse agent = gson.fromJson(reader, AgentResponse.class);
                    String os = agent.getOsName();
                    if (!map.containsKey(os)){
                        map.put(os, new HashMap<>());
                    }
                    for (Package p : agent.getPackages()){
                        Key key = new Key(p.getName(), p.getVersion());
                        if (!map.get(os).containsKey(key)){
                            map.get(os).put(key, new Value(os, p.getVersion(), agent.getId()));
                        }
                        else{
                            map.get(os).get(key).installedOn.add(agent.getId());
                            System.out.println("!");
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

    // outer map key: OS name
    // inner map key: package name, package version
    // inner map value: list of agents, conflict T/F
    private List<Conflict> getConflicts(Map<String, Map<Key, Value>> packages){
        List<Conflict> conflicts = new LinkedList<>();
        for (Map.Entry<String, Map<Key, Value>> i: packages.entrySet()){
            Map<String, List<String>> versionMap = new HashMap<>();
            for (Map.Entry<Key, Value> j: i.getValue().entrySet()){
                Key key = j.getKey();
                Value value = j.getValue();
                if (versionMap.containsKey(key.packageName)){
                    if (!versionMap.get(key.packageName).contains(key.packageVersion)){
                        versionMap.get(key.packageName).add(key.packageVersion);
                    }
                }
                else{
                    versionMap.put(key.packageName, new LinkedList<>());
                    versionMap.get(key.packageName).add(key.packageVersion);
                }
            }
            for (Map.Entry<Key, Value> j: i.getValue().entrySet()){
                if (versionMap.get(j.getKey().packageName).size() > 1){
                    Conflict conflict = new Conflict(j.getKey().packageName, j.getValue().os, j.getKey().packageVersion);
                    conflict.setAgentId(j.getValue().installedOn);
                    conflicts.add(conflict);
                }
            }
        }
        return conflicts;
    }

    private static class Value{
        String os;
        String packageVersion;
        List<Long> installedOn;
        boolean conflict = false;

        public Value(String os, String packageVersion, long agentId){
            this.os = os;
            this.packageVersion = packageVersion;
            installedOn = new ArrayList<>();
            installedOn.add(agentId);
        }

        public String toString(){
            return "[" + os + ", " + packageVersion + ", " + installedOn + ", " + conflict + "]";
        }
    }

    private static class Key{
        String packageName;
        String packageVersion;

        public Key(String packageName, String packageVersion){
            this.packageName = packageName;
            this.packageVersion = packageVersion;
        }

        public String toString(){
            return "[" + packageName + ", " + packageVersion + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(packageName, key.packageName) &&
                    Objects.equals(packageVersion, key.packageVersion);
        }

        public int hashCode(){
            String hash = packageName + packageVersion;
            return hash.hashCode();
        }
    }
}


