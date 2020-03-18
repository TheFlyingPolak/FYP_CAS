package com.gajewski.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.bind.annotation.*;
import com.gajewski.structures.AgentResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Controller {
    private static final String PATH = "agents/";
    @PostMapping("/agents")
    public int postAgentData(@RequestBody AgentResponse response) throws IOException{
        Gson gson = new GsonBuilder().create();
        int id = findFreeId();
        String path = PATH + id + ".json";
        Writer writer = new FileWriter(path);
        gson.toJson(response, writer);
        writer.close();
        return id;
    }

    @PutMapping("/agents/{id}")
    public AgentResponse putAgentData(@PathVariable(value = "id") int id,
                                          @RequestBody AgentResponse response)throws Exception{
        if (!idExists(id))
            throw new FileNotFoundException();
        Gson gson = new GsonBuilder().create();
        String path = PATH + id + ".json";
        Writer writer = new FileWriter(path);
        gson.toJson(response, writer);
        writer.close();
        return response;
    }

    @GetMapping("/agents")
    public List<AgentResponse> getAllAgentData(){
        List<AgentResponse> list = new ArrayList<>();
        File file = new File(PATH);
        String[] paths = file.list();
        assert paths != null;
        Gson gson = new Gson();
        for (String pathname : paths){
            if (pathname.endsWith(".json")) {
                try {
                    list.add(gson.fromJson(new FileReader(PATH + pathname), AgentResponse.class));
                } catch (FileNotFoundException e) {
                    System.out.println("File " + pathname + " not found");
                }
            }
        }
        return list;
    }

    public int findFreeId(){
        int id = 0;
        while (new File(PATH + id + ".json").exists())
            id++;
        return id;
    }

    public boolean idExists(int id){
        return new File(PATH + id + ".json").exists();
    }
}
