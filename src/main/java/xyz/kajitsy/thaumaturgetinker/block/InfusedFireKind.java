package xyz.kajitsy.thaumaturgetinker.block;

public enum InfusedFireKind {
    AIR, WATER, FIRE, EARTH, ORDER, ENTROPY;

    public String texturePrefix() {
        return switch (this) {
            case AIR -> "fireair";
            case WATER -> "firewater";
            case FIRE -> "firefire";
            case EARTH -> "fireearth";
            case ORDER -> "fireorder";
            case ENTROPY -> "firechaos";
        };
    }
}
