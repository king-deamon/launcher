package com.example.gravitlauncher;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Simplified Minecraft launcher inspired by GravitLauncher.
 * Provides offline login, modpack download and game launching.
 */
public class GravitLauncher extends JFrame {
    private final JComboBox<ServerConfig> serverBox = new JComboBox<>();
    private final JTextField javaPath = new JTextField("java");
    private final JSpinner ramSpinner = new JSpinner(new SpinnerNumberModel(4096, 512, 32768, 256));
    private final JTextArea logArea = new JTextArea();
    private String playerName = "Player";

    private List<ServerConfig> servers;

    public GravitLauncher() throws Exception {
        super("GravitLauncher Example");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 520);
        setLayout(new BorderLayout(5,5));

        // Top panel
        JPanel top = new JPanel(new GridLayout(3,2,4,4));
        top.add(new JLabel("Java executable:")); top.add(javaPath);
        top.add(new JLabel("Select server:")); top.add(serverBox);
        top.add(new JLabel("RAM (MB):")); top.add(ramSpinner);
        add(top, BorderLayout.NORTH);

        // Log console
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Button panel
        JPanel btns = new JPanel();
        JButton prepBtn = new JButton("Prepare Instance");
        JButton launchBtn = new JButton("Launch");
        JButton loginBtn = new JButton("Login");
        btns.add(loginBtn); btns.add(prepBtn); btns.add(launchBtn);
        add(btns, BorderLayout.SOUTH);

        // Load servers
        servers = InstanceManager.loadConfigs();
        for (ServerConfig cfg : servers) serverBox.addItem(cfg);
        serverBox.setRenderer((list, value, idx, sel, focus) -> new JLabel(value.name));

        // Actions
        prepBtn.addActionListener(e -> runAsync(this::prepare));
        launchBtn.addActionListener(e -> runAsync(this::launch));
        loginBtn.addActionListener(e -> login());
    }

    private void login() {
        LoginDialog dlg = new LoginDialog(this);
        if (dlg.showDialog()) {
            playerName = dlg.getUsername();
            appendLog("Logged in as " + playerName + "\n");
        }
    }

    private void prepare() {
        try {
            ServerConfig cfg = (ServerConfig) serverBox.getSelectedItem();
            appendLog("== Preparing: " + cfg.name + " ==\n");
            InstanceManager.prepareInstance(cfg, logArea::append);
        } catch (Exception ex) {
            appendLog("Error: " + ex + "\n");
        }
    }

    private void launch() {
        try {
            ServerConfig cfg = (ServerConfig) serverBox.getSelectedItem();
            String javaExe = javaPath.getText().trim();
            int ram = (Integer) ramSpinner.getValue();
            String jar = Paths.get(cfg.instanceDir, cfg.gameJar).toString();

            ProcessBuilder pb = new ProcessBuilder(
                javaExe,
                "-Xmx" + ram + "M",
                "-jar", jar,
                "--server", cfg.name,
                "--player", playerName
            );
            pb.directory(Paths.get(cfg.instanceDir).toFile());
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            appendLog("== Launching " + cfg.name + " as " + playerName + " ==\n");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) appendLog(line + "\n");
            }
        } catch (Exception ex) {
            appendLog("Launch error: " + ex + "\n");
        }
    }

    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> logArea.append(text));
    }

    private void runAsync(Runnable r) {
        Executors.newSingleThreadExecutor().submit(r);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                new GravitLauncher().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
