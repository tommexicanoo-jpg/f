# El Eco del Silencio (The Echo) v2.0 — Fabric 1.21.1

Proyecto fuente limpio (mappings Yarn). El `sources.jar` anterior estaba descompilado con nombres
ofuscados (`class_2960`...) y no se podía recompilar; esto lo reemplaza.

## Compilar
Requiere JDK 21. Con Gradle 8.8+ instalado:

    gradle build            (o ./gradlew build si copias gradlew + gradle/wrapper/gradle-wrapper.jar de la plantilla de Fabric)

El jar queda en `build/libs/the-echo-2.0.0.jar`. Va en `mods/` con Fabric Loader 0.16+ y Fabric API.
IntelliJ: abrir la carpeta como proyecto Gradle y ejecutar la tarea `build`.

## Qué hay de nuevo
- Los días se cuentan desde que instalas el mod (un mundo viejo ya no empieza con todo activado). Cada Eco
  respeta un tope por jugador y un enfriamiento; no spawnean por spawn natural ni se guardan en el mundo.
- Laberinto con pasillos de 3 bloques de ancho (techo a 4).
- Ecos sin nametag.
- Chat falso (`<TuNombre> ...`) en 4 niveles que escalan con los días.
- Ecos: normal, Sombra (skin negra), vuelan, corren y desaparecen, gritan, miran fijamente, caminan, de reojo.
- HUD: "DÍA N" arriba a la derecha y barra Miedo/Paranoia sobre la vida. Ofrendar al espejo baja los tres.
- Memoria del Terreno + Decadencia Silenciosa, El Registrador, paranoia auditiva, falsos bloques de lana,
  ruinas con notas, el Rito, y la corrupción/final.

## Arco de 5 días (v2.1)
- Día 1 amable, día 5 apocalipsis. Todo está ya configurado por defecto (`corruptionDay` y `endingDay` = 4, que es el DÍA 5 en pantalla).
- Día 5: tormenta y noche, mobs no hostiles te atacan, Ecos que te alcanzan, rayos, apagones. A los ~150 s te echa del mundo
  y debes borrar la carpeta `saves/<mundo>` tú mismo. Al hacerlo se crea `gracias.txt` en tu escritorio (el mod no borra nada).
  Se desactiva con `corruptionEnding=false` y/o `writeThanksFile=false`. Vigila `config/the_echo_pending.txt`.
- Lore: estructuras (ruinas y capillas con espejo) con el diario de Elías en 7 entradas.
- Al entrar por primera vez: título "Día 1 de ..." con campana.

## Comandos (op nivel 2)
/echo status | dread N | paranoia N | day N | spawn [observa|reojo|sombra|camina|corre|grita|vuela|lanza|registrador]
/echo chat 0-4 | intro | ruina | decadencia | liberar | hijack | escape | reset

## Config: config/the_echo.json
Todos los "Day" son días del Eco. `corruptionEnding=false` desactiva que el final te eche del mundo.
`eventIntensity` (1.0) multiplica la frecuencia de todo.
