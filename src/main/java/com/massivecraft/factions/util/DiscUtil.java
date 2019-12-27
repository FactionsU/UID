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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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

    public static void write(File file, String content) throws IOException {
        writeBytes(file, content.getBytes(StandardCharsets.UTF_8));
    }

    public static String read(File file) throws IOException {
        return new String(readBytes(file), StandardCharsets.UTF_8);
    }

    // -------------------------------------------- //
    // CATCH
    // -------------------------------------------- //

    private static HashMap<String, Lock> locks = new HashMap<>();

    public static boolean writeCatch(final File file, final String content, boolean sync) {
        String name = file.getName();
        final Lock lock;

        // Create lock for each file if there isn't already one.
        if (locks.containsKey(name)) {
            lock = locks.get(name);
        } else {
            ReadWriteLock rwl = new ReentrantReadWriteLock();
            lock = rwl.writeLock();
            locks.put(name, lock);
        }

        if (sync) {
            lock.lock();
            try {
                write(file, content);
            } catch (IOException e) {
                FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to write file " + file.getAbsolutePath(), e);
            } finally {
                lock.unlock();
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    lock.lock();
                    try {
                        write(file, content);
                    } catch (IOException e) {
                        FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to write file " + file.getAbsolutePath(), e);
                    } finally {
                        lock.unlock();
                    }
                }
            }.runTaskAsynchronously(FactionsPlugin.getInstance());
        }

        return true; // don't really care but for some reason this is a boolean.
    }

    public static String readCatch(File file) {
        try {
            return read(file);
        } catch (IOException e) {
            return null;
        }
    }
}
