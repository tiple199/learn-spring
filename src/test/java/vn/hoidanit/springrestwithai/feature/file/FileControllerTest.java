package vn.hoidanit.springrestwithai.feature.file;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import vn.hoidanit.springrestwithai.common.TestDataFactory;
import vn.hoidanit.springrestwithai.config.FileUploadProperties;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileUploadProperties uploadProperties;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        testDataFactory.seedPermissions("FILES", "/api/v1/files", "POST");
    }

    @AfterEach
    void cleanUp() throws IOException {
        testDataFactory.cleanup();

        Path uploadDir = Path.of(uploadProperties.getBaseDir());
        if (Files.exists(uploadDir)) {
            Files.walk(uploadDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    // ========== POST /api/v1/files ==========

    @Test
    @DisplayName("POST /files - 201: valid jpg in avatars folder returns upload response")
    void uploadFile_validRequest_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.message", is("File uploaded")))
                .andExpect(jsonPath("$.data.fileName", notNullValue()))
                .andExpect(jsonPath("$.data.fileName", startsWith("")))
                .andExpect(jsonPath("$.data.folder", is("avatars")))
                .andExpect(jsonPath("$.data.fileUrl", startsWith("/uploads/avatars/")))
                .andExpect(jsonPath("$.data.size", is(1024)))
                .andExpect(jsonPath("$.data.uploadedAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /files - 201: valid png in logos folder")
    void uploadFile_validPngInLogos_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", new byte[2048]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "logos")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.folder", is("logos")));
    }

    @Test
    @DisplayName("POST /files - 400: disallowed extension returns bad request")
    void uploadFile_invalidExtension_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /files - 400: file size exceeds 5 MB returns bad request")
    void uploadFile_fileTooLarge_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", new byte[5_242_881]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /files - 400: invalid folder name returns bad request")
    void uploadFile_invalidFolder_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "documents")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(400)));
    }

    @Test
    @DisplayName("POST /files - 201: filename with special characters is sanitized and upload succeeds")
    void uploadFile_invalidFileName_sanitizesAndReturns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad@file!.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars")
                        .with(testDataFactory.jwtWithPermission()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode", is(201)))
                .andExpect(jsonPath("$.data.fileName", endsWith("_bad_file_.jpg")));
    }

    @Test
    @DisplayName("POST /files - 401: missing JWT token returns unauthorized")
    void uploadFile_noToken_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /files - 403: no permission returns forbidden")
    void uploadFile_noPermission_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/files")
                        .file(file)
                        .param("folder", "avatars")
                        .with(testDataFactory.jwtWithoutPermission()))
                .andExpect(status().isForbidden());
    }
}
