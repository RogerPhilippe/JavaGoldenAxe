package infra;


import infra.Actors.Actor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class SpriteSheetEditorView extends JPanel implements MouseListener {

    private BufferedImage image;
    private BufferedImage image2;
    
    private AffineTransform transform;
    private int x;
    private int y;

    private Rectangle rectangle;
    
    public SpriteSheetEditorView(Actor actor) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream(actor.path + Actors.IMAGE));
            image2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);            
            Graphics ig2 = image2.getGraphics();
        } catch (IOException ex) {
            Logger.getLogger(SpriteSheetEditorView.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        transform = new AffineTransform();
        rectangle = new Rectangle();
    }

    public void start() {
        addKeyListener(new Input());
        addMouseListener(this);
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
        int speed = 5;
        if (Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            x -= speed;
        } else if (Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            x += speed;
        }

        if (Input.isKeyPressed(KeyEvent.VK_UP)) {
            y -= speed;
        } else if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            y += speed;
        }
    }

    private void draw(Graphics2D g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawLine(0, 0, getWidth(), getHeight());

        g.scale(2.5, 2.5);

        g.translate(-x, -y);
        g.drawImage(image, 0, 0, null);
        
        g.setColor(Color.RED);
        g.draw(rectangle);
    }

    public static void show(final Actor actor) {
        SwingUtilities.invokeLater(() -> {
            SpriteSheetEditorView view = new SpriteSheetEditorView(actor);
            view.setPreferredSize(new Dimension((int) (276 * 2.5), (int) (207 * 2.5)));
            JFrame frame = new JFrame();
            frame.setTitle("Java Sprite Sheet Extractor: " + actor.name);
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
    }

    @Override
    public void mousePressed(MouseEvent e) {
        try {
            transform.setToIdentity();
            transform.scale(2.5, 2.5);
            transform.translate(-x, -y);

            AffineTransform it = transform.createInverse();
            Point ptSrc = new Point(e.getX(), e.getY());
            Point ptDst = new Point();
            it.transform(ptSrc, ptDst);
            
            Graphics i2g = image2.getGraphics();
            i2g.setColor(Color.MAGENTA);
            i2g.fillRect(0, 0, image2.getWidth(), image2.getHeight());
            
            if (SwingUtilities.isLeftMouseButton(e)) {
                rectangle.setBounds(ptDst.x, ptDst.y, 1, 1);
            }
            
            extractSprite(rectangle, ptDst.x, ptDst.y);
            
            //if (SwingUtilities.isMiddleMouseButton(e)) {
                rectangle.width -= rectangle.x - 1;
                rectangle.height -= rectangle.y - 1;
            //}
            
            System.out.println("sprite animationid RIGHT " + rectangle.x + " " + rectangle.y + " " + rectangle.width + " " + rectangle.height + " 0 0");
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(SpriteSheetEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    // --- extract sprite ---
    
    public void extractSprite(Rectangle r, int x, int y) {
        if (getImageRGB(x, y) == image.getRGB(1, 1) || getImage2RGB(x, y) == 0xFFFFFFFF) {
            return;
        }
        image2.setRGB(x, y, 0xFFFFFFFF);
        r.x = x < r.x ? x : r.x;
        r.width = x > r.width ? x : r.width;
        r.y = y < r.y ? y : r.y;
        r.height = y > r.height ? y : r.height;
        extractSprite(r, x, y - 1);
        extractSprite(r, x, y + 1);

        extractSprite(r, x - 1, y);
        extractSprite(r, x + 1, y);
    }

    public int getImageRGB(int x, int y) {
        if (x < 0 || y < 0 || x > image.getWidth() - 1 || y > image.getHeight() - 1) {
            return 0xFF000000;
        }
        return image.getRGB(x, y);
    }

    public int getImage2RGB(int x, int y) {
        if (x < 0 || y < 0 || x > image2.getWidth() - 1 || y > image2.getHeight() - 1) {
            return 0xFFFFFFFF;
        }
        return image2.getRGB(x, y);
    }

}
