package io.github.rose.notification.infra.mybatis.typehandler;

import io.github.rose.notification.domain.value.NotificationStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(NotificationStatus.class)
public class NotificationStatusTypeHandler extends BaseTypeHandler<NotificationStatus> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NotificationStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }

    @Override
    public NotificationStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : NotificationStatus.valueOf(value);
    }

    @Override
    public NotificationStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : NotificationStatus.valueOf(value);
    }

    @Override
    public NotificationStatus getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : NotificationStatus.valueOf(value);
    }
}
