package ru.volnenko.plugin.wagon;

import io.minio.MinioClient;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.maven.wagon.StreamWagon;

public abstract class BaseWagon extends StreamWagon {

    @Override
    public boolean supportsDirectoryCopy() {
        return  true;
    }

    @NonNull
    public String getBasedir() {
        final String base = repository.getBasedir();
        if (base.endsWith("/")) return base.substring(0, base.length() -1);
        return base;
    }

    @NonNull
    public String getMinioPathname() {
        @NonNull final String bucket = getMinioBucket();
        final int length = bucket.length();
        return repository.getBasedir().substring(length+2);
    }

    @NonNull
    public String getMinioBucket() {
        return getBasedir().split("/")[1];
    }

    @NonNull
    public String getMinioHostname() {
        String host = System.getenv("MINIO_HOSTNAME");
        if (host == null) {
            host = System.getProperty("minio.hostname");
            if (host == null) {
                host = repository.getHost();
            }
        }
        return host;
    }

    @NonNull
    @SneakyThrows
    public MinioClient getMinioClient() {
        return new MinioClient(getMinioBasedir(), getMinioUsername(), getMinioPassword());
    }

    @NonNull
    public String getMinioBasedir() {
        return getMinioProtocol() + "://" + getMinioHostname();
    }

    @NonNull
    public String getMinioProtocol() {
        return repository.getProtocol();
    }

    @NonNull
    public String getMinioUsername() {
        String user = System.getenv("MINIO_USERNAME");
        if (user == null) {
            user = System.getProperty("minio.user");
            if (user == null) {
                user = authenticationInfo.getUserName();
                if (user == null) {
                    user = "minio";
                }
            }
        }
        return user;
    }

    @NonNull
    public String getMinioPassword() {
        String password = System.getenv("MINIO_PASSWORD");
        if (password == null) {
            password = System.getProperty("minio.password");
            if (password == null) {
                password = authenticationInfo.getPassword();
                if (password == null) {
                    password = "minio123";
                }
            }
        }
        return password;
    }

}
