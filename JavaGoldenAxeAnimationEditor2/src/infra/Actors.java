package infra;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Leo
 */
public class Actors {

    public static final String IMAGE = "sprite_sheet.png";
    public static final String SPRITE_SHEET = "sprite_sheet.txt";
    public static final String COLLIDERS = "colliders.txt";
    public static final String POINTS = "points.txt";
    
    public static class Actor {
        public String name;
        public String path;

        public Actor(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }
    
    public static final List<Actor> actorsList = new ArrayList<>();

    static {
        actorsList.add(new Actor("chicken leg", "/beasts/chicken_leg/"));
        actorsList.add(new Actor("dragon blue", "/beasts/dragon_blue/"));
        actorsList.add(new Actor("dragon red", "/beasts/dragon_red/"));
        
        actorsList.add(new Actor("bad brothers", "/enemy/bad_brothers/"));
        actorsList.add(new Actor("death adder", "/enemy/death_adder/"));
        actorsList.add(new Actor("heninger", "/enemy/heninger/"));
        actorsList.add(new Actor("lieutenant bitter", "/enemy/lieutenant_bitter/"));
        actorsList.add(new Actor("longmoan", "/enemy/longmoan/"));
        actorsList.add(new Actor("skeleton", "/enemy/skeleton/"));
        actorsList.add(new Actor("storchinaya", "/enemy/storchinaya/"));
        
        actorsList.add(new Actor("ax battler", "/player/ax_battler/"));
        actorsList.add(new Actor("gilius thunderhead", "/player/gilius_thunderhead/"));
        actorsList.add(new Actor("tyris flare", "/player/tyris_flare/"));
        
        actorsList.add(new Actor("highness", "/game/highness/"));
        actorsList.add(new Actor("ending", "/game/ending/"));
        actorsList.add(new Actor("title", "/game/title/"));
        actorsList.add(new Actor("select_player", "/game/select_player/"));
        actorsList.add(new Actor("ranking", "/game/ranking/"));
        
        actorsList.add(new Actor("magic_lighting", "/magic/lightning/"));
        actorsList.add(new Actor("magic_volcano", "/magic/volcano/"));
        actorsList.add(new Actor("magic_fire", "/magic/fire/"));
    }
    
    
}
