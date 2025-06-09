package at.jku.dke.task_app.jdbc.controllers;

import at.jku.dke.etutor.task_app.auth.AuthConstants;
import at.jku.dke.etutor.task_app.dto.ModifyTaskDto;
import at.jku.dke.etutor.task_app.dto.TaskStatus;
import at.jku.dke.task_app.jdbc.ClientSetupExtension;
import at.jku.dke.task_app.jdbc.DatabaseSetupExtension;
import at.jku.dke.task_app.jdbc.controllers.TaskController;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTask;
import at.jku.dke.task_app.jdbc.data.entities.JDBCTaskGroup;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskGroupRepository;
import at.jku.dke.task_app.jdbc.data.repositories.JDBCTaskRepository;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskDto;
import at.jku.dke.task_app.jdbc.dto.ModifyJDBCTaskDto;
import io.restassured.http.ContentType;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({DatabaseSetupExtension.class, ClientSetupExtension.class})
class TaskControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private JDBCTaskRepository repository;

    @Autowired
    private JDBCTaskGroupRepository groupRepository;

    private long taskId;
    private long taskGroupId;

    @BeforeEach
    void initDb() {
        this.repository.deleteAll();
        var group = new JDBCTaskGroup(TaskStatus.APPROVED, "", "", "");
        group.setId(1L);
        group = this.groupRepository.save(group);
        this.taskGroupId = group.getId();
        var task = new JDBCTask(
            1L,
            BigDecimal.TWO,
            TaskStatus.APPROVED,
            group,
            "SELECT * FROM books;",
            "books,loans",
            "int book = 1;\nint user = 42;"
        );
        task.setWrongOutputPenalty(1);
        task.setWrongDbContentPenalty(1);
        task.setAutocommitPenalty(1);
        task.setExceptionHandlingPenalty(1);
        task.setCheckAutocommit(false);
        task.setVariables(null);
        this.repository.save(task);
        this.taskId = task.getId();
    }
    //#region --- GET ---
    @Test
    void getShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("solution", equalTo("SELECT * FROM books;"));
    }

    @Test
    void getShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void getShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .accept(ContentType.JSON)
            // WHEN
            .when()
            .get("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- CREATE ---
    @Test
    void createShouldReturnCreated() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "jdbc", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .header("Location", containsString("/api/task/" + (this.taskId + 2)))
            .body("descriptionDe", any(String.class))
            .body("descriptionEn", any(String.class));
    }

    @Test
    void createShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void createShouldReturnBadRequestOnEmptyBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void createShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .post("/api/task/{id}", this.taskId + 2)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- UPDATE ---
    @Test
    void updateShouldReturnOk() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "jdbc", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("descriptionDe", any(String.class))
            .body("descriptionEn", any(String.class));
    }

    @Test
    void updateShouldReturnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(404);
    }

    @Test
    void updateShouldReturnBadRequestOnInvalidBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "sql", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void updateShouldReturnBadRequestOnEmptyBody() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            .contentType(ContentType.JSON)
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(400);
    }

    @Test
    void updateShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            .contentType(ContentType.JSON)
            .body(new ModifyTaskDto<>(this.taskGroupId, BigDecimal.TEN, "JDBC", TaskStatus.APPROVED, new ModifyJDBCTaskDto("", "", 1,1, 1, true, 1, "" )))
            // WHEN
            .when()
            .put("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    //#region --- DELETE ---
    @Test
    void deleteShouldReturnNoContent() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturnNoContentOnNotFound() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.CRUD_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId + 1)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturnForbidden() {
        given()
            .port(port)
            .header(AuthConstants.AUTH_TOKEN_HEADER_NAME, ClientSetupExtension.SUBMIT_API_KEY)
            // WHEN
            .when()
            .delete("/api/task/{id}", this.taskId)
            // THEN
            .then()
            .log().ifValidationFails()
            .statusCode(403);
    }
    //#endregion

    @Test
    void mapToDto() {
        // Arrange
        var group = new JDBCTaskGroup(TaskStatus.APPROVED, "", "", "");
        group.setId(2L);
        group = this.groupRepository.save(group);
        this.taskGroupId = group.getId();
        var task = new JDBCTask(
            1L,
            BigDecimal.TWO,
            TaskStatus.APPROVED,
            group,
            "SELECT * FROM books;",
            "books,loans",
            "int book = 1;\nint user = 42;"
        );
        task.setWrongOutputPenalty(1);
        task.setWrongDbContentPenalty(1);
        task.setAutocommitPenalty(1);
        task.setExceptionHandlingPenalty(1);
        task.setCheckAutocommit(false);
        task.setVariables(null);
        // Act
        var result = new TaskController(null).mapToDto(task);

        // Assert
        assertEquals("SELECT * FROM books;", result.solution());
    }
}
