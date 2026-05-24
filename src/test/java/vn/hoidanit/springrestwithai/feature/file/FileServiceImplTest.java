package vn.hoidanit.springrestwithai.feature.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import vn.hoidanit.springrestwithai.config.FileUploadProperties;
import vn.hoidanit.springrestwithai.exception.FileUploadException;
import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;

import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @TempDir
    Path tempDir;

    private FileUploadProperties properties;
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        properties = new FileUploadProperties();
        properties.setBaseDir(tempDir.toString());
        properties.setMaxFileSize(5_242_880L);
        properties.setAllowedExtensions(List.of("jpg", "jpeg", "png", "gif", "webp"));
        properties.setAllowedFolders(List.of("avatars", "logos"));
        fileService = new FileServiceImpl(properties);
    }

    // ========== success ==========

    @Test
    @DisplayName("upload - valid jpg file - returns FileUploadResponse with correct fields")
    void upload_validJpgFile_returnsFileUploadResponse() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[1024]);

        FileUploadResponse response = fileService.upload(file, "avatars");

        assertThat(response.folder()).isEqualTo("avatars");
        assertThat(response.fileName()).endsWith("_avatar.jpg");
        assertThat(response.fileUrl()).startsWith("/uploads/avatars/");
        assertThat(response.fileUrl()).endsWith("_avatar.jpg");
        assertThat(response.size()).isEqualTo(1024L);
        assertThat(response.uploadedAt()).isNotNull();
    }

    @Test
    @DisplayName("upload - valid png in logos folder - stores file correctly")
    void upload_validPngInLogosFolder_returnsFileUploadResponse() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", new byte[2048]);

        FileUploadResponse response = fileService.upload(file, "logos");

        assertThat(response.folder()).isEqualTo("logos");
        assertThat(response.fileUrl()).contains("/uploads/logos/");
    }

    // ========== null / empty file ==========

    @Test
    @DisplayName("upload - null file - throws FileUploadException")
    void upload_nullFile_throwsFileUploadException() {
        assertThatThrownBy(() -> fileService.upload(null, "avatars"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Không có file được cung cấp");
    }

    @Test
    @DisplayName("upload - empty file - throws FileUploadException")
    void upload_emptyFile_throwsFileUploadException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileService.upload(file, "avatars"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Không có file được cung cấp");
    }

    // ========== file name validation ==========

    @Test
    @DisplayName("upload - blank original filename - throws FileUploadException")
    void upload_blankFileName_throwsFileUploadException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "   ", "image/jpeg", new byte[1024]);

        assertThatThrownBy(() -> fileService.upload(file, "avatars"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Tên file không được để trống");
    }

    @Test
    @DisplayName("upload - filename with special characters - sanitizes and returns success")
    void upload_invalidFileNameChars_sanitizesAndReturnsResponse() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad@file!name.jpg", "image/jpeg", new byte[1024]);

        FileUploadResponse response = fileService.upload(file, "avatars");

        assertThat(response.fileName()).endsWith("_bad_file_name.jpg");
        assertThat(response.fileUrl()).contains("bad_file_name.jpg");
    }

    // ========== extension validation ==========

    @Test
    @DisplayName("upload - disallowed extension (.pdf) - throws FileUploadException")
    void upload_disallowedExtension_throwsFileUploadException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[1024]);

        assertThatThrownBy(() -> fileService.upload(file, "avatars"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Định dạng file không được phép");
    }

    @Test
    @DisplayName("upload - no extension - throws FileUploadException")
    void upload_noExtension_throwsFileUploadException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "noextension", "application/octet-stream", new byte[1024]);

        assertThatThrownBy(() -> fileService.upload(file, "avatars"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Định dạng file không được phép");
    }

    // ========== file size validation ==========

    @Test
    @DisplayName("upload - file exceeds 5 MB limit - throws FileUploadException")
    void upload_fileTooLarge_throwsFileUploadException() {
        byte[] bigContent = new byte[5_242_881];
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", bigContent);

        assertThatThrownBy(() -> fileService.upload(file, "avatars"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Kích thước file vượt quá");
    }

    // ========== folder validation ==========

    @Test
    @DisplayName("upload - invalid folder name - throws FileUploadException")
    void upload_invalidFolder_throwsFileUploadException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[1024]);

        assertThatThrownBy(() -> fileService.upload(file, "documents"))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Thư mục không hợp lệ");
    }
}
