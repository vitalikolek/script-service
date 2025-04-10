package skepn.script.service.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import skepn.script.service.model.file.FileInfo;
import skepn.script.service.model.file.UserResponse;
import skepn.script.service.util.FilesStorageUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class FilesStorageServiceImpl implements FilesStorageService {

    private final Path root;
    private final AtomicLong fileSize = new AtomicLong(0);
    private final int daysToAdd = 10;

    @Autowired
    public FilesStorageServiceImpl(@Value("${upload.dir}") String uploadDir) {
        this.root = Paths.get(uploadDir);
    }

    @Override
    @SneakyThrows
    public void init() {
        Files.createDirectories(root);
    }

    @Override
    public UserResponse uploadFiles(MultipartFile[] files, String sessionId, String directory) {
        UserResponse response = new UserResponse();
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        response.setTime(dtFormatter.format(localDate));

        List<FileInfo> filesInfo = new ArrayList<>();
        for (MultipartFile file : files) {
            FileInfo fileInfo = save(file, sessionId, directory);
            filesInfo.add(fileInfo);
        }

        response.setStatus("Ok");
        response.setFolderId(sessionId);
        response.setBody(filesInfo);
        return response;
    }

    @SneakyThrows
    public FileInfo save(MultipartFile file, String sessionId, String directory) {
        Path dir = Files.createDirectories(Path.of("uploads", directory));
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileUrl = FilesStorageUtil.generateFileUrl(sessionId, String.valueOf(file));

        fileSize.set(file.getSize());

        FilesStorageUtil.saveFile(file, dir, fileName);

        return new FileInfo(fileName, fileUrl);
    }

    @Override
    public ResponseEntity<List<String>> getFolders() {
        try {
            Path uploadsPath = Path.of("uploads");

            if (!Files.exists(uploadsPath) || !Files.isDirectory(uploadsPath)) {
                return ResponseEntity.notFound().build();
            }

            List<String> folderNames = Files.list(uploadsPath)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(folderNames);

        } catch (IOException e) {
            throw new RuntimeException("Failed to list folders: " + e.getMessage(), e);
        }
    }

    @Override
    @SneakyThrows
    public ResponseEntity<Resource> getDirectory(String dir) {
        Path directoryPath = root.resolve(dir);

        if (!Files.isDirectory(directoryPath)) {
            return ResponseEntity.notFound().build();
        }

        LocalDate expirationDate = LocalDate.now().plusDays(daysToAdd);
        String expirationDateStr = expirationDate.toString();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
            Files.walk(directoryPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String zipEntryName = root.relativize(path).toString();

                        try {
                            zos.putNextEntry(new ZipEntry(zipEntryName));

                            if (zipEntryName.contains("script.js")) {
                                String content = Files.readString(path);
                                content = content.replace("2000-01-01", expirationDateStr);
                                zos.write(content.getBytes(StandardCharsets.UTF_8));
                            } else {
                                Files.copy(path, zos);
                            }

                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException("Error processing file: " + path, e);
                        }
                    });
        }

        ByteArrayResource resource = new ByteArrayResource(byteArrayOutputStream.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dir + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @Override
    @SneakyThrows
    public ResponseEntity<?> deleteFolder(String folder) {
        Path folderPath = this.root.resolve(folder);

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return ResponseEntity.notFound().build();
        }

        Files.walk(folderPath)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        return ResponseEntity.ok().build();
    }
}