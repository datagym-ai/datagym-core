package com.eforce21.lib.bin.data.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BinDataRepoDbMssql extends BinDataRepoDb {

    /**
     * MsSQL-Special: Different function name for checking the size of a binary/blob.
     */
    private static final String STM_SIZE_MSSQL = "select datalength(data) from " + TABLE_NAME + " where id = ?";

    public BinDataRepoDbMssql(JdbcTemplate jt) {
        super(jt);
    }

    @Override
    public InputStream read(final String id) throws IOException {
        throw new UnsupportedOperationException("MsSQL cannot pass streams to the outside.");
    }

    @Override
    public long size(final String id) throws IOException {
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, id);
            }
        };

        ResultSetExtractor<Long> rse = new ResultSetExtractor<Long>() {
            @Override
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getLong(1) : null;
            }
        };
        return jt.query(STM_SIZE_MSSQL, pss, rse);
    }


}
