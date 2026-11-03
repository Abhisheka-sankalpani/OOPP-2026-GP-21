import view.LoginFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        try {

            // Use Cross-Platform Look and Feel
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to apply Look and Feel."
            );
        }

        // Start Application
        SwingUtilities.invokeLater(() -> {

            LoginFrame loginFrame = new LoginFrame();

            loginFrame.setVisible(true);

        });
    }
}