package org.example.single_point_access;

import javax.swing.*;

public class GUIFrameSinglePointAccess {
    private static JFrame appFrame = initFrame();

    private static JFrame initFrame() {
        JFrame frame = new JFrame();
        frame.setSize(700, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }

    public static void changePanel(JPanel panel, String frameTitle){
        appFrame.setContentPane(panel);
        appFrame.setTitle(frameTitle);
        appFrame.getContentPane().revalidate();
        appFrame.getContentPane().repaint();
    }

    public static void showDialogMessage(String message){
        JOptionPane.showMessageDialog(appFrame, message);
    }
}