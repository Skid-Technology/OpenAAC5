package me.konsolas.aac.world;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;
import me.konsolas.aac.protocol.ProtocolVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MaterialAlias {

    LILY_PAD("WATER_LILY", "LILY_PAD"),
    SKULL("SKULL_ITEM", "PLAYER_HEAD"),
    GRAY_DYE(() -> new ItemStack(Material.getMaterial("INK_SACK"), 1, (short) 8), () -> new ItemStack(Material.getMaterial("GRAY_DYE"))),
    LIME_DYE(() -> new ItemStack(Material.getMaterial("INK_SACK"), 1, (short) 10), () -> new ItemStack(Material.getMaterial("LIME_DYE"))),
    BONE_MEAL(() -> new ItemStack(Material.getMaterial("INK_SACK"), 1, (short) 15), () -> new ItemStack(Material.getMaterial("BONE_MEAL"))),
    BOOK_AND_QUILL("BOOK_AND_QUILL", "WRITABLE_BOOK"),
    GREY_GLASS_PANE("STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE"),
    COMMAND_BLOCK("COMMAND", "COMMAND_BLOCK"),
    CLOCK("WATCH", "CLOCK"),
    GREEN_STAINED_CLAY(() -> new ItemStack(Material.getMaterial("STAINED_CLAY"), 1, (short) 5), () -> new ItemStack(Material.getMaterial("GREEN_TERRACOTTA"))),
    GRAY_STAINED_CLAY(() -> new ItemStack(Material.getMaterial("STAINED_CLAY"), 1, (short) 14), () -> new ItemStack(Material.getMaterial("GRAY_TERRACOTTA"))),
    FARMLAND("SOIL", "FARMLAND"),
    PISTON_BASE("PISTON_BASE", "PISTON"),
    PISTON_STICKY_BASE("PISTON_STICKY_BASE", "STICKY_PISTON"),
    PISTON_EXTENSION("PISTON_EXTENSION", "PISTON_HEAD"),
    PISTON_MOVING("PISTON_MOVING_PIECE", "MOVING_PISTON"),
    COBWEB("WEB", "COBWEB"),
    FIREWORK("FIREWORK", "FIREWORK_ROCKET");

    public static final Set<Material> TRAPDOORS;

    public static final Set<Material> SPAWN_EGGS;

    public static final Set<Material> BEDS;

    public static final Set<Material> SHULKER_BOXES;

    public static final Set<Material> CHESTS;

    public static final Set<Material> WATER;

    public static final Set<Material> LAVA;

    public static final Set<Material> ICE;

    public static final Set<Material> CARPET;

    public static final Set<Material> HONEY;

    public static final Set<Material> INTERACTABLE;

    private final Supplier<ItemStack> itemSupplier;

    private final Material material;

    MaterialAlias(String legacy, String modern) {
        this(() -> new ItemStack(Material.getMaterial(legacy)), () -> new ItemStack(Material.getMaterial(modern)));
    }

    MaterialAlias(Supplier<ItemStack> legacy, Supplier<ItemStack> modern) {
        itemSupplier = ProtocolVersion.getServerVersion().isAtLeast(ProtocolVersion.V1_13) ? modern : legacy;
        material = itemSupplier.get().getType();
    }

    public ItemStack getItemStack() {
        return itemSupplier.get();
    }

    public Material getMaterial() {
        return material;
    }

    static {
        EnumSet<Material> values2 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values3 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values4 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values5 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values6 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values7 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values8 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values9 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values10 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values11 = EnumSet.noneOf(Material.class);
        EnumSet<Material> values12 = EnumSet.noneOf(Material.class);
        for (Material material : Material.values()) {
            if (material.name().contains("TRAPDOOR") || material.name().contains("TRAP_DOOR")) {
                values2.add(material);
            }
            if (material.name().contains("SPAWN_EGG")) {
                values3.add(material);
            }
            if (material.name().contains("DOOR")) {
                values12.add(material);
            }
            if (material.name().contains("FENCE_GATE")) {
                values12.add(material);
            }
            if (material.name().contains("BED") && material != Material.BEDROCK) {
                values4.add(material);
            }
            if (material.name().contains("SHULKER_BOX")) {
                values5.add(material);
            }
            if (material.name().endsWith("CHEST")) {
                values6.add(material);
            }
            if (material.name().endsWith("WATER")) {
                values7.add(material);
            }
            if (material.name().endsWith("LAVA")) {
                values8.add(material);
            }
            if (material.name().endsWith("ICE")) {
                values9.add(material);
            }
            if (material.name().contains("CARPET")) {
                values10.add(material);
            }
            if (material.name().contains("HONEY_BLOCK")) {
                values11.add(material);
            }
        }
        TRAPDOORS = Collections.unmodifiableSet(values2);
        SPAWN_EGGS = Collections.unmodifiableSet(values3);
        BEDS = Collections.unmodifiableSet(values4);
        SHULKER_BOXES = Collections.unmodifiableSet(values5);
        CHESTS = Collections.unmodifiableSet(values6);
        WATER = Collections.unmodifiableSet(values7);
        LAVA = Collections.unmodifiableSet(values8);
        ICE = Collections.unmodifiableSet(values9);
        CARPET = Collections.unmodifiableSet(values10);
        HONEY = Collections.unmodifiableSet(values11);
        INTERACTABLE = Collections.unmodifiableSet(values12);
    }
}
