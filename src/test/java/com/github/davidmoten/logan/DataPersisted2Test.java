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
        tree.insert(new IntWithTimestamp("hello".hashCode(), 100L),
                new PropertyWithTimestamp("hello", 1.1, 100L));
        tree.insert(new IntWithTimestamp("there".hashCode(), 200L),
                new PropertyWithTimestamp("there", 1.2, 200L));
        tree.insert(new IntWithTimestamp("hello2".hashCode(), 300L),
                new PropertyWithTimestamp("hello2", 1.3, 300L));
        tree.print();
        Iterator<PropertyWithTimestamp> it = tree.find( //
                new IntWithTimestamp("hello".hashCode(), 0), //
                new IntWithTimestamp("hello".hashCode(), 1000)) //
                .iterator();
        assertEquals(100L, it.next().time);
        assertEquals(200L, it.next().time);
        assertEquals(300L, it.next().time);
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
        new File("target/testbp").mkdirs();
        return BPlusTree.file() //
                .directory("target/testbp") //
                .maxKeys(12) //
                .segmentSizeMB(1) //
                .keySerializer(IntWithTimestamp.SERIALIZER) //
                .valueSerializer(PropertyWithTimestamp.SERIALIZER) //
                .naturalOrder();
    }

}