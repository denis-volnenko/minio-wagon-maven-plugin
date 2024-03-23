package ru.volnenko.plugin.wagon;

import io.minio.MinioClient;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class DiskVisitor extends SimpleFileVisitor<Path> {

    @NonNull
    private final String basedir;

    @NonNull
    private final File source;

    @NonNull
    private final MinioClient minioClient;

    @NonNull
    private final String bucket;

    public DiskVisitor(
            @NonNull String basedir,
            @NonNull File source,
            @NonNull MinioClient minioClient,
            @NonNull String bucket
    ) {
        this.basedir = basedir;
        this.source = source;
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    @SneakyThrows
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (Files.isDirectory(file)) return FileVisitResult.CONTINUE;
        @NonNull final String relative = source.toURI().relativize(file.toUri()).toString();
        @NonNull final String target = basedir + "/" + relative;
        System.out.println("UPLOAD: " + target);
        @NonNull final byte[] bytes = Files.readAllBytes(file);
        @NonNull final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        @NonNull final String mimeType = URLConnection.guessContentTypeFromName(file.toFile().getName());
        minioClient.putObject(bucket, target, inputStream, mimeType);
        return FileVisitResult.CONTINUE;
    }

}
