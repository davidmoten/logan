package com.github.davidmoten.logan;

import java.io.File;

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
        Iterable<PropertyWithTimestamp> it = tree.find(new IntWithTimestamp("hello".hashCode(), 0),
                new IntWithTimestamp("hello".hashCode(), 1000));
        it.forEach(System.out::println);

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