package goldenroad.main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import goldenroad.game.GamePanel;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Golden Road Prototype");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(true);

            GamePanel gamePanel = new GamePanel();
            window.add(gamePanel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);

            gamePanel.startGameLoop();
        });
    }
}
