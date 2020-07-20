package com.massivecraft.factions.config.transition.oldclass.v0;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.massivecraft.factions.util.material.MaterialDb;
import org.bukkit.Material;

import java.io.IOException;

public class MaterialAdapter extends TypeAdapter<Material> {

    @Override
    public void write(JsonWriter out, Material value) throws IOException {
        out.value(value.name());
    }

    @Override
    public Material read(JsonReader in) throws IOException {
        return MaterialDb.get(in.nextString());
    }

}
