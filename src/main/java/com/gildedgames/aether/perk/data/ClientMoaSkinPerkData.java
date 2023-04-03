package com.gildedgames.aether.perk.data;

import com.gildedgames.aether.capability.player.AetherPlayer;
import com.gildedgames.aether.network.AetherPacketHandler;
import com.gildedgames.aether.network.packet.server.ServerMoaSkinPacket;
import com.gildedgames.aether.perk.CustomizationsOptions;
import com.gildedgames.aether.perk.types.MoaData;
import com.gildedgames.aether.perk.types.MoaSkins;
import com.aetherteam.nitrogen.api.users.User;
import com.aetherteam.nitrogen.api.users.UserData;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientMoaSkinPerkData extends ClientPerkData<MoaData> {
    public static final ClientMoaSkinPerkData INSTANCE = new com.gildedgames.aether.perk.data.ClientMoaSkinPerkData();
    private static final Map<UUID, MoaData> CLIENT_USER_SKIN_DATA = new HashMap<>();

    @Override
    public void syncFromClient(Player player) {
        if (this.canSync(player)) {
            User user = UserData.Client.getClientUser();
            UUID uuid = player.getUUID();
            AetherPlayer.get(player).ifPresent((aetherPlayer) -> {
                UUID lastRiddenMoa = aetherPlayer.getLastRiddenMoa();
                CustomizationsOptions.INSTANCE.load();
                String moaSkinName = CustomizationsOptions.INSTANCE.getMoaSkin();
                Map<String, MoaSkins.MoaSkin> moaSkins = MoaSkins.getMoaSkins();
                Map<UUID, MoaData> userSkinsData = this.getClientPerkData();
                if (moaSkinName != null && !moaSkinName.isEmpty() && moaSkins.containsKey(moaSkinName)) {
                    MoaSkins.MoaSkin moaSkin = MoaSkins.getMoaSkins().get(moaSkinName);
                    MoaData moaData = new MoaData(lastRiddenMoa, moaSkin);
                    if (!userSkinsData.containsKey(uuid)
                            || userSkinsData.get(uuid) == null
                            || (userSkinsData.get(uuid).moaUUID() == null && moaData.moaUUID() != null)
                            || (userSkinsData.get(uuid).moaSkin() == null && moaData.moaSkin() != null)
                            || (userSkinsData.get(uuid).moaUUID() != null && moaData.moaUUID() != null && !userSkinsData.get(uuid).moaUUID().equals(moaData.moaUUID()))
                            || (userSkinsData.get(uuid).moaSkin() != null && moaData.moaSkin() != null && !userSkinsData.get(uuid).moaSkin().equals(moaData.moaSkin()))) {
                        if (moaSkin.getUserPredicate().test(user)) {
                            AetherPacketHandler.sendToServer(new ServerMoaSkinPacket.Apply(player.getUUID(), new MoaData(lastRiddenMoa, moaSkin)));
                        }
                    }
                } else if ((moaSkinName == null || moaSkinName.isEmpty()) && userSkinsData.containsKey(uuid) && userSkinsData.get(uuid) != null && (userSkinsData.get(uuid).moaUUID() != null || userSkinsData.get(uuid).moaSkin() != null)) {
                    AetherPacketHandler.sendToServer(new ServerMoaSkinPacket.Remove(player.getUUID()));
                }
            });
        }
    }

    @Override
    protected Map<UUID, MoaData> getMap() {
        return CLIENT_USER_SKIN_DATA;
    }
}
