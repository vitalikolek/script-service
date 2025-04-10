package skepn.script.service.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import skepn.script.service.model.file.UserResponse;
import skepn.script.service.service.FilesStorageService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/scripts")
public class ScriptsFolderController {

    @Autowired
    FilesStorageService storageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponse> uploadFile(@RequestParam MultipartFile[] files, @RequestParam String directory, HttpSession session) {
        String sessionId = session.getId();

        try {
            UserResponse response = storageService.uploadFiles(files, sessionId, directory);
            response.setStatus("OK");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            UserResponse response = new UserResponse();
            response.setStatus(e.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
        }
    }

    @GetMapping("/folders")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_WORKER')")
    public ResponseEntity<List<String>> getUploadFolders() {
        return storageService.getFolders();
    }


    @GetMapping(value = "/{directory}")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_WORKER')")
    public ResponseEntity<Resource> getFile(@PathVariable String directory) {
        return storageService.getDirectory(directory);
    }

    @DeleteMapping("/{dir}")
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteFolder(@PathVariable String dir) {
        return storageService.deleteFolder(dir);
    }
}