package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Test;

import com.github.davidmoten.bplustree.BPlusTree;

public class DataPersistedBPlusTreeTest {

    @Test
    public void test() {
        BPlusTree<IntWithTimestamp, PropertyWithTimestamp> tree = create();
        tree.insert(new IntWithTimestamp(123, 500L), new PropertyWithTimestamp("hello", 1.1, null, 500L));
        tree.insert(new IntWithTimestamp(234, 200L), new PropertyWithTimestamp("there", 1.2, null, 200L));
        tree.insert(new IntWithTimestamp(124, 300L), new PropertyWithTimestamp("hello2", 1.3, null, 300L));
        tree.print();

        {
            Iterator<PropertyWithTimestamp> it = tree.find( //
                    new IntWithTimestamp(123, 0), //
                    new IntWithTimestamp(123, 1000)) //
                    .iterator();
            assertEquals(500L, it.next().time);
            assertFalse(it.hasNext());
        }
        {
            Iterator<PropertyWithTimestamp> it = tree.find( //
                    new IntWithTimestamp(0, 0), //
                    new IntWithTimestamp(1000, 1000)) //
                    .iterator();
            assertEquals(500L, it.next().time);
            assertEquals(300L, it.next().time);
            assertEquals(200L, it.next().time);
            assertFalse(it.hasNext());
        }
    }

    @Test
    public void testTreeSet() {
        TreeSet<IntWithTimestamp> set = new TreeSet<>();
        set.add(new IntWithTimestamp(123, 100L));
        set.add(new IntWithTimestamp(234, 200L));
        set.add(new IntWithTimestamp(124, 300L));
        List<Long> list = set.stream().map(x -> x.time).collect(Collectors.toList());
        assertEquals(Arrays.asList(100L, 300L, 200L), list);
    }

    private BPlusTree<IntWithTimestamp, PropertyWithTimestamp> create() {
        File dir = new File("target/testbp");
        dir.mkdirs();
        for (File f : dir.listFiles()) {
            f.delete();
        }

        return BPlusTree.file() //
                .directory(dir) //
                .maxKeys(12) //
                .segmentSizeMB(1) //
                .keySerializer(IntWithTimestamp.SERIALIZER) //
                .valueSerializer(PropertyWithTimestamp.SERIALIZER) //
                .naturalOrder();
    }

}