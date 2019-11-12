package com.github.davidmoten.logan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import org.junit.Test;

import com.github.davidmoten.bplustree.BPlusTree;

public class DataPersisted2Test {

    @Test
    public void test() {
        BPlusTree<IntWithTimestamp, PropertyWithTimestamp> tree = create();
        tree.insert(new IntWithTimestamp(123, 500L),
                new PropertyWithTimestamp("hello", 1.1, 500L));
        tree.insert(new IntWithTimestamp(234, 200L),
                new PropertyWithTimestamp("there", 1.2, 200L));
        tree.insert(new IntWithTimestamp(124, 300L),
                new PropertyWithTimestamp("hello2", 1.3, 300L));
        tree.print();

        Iterator<PropertyWithTimestamp> it = tree.find( //
                new IntWithTimestamp(123, 0), //
                new IntWithTimestamp(123, 1000)) //
                .iterator();
        assertEquals(500L, it.next().time);
        assertFalse(it.hasNext());
    }

    @Test
    public void testTreeSet() {
        TreeSet<IntWithTimestamp> set = new TreeSet<>();
        set.add(new IntWithTimestamp("hello".hashCode(), 100L));
        set.add(new IntWithTimestamp("there".hashCode(), 200L));
        set.add(new IntWithTimestamp("hello2".hashCode(), 300L));
        set.stream().forEach(System.out::println);
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