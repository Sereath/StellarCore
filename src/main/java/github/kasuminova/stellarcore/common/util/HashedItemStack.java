package github.kasuminova.stellarcore.common.util;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.Objects;

@Desugar
public record HashedItemStack(ItemStack stack, int stackHashCode, int damage, boolean hasTag) {

    public static HashedItemStack ofTag(final ItemStack stack) {
        ItemStack copied = stack.copy();
        return ofTagUnsafe(copied);
    }

    public static HashedItemStack ofTagUnsafe(final ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        boolean hasTag = tag != null && !tag.isEmpty();
        int hash;
        int damage = stack.isItemStackDamageable() ? stack.getItemDamage() : stack.getHasSubtypes() ? stack.getMetadata() : 0;
        if (hasTag) {
            hash = Objects.hash(stack.getItem(), damage, tag);
        } else {
            hash = Objects.hash(stack.getItem(), damage);
        }
        return new HashedItemStack(stack, hash, damage, hasTag);
    }

    public static HashedItemStack ofMeta(final ItemStack stack) {
        ItemStack copied = stack.copy();
        return ofMetaUnsafe(copied);
    }

    public static HashedItemStack ofMetaUnsafe(final ItemStack stack) {
        int metadata = stack.getMetadata();
        return new HashedItemStack(stack, Objects.hash(stack.getItem(), metadata), metadata, false);
    }

    public static String stackToString(final ItemStack stack) {
        String stackTagStr = null;
        String registryName = Objects.requireNonNull(stack.getItem().getRegistryName()).toString();
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && !tag.isEmpty()) {
            stackTagStr = tag.toString();
        }
        return strToBase64(registryName) + "_" + stack.getItemDamage() + (stackTagStr == null ? "" : "_" + strToBase64(stackTagStr));
    }

    public static String strToBase64(final String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof HashedItemStack hashedItemStack) {
            if (hasTag && !hashedItemStack.hasTag) {
                return false;
            }
            return stackEqualsNonNBT(stack, hashedItemStack.stack, damage, hashedItemStack.damage) && (!hasTag || ItemStack.areItemStackTagsEqual(stack, hashedItemStack.stack));
        }
        return false;
    }

    public static boolean stackEqualsNonNBT(@Nonnull ItemStack stack, @Nonnull ItemStack other, final int stackDamage, final int otherDamage) {
        if (stack.isEmpty() && other.isEmpty()) {
            return true;
        }
        if (stack.isEmpty() || other.isEmpty()) {
            return false;
        }
        Item sItem = stack.getItem();
        Item oItem = other.getItem();
        if (sItem.getHasSubtypes() || oItem.getHasSubtypes()) {
            return sItem == oItem && (
                    stackDamage == otherDamage ||
                    stackDamage == OreDictionary.WILDCARD_VALUE ||
                    otherDamage == OreDictionary.WILDCARD_VALUE);
        } else {
            return sItem.equals(other.getItem());
        }
    }

    public HashedItemStack copy() {
        return new HashedItemStack(stack.copy(), stackHashCode, damage, hasTag);
    }

    @Override
    public int hashCode() {
        return stackHashCode;
    }
}
