package ru.volnenko.plugin.wagon;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.maven.wagon.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.*;

public class MinioWagon extends BaseWagon {

    private MinioClient minioClient;

    @Override
    @SneakyThrows
    public void fillInputData(InputData inputData) {
        try {
            inputData.setInputStream(minioClient.getObject(getMinioBucket(), getMinioPathname() + "/" + inputData.getResource().getName()));
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ResourceDoesNotExistException(e.getMessage(), e);
            }
            throw new TransferFailedException(e.getMessage(), e);
        } catch (@NonNull final Exception e) {
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    @Override
    @SneakyThrows
    public void fillOutputData(OutputData outputData)  {
        outputData.setOutputStream(new ByteArrayOutputStream() {
            @Override
            @SneakyThrows
            public void close() {
                @NonNull final ByteArrayInputStream inputStream = new ByteArrayInputStream(toByteArray());
                @NonNull final String resource = outputData.getResource().getName();
                @NonNull final String objectName = getMinioPathname() + "/" + resource;
                @NonNull final String mimeType = URLConnection.guessContentTypeFromName(outputData.getResource().getName());
                minioClient.putObject(getMinioBucket(), objectName, inputStream, mimeType);
            }
        });
    }

    @Override
    @SneakyThrows
    public void closeConnection() {
        minioClient = null;
    }

    @Override
    @SneakyThrows
    protected void openConnectionInternal() {
        System.out.println("MINIO HOST NAME: " + getMinioHostname());
        System.out.println("MINIO BUCKET: " + getMinioBucket());
        minioClient = getMinioClient();
    }

    @Override
    @SneakyThrows
    public void putDirectory(@NonNull final File sourceDirectory, @NonNull final String destinationDirectory) {
        @NonNull final URI uri = sourceDirectory.getAbsoluteFile().toURI();
        @NonNull final DiskVisitor diskVisitor = getDiskVisitor(sourceDirectory);
        Files.walkFileTree(Paths.get(uri), diskVisitor);
    }

    @NonNull
    private DiskVisitor getDiskVisitor(@NonNull final File sourceDirectory) {
        return new DiskVisitor(getMinioPathname(), sourceDirectory, minioClient, getMinioBucket());
    }

}
