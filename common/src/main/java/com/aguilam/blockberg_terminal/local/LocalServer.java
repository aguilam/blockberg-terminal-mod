package com.aguilam.blockberg_terminal.local;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalServer {
    private static Process serverProcess;
    public static Path gameDir;
    public static void startLocalServer(String args) {
        if (serverProcess != null && serverProcess.isAlive()) {
            return;
        }
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String binaryName;
            if (os.contains("win")) {
                binaryName = "server-windows-amd64.exe";
            } else {
                return;
            }

            Path binDir = gameDir.resolve("blockberg-terminal").resolve("bin");
            Files.createDirectories(binDir);
            File targetExe = new File(binDir.toFile(), binaryName);
            if (!Files.exists(targetExe.toPath())) {
                String resourcePath = "/assets/blockberg-terminal/bin/" + binaryName;

                try(InputStream is = LocalServer.class.getResourceAsStream(resourcePath)) {
                    Files.copy(is, targetExe.toPath());
                }
            }

            targetExe.setExecutable(true);
            ProcessBuilder pb = new ProcessBuilder(targetExe.getAbsolutePath(),args);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);

            serverProcess = pb.start();
            System.out.println("Local server started");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (serverProcess != null && serverProcess.isAlive()) {
                    serverProcess.destroy();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void stopLocalServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
            serverProcess = null;
        }
    }
}
