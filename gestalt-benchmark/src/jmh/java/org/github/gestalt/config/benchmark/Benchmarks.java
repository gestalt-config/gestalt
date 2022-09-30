package org.github.gestalt.config.benchmark;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ClassPathConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class Benchmarks {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private Gestalt gestalt;
        private Gestalt gestaltNoCache;

        @Setup
        public void setup() throws GestaltException {
            // Create a map of configurations we wish to inject.
            Map<String, String> configs = new HashMap<>();
            configs.put("db.hosts[0].password", "1234");
            configs.put("db.hosts[1].password", "5678");
            configs.put("db.hosts[2].password", "9012");
            configs.put("db.idleTimeout", "123");

            // using the builder to layer on the configuration files.
            // The later ones layer on and over write any values in the previous
            GestaltBuilder builder = new GestaltBuilder();
            gestalt = builder
                .addSource(new ClassPathConfigSource("/default.properties"))
                .addSource(new ClassPathConfigSource("/dev.properties"))
                .addSource(new MapConfigSource(configs))
                .build();

            // Load the configurations, this will throw exceptions if there are any errors.
            gestalt.loadConfigs();

            GestaltBuilder builderNoCache = new GestaltBuilder();
            gestaltNoCache = builderNoCache
                .addSource(new ClassPathConfigSource("/default.properties"))
                .addSource(new ClassPathConfigSource("/dev.properties"))
                .addSource(new MapConfigSource(configs))
                .useCacheDecorator(false)
                .build();

            // Load the configurations, this will thow exceptions if there are any errors.
            gestaltNoCache.loadConfigs();
        }
    }

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class Avgt extends Benchmarks {}
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class Thrpt extends Benchmarks {}

    @Benchmark
    public String GestaltConfig_String(BenchmarkState state) throws GestaltException {
        return state.gestalt.getConfig("http.pool.maxTotal", String.class);
    }

    @Benchmark
    public HttpPool GestaltConfig_Object(BenchmarkState state) throws GestaltException {
        return state.gestalt.getConfig("http.pool", HttpPool.class);
    }

    @Benchmark
    public String GestaltConfig_String_No_Cache(BenchmarkState state) throws GestaltException {
        return state.gestaltNoCache.getConfig("http.pool.maxTotal", String.class);
    }

    @Benchmark
    public HttpPool GestaltConfig_Object_No_Cache(BenchmarkState state) throws GestaltException {
        return state.gestaltNoCache.getConfig("http.pool", HttpPool.class);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    public Gestalt GestaltConfig_Setup(BenchmarkState state) throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new ClassPathConfigSource("/default.properties"))
            .addSource(new ClassPathConfigSource("/dev.properties"))
            .addSource(new MapConfigSource(configs))
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        return gestalt;
    }


    public static class HttpPool {

        public short maxTotal;
        public long maxPerRoute;
        public int validateAfterInactivity;
        public double keepAliveTimeoutMs = 6000;
        public int idleTimeoutSec = 10;
        public float defaultWait = 33.0F;

        public HttpPool() {

        }

        public short getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(short maxTotal) {
            this.maxTotal = maxTotal;
        }

        public long getMaxPerRoute() {
            return maxPerRoute;
        }

        public void setMaxPerRoute(long maxPerRoute) {
            this.maxPerRoute = maxPerRoute;
        }

        public int getValidateAfterInactivity() {
            return validateAfterInactivity;
        }

        public void setValidateAfterInactivity(int validateAfterInactivity) {
            this.validateAfterInactivity = validateAfterInactivity;
        }

        public double getKeepAliveTimeoutMs() {
            return keepAliveTimeoutMs;
        }

        public void setKeepAliveTimeoutMs(double keepAliveTimeoutMs) {
            this.keepAliveTimeoutMs = keepAliveTimeoutMs;
        }

        public int getIdleTimeoutSec() {
            return idleTimeoutSec;
        }

        public void setIdleTimeoutSec(int idleTimeoutSec) {
            this.idleTimeoutSec = idleTimeoutSec;
        }

        public float getDefaultWait() {
            return defaultWait;
        }

        public void setDefaultWait(float defaultWait) {
            this.defaultWait = defaultWait;
        }
    }
}
