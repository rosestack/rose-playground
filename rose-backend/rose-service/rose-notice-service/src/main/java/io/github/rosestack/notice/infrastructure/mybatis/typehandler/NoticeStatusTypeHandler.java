package io.github.rosestack.notice.infrastructure.mybatis.typehandler;

import io.github.rosestack.notice.domain.value.NoticeStatus;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(NoticeStatus.class)
public class NoticeStatusTypeHandler extends BaseTypeHandler<NoticeStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NoticeStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public NoticeStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : NoticeStatus.valueOf(value);
    }

    @Override
    public NoticeStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : NoticeStatus.valueOf(value);
    }

    @Override
    public NoticeStatus getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : NoticeStatus.valueOf(value);
    }
}
