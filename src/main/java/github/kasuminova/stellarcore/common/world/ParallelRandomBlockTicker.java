package github.kasuminova.stellarcore.common.world;

import com.github.bsideup.jabel.Desugar;
import github.kasuminova.stellarcore.common.util.StellarEnvironment;
import github.kasuminova.stellarcore.shaded.org.jctools.queues.MpmcUnboundedXaddArrayQueue;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.*;
import java.util.stream.IntStream;

public class ParallelRandomBlockTicker {

    public static final ParallelRandomBlockTicker INSTANCE = new ParallelRandomBlockTicker();

    private final Queue<ChunkData> enqueuedChunks = new MpmcUnboundedXaddArrayQueue<>(1000);

    private World currentWorld = null;
    private Random currentRand = null;
    private Profiler profiler = null;

    private ParallelRandomBlockTicker() {
    }

    public void enqueueChunk(final Chunk chunk, final List<TickData> data) {
        enqueuedChunks.offer(new ChunkData(chunk, data));
    }

    public void execute(final World world, final Random rand, final Profiler profiler) {
        Queue<ChunkData> enqueuedChunks = this.enqueuedChunks;
        if (enqueuedChunks.isEmpty()) {
            return;
        }

        this.currentWorld = world;
        this.currentRand = rand;
        this.profiler = profiler;

        final boolean parallel = StellarEnvironment.shouldParallel();
        final int concurrency = parallel ? StellarEnvironment.getConcurrency() : 1;
        final List<List<RandomTickTask>> randomTickData = parallel ? Collections.synchronizedList(new LinkedList<>()) : new LinkedList<>();

        IntStream stream = parallel ? IntStream.range(0, concurrency).parallel() : IntStream.range(0, concurrency);
        stream.forEach(i -> {
            ChunkData data;
            while ((data = enqueuedChunks.poll()) != null) {
                List<RandomTickTask> collectedData = new ObjectArrayList<>();
                for (final TickData tickData : data.data()) {
                    List<RandomTickTask> tasks = getRandomTickData(data.chunk(), tickData);
                    if (tasks.isEmpty()) {
                        continue;
                    }
                    collectedData.addAll(tasks);
                }
                if (!collectedData.isEmpty()) {
                    randomTickData.add(collectedData);
                }
            }
        });

        for (final List<RandomTickTask> randomTickDatum : randomTickData) {
            executeTask(randomTickDatum);
        }

        enqueuedChunks.clear();
    }

    private static List<RandomTickTask> getRandomTickData(Chunk chunk, TickData tickData) {
        ExtendedBlockStorage storage = tickData.blockStorage();
        IntList lcgList = tickData.lcgList();
        int chunkXPos = chunk.x << 4;
        int chunkZPos = chunk.z << 4;
        List<RandomTickTask> enqueuedData = new ObjectArrayList<>(lcgList.size());
        IntListIterator it = lcgList.iterator();
        while (it.hasNext()) {
            int lcg = it.nextInt() >> 2;
            int x = lcg & 15;
            int y = lcg >> 16 & 15;
            int z = lcg >> 8 & 15;
            IBlockState blockState;
            blockState = storage.get(x, y, z);
            Block block;
            block = blockState.getBlock();

            if (block.getTickRandomly()) {
                BlockPos pos = new BlockPos(x + chunkXPos, y + storage.getYLocation(), z + chunkZPos);
                enqueuedData.add(new RandomTickTask(storage, pos, x, y, z));
            }
        }

        return enqueuedData;
    }

    private void executeTask(List<RandomTickTask> tickDataList) {
        Profiler profiler = this.profiler;
        profiler.startSection("randomTick");

        World world = currentWorld;
        Random rand = currentRand;
        for (final RandomTickTask tickData : tickDataList) {
            ExtendedBlockStorage storage = tickData.storage();
            IBlockState blockState = storage.get(tickData.storageX(), tickData.storageY(), tickData.storageZ());

            Block block = blockState.getBlock();
            if (block.getTickRandomly()) {
                block.randomTick(world, tickData.worldPos(), blockState, rand);
            }
        }

        profiler.endSection();
    }

    @Desugar
    public record ChunkData(Chunk chunk, List<TickData> data) {
    }

    @Desugar
    public record TickData(ExtendedBlockStorage blockStorage, IntList lcgList) {
    }

    @Desugar
    public record RandomTickTask(ExtendedBlockStorage storage, BlockPos worldPos, int storageX, int storageY, int storageZ) {
    }

}
