package com.eforce21.lib.bin.data.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.sql.Blob;

/**
 * Entity for binary encapsulation.
 * This is only meant for DB schema create/validate/...
 * Please always access content streaming-wise via BinDataRepo
 * and NEVER EVER access directly via JPA queries or bind FKs to avoid LOB memory copies.
 */
@Entity
@Table(name = "bin_data")
public class BinDataEntity {

    @Id
    private String id;

    @Lob
    private Blob data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }

}
