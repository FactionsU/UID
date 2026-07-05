package dev.kitteh.factions.data.json;

import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;

@NullMarked
public class JsonSaver {
    private static final HashMap<String, Lock> locks = new HashMap<>();

    public static void write(Path path, Supplier<String> content, boolean sync) {
        final Lock lock = locks.computeIfAbsent(path.toAbsolutePath().toString(), _ -> new ReentrantReadWriteLock().writeLock());

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
            Path temp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.writeString(temp, content.get());
            try {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to write file " + path.toAbsolutePath(), e);
        } finally {
            lock.unlock();
        }
    }
}
