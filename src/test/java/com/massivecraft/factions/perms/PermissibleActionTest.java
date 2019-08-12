package com.massivecraft.factions.perms;


import com.massivecraft.factions.util.TL;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PermissibleActionTest {
    @BeforeAll
    public static void lang() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        for (TL item : TL.values()) {
            yamlConfiguration.set(item.getPath(), item.getDefault());
        }
        TL.setFile(yamlConfiguration);
    }

    @DisplayName("Translation existence confirmation")
    @Test
    public void test() {
        List<PermissibleAction> missing = new ArrayList<>();
        for (PermissibleAction action : PermissibleAction.values()) {
            if (action.getDescription() == null) {
                missing.add(action);
            }
        }
        if (!missing.isEmpty()) {
            Assertions.fail("Missing PermissibleAction translations: " + missing);
        }
    }
}
