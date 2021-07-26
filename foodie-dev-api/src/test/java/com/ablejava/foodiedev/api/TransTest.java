package com.ablejava.foodiedev.api;

import com.ablejava.foodiedev.service.impl.TestTransServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Application.class)
public class TransTest {
    @Autowired
    private TestTransServiceImpl testTransService;

//    @Test
    public void myTest() {
        testTransService.testPropagationTrans();
    }

}
