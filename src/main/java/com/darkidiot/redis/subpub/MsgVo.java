package com.darkidiot.redis.subpub;

import com.darkidiot.redis.common.Method;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 发布订阅本地缓存消息实体
 * Copyright (c) for darkidiot
 * Date:2017/4/22
 * Author: <a href="darkidiot@icloud.com">darkidiot</a>
 * School: CUIT
 * Desc:
 */
@Data
@AllArgsConstructor
class MsgVo implements Serializable {
    private String clientId;
    private String serverId;
    private Method method;
    private String groupName;
    private String Key;
}
