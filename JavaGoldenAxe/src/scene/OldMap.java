package scene;

import infra.Audio;
import infra.Dialog;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Offscreen;
import infra.Resource;
import infra.SceneManager;
import static infra.Settings.*;
import infra.State;
import infra.Util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * OldMap class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class OldMap extends State<GoldenAxeGame, SceneManager> {
    
    // how many times it will write in the same frame
    private final int[] writeTimes = { 
        2, // #1 
        4, // #2
        2, // #3 
        64,4,4,16,16,64, // #4
        8,8,4,8,1 // # 5 fin
    };
    
    // direction
    // cc  bb  aa  aa=start corner
    // 00__00__00  bb=1st direction
    // 0=r 0=r 0-1 cc=2nd direction
    // 1=l 1=l | |
    // 2=d 2=d 3-2
    // 3=u 3=u
    private final int[] directions = { 
        0b10_00_00, // #1
        0b10_00_00, // #2
        0b10_00_00, // #3 
        0b01_10_01, 0b00_11_11, 0b10_00_00
            , 0b01_10_01, 0b11_00_11, 0b11_01_10, // #4 
            
        0b00_10_00, 0b10_01_01, 0b10_00_00, 0b00_10_00, 0b10_00_00 // #5 fin
    };
    
    private final boolean[] keepPenPosition = { 
        false, // #1
        false, // #2 
        false, // #3 
        true, true, true, true, true, false, // #4
        true, true, true, true, false // #5 fin
    };
    
    private static final int MAP_MAX_INDEX = 13;
    private static final int PEN_COLOR = -5435376;

    private final Offscreen offscreen;
    private final BufferedImage pen;
    private final BufferedImage mapBackground;
    private final BufferedImage model;
    private final BufferedImage map;
    private final Graphics2D mapG2d;
    
    private int mapIndex = -1;
    private int dataColor = 0;

    private int col = 0;
    private int row = 0;
    private int cols = 17;
    private int rows = 27;
    private boolean finished = true;
    
    private double penX = 150;
    private double penY = -10;
    private double penStartX = 150;
    private double penStartY = -10;
    private int positioningPen = 0;

    private boolean copyingData = false;
    private int offsetX;
    private int offsetY;
    
    private long startTime;
    
    public OldMap(SceneManager manager, GoldenAxeGame owner) {
        super(manager, "old_map", owner);
        offscreen = new Offscreen(450, 297);
        pen = Resource.getImage("old_map_feather_pen");
        mapBackground = Resource.getImage("old_map_background");
        model = Resource.getImage("old_map_model");
        map = new BufferedImage(
                mapBackground.getWidth(), mapBackground.getHeight()
                    , BufferedImage.TYPE_INT_ARGB);
        
        mapG2d = (Graphics2D) map.getGraphics();
        mapG2d.setBackground(new Color(0, 0, 0, 0));
    }

    public void reset() {
        mapIndex = -1;
        dataColor = 0;
        col = 0;
        row = 0;
        cols = 17;
        rows = 27;
        finished = true;
        penX = 150;
        penY = -10;
        penStartX = 150;
        penStartY = -10;
        positioningPen = 0;
        copyingData = false;
        offsetX = 0;
        offsetY = 0;
        mapG2d.clearRect(0, 0, map.getWidth(), map.getHeight());
    }

    @Override
    public void onEnter() {
        String text = Resource.getText("old_map_" + GoldenAxeGame.stage);
        if (text == null) {
            text = "";
        }
        Dialog.show(17, 2, 0, 0, text, false);
        
        if (GoldenAxeGame.stage == 2) {
            reset();
        }
        nextMap();
        
        if (GoldenAxeGame.stage == 9 || GoldenAxeGame.stage == 10) {
            Audio.playMusic("conclusion");
        }
        else {
            Audio.playMusic("old_map");
        }
        
        startTime = -1;
    }

    @Override
    public void onExit() {
        Dialog.hide();
    }
    
    private void nextMap() {
        mapIndex++;
        if (mapIndex > MAP_MAX_INDEX) {
            finished = true;
            mapIndex = MAP_MAX_INDEX;
            return;
        }
        dataColor = 0xffff0000 + mapIndex;
        int startCorner = directions[mapIndex] & 3;
        switch (startCorner) {
            case 0 -> {
                // top left
                col = 0;
                row = 0;
            }
            case 1 -> {
                // top right
                col = rows - 1;
                row = 0;
            }
            case 2 -> {
                // bottom right
                col = cols - 1;
                row = rows - 1;
            }
            case 3 -> {
                // bottom left
                col = 0;
                row = rows - 1;
            }
        }
        cols = 17;
        rows = 27;
        finished = false;
        positioningPen = 0;        
    }

    @Override
    public void fixedUpdate() {
        if (manager.isTransiting()) {
            return;
        }

        Dialog.update();
        
        if (startTime < 0) {
            startTime = Util.getTime() + 1000;
        }
        if (Util.getTime() < startTime) {
            return;
        }
        
        boolean gotoNextScene = Input.isKeyJustPressed(KEY_START_1) 
                                    || Input.isKeyJustPressed(KEY_START_2);
        
        if (GoldenAxeGame.player1 != null 
                && !GoldenAxeGame.player1.isGameOver()) {
            
            GoldenAxeGame.player1.resetControls();
            GoldenAxeGame.player1.updateControlPlayer1();
            gotoNextScene |= GoldenAxeGame.player1.isFireControlEnabled();
        }
        if (GoldenAxeGame.player2 != null 
                && !GoldenAxeGame.player2.isGameOver()) {
            
            GoldenAxeGame.player2.resetControls();
            GoldenAxeGame.player2.updateControlPlayer2();
            gotoNextScene |= GoldenAxeGame.player2.isFireControlEnabled();
        }
        
        if (finished && gotoNextScene) {
            if (!Dialog.isFinished()) {
                Dialog.showAll();
            }
            else if (GoldenAxeGame.stage == 9 || GoldenAxeGame.stage == 10) {
                Audio.stopMusic();
                manager.switchTo("ending");
            }
            else {
                Audio.stopMusic();
                manager.switchTo("stage");
            }
            return;
        }
        
        if (finished || mapIndex < 0) {
            return;
        }
        // removing the pen from the map
        if (positioningPen == 3) {
            if (keepPenPosition[mapIndex]) {
                penStartX = penX;
                penStartY = penX;
                finished = true;
                nextMap();
                return;
            }
            penStartX = 200;
            penStartY = -10;
            double dx = penStartX - penX;
            double dy = penStartY - penY;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length < 1) {
                finished = true;
                return;
            }
            penX = penX + dx * 0.1;
            penY = penY + dy * 0.1;
            return;
        }
        // moving the pen to the map start writing position
        if (positioningPen == 1) {
            double dx = penStartX - penX;
            double dy = penStartY - penY;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length < 1) {
                map.setRGB((int) penStartX, (int) penStartY, PEN_COLOR);
                positioningPen = 2;
                return;
            }
            dx *= 0.1;
            dy *= 0.1;
            if (Math.abs(dx) < 1) dx = Math.signum(dx);
            if (Math.abs(dy) < 1) dy = Math.signum(dy);
            penX = penX + dx;
            penY = penY + dy;
            return;
        }
        if (copyingData) {
            for (int i = 0; i < writeTimes[mapIndex]; i++) {
                if (copyData(col, row)) {
                    copyingData = false;
                    return;
                }
                // first needs to position pen at starting position
                if (positioningPen == 1) {
                    return;
                }
            }
            return;
        }
        boolean found = false;
        while (!found) {
            int firstDirection = (directions[mapIndex] >> 2) & 3; 
            boolean firstDirectionFinished = false;
            switch (firstDirection) {
                case 0 -> {
                    // right
                    col = col + 1;
                    if (col > cols - 1) {
                        col = 0;
                        firstDirectionFinished = true;
                    }
                }
                case 1 -> {
                    // left
                    col = col - 1;
                    if (col < 0) {
                        col = cols - 1;
                        firstDirectionFinished = true;
                    }
                }
                case 2 -> {
                    // down
                    row = row + 1;
                    if (row > rows - 1) {
                        row = 0;
                        firstDirectionFinished = true;
                    }
                }
                case 3 -> {
                    // up
                    row = row - 1;
                    if (row < 0) {
                        row = rows - 1;
                        firstDirectionFinished = true;
                    }
                }
            }
            if (firstDirectionFinished) {
                int secondDirection = (directions[mapIndex] >> 4) & 3; 
                boolean secondDirectionFinished = false;
                switch (secondDirection) {
                    case 0 -> {
                        // right
                        col = col + 1;
                        if (col > cols - 1) {
                            col = 0;
                            secondDirectionFinished = true;
                        }
                    }
                    case 1 -> {
                        // left
                        col = col - 1;
                        if (col < 0) {
                            col = cols - 1;
                            secondDirectionFinished = true;
                        }
                    }
                    case 2 -> {
                        // down
                        row = row + 1;
                        if (row > rows - 1) {
                            row = 0;
                            secondDirectionFinished = true;
                        }
                    }
                    case 3 -> {
                        // up
                        row = row - 1;
                        if (row < 0) {
                            row = rows - 1;
                            secondDirectionFinished = true;
                        }
                    }
                } 
                if (secondDirectionFinished) {
                    positioningPen = 3;
                    return;
                }
            }
            if (containsData(col, row)) {
                copyingData = true;
                int startCorner = directions[mapIndex] & 3;
                switch (startCorner) {
                    case 0 -> {
                        // top left
                        offsetX = 0;
                        offsetY = 0;
                    }
                    case 1 -> {
                        // top right
                        offsetX = 7;
                        offsetY = 0;
                    }
                    case 2 -> {
                        // bottom right
                        offsetX = 7;
                        offsetY = 7;
                    }
                    case 3 -> {
                        // bottom left
                        offsetX = 0;
                        offsetY = 7;
                    }
                }
                found = true;
            }
        }
    }
    
    private boolean copyData(int c, int r) {
        boolean exit = false;
        boolean copyFinished = false;
        while (!exit) {
            int x1 = c * 8 + offsetX;
            int y1 = r * 8 + offsetY;
            if (model.getRGB(x1, y1) == dataColor) {
                if (positioningPen == 0) {
                    penStartX = x1;
                    penStartY = y1;
                    positioningPen = 1;
                }
                else {
                    map.setRGB(x1, y1, PEN_COLOR);
                    penX = x1;
                    penY = y1;
                }
                exit = true;
            }
            int firstDirection = (directions[mapIndex] >> 2) & 3; 
            boolean firstDirectionFinished = false;
            switch (firstDirection) {
                case 0 -> {
                    // right
                    offsetX = offsetX + 1;
                    if (offsetX > 8 - 1) {
                        offsetX = 0;
                        firstDirectionFinished = true;
                    }
                }
                case 1 -> {
                    // left
                    offsetX = offsetX - 1;
                    if (offsetX < 0) {
                        offsetX = 8 - 1;
                        firstDirectionFinished = true;
                    }
                }
                case 2 -> {
                    // down
                    offsetY = offsetY + 1;
                    if (offsetY > 8 - 1) {
                        offsetY = 0;
                        firstDirectionFinished = true;
                    }
                }
                case 3 -> {
                    // up
                    offsetY = offsetY - 1;
                    if (offsetY < 0) {
                        offsetY = 8 - 1;
                        firstDirectionFinished = true;
                    }
                }
            }
            if (firstDirectionFinished) {
                int secondDirection = (directions[mapIndex] >> 4) & 3; 
                boolean secondDirectionFinished = false;
                switch (secondDirection) {
                    case 0 -> {
                        // right
                        offsetX = offsetX + 1;
                        if (offsetX > 8 - 1) {
                            offsetX = 0;
                            secondDirectionFinished = true;
                        }
                    }
                    case 1 -> {
                        // left
                        offsetX = offsetX - 1;
                        if (offsetX < 0) {
                            offsetX = 8 - 1;
                            secondDirectionFinished = true;
                        }
                    }
                    case 2 -> {
                        // down
                        offsetY = offsetY + 1;
                        if (offsetY > 8 - 1) {
                            offsetY = 0;
                            secondDirectionFinished = true;
                        }
                    }
                    case 3 -> {
                        // up
                        offsetY = offsetY - 1;
                        if (offsetY < 0) {
                            offsetY = 8 - 1;
                            secondDirectionFinished = true;
                        }
                    }
                } 
                if (secondDirectionFinished) {
                    copyFinished = true;
                    exit = true;
                }
            }
        }
        return copyFinished;
    }
    
    private boolean containsData(int c, int r) {
        int x1 = c * 8;
        int y1 = r * 8;
        for (int y = y1; y < y1 + 8; y++) {
            for (int x = x1; x < x1 + 8; x++) {
                if (model.getRGB(x, y) == dataColor) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public  void draw(Graphics2D g) {
        Dialog.draw(g);
        
        offscreen.clear();
        Graphics2D og2d = offscreen.getG2d();
        og2d.drawImage(mapBackground, 0, 0, null);
        og2d.drawImage(map, 0, 0, null);
        og2d.drawImage(pen, (int) penX, (int) (penY - pen.getHeight()), null);
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                    , RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        g.drawImage(offscreen.getImage(), 1, 1
                , (int) (offscreen.getWidth() * 0.9457)
                    , (int) (offscreen.getHeight() * 0.9453), null);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                    , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

}
