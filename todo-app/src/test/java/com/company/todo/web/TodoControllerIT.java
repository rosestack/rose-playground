package com.company.todo.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.company.todo.BaseIT;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("it")
class TodoControllerIT extends BaseIT {

    @Test
    void should_list_with_pagination_and_empty_condition() {
        given().when()
                .get("/api/todos?page=1&size=5")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.size", notNullValue());
    }

    @Test
    void should_reject_duplicate_title_on_create() {
        // 先创建一个
        var req = "{\"title\":\"t1\"}";
        given().contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/todos")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        // 再创建同名
        given().contentType(ContentType.JSON)
                .body(req)
                .when()
                .post("/api/todos")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("标题已存在"));
    }
}
