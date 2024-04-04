package org.github.gestalt.config.metrics;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class MetricsManagerTest {

    @Test
    void startGetConfig() {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of(), false);

        metricsManager.finalizeGetConfig(marker, Tags.of());

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);
    }

    @Test
    void startGetConfigTagsStart() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of("test", "ok"), false);

        metricsManager.finalizeGetConfig(marker, Tags.of());

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startGetConfigTagsFinalize() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of(), false);

        metricsManager.finalizeGetConfig(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startGetConfigTagsBoth() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of("run", "1"), false);

        metricsManager.finalizeGetConfig(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startGetConfigMultipleMetricsRecorders() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var recorder2 = new TestMetricsRecorder(1);
        var metricsManager = new MetricsManager(List.of(recorder, recorder2));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of("run", "1"), false);

        metricsManager.finalizeGetConfig(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestMetricsRecord) marker.getMetricsRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startMetric() {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startMetric("db", Tags.of());

        metricsManager.finalizeMetric(marker, Tags.of());

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);
    }

    @Test
    void startMetricTagsStart() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startMetric("db", Tags.of("test", "ok"));

        metricsManager.finalizeMetric(marker, Tags.of());

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startMetricTagsFinalize() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startMetric("db", Tags.of());

        metricsManager.finalizeMetric(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startMetricTagsBoth() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        var marker = metricsManager.startMetric("db", Tags.of("run", "1"));

        metricsManager.finalizeMetric(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startMetricMultipleMetricsRecorders() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var recorder2 = new TestMetricsRecorder(1);
        var metricsManager = new MetricsManager(List.of(recorder, recorder2));

        var marker = metricsManager.startMetric("db", Tags.of("run", "1"));

        metricsManager.finalizeMetric(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestMetricsRecord) marker.getMetricsRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startAddMetricMultipleMetricsRecorders() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var recorder2 = new TestMetricsRecorder(1);
        var metricsManager = new MetricsManager(List.of(recorder));
        metricsManager.addMetricsRecorder(recorder2);

        var marker = metricsManager.startMetric("db", Tags.of("run", "1"));

        metricsManager.finalizeMetric(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestMetricsRecord) marker.getMetricsRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startAddMetricMultipleMetricsRecorders2() throws GestaltException {
        var recorder = new TestMetricsRecorder(0);
        var recorder2 = new TestMetricsRecorder(1);
        var metricsManager = new MetricsManager(List.of());
        metricsManager.addMetricsRecorders(List.of(recorder, recorder2));

        var marker = metricsManager.startMetric("db", Tags.of("run", "1"));

        metricsManager.finalizeMetric(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getMetricsRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestMetricsRecord.class, marker.getMetricsRecord(recorder.recorderId()));

        var testRecord = (TestMetricsRecord) marker.getMetricsRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestMetricsRecord) marker.getMetricsRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void recordMetric() {

        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        metricsManager.recordMetric("db", 5, Tags.of());

        var testRecord = (TestMetricsRecord) recorder.metrics.get("db");
        Assertions.assertEquals(5, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);
    }

    @Test
    void recordMetricTags() {

        var recorder = new TestMetricsRecorder(0);
        var metricsManager = new MetricsManager(List.of(recorder));

        metricsManager.recordMetric("db", 5, Tags.environment("dev"));

        var testRecord = (TestMetricsRecord) recorder.metrics.get("db");
        Assertions.assertEquals(5, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.environment("dev"), testRecord.tags);
    }

    @Test
    void recordMetricMultiple() {

        var recorder = new TestMetricsRecorder(0);
        var recorder2 = new TestMetricsRecorder(1);
        var metricsManager = new MetricsManager(List.of(recorder, recorder2));

        metricsManager.recordMetric("db", 5, Tags.of());

        var testRecord = (TestMetricsRecord) recorder.metrics.get("db");
        Assertions.assertEquals(5, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);

        var testRecord2 = (TestMetricsRecord) recorder2.metrics.get("db");
        Assertions.assertEquals(6, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of(), testRecord2.tags);
    }
}
