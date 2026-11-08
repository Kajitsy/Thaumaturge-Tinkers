package xyz.kajitsy.thaumaturgetinker;

public enum KamiResourceKind {
    ICHOR("ichor", "ichor"),
    CLOTH("ichor_cloth", "ichorCloth"),
    ICHORIUM("ichorium", "ichorium"),
    NUGGET("ichor_nugget", "ichorNugget"),
    CAP("ichor_cap", "ichorCap"),
    ROD("ichorcloth_rod", "ichorclothRod"),
    NETHER_SHARD("nether_shard", "netherShard"),
    ENDER_SHARD("ender_shard", "enderShard");

    public final String id, original;

    KamiResourceKind(String id, String original) {
        this.id = id;
        this.original = original;
    }
}
