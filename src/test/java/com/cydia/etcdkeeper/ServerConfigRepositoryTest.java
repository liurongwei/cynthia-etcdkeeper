package com.cydia.etcdkeeper;

import com.cydia.etcdkeeper.entity.ServerConfig;
import com.cydia.etcdkeeper.repository.ServerConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class ServerConfigRepositoryTest {

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Test
    public void saveTest() throws Exception {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setEndpoints("192.168.87.9:2379");
        serverConfig.setTitle("192.168.87.9");
        ServerConfig result = serverConfigRepository.save(serverConfig);
        log.info(result.toString());
        Assert.assertNotNull(result.getId());
    }

    @Test
    public void findOneTest() throws Exception{
        ServerConfig serverConfig = serverConfigRepository.getOne(1 );
        log.info(serverConfig.toString());
        Assert.assertNotNull(serverConfig);
        Assert.assertTrue(1==serverConfig.getId());
    }
}
