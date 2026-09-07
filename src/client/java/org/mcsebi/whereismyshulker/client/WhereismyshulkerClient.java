package org.mcsebi.whereismyshulker.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.List;

public class WhereismyshulkerClient implements ClientModInitializer {
    private static final int ITEMS_PER_PAGE = 8;

    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ShulkerBoxTracker.getInstance().onWorldLoad());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ShulkerBoxTracker.getInstance().onWorldUnload());
        ClientPlayerBlockBreakEvents.AFTER.register((level, player, pos, state) -> {
            if (state.getBlock() instanceof ShulkerBoxBlock) ShulkerBoxTracker.getInstance().onShulkerBoxBroken(pos, level);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommands.literal("shulker")
                        .executes(context -> showShulkerList(context.getSource(), 1))
                        .then(ClientCommands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> showShulkerList(context.getSource(), IntegerArgumentType.getInteger(context, "page"))))
                        .then(ClientCommands.literal("clear").requires(FabricClientCommandSource::attended).executes(context -> {
                            ShulkerBoxTracker.getInstance().resetShulkerBoxes(false);
                            context.getSource().sendFeedback(Component.literal("Default shulker boxes have been reset.").withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))
                        .then(ClientCommands.literal("clearall").requires(FabricClientCommandSource::attended).executes(context -> {
                            ShulkerBoxTracker.getInstance().resetShulkerBoxes(true);
                            context.getSource().sendFeedback(Component.literal("All shulker boxes have been reset.").withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))));
    }

    private int showShulkerList(FabricClientCommandSource source, int page) {
        ShulkerBoxTracker tracker = ShulkerBoxTracker.getInstance();
        List<ShulkerBoxData> boxes = tracker.getShulkerBoxes();
        if (boxes.isEmpty()) {
            source.sendFeedback(Component.literal("No shulker boxes tracked yet!").withStyle(ChatFormatting.YELLOW));
            return 1;
        }
        int totalPages = (boxes.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if (page > totalPages) {
            source.sendError(Component.literal(totalPages == 1 ? "Invalid page number! There is only one page." : "Invalid page number! Valid pages: 1-" + totalPages));
            return 0;
        }
        int start = (page - 1) * ITEMS_PER_PAGE, end = Math.min(start + ITEMS_PER_PAGE, boxes.size());
        source.sendFeedback(Component.literal("=== Shulker Box Tracker ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        source.sendFeedback(Component.literal("Page " + page + " of " + totalPages + " (" + boxes.size() + " total)").withStyle(ChatFormatting.GRAY));
        BlockPos playerPos = source.getPlayer().blockPosition();
        String playerDim = ShulkerBoxTracker.dimensionId(source.getLevel());
        for (int i = start; i < end; i++) {
            ShulkerBoxData data = boxes.get(i);
            String name = (data.hasCustomName() ? data.getCustomName() : data.getColor() + " Shulker Box").trim();
            MutableComponent dimension = Component.empty(), distance = Component.empty().withStyle(ChatFormatting.DARK_GRAY);
            if (data.getDimension().equals(playerDim)) {
                int vertical = Math.abs(playerPos.getY() - data.getPosition().getY());
                String marker = data.getPosition().getY() < playerPos.getY() ? "v" : "^";
                distance = Component.literal(String.format(" [%d %s, %d %s]", (int) getHorizontalDistance(playerPos, data.getPosition()), getDirection(playerPos, data.getPosition()), vertical, marker)).withStyle(ChatFormatting.DARK_GRAY);
            } else {
                dimension = Component.literal("(").withStyle(ChatFormatting.GRAY).append(Component.literal(formatDimension(data.getDimension())).withStyle(ChatFormatting.WHITE)).append(Component.literal(")").withStyle(ChatFormatting.GRAY));
            }
            MutableComponent message = Component.literal((i + 1) + ". ").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal(name).withStyle(getColorFormatting(data.getColor())))
                    .append(Component.literal(" (").withStyle(ChatFormatting.GRAY)).append(createClickableCoords(data))
                    .append(Component.literal(") ").withStyle(ChatFormatting.GRAY)).append(dimension).append(distance);
            source.sendFeedback(message);
        }
        MutableComponent navigation = Component.empty();
        if (page > 1) navigation.append(nav("[← Prev]", page - 1)).append(" ");
        if (page < totalPages) navigation.append(nav("[Next →]", page + 1));
        if (!navigation.getString().isEmpty()) { source.sendFeedback(Component.empty()); source.sendFeedback(navigation); }
        return 1;
    }

    private MutableComponent nav(String label, int page) {
        return Component.literal(label).withStyle(style -> style.withColor(ChatFormatting.YELLOW)
                .withClickEvent(new ClickEvent.RunCommand("/shulker " + page))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Go to page " + page))));
    }

    private MutableComponent createClickableCoords(ShulkerBoxData data) {
        BlockPos p = data.getPosition();
        return Component.literal(p.getX() + ", " + p.getY() + ", " + p.getZ()).withStyle(style -> style.withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + p.getX() + " " + p.getY() + " " + p.getZ()))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to teleport"))));
    }

    private ChatFormatting getColorFormatting(String color) {
        return switch (color.toLowerCase()) { case "white" -> ChatFormatting.WHITE; case "orange", "brown" -> ChatFormatting.GOLD; case "magenta", "pink" -> ChatFormatting.LIGHT_PURPLE; case "light blue" -> ChatFormatting.AQUA; case "yellow" -> ChatFormatting.YELLOW; case "lime" -> ChatFormatting.GREEN; case "gray" -> ChatFormatting.DARK_GRAY; case "light gray" -> ChatFormatting.GRAY; case "cyan" -> ChatFormatting.DARK_AQUA; case "purple" -> ChatFormatting.DARK_PURPLE; case "blue" -> ChatFormatting.BLUE; case "green" -> ChatFormatting.DARK_GREEN; case "red" -> ChatFormatting.RED; case "black" -> ChatFormatting.BLACK; default -> ChatFormatting.LIGHT_PURPLE; };
    }

    private String getDirection(BlockPos from, BlockPos to) {
        double angle = Math.toDegrees(Math.atan2(-(to.getX() - from.getX()), to.getZ() - from.getZ()));
        if (angle < 0) angle += 360;
        return switch ((int) ((angle + 22.5) / 45) % 8) { case 0 -> "S"; case 1 -> "SW"; case 2 -> "W"; case 3 -> "NW"; case 4 -> "N"; case 5 -> "NE"; case 6 -> "E"; default -> "SE"; };
    }

    private double getHorizontalDistance(BlockPos from, BlockPos to) { return Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ()); }
    private String formatDimension(String dimension) { if (dimension.contains("overworld")) return "Overworld"; if (dimension.contains("the_nether")) return "Nether"; if (dimension.contains("the_end")) return "End"; return dimension; }
}
