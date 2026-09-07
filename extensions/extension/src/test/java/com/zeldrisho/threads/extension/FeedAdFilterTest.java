package com.zeldrisho.threads.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/** Unit tests for {@link FeedAdFilter}. Pure JVM — no Android deps. */
public class FeedAdFilterTest {

    // --- fakes mirroring the obfuscated app surface (A05/A02/Ckh/CDh/DED) ---

    public static class FakeMedia {
        private final boolean ad;
        FakeMedia(boolean ad) { this.ad = ad; }
        public boolean DED() { return ad; }
    }

    public static class FakeFeedUnit {
        private final FakeMedia media;
        FakeFeedUnit(boolean ad) { this.media = new FakeMedia(ad); }
        public FakeMedia A05() { return media; }
    }

    public static class FakeThreadItem {
        private final FakeMedia media;
        FakeThreadItem(boolean ad) { this.media = new FakeMedia(ad); }
        public FakeMedia CDh() { return media; }
    }

    public static class FakeThread {
        private final List<FakeThreadItem> items;
        FakeThread(FakeThreadItem... items) { this.items = Arrays.asList(items); }
        public List<FakeThreadItem> Ckh() { return items; }
    }

    public static class FakeThreadUnit {
        private final FakeThread thread;
        FakeThreadUnit(FakeThread thread) { this.thread = thread; }
        public FakeThread A02() { return thread; }
    }

    public static class FakeAdHeader {
        public boolean DED() { return true; }
    }

    @Test
    public void nullAndEmptyPassthrough() {
        assertEquals(null, FeedAdFilter.filterAds(null));
        List<?> empty = Collections.emptyList();
        assertSame(empty, FeedAdFilter.filterAds(empty));
    }

    @Test
    public void allOrganicReturnsSameInstance() {
        List<Object> in = new ArrayList<>(Arrays.asList(new FakeFeedUnit(false), new FakeFeedUnit(false)));
        assertSame(in, FeedAdFilter.filterAds(in));
    }

    @Test
    public void directDedHeaderRemoved() {
        Object ad = new FakeAdHeader();
        Object post = new FakeFeedUnit(false);
        List<?> out = FeedAdFilter.filterAds(Arrays.asList(ad, post));
        assertEquals(1, out.size());
        assertSame(post, out.get(0));
    }

    @Test
    public void mediaDedRemoved() {
        Object ad = new FakeFeedUnit(true);
        Object post = new FakeFeedUnit(false);
        List<?> out = FeedAdFilter.filterAds(Arrays.asList(ad, post));
        assertEquals(1, out.size());
        assertSame(post, out.get(0));
    }

    @Test
    public void threadCarriedAdRemoved() {
        FakeThread thread = new FakeThread(new FakeThreadItem(false), new FakeThreadItem(true));
        Object adUnit = new FakeThreadUnit(thread);
        Object post = new FakeFeedUnit(false);
        List<?> out = FeedAdFilter.filterAds(Arrays.asList(adUnit, post));
        assertEquals(1, out.size());
        assertSame(post, out.get(0));
    }

    @Test
    public void immutableInputReturnsFilteredCopy() {
        // Regression guard: in-place Iterator.remove() on an immutable list
        // throws UnsupportedOperationException — the filter must copy.
        List<?> in = Collections.unmodifiableList(
                new ArrayList<>(Arrays.asList(new FakeFeedUnit(true), new FakeFeedUnit(false))));
        List<?> out = FeedAdFilter.filterAds(in);
        assertEquals(1, out.size());
    }

    @Test
    public void unknownShapeIsKeptNoCrash() {
        // R8 rename drift: objects without DED/A05/A02 must be kept, never crash.
        Object unknown = new Object();
        List<?> in = Collections.singletonList(unknown);
        assertSame(in, FeedAdFilter.filterAds(in));
    }

    @Test
    public void nullItemsKept() {
        Object post = new FakeFeedUnit(false);
        List<?> out = FeedAdFilter.filterAds(Arrays.asList(null, post));
        assertTrue(out.contains(null));
        assertTrue(out.contains(post));
    }
}
