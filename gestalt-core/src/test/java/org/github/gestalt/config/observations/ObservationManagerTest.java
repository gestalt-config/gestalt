package org.github.gestalt.config.observations;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

class ObservationManagerTest {

    @Test
    void startGetConfig() {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of(), false);

        metricsManager.finalizeGetConfig(marker, Tags.of());

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);
    }

    @Test
    void startGetConfigTagsStart() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of("test", "ok"), false);

        metricsManager.finalizeGetConfig(marker, Tags.of());

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startGetConfigTagsFinalize() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of(), false);

        metricsManager.finalizeGetConfig(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startGetConfigTagsBoth() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of("run", "1"), false);

        metricsManager.finalizeGetConfig(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startGetConfigMultipleMetricsRecorders() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var recorder2 = new TestObservationRecorder(1);
        var metricsManager = new ObservationManager(List.of(recorder, recorder2));

        var marker = metricsManager.startGetConfig("db", TypeCapture.of(DBInfo.class), Tags.of("run", "1"), false);

        metricsManager.finalizeGetConfig(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestObservationRecord) marker.getObservationRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startObservation() {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startObservation("db", Tags.of());

        metricsManager.finalizeObservation(marker, Tags.of());

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);
    }

    @Test
    void startMetricTagsStart() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startObservation("db", Tags.of("test", "ok"));

        metricsManager.finalizeObservation(marker, Tags.of());

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startObservationTagsFinalize() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startObservation("db", Tags.of());

        metricsManager.finalizeObservation(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("test", "ok"), testRecord.tags);
    }

    @Test
    void startObservationTagsBoth() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        var marker = metricsManager.startObservation("db", Tags.of("run", "1"));

        metricsManager.finalizeObservation(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startObservationMultipleMetricsRecorders() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var recorder2 = new TestObservationRecorder(1);
        var metricsManager = new ObservationManager(List.of(recorder, recorder2));

        var marker = metricsManager.startObservation("db", Tags.of("run", "1"));

        metricsManager.finalizeObservation(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestObservationRecord) marker.getObservationRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startAddMetricMultipleMetricsRecorders() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var recorder2 = new TestObservationRecorder(1);
        var metricsManager = new ObservationManager(List.of(recorder));
        metricsManager.addObservationRecorder(recorder2);

        var marker = metricsManager.startObservation("db", Tags.of("run", "1"));

        metricsManager.finalizeObservation(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestObservationRecord) marker.getObservationRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void startAddMetricMultipleMetricsRecorders2() throws GestaltException {
        var recorder = new TestObservationRecorder(0);
        var recorder2 = new TestObservationRecorder(1);
        var metricsManager = new ObservationManager(List.of());
        metricsManager.addObservationRecorders(List.of(recorder, recorder2));

        var marker = metricsManager.startObservation("db", Tags.of("run", "1"));

        metricsManager.finalizeObservation(marker, Tags.of("test", "ok"));

        Assertions.assertNotNull(marker.getObservationRecord(recorder.recorderId()));
        Assertions.assertInstanceOf(TestObservationRecord.class, marker.getObservationRecord(recorder.recorderId()));

        var testRecord = (TestObservationRecord) marker.getObservationRecord(recorder.recorderId());
        Assertions.assertEquals(10, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);

        var testRecord2 = (TestObservationRecord) marker.getObservationRecord(recorder2.recorderId());
        Assertions.assertEquals(11, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of("run", "1", "test", "ok"), testRecord.tags);
    }

    @Test
    void recordObservation() {

        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        metricsManager.recordObservation("db", 5, Tags.of());

        var testRecord = (TestObservationRecord) recorder.metrics.get("db");
        Assertions.assertEquals(5, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);
    }

    @Test
    void recordObservationTags() {

        var recorder = new TestObservationRecorder(0);
        var metricsManager = new ObservationManager(List.of(recorder));

        metricsManager.recordObservation("db", 5, Tags.environment("dev"));

        var testRecord = (TestObservationRecord) recorder.metrics.get("db");
        Assertions.assertEquals(5, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.environment("dev"), testRecord.tags);
    }

    @Test
    void recordObservationMultiple() {

        var recorder = new TestObservationRecorder(0);
        var recorder2 = new TestObservationRecorder(1);
        var metricsManager = new ObservationManager(List.of(recorder, recorder2));

        metricsManager.recordObservation("db", 5, Tags.of());

        var testRecord = (TestObservationRecord) recorder.metrics.get("db");
        Assertions.assertEquals(5, testRecord.data);
        Assertions.assertEquals("db", testRecord.path);
        Assertions.assertEquals(Tags.of(), testRecord.tags);

        var testRecord2 = (TestObservationRecord) recorder2.metrics.get("db");
        Assertions.assertEquals(6, testRecord2.data);
        Assertions.assertEquals("db", testRecord2.path);
        Assertions.assertEquals(Tags.of(), testRecord2.tags);
    }
}
