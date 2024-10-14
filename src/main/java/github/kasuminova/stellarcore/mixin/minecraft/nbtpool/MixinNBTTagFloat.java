package github.kasuminova.stellarcore.mixin.minecraft.nbtpool;

import github.kasuminova.stellarcore.common.pool.NBTTagPrimitivePool;
import github.kasuminova.stellarcore.mixin.util.StellarPooledNBT;
import net.minecraft.nbt.NBTTagFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(NBTTagFloat.class)
public abstract class MixinNBTTagFloat implements StellarPooledNBT {

    @Override
    public Object stellar_core$getPooledNBT() {
        return NBTTagPrimitivePool.getTagFloat((NBTTagFloat) (Object) this);
    }

    /**
     * @author Kasumi_Nova
     * @reason Constant Tag.
     */
    @Nonnull
    @Overwrite
    public NBTTagFloat copy() {
        return (NBTTagFloat) (Object) this;
    }

    /**
     * @author Kasumi_Nova
     * @reason Constant Tag.
     */
    @Inject(method = "equals", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("RedundantCast")
    public void equals(final Object obj, final CallbackInfoReturnable<Boolean> cir) {
        if (obj == (Object) this) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

}
