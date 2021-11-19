package infra;

import static infra.Settings.*;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SceneManager class.
 * 
 * Extension of StateManager that offers a transition effect between scenes.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class SceneManager extends StateManager<GoldenAxeGame, SceneManager> {

    private static final List<Rectangle> TRANSITION_CLIPS = 
                                            new ArrayList<Rectangle>();

    static {
        createSceneTransitionClips();
    }
    
    private static void createSceneTransitionClips() {
        for (int i = 0; i < 16; i++) {
            TRANSITION_CLIPS.add(new Rectangle(0, 0, 0, 0));
        }
        double x = 0.0, y = 0.0, speed = 1.0;
        double halfWidth = CANVAS_WIDTH / 2.0;
        double halfHeight = CANVAS_HEIGHT / 2.0;
        double aspectRatio = (double) CANVAS_WIDTH / CANVAS_HEIGHT;
        while (x < halfWidth + 2 || y < halfHeight + 2) {
            x += speed * 0.5 * aspectRatio;
            y += speed * 0.5;
            speed += 0.1;
            Rectangle clip = new Rectangle(
                (int) (halfWidth - x), (int) (halfHeight - y)
                    , (int) (2 * x), (int) (2 * y));
            
            TRANSITION_CLIPS.add(clip);
        }
        Collections.reverse(TRANSITION_CLIPS);
    }

    private int transitionState = 0;
    private int transitionProgress = 0;

    public boolean isTransiting() {
        return transitionState != 0;
    }
    
    @Override
    public State<GoldenAxeGame, SceneManager> switchTo(String stateName) {
        if (isTransiting()) {
            throw new RuntimeException(
                    "can't change state while in transition !");
        }
        return super.switchTo(stateName); 
    }

    public void update(long delta) {
        if (currentState != null && transitionState == 0) {
            currentState.update(delta); 
        }
    }
    
    @Override
    public void fixedUpdate() {
        switch (transitionState) {
            case 0 -> { 
                if (nextState != null) {
                    transitionState = 1;
                }
                else if (currentState != null) {
                    currentState.fixedUpdate();
                }
            }
            case 1 -> {
                // fade in
                transitionProgress++;
                if (transitionProgress > TRANSITION_CLIPS.size() - 1) {
                    transitionProgress = TRANSITION_CLIPS.size() - 1;
                    transitionState = 2;
                }
            }
            case 2 -> {
                if (currentState != null) {
                    currentState.onExit();
                }
                currentState = nextState;
                nextState = null;
                currentState.onEnter();
                transitionState = 3;
                if (currentState != null) {
                    currentState.fixedUpdate();
                }
            }
            case 3 -> {
                // fade out
                transitionProgress--;
                if (transitionProgress < 0) {
                    transitionProgress = 0;
                    transitionState = 0;
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (transitionProgress > 0) {
            g.setClip(TRANSITION_CLIPS.get(transitionProgress));
        }
        if (currentState != null) {
            currentState.draw(g);
        }
    }

}
