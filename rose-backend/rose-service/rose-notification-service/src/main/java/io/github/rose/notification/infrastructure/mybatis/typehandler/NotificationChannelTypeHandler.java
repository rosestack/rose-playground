package io.github.rose.notification.infrastructure.mybatis.typehandler;

import io.github.rose.notification.domain.value.NotificationChannelType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(NotificationChannelType.class)
public class NotificationChannelTypeHandler extends BaseTypeHandler<NotificationChannelType> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NotificationChannelType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public NotificationChannelType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : NotificationChannelType.valueOf(value);
    }

    @Override
    public NotificationChannelType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : NotificationChannelType.valueOf(value);
    }

    @Override
    public NotificationChannelType getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : NotificationChannelType.valueOf(value);
    }
}
