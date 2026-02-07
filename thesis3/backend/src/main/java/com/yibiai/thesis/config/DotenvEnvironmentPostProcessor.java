package com.yibiai.thesis.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String userDir = System.getProperty("user.dir", ".");
        System.err.println("[DotenvPostProcessor] user.dir = " + userDir);

        java.util.List<Path> candidates = new java.util.ArrayList<>();
        candidates.add(Path.of(userDir, ".env"));
        candidates.add(Path.of(userDir, "../.env"));
        candidates.add(Path.of(userDir, "../../.env"));
        candidates.add(Path.of(".env"));
        candidates.add(Path.of("../.env"));
        candidates.add(Path.of("../../.env"));

        try {
            Path jarPath = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            Path jarDir = jarPath.getParent();
            if (jarDir != null) {
                candidates.add(jarDir.resolve(".env"));
                candidates.add(jarDir.resolve("../.env"));
                candidates.add(jarDir.resolve("../../.env"));
            }
        } catch (Exception ignored) {
        }

        for (Path p : candidates) {
            try {
                Path resolved = p.toAbsolutePath().normalize();
                System.err.println("[DotenvPostProcessor] checking: " + resolved + " exists=" + Files.exists(resolved));
                if (Files.exists(resolved)) {
                    Map<String, Object> props = parseDotenv(resolved);
                    if (!props.isEmpty()) {
                        Map<String, Object> mapped = new HashMap<>(props);
                        for (Map.Entry<String, Object> entry : props.entrySet()) {
                            System.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
                        }
                        Object dsKey = props.get("DEEPSEEK_API_KEY");
                        if (dsKey != null) {
                            mapped.put("deepseek.api.key", dsKey);
                        }
                        environment.getPropertySources().addFirst(
                                new MapPropertySource("dotenv-" + resolved, mapped)
                        );
                        System.err.println("[DotenvPostProcessor] Loaded " + mapped.size() + " properties from: " + resolved);
                    }
                    return;
                }
            } catch (Exception e) {
                System.err.println("[DotenvPostProcessor] error checking " + p + ": " + e.getMessage());
            }
        }
        System.err.println("[DotenvPostProcessor] No .env file found in any candidate path");
    }

    private Map<String, Object> parseDotenv(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        Map<String, Object> map = new HashMap<>();
        boolean first = true;
        for (String line : lines) {
            if (first) {
                first = false;
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
            }
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                value = value.substring(1, value.length() - 1);
            }
            map.put(key, value);
        }
        return map;
    }

    private Path[] concat(Path[] a, Path[] b) {
        Path[] result = new Path[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
