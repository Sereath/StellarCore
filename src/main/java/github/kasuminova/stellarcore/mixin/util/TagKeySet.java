package github.kasuminova.stellarcore.mixin.util;

import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import javax.annotation.Nonnull;

public class TagKeySet extends AbstractObjectSet<String> {

    private final AbstractObjectSet<String> parent;
    private final Runnable onChanged;

    public TagKeySet(final AbstractObjectSet<String> parent, final Runnable onChanged) {
        this.parent = parent;
        this.onChanged = onChanged;
    }

    @Nonnull
    @Override
    public ObjectIterator<String> iterator() {
        return new TagKeyIterator(parent.iterator());
    }

    @Override
    public int size() {
        return parent.size();
    }

    @Override
    public boolean rem(final Object key) {
        boolean removed = super.rem(key);
        if (removed) {
            if (onChanged != null) {
                onChanged.run();
            }
        }
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        if (onChanged != null) {
            onChanged.run();
        }
    }

    private class TagKeyIterator implements ObjectIterator<String> {

        private final ObjectIterator<String> parent;

        private TagKeyIterator(final ObjectIterator<String> parent) {
            this.parent = parent;
        }

        @Override
        public void remove() {
            parent.remove();
            if (onChanged != null) {
                onChanged.run();
            }
        }

        @Override
        public String next() {
            return parent.next();
        }

        @Override
        public boolean hasNext() {
            return parent.hasNext();
        }

        @Override
        public int skip(final int n) {
            return parent.skip(n);
        }

    }

}
