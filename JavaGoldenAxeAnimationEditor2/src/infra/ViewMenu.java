package infra;


import infra.Actors.Actor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class ViewMenu extends JPanel {
    
    private Actor selectedActor;
    
    public ViewMenu() {
        createGui();
    }

    JPanel rootPanel = new JPanel();
    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    JLabel selectedActorLable = new JLabel("selected actor: ---");
           
    private void createGui() {
        ActorSelectionHandler actorSelectionHandler = new ActorSelectionHandler();
        
        rootPanel.setLayout(new BorderLayout(8, 8));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        
        rootPanel.add(leftPanel, BorderLayout.WEST);
        rootPanel.add(rightPanel, BorderLayout.EAST);
        rootPanel.add(selectedActorLable, BorderLayout.NORTH);
        
        Actors.actorsList.forEach(actor -> {
            JButton button = new JButton(actor.name);
            button.putClientProperty("actor", actor);
            button.addActionListener(actorSelectionHandler);
            leftPanel.add(button);
        });

        JButton spriteSheetEditorButton = new JButton("sprite sheet editor");
        JButton spriteCenterEditorButton = new JButton("sprite center editor");
        JButton collidersEditorButton = new JButton("colliders editor");
        JButton pointsEditorButton = new JButton("points editor");
        
        spriteSheetEditorButton.putClientProperty("option", 0);
        spriteCenterEditorButton.putClientProperty("option", 1);
        collidersEditorButton.putClientProperty("option", 2);
        pointsEditorButton.putClientProperty("option", 3);
        
        EditorSelectionHandler editorSelectionHandler = new EditorSelectionHandler();
        spriteSheetEditorButton.addActionListener(editorSelectionHandler);
        spriteCenterEditorButton.addActionListener(editorSelectionHandler);
        collidersEditorButton.addActionListener(editorSelectionHandler);
        pointsEditorButton.addActionListener(editorSelectionHandler);
        
        rightPanel.add(spriteSheetEditorButton);
        rightPanel.add(spriteCenterEditorButton);
        rightPanel.add(collidersEditorButton);
        rightPanel.add(pointsEditorButton);
        
        add(rootPanel);
    }

    private class ActorSelectionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            selectedActor = (Actor) source.getClientProperty("actor");
            System.out.println(selectedActor.path);
            selectedActorLable.setText("selected actor: " + selectedActor.name);
            repaint();
        }

    }
    
    private class EditorSelectionHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedActor == null) {
                return;
            }
            
            JButton source = (JButton) e.getSource();
            int selectedEditor = (Integer) source.getClientProperty("option");
            switch (selectedEditor) {
                case 0:
                    SpriteSheetEditorView.show(selectedActor);
                    break;
                case 1: 
                    SpriteCenterEditorView.show(selectedActor);
                    break;
                case 2: 
                    ColliderEditorView.show(selectedActor);
                    break;
                case 3: 
                    SpritePointsEditor.show(selectedActor);
                    break;
            }
        }

    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ViewMenu view = new ViewMenu();
            view.setPreferredSize(new Dimension((int) (276 * 2.5), (int) (207 * 3)));
            JFrame frame = new JFrame();
            frame.setTitle("Java Golden Axe Animation Editor");
            frame.getContentPane().add(view);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
        });
    }

}
