package xyz.crearts.stream.pgq.integration;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class PgqEventRowMapper implements RowMapper<PgqEvent> {
    @Override
    public PgqEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        PgqEvent res = new PgqEvent();

        res.setEvId(rs.getLong("ev_id"));
        res.setEvTime(rs.getDate("ev_time"));
        res.setEveTxId(rs.getLong("ev_txid"));
        res.setEvRetry(rs.getLong("ev_retry"));
        res.setEvType(rs.getString("ev_type"));
        res.setEvData(rs.getString("ev_data"));

        var evHeaders = new HashMap<String, String>();
        var extra = rs.getString("ev_extra1");
        if (extra != null) {
            evHeaders.put("TAGS", extra);
        }
        if (!evHeaders.isEmpty()) {
            res.setEvHeaders(evHeaders);
        }

        return res;
    }

}
