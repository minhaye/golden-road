package goldenroad.main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import goldenroad.game.GamePanel;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Golden Road Prototype");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(1980, 1080);

            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setFocusable(true);
            window.setResizable(false);
            window.setVisible(true);
            gamePanel.loadMap();
            gamePanel.startGameLoop();
        });
    }

    
}
