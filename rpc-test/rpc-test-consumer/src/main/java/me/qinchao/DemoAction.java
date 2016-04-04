/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qinchao;


import me.qinchao.annotation.Reference;
import me.qinchao.hello.HelloService;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;

import javax.annotation.PostConstruct;

@Component
public class DemoAction {

    @Reference
    private HelloService helloService;

    @PostConstruct
    public void start() throws Exception {
        String qqqq = helloService.hello("qqqq");
        System.out.println("helloService.hello ===> " + qqqq);
    }
}