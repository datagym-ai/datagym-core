package com.eforce21.lib.bin.data.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * {@link BinDataRepo} storing files in a database using prepared statements via {@link JdbcTemplate}.
 * Works with H2, MySQL.
 */
public class BinDataRepoDb implements BinDataRepo {

    protected static final String TABLE_NAME = "bin_data";

    private static final String STM_INSERT = "insert into " + TABLE_NAME + " (id,data) values (?,?)";
    private static final String STM_STREAM = "select data from " + TABLE_NAME + " where id = ?";
    private static final String STM_DELETE = "delete from " + TABLE_NAME + " where id = ?";
    private static final String STM_SIZE = "select length(data) from " + TABLE_NAME + " where id = ?";

    protected JdbcTemplate jt;

    public BinDataRepoDb(JdbcTemplate jt) {
        this.jt = jt;
    }

    @Override
    public String write(InputStream is) throws IOException {
        final String id = UUID.randomUUID().toString();

        jt.execute(STM_INSERT, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps) throws DataAccessException, SQLException {
                ps.setString(1, id);
                ps.setBinaryStream(2, is);
                return ps.execute();
            }
        });

        return id;
    }

    @Override
    public InputStream read(final String id) throws IOException {
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, id);
            }
        };

        ResultSetExtractor<InputStream> rse = new ResultSetExtractor<InputStream>() {
            @Override
            public InputStream extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getBinaryStream(1) : null;
            }
        };

        return jt.query(STM_STREAM, pss, rse);
    }

    @Override
    public void read(String id, Consumer<InputStream> isConsumer) throws IOException {
        PreparedStatementSetter pss = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, id);
            }
        };

        ResultSetExtractor<Void> rse = new ResultSetExtractor<Void>() {
            @Override
            public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    isConsumer.accept(rs.getBinaryStream(1));
                }
                return null;
            }
        };

        jt.query(STM_STREAM, pss, rse);
    }

    @Override
    public void delete(final String id) throws IOException {
        jt.execute(STM_DELETE, new PreparedStatementCallback<Boolean>() {
            @Override
            public Boolean doInPreparedStatement(PreparedStatement ps) throws DataAccessException, SQLException {
                ps.setString(1, id);
                return ps.execute();
            }
        });
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

        return jt.query(STM_SIZE, pss, rse);
    }


}
