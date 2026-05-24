package vn.hoidanit.springrestwithai.feature.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.config.FileUploadProperties;
import vn.hoidanit.springrestwithai.exception.FileUploadException;
import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;

@Service
public class FileServiceImpl implements FileService {

    private final FileUploadProperties uploadProperties;

    public FileServiceImpl(FileUploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public FileUploadResponse upload(MultipartFile file, String folder) {
        validateFile(file, folder);
        String originalName = file.getOriginalFilename();
        String sanitizedName = sanitizeFileName(originalName);
        String storedName = System.currentTimeMillis() + "_" + sanitizedName;

        Path targetDir = Path.of(uploadProperties.getBaseDir(), folder);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new FileUploadException("Không thể tạo thư mục lưu trữ: " + e.getMessage());
        }

        try {
            Path targetPath = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileUploadException("Không thể lưu file: " + e.getMessage());
        }

        String fileUrl = "/uploads/" + folder + "/" + storedName;
        return new FileUploadResponse(storedName, folder, fileUrl, file.getSize(), Instant.now());
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^A-Za-z0-9._\\-]", "_");
    }

    private void validateFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Không có file được cung cấp");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new FileUploadException("Tên file không được để trống");
        }

        String extension = getExtension(originalName);
        if (!uploadProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
            String allowed = String.join(", ", uploadProperties.getAllowedExtensions());
            throw new FileUploadException("Định dạng file không được phép. Chỉ chấp nhận: " + allowed);
        }

        if (file.getSize() > uploadProperties.getMaxFileSize()) {
            long maxMb = uploadProperties.getMaxFileSize() / (1024 * 1024);
            throw new FileUploadException("Kích thước file vượt quá " + maxMb + " MB");
        }

        if (!uploadProperties.getAllowedFolders().contains(folder)) {
            String allowed = String.join(", ", uploadProperties.getAllowedFolders());
            throw new FileUploadException("Thư mục không hợp lệ. Chỉ chấp nhận: " + allowed);
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
}
