package com.example.gravitlauncher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads and prepares modpack instances.
 */
public class InstanceManager {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Load list of servers from resource JSON. */
    public static List<ServerConfig> loadConfigs() throws IOException {
        try (InputStream in = InstanceManager.class.getResourceAsStream("/servers.json")) {
            return MAPPER.readValue(in, new TypeReference<List<ServerConfig>>(){});
        }
    }

    /** Download ZIP modpack and extract to instance dir. */
    public static void prepareInstance(ServerConfig cfg, ConsoleWriter log) throws IOException {
        Path inst = Paths.get(cfg.instanceDir);
        if (Files.exists(inst)) {
            log.append("Cleaning existing instance: " + cfg.name + "...\n");
            deleteDirectory(inst);
        }
        Files.createDirectories(inst);
        log.append("Downloading modpack for " + cfg.name + "...\n");
        Path zip = inst.resolve("pack.zip");
        downloadFile(cfg.modpackUrl, zip, log);
        log.append("Extracting modpack...\n");
        unzip(zip, inst);
        Files.delete(zip);
        log.append("Instance ready: " + cfg.name + "\n");
    }

    private static void downloadFile(String urlStr, Path target, ConsoleWriter log) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(target)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }
        }
    }

    private static void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path out = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    try (OutputStream os = Files.newOutputStream(out)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = zis.read(buf)) > 0) os.write(buf, 0, len);
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
             .sorted(Comparator.reverseOrder())
             .forEach(p -> {
                 try { Files.delete(p); } catch (IOException ignored) {}
             });
    }

    /** Simple console appender interface. */
    public interface ConsoleWriter { void append(String text); }
}
