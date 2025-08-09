// SkillRegistry.java
package com.chengcode.sgsmod.skill;

import java.util.HashMap;
import java.util.Map;

public class SkillRegistry {
    private static final Map<String, Skill> SKILLS = new HashMap<>();

    public static void register(Skill skill) {
        SKILLS.put(skill.getName(), skill);
    }

    public static Skill get(String name) {
        return SKILLS.get(name);
    }

    public static Map<String, Skill> getAllSkills() {
        return SKILLS;
    }
}
