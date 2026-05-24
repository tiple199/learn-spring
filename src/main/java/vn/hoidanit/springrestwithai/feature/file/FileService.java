package vn.hoidanit.springrestwithai.feature.file;

import org.springframework.web.multipart.MultipartFile;

import vn.hoidanit.springrestwithai.feature.file.dto.FileUploadResponse;

public interface FileService {

    FileUploadResponse upload(MultipartFile file, String folder);
}
