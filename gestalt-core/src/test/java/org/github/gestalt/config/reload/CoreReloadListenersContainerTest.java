package org.github.gestalt.config.reload;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class CoreReloadListenersContainerTest {

    @Test
    void registerListener() {

        var reloadContainer = new CoreReloadListenersContainer();

        TestListener listener1 = new TestListener();

        reloadContainer.registerListener(listener1);

        reloadContainer.reload();

        Assertions.assertEquals(1, listener1.atomicInt.get());

        reloadContainer.reload();

        Assertions.assertEquals(2, listener1.atomicInt.get());
    }

    @Test
    void removeListener() {

        var reloadContainer = new CoreReloadListenersContainer();

        TestListener listener1 = new TestListener();

        reloadContainer.registerListener(listener1);

        reloadContainer.reload();

        Assertions.assertEquals(1, listener1.atomicInt.get());

        reloadContainer.removeListener(listener1);
        reloadContainer.reload();

        Assertions.assertEquals(1, listener1.atomicInt.get());

        var listeners = reloadContainer.getListeners();
        Assertions.assertEquals(0, listeners.size());
    }

    @Test
    void gcedListener() {

        var reloadContainer = new CoreReloadListenersContainer();

        TestListener listener1 = new TestListener();

        reloadContainer.registerListener(listener1);

        reloadContainer.reload();

        Assertions.assertEquals(1, listener1.atomicInt.get());

        listener1 = null;
        System.gc();
        reloadContainer.reload();

        var listeners = reloadContainer.getListeners();
        Assertions.assertEquals(0, listeners.size());
    }

    static class TestListener implements CoreReloadListener {

        AtomicInteger atomicInt = new AtomicInteger(0);

        @Override
        public void reload() {
            atomicInt.addAndGet(1);
        }
    }
}
