package com.google.cloudbowl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.checkerframework.checker.units.qual.A;

import java.util.List;
import java.util.Map;
import java.util.Random;


import java.util.Optional;

@Path("/{s:.*}")
public class App {

    static class Self {
        public String href;
    }

    static class Links {
        public Self self;
    }

    static class PlayerState {
        public Integer x;
        public Integer y;
        public String direction;
        public Boolean wasHit;
        public Integer score;
    }

    static class Arena {
        public List<Integer> dims;
        public Map<String, PlayerState> state;
    }

    static class ArenaUpdate {
        public Links _links;
        public Arena arena;
    }

    @GET
    public String index() {
        return "Let the battle begin!";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String index(ArenaUpdate arenaUpdate) {
        String[] commands = new String[]{"F", "R", "L"};
        int i = new Random().nextInt(3);
        
        List<Integer> position = getMyPosition(arenaUpdate);
        
        System.out.println("pos x: " + position.get(0) + " y: " + position.get(1));

        // jeśli jestem uderzony - uciekaj
        if(wasIHit(arenaUpdate)){

            System.out.println("Musze uciekac");
            if(!canGoForward(arenaUpdate)) {

                System.out.println("Uciekam do przodu!");
                return "F";
            }

            System.out.println("Uciekam w prawo!");
            return "R";
        }

        // jeśli ktos na celowniku ponizej 3 strzelaj
        if(someoneOnTarget(arenaUpdate)){
            System.out.println("Rzucam");
            return "T";
        }
        //jeśli ktoś w zasięgu jednego kroku idź
        if(someoneOnOneStepForward(arenaUpdate)){
            System.out.println("Idę do przodu, bo ktoś tam jest!");
            return "F";
        }

        String command = commands[i];
        System.out.println("Komenda " + command);
        return command;
    }

    private boolean someoneOnTarget(ArenaUpdate arenaUpdate) { 
        PlayerState myPlayerState = getMyState(arenaUpdate);

        switch(myPlayerState.direction) { 
            case "N":
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-2) || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-3);
            case "S":
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+2) || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+3);
            case "W":
                return isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) || isPlayerOn(arenaUpdate, myPlayerState.x-2, myPlayerState.y) || isPlayerOn(arenaUpdate, myPlayerState.x-3, myPlayerState.y);
            case "E":
                return isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || isPlayerOn(arenaUpdate, myPlayerState.x+2, myPlayerState.y) || isPlayerOn(arenaUpdate, myPlayerState.x+3, myPlayerState.y);
        }

        return false;
    }


    private boolean canGoForward(ArenaUpdate arenaUpdate) { 
        PlayerState myPlayerState = getMyState(arenaUpdate);

        switch(myPlayerState.direction) { 
            case "N":
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) || myPlayerState.y-1 < 0;
            case "S":
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) || myPlayerState.y+1 > arenaUpdate.arena.dims.get(1);
            case "W":
                return isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) || myPlayerState.x-1 < 0;
            case "E":
                return isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || myPlayerState.x-1 > arenaUpdate.arena.dims.get(0);
        }

        return false;
    }
    private boolean someoneOnOneStepForward(ArenaUpdate arenaUpdate) { 
        PlayerState myPlayerState = getMyState(arenaUpdate);

        switch(myPlayerState.direction) { 
            case "N":
                System.out.println("Jest ktoś na północy");
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-4);
            case "S":
                System.out.println("Jest ktoś na południu");
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+4);
            case "W":
                System.out.println("Jest ktoś na zachodzie");
                return isPlayerOn(arenaUpdate, myPlayerState.x-4, myPlayerState.y);
            case "E":
                System.out.println("Jest ktoś na wschodzie");
                return isPlayerOn(arenaUpdate, myPlayerState.x+4, myPlayerState.y);
        }

        return false;
    }

    private List<Integer> getMyPosition(ArenaUpdate arenaUpdate) {
        PlayerState playerState = getMyState(arenaUpdate);
        return List.of(playerState.x, playerState.y);
    }

    private PlayerState getMyState(ArenaUpdate arenaUpdate) { 
        return arenaUpdate.arena.state.get(arenaUpdate._links.self.href);
    }

    private boolean isPlayerOn(ArenaUpdate arenaUpdate, int x, int y) { 
        Optional<PlayerState> ps = arenaUpdate.arena.state.values().stream()
            .filter(p -> p.x == x && p.y == y)
            .findFirst();
            ps.ifPresent(p -> System.out.println("Na polu x: " + p.x + " y: " + p.y + " jest: " + p.score));
        return ps.isPresent();
    }

    private boolean wasIHit(ArenaUpdate arenaUpdate) { 
        PlayerState playerState = getMyState(arenaUpdate);
        return playerState.wasHit;
    }
}
