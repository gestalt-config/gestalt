package org.github.gestalt.config.micrometer.observations;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.micrometer.builder.MicrometerModuleConfigBuilder;
import org.github.gestalt.config.micrometer.config.MicrometerModuleConfig;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerObservationRecorderTest {

    @Test
    public void testRecorderId() {

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        Assertions.assertEquals("MicrometerObservationRecorder", recorder.recorderId());
    }

    @Test
    public void testGetMetric() throws GestaltException {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeClass(true)
            .setIncludePath(true)
            .setIncludeOptional(true)
            .setIncludeTags(true)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startGetConfig("test", TypeCapture.of(String.class), Tags.environment("dev"), true);

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("config.get", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags())
            .contains(Tag.of("path", "test"))
            .contains(Tag.of("class", "String"))
            .contains(Tag.of("optional", "true"))
            .contains(Tag.of("environment", "dev"));

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(marker, Tags.of("error", "none"));

        assertThat(registry.getMetersAsString())
            .startsWith("test.config.get(TIMER)[class='String', environment='dev', error='none', optional='true', path='test']; " +
                "count=1.0, total_time=");
    }

    @Test
    public void testGetMetricNoTags() {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeClass(false)
            .setIncludePath(false)
            .setIncludeOptional(false)
            .setIncludeTags(false)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startGetConfig("test", TypeCapture.of(String.class), Tags.environment("dev"), true);

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("config.get", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags()).isEmpty();

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(marker, Tags.of());

        assertThat(registry.getMetersAsString())
            .startsWith("test.config.get(TIMER)[]; count=1.0, total_time=");
    }

    @Test
    public void testGetGenericMetric() throws GestaltException {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeTags(true)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startObservation("reload", Tags.environment("dev"));

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("reload", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags())
            .contains(Tag.of("environment", "dev"));

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(marker, Tags.of("error", "none"));

        assertThat(registry.getMetersAsString())
            .startsWith("test.reload(TIMER)[environment='dev', error='none']; count=1.0, total_time=");
    }

    @Test
    public void testGetGenericMetricNoTags() {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeTags(false)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startObservation("reload", Tags.environment("dev"));

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("reload", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags()).isEmpty();

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(marker, Tags.of());

        assertThat(registry.getMetersAsString())
            .startsWith("test.reload(TIMER)[]; count=1.0, total_time=");
    }

    @Test
    public void testCountMetric() {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeTags(true)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        recorder.recordObservation("myMetric", 2.0, Tags.environment("dev"));

        assertThat(registry.getMetersAsString())
            .startsWith("test.myMetric(COUNTER)[environment='dev']; count=2.0");


        recorder.recordObservation("myMetric", 1, Tags.environment("dev"));

        assertThat(registry.getMetersAsString())
            .startsWith("test.myMetric(COUNTER)[environment='dev']; count=3.0");
    }

    @Test
    public void testNoConfig() throws GestaltException {

        GestaltConfig gestaltConfig = new GestaltConfig();

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startGetConfig("test", TypeCapture.of(String.class), Tags.environment("dev"), true);

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("config.get", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags()).isEmpty();

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(marker, Tags.of("error", "none"));

        assertThat(((SimpleMeterRegistry) recorder.getMeterRegistry()).getMetersAsString())
            .startsWith("gestalt.config.get(TIMER)[error='none']; count=1.0, total_time=");
    }

    @Test
    public void testFinalizeGetMetricWrongMetricType() throws GestaltException {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeClass(true)
            .setIncludePath(true)
            .setIncludeOptional(true)
            .setIncludeTags(true)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startGetConfig("test", TypeCapture.of(String.class), Tags.environment("dev"), true);

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("config.get", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags())
            .contains(Tag.of("path", "test"))
            .contains(Tag.of("class", "String"))
            .contains(Tag.of("optional", "true"))
            .contains(Tag.of("environment", "dev"));

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(null, Tags.of("error", "none"));

        assertThat(registry.getMetersAsString())
            .startsWith("");
    }

    @Test
    public void testFinalizeWrongMetricType() throws GestaltException {
        var registry = new SimpleMeterRegistry();
        MicrometerModuleConfig metricConfig = MicrometerModuleConfigBuilder
            .builder()
            .setIncludeClass(true)
            .setIncludePath(true)
            .setIncludeOptional(true)
            .setIncludeTags(true)
            .setPrefix("test")
            .setMeterRegistry(registry)
            .build();

        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(metricConfig);

        MicrometerObservationRecorder recorder = new MicrometerObservationRecorder();
        recorder.applyConfig(gestaltConfig);

        var marker = recorder.startObservation("myMetric", Tags.environment("dev"));

        Assertions.assertInstanceOf(MicrometerObservationRecord.class, marker);
        Assertions.assertEquals("myMetric", marker.metric());

        var metricsMarker = (MicrometerObservationRecord) marker;
        assertThat(metricsMarker.getTags()).contains(Tag.of("environment", "dev"));

        Assertions.assertNotNull(metricsMarker.getSample());

        recorder.finalizeObservation(null, Tags.of("error", "none"));

        assertThat(registry.getMetersAsString()).startsWith("");
    }
}
