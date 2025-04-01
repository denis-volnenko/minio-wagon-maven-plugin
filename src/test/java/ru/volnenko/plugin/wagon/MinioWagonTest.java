package ru.volnenko.plugin.wagon;

import org.junit.Assert;
import org.junit.Test;

public class MinioWagonTest {

    @Test
    public void test() {
        final MinioWagon wagon = new MinioWagon();
        Assert.assertNull(wagon.getMinioClient());
    }

}
