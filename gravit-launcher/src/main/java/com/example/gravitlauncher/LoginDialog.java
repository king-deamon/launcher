package com.example.gravitlauncher;

import javax.swing.*;
import java.awt.*;

/**
 * Simple login dialog collecting username and password.
 * In this example the credentials are not verified and used only to
 * display the player name when launching the game.
 */
public class LoginDialog extends JDialog {
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private boolean confirmed = false;

    public LoginDialog(Frame owner) {
        super(owner, "Login", true);
        setSize(300, 150);
        setLayout(new BorderLayout(5,5));
        setLocationRelativeTo(owner);

        JPanel fields = new JPanel(new GridLayout(2,2,4,4));
        fields.add(new JLabel("Username:"));
        fields.add(username);
        fields.add(new JLabel("Password:"));
        fields.add(password);
        add(fields, BorderLayout.CENTER);

        JButton ok = new JButton("OK");
        ok.addActionListener(e -> { confirmed = true; setVisible(false); });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> { confirmed = false; setVisible(false); });
        JPanel btns = new JPanel();
        btns.add(ok); btns.add(cancel);
        add(btns, BorderLayout.SOUTH);
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public String getUsername() { return username.getText().trim(); }
    public String getPassword() { return new String(password.getPassword()); }
}
