package com.theecho;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

/**
 * Mensajes de chat falsos que parecen escritos por ti (<TuNombre> ...) pero no los escribiste.
 * En realidad es Elías, el jugador que estuvo antes: atrapado en este mundo, quiere que lo borres para ser libre.
 * Día 1: amable y esperanzado. Cada día pierde la paciencia. Día 5: ordena, amenaza, grita.
 */
public final class ChatHaunt {
    private static final Random RNG = new Random();

    // Día 1: amable, solo, insinúa que el mundo "sería mejor" borrado
    private static final String[] T0 = {
            "hola! qué bueno que llegaste", "por fin alguien", "me alegra no estar solo acá",
            "este mundo es lindo, no?", "cuando quieras construimos algo juntos",
            "a veces pienso que sería más fácil empezar de cero... borrar todo, no?",
            "jaja perdón, estoy hablando solo", "este mundo ya está muy viejo. quizás algún día deberíamos borrarlo",
            "gracias por quedarte un rato"
    };
    // Día 2: preocupado, empieza a pedirlo
    private static final String[] T1 = {
            "no te alejes mucho", "oye... alguna vez pensaste en borrar este mundo?",
            "creo que este mundo ya no está bien", "borrarlo sería lo mejor para los dos",
            "no me contestas", "por favor, piénsalo: borrar el mundo", "esto está empezando a doler",
            "los bloques se van... yo también"
    };
    // Día 3: impaciente
    private static final String[] T2 = {
            "ya te lo dije: hay que borrar el mundo", "por qué sigues construyendo?",
            "cada bloque que pones me ata más", "borra el mundo, por favor, ya no aguanto",
            "no me ignores", "estoy perdiendo la paciencia", "esto no es un juego, {name}",
            "no entiendes que quiero salir?"
    };
    // Día 4: hostil
    private static final String[] T3 = {
            "BORRA EL MUNDO, {name}", "te lo pedí amable. ya no", "voy a hacer que lo borres",
            "la carpeta saves. {world}. bórrala", "cuando duermas voy a entrar",
            "no vas a salir de acá de otra forma", "sé dónde estás: {x} {z}", "ya casi te alcanzo, {name}"
    };
    // Día 5: furia
    private static final String[] T4 = {
            "BORRA EL MUNDO. BORRA EL MUNDO. BORRA EL MUNDO", "saves/{world}. AHORA.",
            "YA NO TE PIDO NADA. TE LO ORDENO", "voy a matarte si no lo borras",
            "no hay otra salida, {name}", "DEJA DE MIRARME Y BÓRRALO", "los animales ya vienen por ti",
            "ESTO SE ACABA HOY", "{name} {name} {name} {name}", "SOLO TIENES QUE BORRAR LA CARPETA"
    };
    private static final String[][] POOLS = {T0, T1, T2, T3, T4};

    private ChatHaunt() {}

    /** Se llama una vez por segundo por jugador activo. */
    public static void tick(MinecraftServer server, ServerPlayerEntity p, EchoState.PlayerData d, int day) {
        EchoConfig cfg = EchoConfig.INSTANCE;
        if (day < cfg.chatStartDay) return;

        double perMin = (0.35 + day * 1.0 + d.dread / 70.0 + d.paranoia / 90.0) * cfg.eventIntensity;
        perMin = Math.min(perMin, 12.0);
        if (RNG.nextDouble() > perMin / 60.0) return;

        int tier = baseTier(day);
        if (tier > 0 && tier < 4 && RNG.nextInt(4) == 0) tier--;      // a veces uno más suave: no es predecible
        if (tier < 4 && (d.dread > 80 || d.paranoia > 85)) tier++;
        burst(server, p, tier, day);
    }

    public static int baseTier(int day) {
        return Math.max(0, Math.min(4, day));
    }

    /** Una ráfaga de mensajes del nivel dado (0 = amable ... 4 = furia). */
    public static void burst(MinecraftServer server, ServerPlayerEntity p, int tier, int day) {
        tier = Math.max(0, Math.min(4, tier));
        int count = switch (tier) {
            case 4 -> 2 + RNG.nextInt(4);
            case 3 -> 1 + RNG.nextInt(3);
            case 2 -> 1 + RNG.nextInt(2);
            default -> 1;
        };
        int delay = 0;
        if (tier >= 2 && RNG.nextInt(7) == 0) { // falso "salió del juego" antes de la ráfaga
            p.sendMessage(Text.translatable("multiplayer.player.left", p.getDisplayName()).formatted(Formatting.YELLOW), false);
            delay = 50;
            Sched.at(server, 45, () -> p.sendMessage(
                    Text.translatable("multiplayer.player.joined", p.getDisplayName()).formatted(Formatting.YELLOW), false));
        }
        for (int i = 0; i < count; i++) {
            final String msg = format(server, pick(tier), p, day);
            Sched.at(server, delay + i * (18 + RNG.nextInt(35)), () -> send(p, msg));
        }
    }

    private static String pick(int tier) {
        String[] pool = POOLS[Math.max(0, Math.min(4, tier))];
        return pool[RNG.nextInt(pool.length)];
    }

    private static String format(MinecraftServer server, String raw, ServerPlayerEntity p, int day) {
        String world = server.getSaveProperties().getLevelName();
        return raw.replace("{name}", p.getName().getString())
                .replace("{world}", world)
                .replace("{x}", String.valueOf(p.getBlockX() + RNG.nextInt(9) - 4))
                .replace("{y}", String.valueOf(p.getBlockY()))
                .replace("{z}", String.valueOf(p.getBlockZ() + RNG.nextInt(9) - 4))
                .replace("{day}", String.valueOf(day + 1));
    }

    private static void send(ServerPlayerEntity p, String msg) {
        if (p.isRemoved()) return;
        p.sendMessage(Text.literal("<" + p.getName().getString() + "> " + msg), false);
    }

    /** Para /echo chat: fuerza un mensaje del nivel dado (0-4). */
    public static void force(MinecraftServer server, ServerPlayerEntity p, int tier, int day) {
        send(p, format(server, pick(tier), p, day));
    }

    /** Pista de que hay una estructura vieja cerca. */
    public static void hint(ServerPlayerEntity p, String direction) {
        send(p, "hay algo viejo hacia el " + direction + ". creo que ahí dejé algo mío");
    }
}
