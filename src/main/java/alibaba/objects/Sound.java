package alibaba.objects;

public enum Sound {
    SNAKE_CHARMER_1("snake-charmer-1.ogg", false, 0.3f),
    SNAKE_CHARMER_2("snake-charmer-2.ogg", false, 0.3f),
    SNAKE_CHARMER_3("snake-charmer-3.ogg", false, 0.3f),
    BLOCKED("blocked.ogg", false, 0.3f),
    FLEE("flee.ogg", false, 0.3f),
    ERROR("error.ogg", false, 0.3f),
    TRIGGER("trigger.ogg", false, 0.3f),
    PC_ATTACK("pc_attack.ogg", false, 0.3f),
    PC_STRUCK("pc_struck.ogg", false, 0.3f),
    NPC_ATTACK("npc_attack.ogg", false, 0.3f),
    NPC_STRUCK("npc_struck.ogg", false, 0.3f),
    EVADE("evade.ogg", false, 0.3f);

    String file;
    boolean looping;
    float volume;

    private Sound(String name, boolean looping, float volume) {
        this.file = name;
        this.looping = looping;
        this.volume = volume;
    }

    public String getFile() {
        return this.file;
    }

    public boolean getLooping() {
        return this.looping;
    }

    public float getVolume() {
        return this.volume;
    }

}
