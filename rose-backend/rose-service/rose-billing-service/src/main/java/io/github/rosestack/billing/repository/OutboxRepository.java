package io.github.rosestack.billing.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.rosestack.billing.entity.OutboxRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OutboxRepository extends BaseMapper<OutboxRecord> {
}

