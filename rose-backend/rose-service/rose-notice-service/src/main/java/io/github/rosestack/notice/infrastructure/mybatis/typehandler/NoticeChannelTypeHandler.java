package io.github.rosestack.notice.infrastructure.mybatis.typehandler;

import io.github.rosestack.notice.domain.value.NoticeChannelType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(NoticeChannelType.class)
public class NoticeChannelTypeHandler extends BaseTypeHandler<NoticeChannelType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NoticeChannelType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public NoticeChannelType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : NoticeChannelType.valueOf(value);
    }

    @Override
    public NoticeChannelType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : NoticeChannelType.valueOf(value);
    }

    @Override
    public NoticeChannelType getNullableResult(java.sql.CallableStatement cs, int columnIndex)
            throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : NoticeChannelType.valueOf(value);
    }
}
