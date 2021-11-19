package scene;

import infra.State;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Resource;
import infra.SceneManager;
import static infra.Settings.*;
import infra.Util;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * TestScene class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class WinnersDontUseDrugs extends State<GoldenAxeGame, SceneManager> {
    
    private final BufferedImage winnersDontUseDrugs;
    private long startTime;
    
    public WinnersDontUseDrugs(SceneManager manager, GoldenAxeGame game) {
        super(manager, "winners_dont_use_drugs", game);
        winnersDontUseDrugs = Resource.getImage("winners_dont_use_drugs");
    }
    
    @Override
    public void onEnter() {
        startTime = Util.getTime() + 5000;
    }
    
    @Override
    public void fixedUpdate() {
        if (!manager.isTransiting()
                && (Util.getTime() >= startTime 
                    || Input.isKeyJustPressed(KEY_START_1)
                        || Input.isKeyJustPressed(KEY_START_2))) {
            
            manager.switchTo("title");
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.drawImage(winnersDontUseDrugs, 0, 0, null);
    }

}
