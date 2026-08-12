package com.tnts.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comando /tnts: utilidades del mod sin salir del juego.
 * <p>
 * - /tnts give &lt;nombre&gt; [cantidad]  — darte cualquier TNT o item del mod
 * - /tnts list                        — lista todos los items del mod
 * <p>
 * Requiere permiso de operador (2). Los nombres se aceptan con o sin
 * prefijo "tnts:".
 */
public class TntsCommands {

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("tnts")
                        .requires(ctx -> ctx.hasPermission(2))
                        .then(Commands.literal("give")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> give(ctx, 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> give(ctx,
                                                        IntegerArgumentType.getInteger(ctx, "count"))))))
                        .then(Commands.literal("list")
                                .executes(TntsCommands::list))
        );
    }

    private static int give(CommandContext<CommandSourceStack> ctx, int count) {
        CommandSourceStack src = ctx.getSource();
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("Este comando solo lo puede usar un jugador."));
            return 0;
        }
        String name = StringArgumentType.getString(ctx, "name");
        // acepta "mega_tnt" o "tnts:mega_tnt"
        String key = name.contains(":") ? name : "tnts:" + name;
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            src.sendFailure(Component.literal("No existe el item '" + name
                    + "'. Usa /tnts list para ver todos."));
            return 0;
        }
        ItemStack stack = new ItemStack(item, Math.min(count, 64));
        if (!player.getInventory().add(stack)) {
            // inventario lleno: se deja caer
            player.drop(stack, false);
        }
        src.sendSuccess(() -> Component.literal("§aTe has dado §f"
                + stack.getCount() + "x §a" + item.getDescription().getString()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        List<String> names = new ArrayList<>();
        for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
            if (key.getNamespace().equals("tnts")) {
                names.add(key.getPath());
            }
        }
        names.sort(Comparator.naturalOrder());
        src.sendSuccess(() -> Component.literal("§6Items de TNTs Locos (" + names.size()
                + "): §f" + String.join(", ", names)), true);
        return 1;
    }
}
