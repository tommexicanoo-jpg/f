package com.theecho;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Estructuras abandonadas con el diario de Elías, el jugador que estuvo antes que tú.
 * Los pares son ruinas con un atril; los impares son capillas con un Espejo del Eco (que además te protegen).
 * Cada estructura nueva guarda la siguiente entrada del diario: la historia se descubre poco a poco.
 */
public final class Shrines {
    public static final String NOTE_PREFIX = "Diario de Elías";
    public static final int NOTE_COUNT = 7;
    private static final Random RNG = new Random();

    private static final String[][] NOTES = {
            { // 1
                    "Día 1.\n\nEncontré este mundo vacío y silencioso. Es hermoso.\n\nConstruí una cabaña junto al río y por primera vez en mucho tiempo me sentí en paz.",
                    "Tengo la sensación de que alguien me ayudó a elegir el sitio.\n\nPero estoy solo.\n\nSolo yo y el viento."
            },
            { // 2
                    "Día 2.\n\nLa ventana de la cabaña ya no está.\n\nPensé que fue un creeper. No hay cráteres. No hay nada.",
                    "Esta mañana la puerta estaba abierta y el suelo había vuelto a ser pasto, como si nunca hubiera pisado ahí.\n\nEl mundo me está olvidando."
            },
            { // 3
                    "Día 3.\n\nHay una figura blanca muy lejos, entre las montañas.\n\nCuando me detengo, se detiene. Cuando miro al suelo, baja la cabeza.\n\nMe copia.",
                    "No corre. No grita. Nunca se acerca si lo miro.\n\nPero cada vez que me quedo quieto, aparece un bloque más cerca.",
                    "Lo llamé el Registrador.\n\nPorque solo hace eso: registrarme."
            },
            { // 4
                    "Día 4.\n\nConstruí un espejo y le di mi primera pala. Todo se calmó un día entero.\n\nPero mi reflejo sonríe antes que yo.",
                    "Y en el chat aparecen mensajes con mi nombre. Dicen que borre el mundo.\n\nSon mis palabras.\n\nYo no las escribí."
            },
            { // 5
                    "Día 5.\n\nLo entendí.\n\nNo es algo que me persigue. Es lo que soy ahora. Lo que construyo queda grabado, y este mundo quiere olvidarlo todo para dejarme ir.",
                    "El Rito es simple: deshacer lo que hice. Devolver cada bloque.\n\nPero esta cabaña es lo único que me queda.\n\nNo puedo."
            },
            { // 6
                    "Seguí construyendo.\n\nEl cielo se puso gris como una foto vieja.\n\nLos animales dejaron de hacer ruido. Y después... vinieron por mí.\n\nLas vacas. Los aldeanos. Todos.",
                    "Los mensajes dejaron de ser amables.\n\nAhora gritan.\n\nEl juego mismo me pidió que lo borrara.\n\nFue lo único que no esperaba."
            },
            { // 7
                    "Si lees esto, yo ya no estoy.\n\nNo es una amenaza. Es lo que quiero.\n\nSoy yo el que escribe en tu chat. Perdóname por el miedo. No sabía cómo pedírtelo.",
                    "Cierra el juego. Abre la carpeta saves. Borra este mundo.\n\nCuando lo hagas, voy a dejarte algo en tu escritorio.\n\nUna sola palabra.",
                    "Gracias por leerme.\n\n- Elías"
            }
    };

    private Shrines() {}

    /** Cada ~15 s por jugador activo. */
    public static void tick(ServerWorld w, ServerPlayerEntity p, EchoState.PlayerData d, int day, EchoState st) {
        EchoConfig cfg = EchoConfig.INSTANCE;
        if (day < cfg.shrineDay || d.freed) return;
        int max = Math.min(NOTE_COUNT, 1 + (day - cfg.shrineDay) * 2);
        if (d.shrines >= max) return;
        if (RNG.nextInt(2) != 0) return;

        for (int i = 0; i < 16; i++) {
            double ang = RNG.nextDouble() * Math.PI * 2;
            double dist = 50 + RNG.nextDouble() * 45;
            int x = (int) Math.floor(p.getX() + Math.cos(ang) * dist);
            int z = (int) Math.floor(p.getZ() + Math.sin(ang) * dist);
            BlockPos origin = tryPlace(w, x, z, d.shrines);
            if (origin != null) {
                d.shrines++;
                st.markDirty();
                double dx = origin.getX() - p.getX(), dz = origin.getZ() - p.getZ();
                String dir = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? "este" : "oeste") : (dz > 0 ? "sur" : "norte");
                Sched.at(w.getServer(), 100 + RNG.nextInt(400), () -> ChatHaunt.hint(p, dir));
                return;
            }
        }
    }

    /** Construye la estructura en (x, z) si el terreno es llano y seco. Devuelve la esquina o null. */
    public static BlockPos tryPlace(ServerWorld w, int x, int z, int noteIndex) {
        for (int dx = 0; dx <= 6; dx += 6) {
            for (int dz = 0; dz <= 6; dz += 6) {
                if (!w.isChunkLoaded((x + dx) >> 4, (z + dz) >> 4)) return null;
            }
        }
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int dx = 0; dx <= 6; dx += 3) {
            for (int dz = 0; dz <= 6; dz += 3) {
                int y = w.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x + dx, z + dz);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                BlockState top = w.getBlockState(new BlockPos(x + dx, y - 1, z + dz));
                if (!top.getFluidState().isEmpty() || top.isOf(Blocks.ICE) || top.isOf(Blocks.LAVA)) return null;
            }
        }
        if (maxY - minY > 3) return null;

        BlockPos o = new BlockPos(x, maxY, z);
        build(w, o, noteIndex, noteIndex % 2 == 1);
        return o;
    }

    private static void build(ServerWorld w, BlockPos o, int noteIndex, boolean chapel) {
        int flags = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;
        for (int dx = 0; dx < 7; dx++) {
            for (int dz = 0; dz < 7; dz++) {
                BlockPos floor = o.add(dx, -1, dz);
                for (int k = 0; k < 4; k++) {
                    BlockPos fp = floor.down(k);
                    if (!w.getBlockState(fp).isAir() && !w.getBlockState(fp).isReplaceable() && k > 0) break;
                    BlockState fs = chapel
                            ? (RNG.nextInt(3) == 0 ? Blocks.DEEPSLATE_TILES : Blocks.POLISHED_DEEPSLATE).getDefaultState()
                            : (RNG.nextInt(3) == 0 ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE).getDefaultState();
                    w.setBlockState(fp, fs, flags);
                }
                boolean edge = dx == 0 || dz == 0 || dx == 6 || dz == 6;
                for (int h = 0; h < 6; h++) {
                    BlockPos p = o.add(dx, h, dz);
                    if (!w.getBlockState(p).isAir()) w.setBlockState(p, Blocks.AIR.getDefaultState(), flags);
                }
                if (edge) {
                    boolean door = (dx == 3 && dz == 6);
                    boolean backWall = dz == 0;
                    int hgt;
                    if (door) hgt = 0;
                    else if (chapel) hgt = backWall ? 4 : 2 + RNG.nextInt(3);
                    else hgt = 1 + RNG.nextInt(3);
                    if (!backWall && RNG.nextInt(7) == 0) hgt = 0; // muros rotos
                    for (int h = 0; h < hgt; h++) {
                        BlockState s;
                        if (chapel) {
                            s = switch (RNG.nextInt(4)) {
                                case 0 -> Blocks.DEEPSLATE_BRICKS.getDefaultState();
                                case 1 -> Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState();
                                case 2 -> Blocks.MOSSY_STONE_BRICKS.getDefaultState();
                                default -> Blocks.DEEPSLATE_TILES.getDefaultState();
                            };
                        } else {
                            s = switch (RNG.nextInt(4)) {
                                case 0 -> Blocks.MOSSY_STONE_BRICKS.getDefaultState();
                                case 1 -> Blocks.MOSSY_COBBLESTONE.getDefaultState();
                                case 2 -> Blocks.CRACKED_STONE_BRICKS.getDefaultState();
                                default -> Blocks.COBBLESTONE.getDefaultState();
                            };
                        }
                        w.setBlockState(o.add(dx, h, dz), s, flags);
                    }
                } else if (RNG.nextInt(9) == 0) {
                    w.setBlockState(o.add(dx, 0, dz), Blocks.COBWEB.getDefaultState(), flags);
                }
            }
        }

        // El atril con la entrada del diario
        BlockPos lec = chapel ? o.add(3, 0, 4) : o.add(3, 0, 3);
        Direction face = chapel ? Direction.NORTH : Direction.Type.HORIZONTAL.random(w.getRandom());
        BlockState lecState = Blocks.LECTERN.getDefaultState().with(LecternBlock.FACING, face).with(LecternBlock.HAS_BOOK, true);
        w.setBlockState(lec, lecState, Block.NOTIFY_ALL);
        if (w.getBlockEntity(lec) instanceof LecternBlockEntity le) le.setBook(makeNote(noteIndex));

        if (chapel) {
            // Un Espejo del Eco contra el muro del fondo: protege la zona (cuenta como espejo colocado)
            BlockState mirror = ModBlocks.ECHO_MIRROR.getDefaultState().with(net.minecraft.block.HorizontalFacingBlock.FACING, Direction.SOUTH);
            BlockPos m1 = o.add(3, 0, 1);
            w.setBlockState(m1, mirror, Block.NOTIFY_ALL);
            w.setBlockState(m1.up(), mirror, Block.NOTIFY_ALL);
            EchoManager.registerMirror(w, m1);
            w.setBlockState(o.add(2, 0, 4), Blocks.SOUL_LANTERN.getDefaultState(), flags);
            w.setBlockState(o.add(4, 0, 4), Blocks.SOUL_LANTERN.getDefaultState(), flags);
            w.setBlockState(o.add(1, 0, 1), Blocks.CANDLE.getDefaultState().with(CandleBlock.CANDLES, 3), flags);
            w.setBlockState(o.add(5, 0, 1), Blocks.CANDLE.getDefaultState().with(CandleBlock.CANDLES, 2), flags);
        } else {
            w.setBlockState(o.add(2, 0, 3), Blocks.CANDLE.getDefaultState(), flags);
            w.setBlockState(o.add(4, 0, 3), Blocks.CANDLE.getDefaultState(), flags);
        }
    }

    public static ItemStack makeNote(int index) {
        int i = Math.floorMod(index, NOTES.length);
        List<RawFilteredPair<Text>> list = new ArrayList<>();
        for (String s : NOTES[i]) list.add(RawFilteredPair.of(Text.literal(s)));
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT,
                new WrittenBookContentComponent(RawFilteredPair.of(NOTE_PREFIX + " (" + (i + 1) + ")"), "Elías", 0, list, true));
        return book;
    }

    /** ¿Lleva el jugador una página del diario de Elías? */
    public static boolean hasNote(ServerPlayerEntity p) {
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (!s.isOf(Items.WRITTEN_BOOK)) continue;
            WrittenBookContentComponent c = s.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            if (c != null && c.title().raw().startsWith(NOTE_PREFIX)) return true;
        }
        return false;
    }
}
