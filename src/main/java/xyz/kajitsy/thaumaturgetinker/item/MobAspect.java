package xyz.kajitsy.thaumaturgetinker.item;

/**
 * The 17 aspects that can be harvested from mobs using the Blood Sword in build 164.
 */
public enum MobAspect {
    AQUA("aqua", "Aqua"),
    HUMANUS("humanus", "Humanus"),
    AER("aer", "Aer"),
    VOLATUS("volatus", "Volatus"),
    IGNIS("ignis", "Ignis"),
    PRAECANTATIO("praecantatio", "Praecantatio"),
    EXANIMIS("exanimis", "Exanimis"),
    CORPUS("corpus", "Corpus"),
    BESTIA("bestia", "Bestia"),
    VENENUM("venenum", "Venenum"),
    TERRA("terra", "Terra"),
    ALIENIS("alienis", "Alienis"),
    ITER("iter", "Iter"),
    METALLUM("metallum", "Metallum"),
    LIMUS("limus", "Limus"),
    PANNUS("pannus", "Pannus"),
    MESSIS("messis", "Messis");

    private final String tag;
    private final String name;

    MobAspect(String tag, String name) {
        this.tag = tag;
        this.name = name;
    }

    public String tag() {
        return tag;
    }

    public String displayName() {
        return name;
    }
}
