package dev.kitteh.factions.config;

import com.google.common.reflect.TypeToken;
import com.typesafe.config.ConfigRenderOptions;
import dev.kitteh.factions.config.annotation.Comment;
import dev.kitteh.factions.config.annotation.ConfigName;
import dev.kitteh.factions.config.annotation.DefinedType;
import dev.kitteh.factions.config.annotation.WipeOnReload;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApiStatus.Internal
public class Loader {
    public static void loadAndSave(String file, Object config) throws IOException, IllegalAccessException {
        HoconConfigurationLoader loader = getLoader(file);
        loadAndSave(loader, config);
    }

    public static HoconConfigurationLoader getLoader(String file) {
        Path configFolder = AbstractFactionsPlugin.instance().getDataFolder().toPath().resolve("config");
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdir();
        }
        Path path = configFolder.resolve(file + ".conf");
        return HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(ConfigurationOptions.defaults().withNativeTypes(null))
                .setRenderOptions(ConfigRenderOptions.defaults().setComments(true).setOriginComments(false).setJson(false))
                .build();
    }

    public static void loadAndSave(HoconConfigurationLoader loader, Object config) throws IOException, IllegalAccessException {
        CommentedConfigurationNode node = loader.load();
        CommentedConfigurationNode newNode = loader.createEmptyNode();

        loadNode(node, newNode, config);

        loader.save(newNode);
    }

    private static final Set<Class<?>> types = new HashSet<>();

    static {
        types.add(Boolean.TYPE);
        types.add(Byte.TYPE);
        types.add(Character.TYPE);
        types.add(Double.TYPE);
        types.add(Float.TYPE);
        types.add(Integer.TYPE);
        types.add(Long.TYPE);
        types.add(Short.TYPE);
        types.add(List.class);
        types.add(Map.class);
        types.add(Set.class);
        types.add(String.class);
    }

    @SuppressWarnings("unchecked")
    private static void loadNode(CommentedConfigurationNode current, CommentedConfigurationNode newNode, Object object) throws IllegalAccessException {
        for (Field field : getFields(object.getClass())) {
            if (field.isSynthetic()) {
                continue;
            }
            if ((field.getModifiers() & Modifier.TRANSIENT) != 0) {
                if (field.getAnnotation(WipeOnReload.class) != null) {
                    field.setAccessible(true);
                    field.set(object, null);
                }
                continue;
            }
            field.setAccessible(true);
            ConfigName configName = field.getAnnotation(ConfigName.class);
            Comment comment = field.getAnnotation(Comment.class);
            DefinedType definedType = field.getAnnotation(DefinedType.class);
            String confName = configName == null || configName.value().isEmpty() ? field.getName() : configName.value();
            CommentedConfigurationNode curNode = current.getNode(confName);
            CommentedConfigurationNode newNewNode = newNode.getNode(confName);
            boolean needsValue = curNode.isVirtual() || curNode.getValue() == null || field.getAnnotation(WipeOnReload.class) != null;
            if (comment != null) {
                newNewNode.setComment(comment.value());
            }
            Object defaultValue = field.get(object);
            if (types.contains(field.getType())) {
                if (needsValue) {
                    if (definedType == null) {
                        newNewNode.setValue(defaultValue);
                    } else {
                        try {
                            Field tokenField = field.getDeclaringClass().getDeclaredField(field.getName() + "Token");
                            tokenField.setAccessible(true);
                            newNewNode.setValue((TypeToken<Object>) tokenField.get(object), defaultValue);
                        } catch (ObjectMappingException | NoSuchFieldException e) {
                            AbstractFactionsPlugin.instance().getLogger().severe("Failed horrifically to handle " + confName);
                        }
                    }
                } else {
                    try {
                        if (Set.class.isAssignableFrom(field.getType()) && List.class.isAssignableFrom(curNode.getValue().getClass())) {
                            field.set(object, new HashSet<Object>((List<?>) curNode.getValue()));
                        } else {
                            field.set(object, curNode.getValue());
                        }
                        newNewNode.setValue(curNode.getValue());
                    } catch (IllegalArgumentException ex) {
                        AbstractFactionsPlugin.instance().getLogger().severe("Found incorrect type for " + getNodeName(curNode.getPath()) + ": Expected " + field.getType() + ", found " + curNode.getValue().getClass());
                        field.set(object, defaultValue);
                    }
                }
            } else {
                if (defaultValue == null) {
                    curNode.setValue(null);
                    newNewNode.setValue(null);
                } else {
                    loadNode(curNode, newNewNode, defaultValue);
                }
            }
        }
    }

    private static List<Field> getFields(Class<?> clazz) {
        return getFields(new ArrayList<>(), clazz);
    }

    private static List<Field> getFields(List<Field> fields, Class<?> clazz) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != null) {
            getFields(fields, clazz.getSuperclass());
        }

        return fields;
    }

    private static String getNodeName(Object[] path) {
        StringBuilder builder = new StringBuilder();
        for (Object o : path) {
            if (o != null) {
                builder.append(o).append('.');
            }
        }
        return builder.substring(0, builder.length() - 1);
    }
}
