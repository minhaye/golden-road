package goldenroad.main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import goldenroad.game.GamePanel;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Scientific Witchery");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            
            GamePanel gamePanel = new GamePanel();
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.add(gamePanel);
            window.pack();
            window.setSize(1980, 1080);
            window.setLocationRelativeTo(null);
            window.setFocusable(true);
            window.setResizable(false);
            window.setVisible(true);
            gamePanel.requestFocusInWindow();
            gamePanel.loadMap();
            gamePanel.loadParallax();
            gamePanel.startGameLoop();
        });
    }

    
}
