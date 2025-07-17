package io.github.rose.log;

import io.github.rose.core.model.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

public interface EntityService {
    Optional<HasId> findById(Serializable id);

//    default Pageable findAllByTenantId(String tenantId, Page page) {
//        throw new UnsupportedOperationException();
//    }

    default long countByTenantId(String tenantId) {
        throw new IllegalArgumentException("Not implemented for " + getEntityType());
    }

    default void deleteById(String id) {
        throw new IllegalArgumentException(getEntityType().getNormalName() + " deletion not supported");
    }

    default void deleteByTenantId(String tenantId) {
        throw new IllegalArgumentException("Deletion by tenant id not supported for " + getEntityType().getNormalName());
    }

    default Optional<String> fetchEntityName(Serializable entityId) {
        return fetchAndConvert(entityId, this::getName);
    }

    default Optional<String> fetchEntityCode(Serializable entityId) {
        return fetchAndConvert(entityId, this::getCode);
    }

    default String getCode(HasId entity) {
        return entity instanceof HasCode ? ((HasCode) entity).getCode() : null;
    }

    default String getName(HasId entity) {
        if (entity instanceof HasName && StringUtils.isNotEmpty(((HasName) entity).getName())) {
            return ((HasName) entity).getName();
        }
        if (entity instanceof HasPhone && StringUtils.isNotEmpty(((HasPhone) entity).getPhone())) {
            return ((HasPhone) entity).getPhone();
        }
        if (entity instanceof HasEmail && StringUtils.isNotEmpty(((HasEmail) entity).getEmail())) {
            return ((HasEmail) entity).getEmail();
        }
        return null;
    }

    default <T> Optional<T> fetchAndConvert(Serializable entityId, Function<HasId, T> converter) {
        Optional<HasId> entityOpt = findById(entityId);
        return entityOpt.map(converter);
    }

    EntityType getEntityType();

}
