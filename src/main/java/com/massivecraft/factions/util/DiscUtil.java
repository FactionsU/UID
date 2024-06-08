package com.massivecraft.factions.util;

import com.massivecraft.factions.FactionsPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.logging.Level;

public class DiscUtil {

    // -------------------------------------------- //
    // BYTE
    // -------------------------------------------- //


    public static byte[] readBytes(File file) throws IOException {
        int length = (int) file.length();
        byte[] output = new byte[length];
        InputStream in = new FileInputStream(file);
        int offset = 0;
        while (offset < length) {
            offset += in.read(output, offset, (length - offset));
        }
        in.close();
        return output;
    }

    public static void writeBytes(File file, byte[] bytes) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream out = new FileOutputStream(file);
        out.write(bytes);
        out.close();
    }

    // -------------------------------------------- //
    // STRING
    // -------------------------------------------- //

    static void write(File file, String content) throws IOException {
        writeBytes(file, content.getBytes(StandardCharsets.UTF_8));
    }

    public static String read(File file) throws IOException {
        return new String(readBytes(file), StandardCharsets.UTF_8);
    }

    // -------------------------------------------- //
    // CATCH
    // -------------------------------------------- //

    private static final HashMap<String, Lock> locks = new HashMap<>();


    public static boolean writeCatch(final File file, final String content, boolean sync) {
        write(file, () -> content, sync);

        return true; // don't really care but for some reason this is a boolean.
    }

    public static void write(File file, Supplier<String> content, boolean sync) {
        final Lock lock = locks.computeIfAbsent(file.getName(), n -> new ReentrantReadWriteLock().writeLock());

        if (sync) {
            write(lock, file, content);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    write(lock, file, content);
                }
            }.runTaskAsynchronously(FactionsPlugin.getInstance());
        }
    }

    private static void write(Lock lock, File file, Supplier<String> content) {
        lock.lock();
        try {
            write(file, content.get());
        } catch (IOException e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to write file " + file.getAbsolutePath(), e);
        } finally {
            lock.unlock();
        }
    }

    public static String readCatch(File file) {
        try {
            return read(file);
        } catch (IOException e) {
            return null;
        }
    }
}
