package com.example.webrtc.config;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
@Component
public class DbTemplate {
    /**
     * 模拟数据库存储 user_id <-> session_id 的关系
     */
    public static final ConcurrentHashMap<String, UUID> DB = new ConcurrentHashMap<>();

    /**
     * 获取所有SessionId
     *
     * @return SessionId列表
     */
    public List<UUID> findAll() {
        return new ArrayList<>(DB.values());
    }

    /**
     * 根据UserId查询SessionId
     *
     * @param userId 用户id
     * @return SessionId
     */
    public Optional<UUID> findByUserId(String userId) {
        return Optional.ofNullable(DB.get(userId));
    }

    /**
     * 保存/更新 user_id <-> session_id 的关系
     *
     * @param userId    用户id
     * @param sessionId SessionId
     */
    public void save(String userId, UUID sessionId) {
        DB.put(userId, sessionId);
    }

    /**
     * 删除 user_id <-> session_id 的关系
     *
     * @param userId 用户id
     */
    public void deleteByUserId(String userId) {
        DB.remove(userId);
    }

}
