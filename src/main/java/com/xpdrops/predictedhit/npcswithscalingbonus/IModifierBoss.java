package com.xpdrops.predictedhit.npcswithscalingbonus;

public interface IModifierBoss
{
    boolean containsId(int npcId);
    double getModifier(int npcId);
}
