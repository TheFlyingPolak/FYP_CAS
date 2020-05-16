package com.gajewski.admin;

import com.gajewski.structures.Command;
import com.gajewski.structures.Conflict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gajewski.structures.AgentResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;

@RestController
public class DataController {
    private static final int COMMAND_PORT = 8081;
    private final RestTemplate restTemplate = new RestTemplateBuilder().build();

    @Autowired
    private DataRepository repository;

    @PostMapping("/agents")
    public long postAgentData(@RequestBody AgentResponse response){
        long id = findFreeId();
        response.setAgentId(id);
        storeResponse(response);
        return id;
    }

    @PutMapping("/agents/{id}")
    public AgentResponse putAgentData(@PathVariable(value = "id") int id,
                                          @RequestBody AgentResponse response)throws Exception{
        if (!idExists(id))
            throw new FileNotFoundException("ID " + id + " not stored");
        storeResponse(response);
        return response;
    }

    @GetMapping("/agents")
    public List<AgentResponse> getAllAgentData(){
        return repository.findAll();
    }

    @GetMapping("/conflicts")
    public List<Conflict> getConflicts(){
        ConflictFinder finder = new ConflictFinder(repository);
        return finder.findConflicts();
    }

    @PostMapping("/command/{id}")
    public ResponseEntity<String> sendCommandToOne(@PathVariable(value = "id") long id,
                                                   @RequestBody Command command){
        AgentResponse agent = repository.findByAgentId(id);
        if (agent == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        sendCommand(agent.getHostname(), command);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/command")
    public ResponseEntity<String> sendCommandToAll(@RequestBody Command command){
        List<AgentResponse> agents = repository.findAll();
        for (AgentResponse agent : agents){
            sendCommand(agent.getHostname(), command);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public void sendCommand(String host, Command command) throws HttpClientErrorException {
        String url = "http://" + host + ":" + COMMAND_PORT + "/command";
        ResponseEntity<Command> response = restTemplate.postForEntity(url, command, Command.class);
        if (response.getStatusCode() != HttpStatus.OK)
            throw new HttpClientErrorException(response.getStatusCode());
    }

    public long findFreeId(){
        long id = 0;
        while(repository.findByAgentId(id) != null)
            id++;
        return id;
    }

    public boolean idExists(long id){
        return repository.findByAgentId(id) != null;
    }

    private void storeResponse(AgentResponse response){
        if (repository.existsById(response.getAgentId()))
            repository.deleteById(response.getAgentId());
        repository.save(response);
    }
}
