package com.google.cloudbowl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.Random;


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
        return "Let the battle begin! 1.1";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public String index(ArenaUpdate arenaUpdate) {
    
        List<Integer> position = getMyPosition(arenaUpdate);
        
        System.out.println("pos x: " + position.get(0) + " y: " + position.get(1));

        // jeśli jestem uderzony - uciekaj
        if(wasIHit(arenaUpdate)){

            System.out.println("Musze uciekac");
            if(canGoForward(arenaUpdate)) {
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
            System.out.println("Ide do przodu, bo ktos jest o jedno pole ode mnie!");
            return "F";
        }

        //jesli ktos w zasiegu ale w innym kierunku
        if(someoneOnNorth(arenaUpdate)) { 
            System.out.println("Ktos na polnocy, obracam sie");
            return "N";
        }
        if(someoneOnSouth(arenaUpdate)) { 
            System.out.println("Ktos na poludniu, obracam sie");
            return "S";
        }
        if(someoneOnWest(arenaUpdate)) { 
            System.out.println("Ktos na zachodzie, obracam sie");
            return "W";
        }
        if(someoneOnEast(arenaUpdate)) { 
            System.out.println("Ktos na wschodzie, obracam sie");
            return "E";
        }

        if(canGoForward(arenaUpdate)) {
            System.out.println("Brak pomyslu, ide do przodu");
            return "F";
        }

        return leftOrRight();
    }

    private boolean someoneOnEast(ArenaUpdate arenaUpdate) {
        PlayerState myPlayerState = getMyState(arenaUpdate);

        return isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) 
            || isPlayerOn(arenaUpdate, myPlayerState.x+2, myPlayerState.y) 
            || isPlayerOn(arenaUpdate, myPlayerState.x+3, myPlayerState.y);

    }

    private boolean someoneOnWest(ArenaUpdate arenaUpdate) {
        PlayerState myPlayerState = getMyState(arenaUpdate);
        return isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) 
            || isPlayerOn(arenaUpdate, myPlayerState.x-2, myPlayerState.y) 
            || isPlayerOn(arenaUpdate, myPlayerState.x-3, myPlayerState.y);
            
        
    }

    private boolean someoneOnSouth(ArenaUpdate arenaUpdate) {
        PlayerState myPlayerState = getMyState(arenaUpdate);

        return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) 
            || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+2) 
            || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+3);
    }

    private boolean someoneOnNorth(ArenaUpdate arenaUpdate) {
        PlayerState myPlayerState = getMyState(arenaUpdate);
        return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) 
            || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-2) 
            || isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-3);
    }

    private String leftOrRight() {
        String[] commands = new String[]{"R", "L"};
        String command = commands[new Random().nextInt(2)];
        System.out.println("Losowy kierunek: " + command);
        return command;
    }

    private boolean someoneOnTarget(ArenaUpdate arenaUpdate) {
        PlayerState myPlayerState = getMyState(arenaUpdate);

        switch(myPlayerState.direction) { 
            case "N":
                return someoneOnNorth(arenaUpdate);
            case "S":
                return someoneOnSouth(arenaUpdate);
            case "W":
                return someoneOnWest(arenaUpdate);
            case "E":
                return someoneOnEast(arenaUpdate);
        }

        return false;
    }


    private boolean canGoForward(ArenaUpdate arenaUpdate) { 
        PlayerState myPlayerState = getMyState(arenaUpdate);

        switch(myPlayerState.direction) { 
            case "N":
                return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) || myPlayerState.y-1 < 0);
            case "S":
                return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) || myPlayerState.y+1 > arenaUpdate.arena.dims.get(1));
            case "W":
                return !(isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) || myPlayerState.x-1 < 0);
            case "E":
                return !(isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || myPlayerState.x-1 > arenaUpdate.arena.dims.get(0));
        }

        return true;
    }
    private boolean someoneOnOneStepForward(ArenaUpdate arenaUpdate) { 
        PlayerState myPlayerState = getMyState(arenaUpdate);

        switch(myPlayerState.direction) { 
            case "N":
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-4);
            case "S":
                return isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+4);
            case "W":
                return isPlayerOn(arenaUpdate, myPlayerState.x-4, myPlayerState.y);
            case "E":
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
        return arenaUpdate.arena.state.values().stream()
            .filter(p -> p.x == x && p.y == y)
            .findFirst()
            .isPresent();
    }

    private boolean wasIHit(ArenaUpdate arenaUpdate) { 
        PlayerState playerState = getMyState(arenaUpdate);
        return playerState.wasHit;
    }
}
