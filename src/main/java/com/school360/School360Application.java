package com.school360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

@SpringBootApplication
public class School360Application {

    public static void main(String[] args) {
        // Disable headless mode so Desktop API can launch browser
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(School360Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openIndexPageAfterStartup() {
        String url = "http://localhost:8080/index.html";
        System.out.println(">>> School360 Started Successfully! Opening Chrome browser at " + url + " <<<");

        boolean opened = false;
        String os = System.getProperty("os.name", "").toLowerCase();

        // 1. Attempt to launch Google Chrome directly
        try {
            if (os.contains("win")) {
                String localAppData = System.getenv("LOCALAPPDATA");
                String programFiles = System.getenv("ProgramFiles");
                String programFilesX86 = System.getenv("ProgramFiles(x86)");

                String[] chromePaths = {
                    programFiles != null ? programFiles + "\\Google\\Chrome\\Application\\chrome.exe" : null,
                    programFilesX86 != null ? programFilesX86 + "\\Google\\Chrome\\Application\\chrome.exe" : null,
                    localAppData != null ? localAppData + "\\Google\\Chrome\\Application\\chrome.exe" : null
                };

                for (String path : chromePaths) {
                    if (path != null && new java.io.File(path).exists()) {
                        new ProcessBuilder(path, url).start();
                        opened = true;
                        break;
                    }
                }

                if (!opened) {
                    new ProcessBuilder("cmd", "/c", "start", "chrome", url).start();
                    opened = true;
                }
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", "-a", "Google Chrome", url).start();
                opened = true;
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("google-chrome", url).start();
                opened = true;
            }
        } catch (Throwable e) {
            System.out.println("Chrome direct launch notice: " + e.getMessage() + ". Falling back to default browser...");
        }

        // 2. Fallback to default browser if direct Chrome launch did not succeed
        if (!opened) {
            try {
                if (!java.awt.GraphicsEnvironment.isHeadless() && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                } else if (os.contains("win")) {
                    new ProcessBuilder("cmd", "/c", "start", url).start();
                } else if (os.contains("mac")) {
                    new ProcessBuilder("open", url).start();
                } else if (os.contains("nix") || os.contains("nux")) {
                    new ProcessBuilder("xdg-open", url).start();
                }
            } catch (Throwable e) {
                System.err.println("Failed to automatically launch browser: " + e.getMessage());
            }
        }
    }
}

