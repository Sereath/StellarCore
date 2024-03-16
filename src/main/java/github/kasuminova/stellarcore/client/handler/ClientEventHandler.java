package github.kasuminova.stellarcore.client.handler;

import github.kasuminova.stellarcore.StellarCore;
import github.kasuminova.stellarcore.client.profiler.PacketProfiler;
import github.kasuminova.stellarcore.client.profiler.TEUpdatePacketProfiler;
import github.kasuminova.stellarcore.client.util.TitleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class ClientEventHandler {
    public static final ClientEventHandler INSTANCE = new ClientEventHandler();

    private long clientTick = 0;

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        clientTick++;

        if (clientTick % 5 == 0) {
            TitleUtils.checkTitleState();
        }
    }
}
