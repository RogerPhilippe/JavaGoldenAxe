package scene;

import infra.Dialog;
import infra.State;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Resource;
import infra.SceneManager;
import static infra.Settings.*;
import infra.Util;
import java.awt.Graphics2D;

/**
 * Disclaimer class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Disclaimer extends State<GoldenAxeGame, SceneManager> {
    
    private long startTime = -1;
    
    public Disclaimer(SceneManager manager, GoldenAxeGame game) {
        super(manager, "disclaimer", game);
    }
    
    @Override
    public void onEnter() {
        Dialog.show(6, 3, 0, 0, Resource.getText("disclaimer"), false);
    }

    @Override
    public void onExit() {
        Dialog.hide();
    }
    
    @Override
    public void fixedUpdate() {
        Dialog.update();
        if (!Dialog.isFinished() 
                && (Input.isKeyJustPressed(KEY_START_1)
                    || Input.isKeyJustPressed(KEY_START_2))) {
            
            Dialog.showAll();
        }
        else if (startTime < 0 && Dialog.isFinished()) {
            startTime = Util.getTime() + 3000;
        }
        else if (Dialog.isFinished() && !manager.isTransiting() 
                && (Util.getTime() >= startTime 
                    || (Input.isKeyJustPressed(KEY_START_1) 
                        || Input.isKeyJustPressed(KEY_START_2)))) {
            
            manager.switchTo("winners_dont_use_drugs");
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Dialog.draw(g);
    }

    
    
}
