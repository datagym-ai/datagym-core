package com.eforce21.lib.bin.file.example;

import com.eforce21.lib.bin.file.entity.BinFileEntity;

/**
 * Example of how to map from entity to TO.
 */
public abstract class BinFileMapperExample {

    public static BinFileExample map(BinFileEntity from) {
        BinFileExample to = new BinFileExample();
        to.setId(from.getId());
        to.setName(from.getName());
        to.setSize(from.getSize());
        to.setTsCreate(from.getTsCreate());
        to.setMime(from.getMime());
        return to;
    }

}
