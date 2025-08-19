package io.github.rosestack.notification.infrastructure.mybatis.typehandler;

import io.github.rosestack.notification.domain.value.TargetType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(TargetType.class)
public class TargetTypeTypeHandler extends BaseTypeHandler<TargetType> {
	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, TargetType parameter, JdbcType jdbcType)
		throws SQLException {
		ps.setString(i, parameter.name());
	}

	@Override
	public TargetType getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String value = rs.getString(columnName);
		return value == null ? null : TargetType.valueOf(value);
	}

	@Override
	public TargetType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String value = rs.getString(columnIndex);
		return value == null ? null : TargetType.valueOf(value);
	}

	@Override
	public TargetType getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
		String value = cs.getString(columnIndex);
		return value == null ? null : TargetType.valueOf(value);
	}
}
