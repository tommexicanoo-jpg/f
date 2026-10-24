package com.theecho;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Se guarda en config/the_echo.json.
 * Todos los "Day" son DÍAS DEL ECO (el contador de la esquina superior derecha, empezando en 0),
 * no días del mundo: así un mundo viejo no empieza con el terror ya a tope, y las ofrendas al espejo los reducen.
 */
public final class EchoConfig {
    public int configVersion = 3;

    // --- Arco de 5 días. Los "Day" son días del Eco EMPEZANDO EN 0: 0 = DÍA 1 en pantalla, 4 = DÍA 5 (el final) ---
    public int chatStartDay = 0;       // DÍA 1: mensajes amables que ya insinúan borrar el mundo
    public int audioStartDay = 1;      // pasos / puertas / cofres falsos
    public int echoStartDay = 1;       // primeras apariciones de Ecos
    public int registrarDay = 2;       // aparece El Registrador
    public int decayStartDay = 2;      // la Decadencia Silenciosa empieza a revertir tus bloques
    public int walkDay = 2;
    public int rushDay = 2;            // Ecos que corren hacia ti y desaparecen
    public int fakeBlockDay = 2;       // "aldeano" que en realidad es lana
    public int stareAnimalsDay = 2;    // aldeanos/animales que te miran fijamente
    public int shrineDay = 0;          // estructuras con notas y espejos (lore de Elías)
    public int screamDay = 3;          // Ecos que gritan
    public int flyDay = 3;             // Ecos que vuelan
    public int mimicChopDay = 2;       // un Eco imita que tala un árbol
    public int shadowDay = 3;          // Ecos de skin negra
    public int interfaceDay = 2;       // carteles que mutan, antorchas que parpadean
    public int hijackDay = 3;          // el Eco puede llevarte al laberinto al dormir
    public int corruptionDay = 4;      // DÍA 5: cielo sepia, todo se corrompe
    public int endingDay = 4;          // DÍA 5: el apocalipsis y el final

    // --- el final ---
    public boolean corruptionEnding = true;   // al terminar el apocalipsis, te echa y debes borrar el mundo
    public int apocalypseSeconds = 150;       // cuánto dura el caos del día 5 antes de echarte
    public boolean writeThanksFile = true;    // al borrar la carpeta del mundo, crea gracias.txt en el escritorio

    // --- sistema de terreno ---
    public boolean enableDecay = true;
    public double decayGraceDays = 0.5;   // días del mundo antes de que un bloque tuyo pueda revertirse
    public int riteTolerance = 6;         // bloques "tuyos" que pueden quedar y aun así completar el Rito

    // --- miedo / espejo ---
    public double eventIntensity = 1.0;   // multiplicador global de frecuencia de eventos
    public double dreadPerMinute = 4.0;
    public double hijackDread = 60.0;
    public double offeringDreadReduction = 35.0;
    public double offeringParanoiaReduction = 35.0;
    public int offeringDayRelief = 1;     // cuántos días baja el contador al ofrendar
    public int offeringBanishDays = 1;
    public int maxBanishDays = 5;
    public int mirrorRadius = 24;
    public int mazeSize = 7;              // celdas por lado del laberinto (5 a 11); los pasillos miden 3 de ancho
    public boolean enableRealm = true;

    public static EchoConfig INSTANCE = new EchoConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("the_echo.json");
        try {
            if (Files.exists(path)) {
                try (Reader r = Files.newBufferedReader(path)) {
                    EchoConfig c = GSON.fromJson(r, EchoConfig.class);
                    // Una config vieja anterior usaba otros días: se descarta para usar los nuevos valores
                    if (c != null && c.configVersion >= 3) INSTANCE = c;
                }
            }
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, w);
            }
        } catch (IOException | RuntimeException e) {
            TheEcho.LOGGER.error("No se pudo leer/escribir la config del Eco", e);
        }
    }
}
