package com.chengcode.sgsmod.skill;

import java.util.List;

public class SkillData {
    private List<Skill> skills;

    public SkillData(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
}
