package dev.kitteh.factions.data.json;

import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;

public class JsonSaver {
    private static final HashMap<String, Lock> locks = new HashMap<>();

    public static void write(Path path, Supplier<String> content, boolean sync) {
        final Lock lock = locks.computeIfAbsent(path.toAbsolutePath().toString(), n -> new ReentrantReadWriteLock().writeLock());

        if (sync) {
            write(lock, path, content);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    write(lock, path, content);
                }
            }.runTaskAsynchronously(AbstractFactionsPlugin.instance());
        }
    }

    private static void write(Lock lock, Path path, Supplier<String> content) {
        lock.lock();
        try {
            Files.writeString(path, content.get());
        } catch (IOException e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to write file " + path.toAbsolutePath(), e);
        } finally {
            lock.unlock();
        }
    }
}
