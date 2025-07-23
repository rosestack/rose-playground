package io.github.rose.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rose.device.dto.ProductCategoryCreateRequest;
import io.github.rose.device.dto.ProductCategoryUpdateRequest;
import io.github.rose.device.entity.ProductCategory;
import io.github.rose.device.service.ProductCategoryService;
import io.github.rose.device.vo.ProductCategoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 产品分类控制器测试类
 *
 * @author rose
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
class ProductCategoryControllerTest {

    @Mock
    private ProductCategoryService productCategoryService;

    @InjectMocks
    private ProductCategoryController productCategoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productCategoryController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateCategory() throws Exception {
        // 准备测试数据
        ProductCategoryCreateRequest request = new ProductCategoryCreateRequest();
        request.setName("测试分类");
        request.setCode("test_category");
        request.setDescription("测试分类描述");
        request.setType(ProductCategory.CategoryType.CUSTOM);
        request.setStatus(ProductCategory.CategoryStatus.ACTIVE);

        ProductCategoryVO response = new ProductCategoryVO();
        response.setId(1L);
        response.setName("测试分类");
        response.setCode("test_category");

        when(productCategoryService.createCategory(any(ProductCategoryCreateRequest.class)))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(post("/api/product/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试分类"))
                .andExpect(jsonPath("$.data.code").value("test_category"));
    }

    @Test
    void testUpdateCategory() throws Exception {
        // 准备测试数据
        ProductCategoryUpdateRequest request = new ProductCategoryUpdateRequest();
        request.setName("更新后的分类");
        request.setDescription("更新后的描述");

        ProductCategoryVO response = new ProductCategoryVO();
        response.setId(1L);
        response.setName("更新后的分类");
        response.setCode("test_category");

        when(productCategoryService.updateCategory(any(ProductCategoryUpdateRequest.class)))
                .thenReturn(response);

        // 执行测试
        mockMvc.perform(put("/api/product/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("更新后的分类"));
    }

    @Test
    void testDeleteCategory() throws Exception {
        when(productCategoryService.deleteCategory(1L)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/product/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetCategory() throws Exception {
        // 准备测试数据
        ProductCategoryVO response = new ProductCategoryVO();
        response.setId(1L);
        response.setName("测试分类");
        response.setCode("test_category");

        when(productCategoryService.getCategoryById(1L)).thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/product/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试分类"));
    }

    @Test
    void testGetCategoryNotFound() throws Exception {
        when(productCategoryService.getCategoryById(999L)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/api/product/categories/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
} 