package com.aguilam.blockberg_terminal.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.aguilam.blockberg_terminal.config.ConfigManager.ConfigData;
import com.aguilam.blockberg_terminal.local.utils;

public class LocalServer {
    private static Process serverProcess;
    public static Path gameDir;
    private static String ServerURL;

    public static String startLocalServer(ConfigData args) {
        if (serverProcess != null && serverProcess.isAlive()) {
            return ServerURL;
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String binaryName;
            if (os.contains("win")) {
                binaryName = "server-windows-amd64.exe";
            } else {
                throw new UnsupportedOperationException("Not supported os: " + os);
            }

            Path binDir = gameDir.resolve("blockberg-terminal").resolve("bin");
            Files.createDirectories(binDir);
            File targetExe = new File(binDir.toFile(), binaryName);
            if (!Files.exists(targetExe.toPath())) {
                String resourcePath = "/assets/blockberg-terminal/bin/" + binaryName;

                try(InputStream is = LocalServer.class.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        throw new FileNotFoundException("Resource not found: " + resourcePath);
                    }
                    Files.copy(is, targetExe.toPath());
                }
            }
            targetExe.setExecutable(true);
            List<String> stringArgs = utils.buildArgs(args);
            List<String> command = new ArrayList<>();
            command.add(targetExe.getAbsolutePath());
            command.addAll(stringArgs);
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            serverProcess = pb.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (serverProcess != null && serverProcess.isAlive()) {
                    serverProcess.destroy();
                }
            }));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Go Server Startup] " + line);
                    if (line.startsWith("SERVER_READY:")) {
                        String serverAddress = line.substring("SERVER_READY:".length()).trim();
                        
                        CompletableFuture.runAsync(() -> 
                            reader.lines().forEach(serLine -> System.out.println("[Go Server] " + serLine))
                        );
                        ServerURL = "http://" + serverAddress;
                        return ServerURL;
                    }
                }
            }
            System.out.println("Local server started");
            throw new IllegalStateException("Error while server start");
        } catch (Exception e) {
            throw new RuntimeException("Failed to start local server", e);
        }
    }
    public static void stopLocalServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            serverProcess = null;
        }
    }
}
