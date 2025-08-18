package com.chengcode.sgsmod.compaign;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;

public record CampaignDef(
        String id,
        String name,
        String faction,
        String era,
        String difficulty,
        List<String> reward,
        List<String> tags,
        String summary,
        String coverPath,
        CampaignStartCallback onStart
)
{
    public interface CampaignStartCallback {

        void run(PlayerEntity player);
    }
}
