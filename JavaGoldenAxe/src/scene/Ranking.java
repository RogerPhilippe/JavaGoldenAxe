package scene;

import infra.Actor;
import infra.Animator;
import infra.Animator.AnimatorEvent;
import infra.AnimatorEventListener;
import infra.Audio;
import infra.Background;
import static infra.Direction.*;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Resource;
import infra.SceneManager;
import static infra.Settings.*;
import infra.State;
import infra.TextRenderer;
import infra.Util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Ranking class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class Ranking extends State<GoldenAxeGame, SceneManager> 
                                        implements AnimatorEventListener {
    
    private Background background;
    private Animator animator;
    private BufferedImage digitsWhiteImage;
    private BufferedImage digitsRedImage;
    private boolean player1Available;
    private boolean player2Available;
    private int player1RankingIndex;
    private int player2RankingIndex;
    private final int[] rankingCounter = { 0, 0, 0, 0, 0, 0, 0, 0 };
    private boolean returnableToTitle;
    
    public Ranking(SceneManager manager, GoldenAxeGame game) {
        super(manager, "ranking", game);
    }
    
    private void reset() {
        background = null;
        animator = null;
        player1Available = false;
        player2Available = false;
        player1RankingIndex = -1;
        player2RankingIndex = -1;
        returnableToTitle = false;
    }
    
    @Override
    public void onEnter() {
        reset();
        background = new Background("select_player_background"
                                    , 0.25, 0.0, 0.0, 0.0, 1.0, 1.0, true);
        
        animator = Resource.getAnimators("ranking").get("ranking");
        animator.setListener(this);
        player1Available = GoldenAxeGame.player1 != null;
        player2Available = GoldenAxeGame.player2 != null;
        
        digitsWhiteImage = animator.getActorsById().get("table")
                .getAnimationPlayer().getAnimation("digits_white")
                    .getFrame(0).getSprite(0).getImage();

        digitsRedImage = animator.getActorsById().get("table")
                .getAnimationPlayer().getAnimation("digits_red")
                    .getFrame(0).getSprite(0).getImage();
        
        animator.play();
        Audio.playMusic("ranking");
    }

    @Override
    public void onExit() {
        reset();
        System.gc();
    }
    
    @Override
    public void fixedUpdate() {
        background.update();
        animator.fixedUpdate();
        fixPlayersDirections();
        updateSwords();
        if (returnableToTitle && (Input.isKeyJustPressed(KEY_START_1) 
                || Input.isKeyJustPressed(KEY_START_2) 
                    || Input.isKeyJustPressed(KEY_CANCEL))) {
            
            Audio.stopMusic();
            manager.switchTo("title");
        }
    }

    private void fixPlayersDirections() {
        if (player1Available) {
            GoldenAxeGame.player1.setDirection(RIGHT);
        }
        if (player2Available) {
            GoldenAxeGame.player2.setDirection(LEFT);
        }
    }
    
    private void updateSwords() {
        Actor swordRight = animator.getActorsById().get("sword_right");
        swordRight.setWy(59.0 + 17 * player1RankingIndex);
        if (player1RankingIndex < 0) {
            swordRight.setWy(999);
        }

        Actor swordLeft = animator.getActorsById().get("sword_left");
        swordLeft.setWy(59.0 + 17 * player2RankingIndex);
        if (player2RankingIndex < 0) {
            swordLeft.setWy(999);
        }
    }
    
    private String getFormattedStrength(double strength) {
        String formattedStrength = "     " + String.format("%.1f", strength);
        formattedStrength = formattedStrength.substring(
                formattedStrength.length() - 5, formattedStrength.length());
        
        return formattedStrength;
    }
    
    @Override
    public void draw(Graphics2D g) {
        background.draw(g, 0.0, 0.0);
        
        if (player1Available) {
            TextRenderer.draw(g, "STRENGTH", 2, 1);
            double strength = GoldenAxeGame.player1.getStrength();
            drawNumber(g, getFormattedStrength(strength), 24, 24, Color.RED);
        }

        if (player2Available) { 
            TextRenderer.draw(g, "STRENGTH", 25, 1);
            double strength = GoldenAxeGame.player2.getStrength();
            drawNumber(g, getFormattedStrength(strength), 208, 24, Color.RED);
        }

        animator.draw(g, null, 0.0, 0.0);
    }

    @Override
    public void onAnimatorEventTriggered(AnimatorEvent event) {
        switch (event.eventId) {
            case "update_table_data" -> {
                updateTableData();
                returnableToTitle = true;
                if (player1Available) {
                    animator.addActor(GoldenAxeGame.player1);
                }
                if (player2Available) {
                    animator.addActor(GoldenAxeGame.player2);
                }
            }
        }
    }
    
    private void drawNumber(
            Graphics2D g, String number, int x, int y, Color color) {
        
        BufferedImage digitsImage;
        if (color == Color.WHITE) {
            digitsImage = digitsWhiteImage;
        }
        else {
            digitsImage = digitsRedImage;
        }
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (c == ' ') {
                continue;
            }
            int digitIndex = c == '.' ? 10 : (int) (c - 48);
            int dx1 = x + 10 * i;
            int dy1 = y;
            int dx2 = dx1 + 10;
            int dy2 = dy1 + 15;
            int sx1 = digitIndex * 10;
            int sy1 = 0;
            int sx2 = sx1 + 10;
            int sy2 = sy1 + 15;
            g.drawImage(digitsImage
                    , dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        }
    }
    
    private void updateTableData() {
        if (player1Available) {
            player1RankingIndex = getRankingIndex(
                    GoldenAxeGame.player1.getStrength());
            
            rankingCounter[player1RankingIndex]++;
        }
        if (player2Available) {
            player2RankingIndex = getRankingIndex(
                    GoldenAxeGame.player2.getStrength());
            
            rankingCounter[player2RankingIndex]++;
        }
        
        Actor tableData = animator.getActorsById().get("table_data");
        BufferedImage image = tableData.getAnimationPlayer()
            .getCurrentAnimation().getFrame(0).getSprite(0).getImage();
        
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setBackground(Util.getColor("0x00000000"));
        g.clearRect(0, 0, 18, 135);
        
        for (int i = 0; i < rankingCounter.length; i++) {
            String counter = "00" + rankingCounter[i];
            counter = counter.substring(counter.length() - 2, counter.length());
            drawNumber(g, counter, 0, (int) (i * 17.2), Color.WHITE);
        }
    }

    private int getRankingIndex(double strength) {
        if (strength <= 35.0) {
            return 7;
        }
        else if (strength <= 40.0) {
            return 6;
        }
        else if (strength <= 45.0) {
            return 5;
        }
        else if (strength <= 50.0) {
            return 4;
        }
        else if (strength <= 55.0) {
            return 3;
        }
        else if (strength <= 60.0) {
            return 2;
        }
        else if (strength <= 100.0) {
            return 1;
        }
        else {
            return 0;
        }
    }

}
