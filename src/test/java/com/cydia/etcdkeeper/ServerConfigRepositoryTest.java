package com.cydia.etcdkeeper;

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
        User user = new User();
        user.setName("郑龙飞");
        user.setUrl("http://merryyou.cn");
        User result = serverConfigRepository.save(user);
        log.info(result.toString());
        Assert.assertNotNull(user.getId());
    }

    @Test
    public void findOneTest() throws Exception{
        User user = serverConfigRepository.getOne(11 );
        log.info(user.toString());
        Assert.assertNotNull(user);
        Assert.assertTrue(1l==user.getId());
    }
}
