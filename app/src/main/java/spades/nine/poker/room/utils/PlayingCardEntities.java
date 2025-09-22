package spades.nine.poker.room.utils;

import java.util.HashMap;
import java.util.Map;

import spades.nine.poker.room.entity.PlayingCardEntity;

public class PlayingCardEntities {
    private PlayingCardEntities() {}

    public static PlayingCardEntity sampleInstance() {
        Map<String, Object> project = new HashMap<>();
        project.put("desc", "DB security summary metric group development");
        project.put("metrics", new String[] {
            "FGA",
            "Oracle wallet",
            "Privilege analysis",
            "Privileges & Roles",
            "Unified auditing",
            "Virtual Private Database",
            "Database Vault"
        });
        Map<String, Object> content = new HashMap<>();
        content.put("startDate", "2024.09");
        content.put("endDate", "2025.08");
        content.put("projects", new Object[]{
            "Jersey package uptake for Enterprise Manager REST client",
            project,
            "Performance metrics expansion to Autonomous DB targets"
        });

        PlayingCardEntity sample =  new PlayingCardEntity();
        sample.setHeading("Proven experience");
        sample.setLabel("Oracle Corporation");
        sample.setSublabel("Member of Technical Staff");
        sample.setContent(content);
        return sample;
    }
}
