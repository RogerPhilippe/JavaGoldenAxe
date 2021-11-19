package infra;


import infra.Actors.Actor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Points editor
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class SpritePointsEditor extends JPanel implements KeyListener, MouseListener {

    private class MyPoint extends Point {
        String id;

        public MyPoint(String id, int x, int y) {
            super(x, y);
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "point " + id + " " + x + " " + y;
        }
        
    }
    
    public static final double SCALE = 2.5;
    
    public String projectPath;
    public String file;
    public String imageFile;

    private BufferedImage image;
    private int x;
    private int y;
    
    private List<MyPoint> points = new ArrayList<>();
    private MyPoint currentPoint;
    
    public SpritePointsEditor(Actor actor) {
        projectPath = System.getProperty("user.dir") + "\\";
        file = actor.path + Actors.POINTS;
        imageFile = actor.path + Actors.IMAGE;
        try {
            image = ImageIO.read(getClass().getResourceAsStream(imageFile));
        } catch (IOException ex) {
            Logger.getLogger(SpriteSheetEditorView.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    public void start() {
        addKeyListener(new Input());
        addKeyListener(this);
        addMouseListener(this);
        loadPoints();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        update();
        draw((Graphics2D) g);
        try {
            Thread.sleep(1000 / 60);
        } catch (InterruptedException ex) {
        }
        repaint();
    }

    private void update() {
        double speed = 4;
        
        // scroll screen
        if (Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            x -= speed;
        } 
        if (Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            x += speed;
        }

        if (Input.isKeyPressed(KeyEvent.VK_UP)) {
            y -= speed;
        } 
        if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            y += speed;
        }

        speed = 1;
        
        if (Input.isKeyJustPressed(KeyEvent.VK_G)) {
            currentPoint.x -= speed;
        } 
        if (Input.isKeyJustPressed(KeyEvent.VK_J)) {
            currentPoint.x += speed;
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_Y)) {
            currentPoint.y -= speed;
        } 
        if (Input.isKeyJustPressed(KeyEvent.VK_H)) {
            currentPoint.y += speed;
        }
        
    }

    private void draw(Graphics2D g) {
        g.clearRect(0, 0, getWidth(), getHeight());

        g.scale(SCALE, SCALE);
        
        //g.translate(276 / 2, 207 / 2);
        //g.drawLine(-276 / 2, 0, 276, 0);
        //g.drawLine(0, -207 / 2, 0, 207);

        g.translate(-x, -y);
        
        g.drawImage(image, 0, 0, null);
        
        for (MyPoint point : points) {

            if (currentPoint != null && currentPoint == point) {
                g.setColor(Color.RED);
            }
            else {
                g.setColor(Color.BLUE);
            }
            //g.drawRect(point.x - 4, point.y - 4, 8, 8);
            g.drawLine(point.x - 4, point.y, point.x + 4, point.y);
            g.drawLine(point.x, point.y - 4, point.x, point.y + 4);
            g.drawString("" + point.getId(), point.x + 2, point.y - 2);
        }
    }

    public static void show(Actor actor) {
        SwingUtilities.invokeLater(() -> {
            SpritePointsEditor view = new SpritePointsEditor(actor);
            view.setPreferredSize(new Dimension((int) (276 * SCALE), (int) (207 * SCALE)));
            JFrame frame = new JFrame();
            frame.setTitle("Java Sprite Points Editor: " + actor.name);
            frame.getContentPane().add(view);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
            view.start();
        });
    }

    // --- mouse ---
    private final Rectangle currentCollider = new Rectangle();
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int cx = (int) ((e.getX()) / SCALE) + x;
        int cy = (int) ((e.getY()) / SCALE) + y;
        
        
        // select existent collider
        for (MyPoint point : points) {
            currentCollider.setBounds(point.x - 4, point.y - 4, 8, 8);
            if (currentCollider.contains(cx, cy)) {
                currentPoint = point;
                return;
            }
        }
        
        // new point
        String newPointId = JOptionPane.showInputDialog("new point id");
        points.add(currentPoint = new MyPoint(newPointId, cx, cy));
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // --- KeyListener ---
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
        if (e.getKeyCode() == KeyEvent.VK_1) {
        } 
        else if (e.getKeyCode() == KeyEvent.VK_2) {
        }        
        
        // delete
        if (e.getKeyCode() == KeyEvent.VK_DELETE && currentPoint != null) {
            points.remove(currentPoint);
            currentPoint = null;
        } 
        
        // save file
        if (e.getKeyCode() == KeyEvent.VK_0) {
            savePoints();
        } 

    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
 
    private void loadPoints() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(file)));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] data = line.split("\\ ");
                if (data[0].equals("point")) {
                    String name = data[1];
                    int x = Integer.parseInt(data[2]);
                    int y = Integer.parseInt(data[3]);
                    points.add(currentPoint = new MyPoint(name, x, y));
                }
            }
            br.close();
        }
        catch (Exception ex) {
            Logger.getLogger(SpriteCenterEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void savePoints() {
        try {
            PrintWriter pw = new PrintWriter(projectPath + "res/" + file);
            for (MyPoint point : points) {
                pw.println(point);
            }
            pw.close();
        }
        catch (Exception ex) {
            Logger.getLogger(SpriteCenterEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
}
