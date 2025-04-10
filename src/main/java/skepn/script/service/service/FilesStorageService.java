package skepn.script.service.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import skepn.script.service.model.file.UserResponse;

import java.util.List;

public interface FilesStorageService {

    public void init();

    public UserResponse uploadFiles(MultipartFile[] files, String sessionId, String directory);

    public ResponseEntity<List<String>> getFolders();

    public ResponseEntity<Resource> getDirectory(String dir);

    public ResponseEntity<?> deleteFolder(String folder);
}