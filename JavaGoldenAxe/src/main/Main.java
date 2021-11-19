package main;

import infra.Display;
import infra.GameCanvas;
import infra.GoldenAxeGame;
import infra.Resource;
import javax.swing.SwingUtilities;

/**
 * Main class.
 * 
 * Game entry point.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameCanvas gameCanvas = new GameCanvas(new GoldenAxeGame());
            Display display = new Display(gameCanvas);
            display.setTitle("Golden Axe");
            display.setIconImage(Resource.getImage("icon"));
            display.start();
        });
    }   
    
}
