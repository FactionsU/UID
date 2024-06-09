package dev.kitteh.factions.util;

import dev.kitteh.factions.FactionsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;

public class DiscUtil {
    private static final HashMap<String, Lock> locks = new HashMap<>();

    public static void writeCatch(final File file, final String content, boolean sync) {
        writeCatch(file, () -> content, sync);
    }

    public static void writeCatch(File file, Supplier<String> content, boolean sync) {
        final Lock lock = locks.computeIfAbsent(file.getName(), n -> new ReentrantReadWriteLock().writeLock());

        if (sync) {
            writeCatch(lock, file, content);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    writeCatch(lock, file, content);
                }
            }.runTaskAsynchronously(FactionsPlugin.getInstance());
        }
    }

    private static void writeCatch(Lock lock, File file, Supplier<String> content) {
        lock.lock();
        try {
            Files.writeString(file.toPath(), content.get());
        } catch (IOException e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to write file " + file.getAbsolutePath(), e);
        } finally {
            lock.unlock();
        }
    }

    public static String readCatch(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to read file " + file.getAbsolutePath(), e);
            return null;
        }
    }
}
