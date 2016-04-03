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
package me.qinchao.protocol;

import me.qinchao.api.AbstractConfig;
import me.qinchao.api.ProtocolConfig;

/**
 * Protocol.
 */
public interface Protocol {

    /**
     * 暴露远程服务
     */
    void export(Object serviceObject, ProtocolConfig protocolConfig);

    /**
     * 引用远程服务
     */
    <T> T refer(Class<T> type, AbstractConfig protocolConfig);

}