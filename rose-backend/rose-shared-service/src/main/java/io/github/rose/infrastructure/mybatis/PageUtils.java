package io.github.rose.infrastructure.mybatis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 * <p>
 * 提供 Spring Data Page 和 MyBatis-Plus IPage 之间的转换功能。
 * </p>
 */
@UtilityClass
public class PageUtils {

    /**
     * 将 MyBatis-Plus IPage 转换为 Spring Data Page
     *
     * @param iPage MyBatis-Plus 分页对象
     * @param <T>   数据类型
     * @return Spring Data Page 对象
     */
    public static <T> Page<T> toSpringPage(IPage<T> iPage) {
        if (iPage == null) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(
                (int) (iPage.getCurrent() - 1), // MyBatis-Plus 页码从1开始，Spring Data 从0开始
                (int) iPage.getSize()
        );

        return new PageImpl<>(
                iPage.getRecords(),
                pageable,
                iPage.getTotal()
        );
    }

    /**
     * 将 Spring Data Page 转换为 MyBatis-Plus IPage
     *
     * @param page Spring Data 分页对象
     * @param <T>  数据类型
     * @return MyBatis-Plus IPage 对象
     */
    public static <T> IPage<T> toMybatisPage(Page<T> page) {
        if (page == null) {
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> iPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();

        iPage.setCurrent(page.getNumber() + 1); // Spring Data 页码从0开始，MyBatis-Plus 从1开始
        iPage.setSize(page.getSize());
        iPage.setTotal(page.getTotalElements());
        iPage.setRecords(page.getContent());

        return iPage;
    }

    /**
     * 创建 MyBatis-Plus 分页对象
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 页大小
     * @param <T>      数据类型
     * @return MyBatis-Plus IPage 对象
     */
    public static <T> IPage<T> createMybatisPage(int pageNum, int pageSize) {
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
    }

    /**
     * 创建 Spring Data 分页对象
     *
     * @param pageNum  页码（从0开始）
     * @param pageSize 页大小
     * @return Spring Data Pageable 对象
     */
    public static Pageable createSpringPageable(int pageNum, int pageSize) {
        return PageRequest.of(pageNum, pageSize);
    }

    /**
     * 创建带排序的 Spring Data 分页对象
     *
     * @param pageNum  页码（从0开始）
     * @param pageSize 页大小
     * @param sort     排序条件
     * @return Spring Data Pageable 对象
     */
    public static Pageable createSpringPageable(int pageNum, int pageSize, Sort sort) {
        return PageRequest.of(pageNum, pageSize, sort);
    }

    /**
     * 将分页查询结果进行类型转换
     *
     * @param sourcePage 源分页对象
     * @param converter  转换函数
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 转换后的分页对象
     */
    public static <S, T> Page<T> convertPage(Page<S> sourcePage, Function<S, T> converter) {
        return sourcePage.map(converter);
    }

    /**
     * 将 IPage 查询结果进行类型转换
     *
     * @param sourceIPage 源分页对象
     * @param converter   转换函数
     * @param <S>         源类型
     * @param <T>         目标类型
     * @return 转换后的分页对象
     */
    public static <S, T> IPage<T> convertIPage(IPage<S> sourceIPage, Function<S, T> converter) {
        if (sourceIPage == null || sourceIPage.getRecords() == null) {
            return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        }

        List<T> convertedRecords = sourceIPage.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> apiResponse =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        apiResponse.setCurrent(sourceIPage.getCurrent());
        apiResponse.setSize(sourceIPage.getSize());
        apiResponse.setTotal(sourceIPage.getTotal());
        apiResponse.setRecords(convertedRecords);

        return apiResponse;
    }
}