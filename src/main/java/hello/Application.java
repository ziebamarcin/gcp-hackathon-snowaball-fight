package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);
  
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

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }

  @GetMapping("/")
  public String index() {
    return "Let the battle begin! spring";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
    List<Integer> position = getMyPosition(arenaUpdate);

    LOG.info("pos x: " + position.get(0) + " y: " + position.get(1));

    // jeśli jestem uderzony - uciekaj
    if(wasIHit(arenaUpdate)){

      LOG.info("Musze uciekac");
      if(canGoForward(arenaUpdate)) {
        LOG.info("Uciekam do przodu!");
        return "F";
      }
      if(canGoLeft(arenaUpdate)) {
        LOG.info("Uciekam na lewo!");
        return "L";
      }
      if(canGoRight(arenaUpdate)) {
        LOG.info("Uciekam na na prawo");
        return "R";
      }
      if(canGoBackward(arenaUpdate)) {
        LOG.info("Moge uciekac do tylu. Obracam sie w prawo!");
        return "R";
      }
      LOG.info("Nie moge uciec - rzucam!");
      return "T";
    }

    // jeśli ktos na celowniku ponizej 3 strzelaj
    if(someoneOnTarget(arenaUpdate)){
      LOG.info("Rzucam");
      return "T";
    }
    //jeśli ktoś w zasięgu jednego kroku idź
    if(someoneOnOneStepForward(arenaUpdate)){
      LOG.info("Ide do przodu, bo ktos jest o jedno pole ode mnie!");
      return "F";
    }

    //jesli ktos w zasiegu ale w innym kierunku
    if(someoneOnNorth(arenaUpdate)) {
      LOG.info("Ktos na polnocy, obracam sie");
      return getTurn("N", arenaUpdate);
    }
    if(someoneOnSouth(arenaUpdate)) {
      LOG.info("Ktos na poludniu, obracam sie");
      return getTurn("S", arenaUpdate);
    }
    if(someoneOnWest(arenaUpdate)) {
      LOG.info("Ktos na zachodzie, obracam sie");
      return getTurn("W", arenaUpdate);
    }
    if(someoneOnEast(arenaUpdate)) {
      LOG.info("Ktos na wschodzie, obracam sie");
      return getTurn("E", arenaUpdate);
    }

    if(canGoForward(arenaUpdate)) {
      LOG.info("Brak pomyslu, ide do przodu");
      return "F";
    }

    return leftOrRight();
  }
  private String getTurn(String direction, ArenaUpdate arenaUpdate) {
    String currentDir = getMyState(arenaUpdate).direction;

    if(currentDir.equals("N") && direction.equals("W"))
      return "L";
    if(currentDir.equals("N") && direction.equals("E"))
      return "R";
    if(currentDir.equals("N") && direction.equals("S"))
      return "L";


    if(currentDir.equals("W") && direction.equals("N"))
      return "R";
    if(currentDir.equals("W") && direction.equals("E"))
      return "L";
    if(currentDir.equals("W") && direction.equals("S"))
      return "L";



    if(currentDir.equals("S") && direction.equals("N"))
      return "L";
    if(currentDir.equals("S") && direction.equals("E"))
      return "L";
    if(currentDir.equals("S") && direction.equals("W"))
      return "R";




    if(currentDir.equals("E") && direction.equals("N"))
      return "L";
    if(currentDir.equals("E") && direction.equals("W"))
      return "L";
    if(currentDir.equals("E") && direction.equals("S"))
      return "R";

    return "L";
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
    LOG.info("Losowy kierunek: " + command);
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
        return !(isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || myPlayerState.x+1 > arenaUpdate.arena.dims.get(0));
    }

    return true;
  }

  private boolean canGoLeft(ArenaUpdate arenaUpdate) {
    PlayerState myPlayerState = getMyState(arenaUpdate);

    switch(myPlayerState.direction) {
      case "N":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) || myPlayerState.x-1 < 0);
      case "S":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || myPlayerState.x+1 > arenaUpdate.arena.dims.get(0));
      case "W":

        return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) || myPlayerState.y+1 > arenaUpdate.arena.dims.get(1));
      case "E":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) || myPlayerState.y-1 < 0);
    }

    return true;
  }

  private boolean canGoRight(ArenaUpdate arenaUpdate) {
    PlayerState myPlayerState = getMyState(arenaUpdate);

    switch(myPlayerState.direction) {
      case "N":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || myPlayerState.x+1 > arenaUpdate.arena.dims.get(0));
      case "S":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) || myPlayerState.x-1 < 0);
      case "W":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) || myPlayerState.y-1 < 0);
      case "E":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) || myPlayerState.y+1 > arenaUpdate.arena.dims.get(1));
    }

    return true;
  }

  private boolean canGoBackward(ArenaUpdate arenaUpdate) {
    PlayerState myPlayerState = getMyState(arenaUpdate);

    switch(myPlayerState.direction) {
      case "N":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y+1) || myPlayerState.y+1 > arenaUpdate.arena.dims.get(1));
      case "S":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x, myPlayerState.y-1) || myPlayerState.y-1 < 0);
      case "W":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x+1, myPlayerState.y) || myPlayerState.x+1 > arenaUpdate.arena.dims.get(0));
      case "E":
        return !(isPlayerOn(arenaUpdate, myPlayerState.x-1, myPlayerState.y) || myPlayerState.x-1 < 0);
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

