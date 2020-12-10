package com.self.learnjava.service;

import io.lettuce.core.api.sync.RedisCommands;

@FunctionalInterface
public interface SyncCommandCallback<T> {
    // 在此操作Redis:
	T doInConnection(RedisCommands<String, String> commands);
}
