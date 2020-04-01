package com.gajewski.admin;

import com.gajewski.structures.Command;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gajewski.structures.AgentResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {
    private static final int COMMAND_PORT = 8081;
    private static final String PATH = "E:/admin/agents/";
    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

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

    @PostMapping("/command/{id}")
    public ResponseEntity<String> sendCommandToOne(@PathVariable(value = "id") int id,
                                                   @RequestBody Command command){
        try{
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(PATH + id + ".json"));
            AgentResponse agent = gson.fromJson(reader, AgentResponse.class);
            reader.close();
            sendCommand(agent.getHostname(), command);

            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (IOException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/command")
    public ResponseEntity<String> sendCommandToAll(@RequestBody Command command){
        //Map<String, String> response = new HashMap<>();
        try{
            Gson gson = new Gson();
            File file = new File(PATH);
            String[] paths = file.list();
            assert paths != null;
            for (String pathname : paths){
                if (pathname.endsWith(".json")){
                        Reader reader = Files.newBufferedReader(Paths.get(PATH + pathname));
                        AgentResponse agent = gson.fromJson(reader, AgentResponse.class);
                        sendCommand(agent.getHostname(), command);
                }
            }
        }
        catch (IOException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void sendCommand(String host, Command command) throws HttpClientErrorException {
        String url = "http://" + host + ":" + COMMAND_PORT + "/command";
        ResponseEntity<Command> response = restTemplate.postForEntity(url, command, Command.class);
        if (response.getStatusCode() != HttpStatus.OK)
            throw new HttpClientErrorException(response.getStatusCode());
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
