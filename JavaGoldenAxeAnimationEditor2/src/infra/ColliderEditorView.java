package infra;


import infra.Actors.Actor;
import infra.Collider.Type;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Colliders
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class ColliderEditorView extends JPanel implements KeyListener, MouseListener {

    public static final double SCALE = 2.5;

    public String projectPath;
    public String file;
    public String imageFile;
    
    private BufferedImage image;
    private int x;
    private int y;
    
    private List<Collider> colliders = new ArrayList<>();
    private Collider currentCollider;
    
    private static final Color SELECTED_ATTACK = new Color(255, 0, 0, 128);
    private static final Color SELECTED_BODY = new Color(0, 0, 255, 128);
    private static final Color SELECTED_FIRE = new Color(255, 255, 0, 128);
    private static final Color SELECTED_TOUCH = new Color(0, 255, 255, 128);
    
    public ColliderEditorView(Actor actor) {
        projectPath = System.getProperty("user.dir") + "\\";
        file = actor.path + Actors.COLLIDERS;
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
        loadColliders();
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
            currentCollider.x -= speed;
        } 
        if (Input.isKeyJustPressed(KeyEvent.VK_J)) {
            currentCollider.x += speed;
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_Y)) {
            currentCollider.y -= speed;
        } 
        if (Input.isKeyJustPressed(KeyEvent.VK_H)) {
            currentCollider.y += speed;
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_S)) {
            currentCollider.width -= speed;
        } 
        if (Input.isKeyJustPressed(KeyEvent.VK_F)) {
            currentCollider.width += speed;
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_E)) {
            currentCollider.height -= speed;
        } 
        if (Input.isKeyJustPressed(KeyEvent.VK_D)) {
            currentCollider.height += speed;
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
        
        for (Collider collider : colliders) {

            if (currentCollider != null && currentCollider == collider) {
                if (collider.getType() == Collider.Type.BODY) {
                    g.setColor(SELECTED_BODY);
                }
                else if (collider.getType() == Collider.Type.ATTACK) {
                    g.setColor(SELECTED_ATTACK);
                }
                else if (collider.getType() == Collider.Type.FIRE) {
                    g.setColor(SELECTED_FIRE);
                }
                else {
                    g.setColor(SELECTED_TOUCH);
                }
                g.fillRect(collider.x, collider.y, collider.width, collider.height);
            }
            else {
                if (collider.getType() == Collider.Type.BODY) {
                    g.setColor(Color.BLUE);
                }
                else if (collider.getType() == Collider.Type.ATTACK) {
                    g.setColor(Color.RED);
                }
                else if (collider.getType() == Collider.Type.FIRE) {
                    g.setColor(Color.ORANGE);
                }
                else {
                    g.setColor(Color.GREEN);
                }
                g.drawRect(collider.x, collider.y, collider.width, collider.height);
            }
            
            g.drawString("" + collider.getReaction(), collider.x + 16, collider.y);
        }
        
    }

    public static void show(final Actor actor) {
        SwingUtilities.invokeLater(() -> {
            ColliderEditorView view = new ColliderEditorView(actor);
            view.setPreferredSize(new Dimension((int) (276 * SCALE), (int) (207 * SCALE)));
            JFrame frame = new JFrame();
            frame.setTitle("Java Collider Editor: " + actor.name);
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
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int cx = (int) ((e.getX()) / SCALE) + x;
        int cy = (int) ((e.getY()) / SCALE) + y;
        
        // select existent collider
        for (Collider collider : colliders) {
            if (collider.contains(cx, cy)) {
                currentCollider = collider;
                return;
            }
        }
        
        // new collider
        int cwidth = (int) (50 / 2.5);
        int cheight = (int) (50 / 2.5);
        colliders.add(currentCollider = new Collider(cx, cy, cwidth, cheight));
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

        // save file
        if (e.getKeyCode() == KeyEvent.VK_0) {
            saveColliders();
        } 

        // set attack type
        if (e.getKeyCode() == KeyEvent.VK_1 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.NONE);
        }
        if (e.getKeyCode() == KeyEvent.VK_2 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.STUNNED_1);
        }
        if (e.getKeyCode() == KeyEvent.VK_3 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.STUNNED_2);
        }
        if (e.getKeyCode() == KeyEvent.VK_4 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.KNOCK_DOWN);
        }
        if (e.getKeyCode() == KeyEvent.VK_5 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.ATTACK_SHORT);
        }
        if (e.getKeyCode() == KeyEvent.VK_6 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.GRAB_AND_THROW);
        }
        if (e.getKeyCode() == KeyEvent.VK_7 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.PICK_UP);
        }
        if (e.getKeyCode() == KeyEvent.VK_8 && currentCollider != null) {
            currentCollider.setReaction(Collider.Reaction.MOUNT);
        }

        
        // set attack collider type
        if (e.getKeyCode() == KeyEvent.VK_A && currentCollider != null) {
            currentCollider.setType(Collider.Type.ATTACK);
        }
        
        // set body collider type
        if (e.getKeyCode() == KeyEvent.VK_B && currentCollider != null) {
            currentCollider.setType(Collider.Type.BODY);
        }

        // set fire collider type
        if (e.getKeyCode() == KeyEvent.VK_Q && currentCollider != null) {
            currentCollider.setType(Collider.Type.FIRE);
        }

        // set touch collider type
        if (e.getKeyCode() == KeyEvent.VK_Z && currentCollider != null) {
            currentCollider.setType(Collider.Type.TOUCH);
        }

        // delete
        if (e.getKeyCode() == KeyEvent.VK_DELETE && currentCollider != null) {
            colliders.remove(currentCollider);
            currentCollider = null;
        } 
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
 
    private void loadColliders() {
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
                if (data[0].equals("collider")) {
                    // collider BODY NONE 10 16 23 61
                    Collider.Type colliderType = Collider.Type.valueOf(data[1]);
                    Collider.Reaction reaction = null;
                    try {
                        reaction = Collider.Reaction.valueOf(data[2]);
                    }
                    catch (IllegalArgumentException e) {}
                    
                    int x = Integer.parseInt(data[3]);
                    int y = Integer.parseInt(data[4]);
                    int width = Integer.parseInt(data[5]);
                    int height = Integer.parseInt(data[6]);
                    colliders.add(currentCollider = new Collider(x, y, width, height));
                    currentCollider.setType(colliderType);
                    currentCollider.setReaction(reaction);
                }
            }
            br.close();
        }
        catch (Exception ex) {
            Logger.getLogger(SpriteCenterEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void saveColliders() {
        try {
            PrintWriter pw = new PrintWriter(projectPath + "res/" +  file);
            for (Collider collider : colliders) {
                pw.println(collider);
            }
            pw.close();
        }
        catch (Exception ex) {
            Logger.getLogger(SpriteCenterEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
}
