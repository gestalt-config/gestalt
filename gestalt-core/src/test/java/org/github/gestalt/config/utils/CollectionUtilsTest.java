package org.github.gestalt.config.utils;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.github.gestalt.config.utils.CollectionUtils.buildOrderedConfigPriorities;

class CollectionUtilsTest {

    @Test
    void distinctBy() {
        List<String> myStrings = List.of("A", "B", "C", "D", "A", "E", "C");

        List<String> distinct = myStrings.stream().filter(CollectionUtils.distinctBy(String::valueOf)).collect(Collectors.toList());
        Assertions.assertTrue(distinct.contains("A"));
        Assertions.assertEquals(distinct, List.of("A", "B", "C", "D", "E"));
    }

    @Test
    void buildOrderedConfigPrioritiesTest() {
        var t1 = new Test1();
        var t2 = new Test2();
        var t10 = new Test10();
        var t150 = new Test150();
        List<Object> configPriorityList = List.of(t1, t150, t10, t2);
        configPriorityList = buildOrderedConfigPriorities(configPriorityList, true);

        Assertions.assertEquals(configPriorityList.get(0), t1);
        Assertions.assertEquals(configPriorityList.get(1), t2);
        Assertions.assertEquals(configPriorityList.get(2), t10);
        Assertions.assertEquals(configPriorityList.get(3), t150);
    }

    @Test
    void buildOrderedConfigPrioritiesTestDescending() {
        var t1 = new Test1();
        var t2 = new Test2();
        var t10 = new Test10();
        var t150 = new Test150();
        List<Object> configPriorityList = List.of(t1, t150, t10, t2);
        configPriorityList = buildOrderedConfigPriorities(configPriorityList, false);

        Assertions.assertEquals(configPriorityList.get(3), t1);
        Assertions.assertEquals(configPriorityList.get(2), t2);
        Assertions.assertEquals(configPriorityList.get(1), t10);
        Assertions.assertEquals(configPriorityList.get(0), t150);
    }

    @ConfigPriority(1)
    private static class Test1 {
    }

    @ConfigPriority(2)
    private static class Test2 {
    }

    @ConfigPriority(10)
    private static class Test10 {
    }

    @ConfigPriority(150)
    private static class Test150 {
    }
}
