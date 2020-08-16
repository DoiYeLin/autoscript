package com.yaoyaoing.autoscript.common

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
open class RedisService {

    @Autowired
    private lateinit var redis: RedisTemplate<String, Any>

    open fun set(key: String, value: Any) {
        redis.opsForValue().set(key, value)
    }

    open fun set(key: String, value: Any, timeout: Long) {
        redis.opsForValue().set(key, value, timeout, TimeUnit.SECONDS)
    }

    open fun get(key: String): Any {
        return redis.opsForValue().get(key)
    }

    open fun del(key: String) {
        redis.delete(key)
    }

    open fun del(key: List<String>) {
        redis.delete(key)
    }

    /**
     * 设置过期时间
     */
    open fun expire(key: String, time: Long): Boolean {
        return redis.expire(key, time, TimeUnit.SECONDS)
    }

    /**
     * 获取过期时间
     */
    open fun getExpire(key: String): Long {
        return redis.getExpire(key, TimeUnit.SECONDS)
    }

    /**
     * 判断是否有该属性
     */
    open fun hasKey(key: String): Boolean {
        return redis.hasKey(key)
    }

    /**
     * 按delta递增
     */
    open fun incr(key: String, delta: Long): Long? {
        return redis.opsForValue().increment(key, delta)
    }

    /**
     * 按delta递减
     */
    open fun decr(key: String, delta: Long): Long? {
        return redis.opsForValue().decrement(key, delta)
    }

    /**
     * 获取Hash结构中的属性
     */
    open fun hGet(key: String, hashKey: String): Any? {
//        return redis.opsForHash<String, String>().get(key, hashKey)
        return redis.opsForHash<String, Any>().get(key, hashKey)
    }

    /**
     * 向Hash结构中放入一个属性
     */
    open fun hSet(key: String, hashKey: String, value: Any, time: Long): Boolean {
        redis.opsForHash<String, Any>().put(key, hashKey, value)
        return expire(key, time)
    }

    /**
     * 向Hash结构中放入一个属性
     */
    open fun hSet(key: String, hashKey: String, value: Any) {
        redis.opsForHash<String, Any>().put(key, hashKey, value)
    }

    /**
     * 直接获取整个Hash结构
     */
    open fun hGetAll(key: String): Map<String, Any> {
        return redis.opsForHash<String, Any>().entries(key)
    }

    /**
     * 直接设置整个Hash结构
     */
    open fun hSetAll(key: String, map: Map<String, Any>) {
        redis.opsForHash<String, Any>().putAll(key, map)
    }

    /**
     * 删除Hash结构中的属性
     */
    open fun hDel(key: String, hashKey: Any) {
        redis.opsForHash<String, Any>().delete(key, hashKey)
    }

    /**
     * 判断Hash结构中是否有该属性
     */
    open fun hHasKey(key: String, hashKey: String): Boolean {
        return redis.opsForHash<String, Any>().hasKey(key, hashKey)
    }

    /**
     * Hash结构中属性递增
     */
    open fun hIncr(key: String, hashKey: String, delta: Long): Long {
        return redis.opsForHash<String, Any>().increment(key, hashKey, delta)
    }

    /**
     * Hash结构中属性递减
     */
    open fun hDecr(key: String, hashKey: String, delta: Long): Long {
        return redis.opsForHash<String, Any>().increment(key, hashKey, -delta)
    }

    /**
     * 获取Set结构
     */
    open fun sMembers(key: String): Set<Any> {
        return redis.opsForSet().members(key)
    }

    /**
     * 向Set结构中添加属性
     */
    open fun sAdd(key: String, values: Any): Long {
        return redis.opsForSet().add(key, values)
    }

    /**
     * 是否为Set中的属性
     */
    open fun sIsMember(key: String, value: Any): Boolean {
        return redis.opsForSet().isMember(key, value)
    }

    /**
     * 获取Set结构的长度
     */
    open fun sSize(key: String): Long {
        return redis.opsForSet().size(key)
    }

    /**
     * 删除Set结构中的属性
     */
    open fun sRemove(key: String, values: Any): Long {
        return redis.opsForSet().remove(key, values)
    }

    /**
     * 获取List结构中的属性
     */
    open fun <Object> lRange(key: String, start: Long, end: Long): List<Any> {
        return redis.opsForList().range(key, start, end)
    }

    /**
     * 获取List结构的长度
     */
    open fun lSize(key: String): Long {
        return redis.opsForList().size(key)
    }

    /**
     * 根据索引获取List中的属性
     */
    open fun lIndex(key: String, index: Long): Any {
        return redis.opsForList().index(key, index)
    }

    /**
     * 向List结构中添加属性
     */
    open fun lPush(key: String, value: Any): Long {
        return redis.opsForList().leftPushAll(key, value)
    }

    /**
     * 从List结构中移除属性
     */
    open fun lRemove(key: String, count: Long, value: Any): Long {
        return redis.opsForList().remove(key, count, value)
    }


    open fun zadd(key: String, member: String, score: Double) {
        redis.opsForZSet().add(key, member, score)
    }

    open fun zCard(key: String): Long {
        return redis.opsForZSet().zCard(key)
    }

    open fun zscoreAdd(key: String, member: String, score: Double): Double {
        return redis.opsForZSet().incrementScore(key, member, score)
    }

    open fun zrem(key: String, member: Any) {
        redis.opsForZSet().remove(key, member)
    }

    open fun zremRangeByScore(key: String, min: Double, max: Double) {
        redis.opsForZSet().removeRangeByScore(key, min, max)
    }

    open fun zscore(key: String, member: String): Double {
        if (redis.hasKey(key)) {
            val score = redis.opsForZSet().score(key, member)
            if (score != null) {
                return score
            }
        }
        return 0.0
    }

    /**
     * 判断value在zset中的排名  zrank
     *
     * @param key
     * @param value
     * @return
     */
    open fun zrank(key: String, value: String): Long {
        return redis.opsForZSet().rank(key, value);
    }

    /**
     *
     */
    open fun zrange(key: String, start: Long, end: Long): MutableSet<Any> {
        return redis.opsForZSet().range(key, start, end)
    }

    open fun zrangeWithScore(key: String, start: Long, end: Long): MutableSet<ZSetOperations.TypedTuple<Any>>? {
        return redis.opsForZSet().rangeWithScores(key, start, end)
    }

    open fun zrevrange(key: String, start: Long, end: Long): MutableSet<Any> {
        return redis.opsForZSet().reverseRange(key, start, end)
    }

    open fun zreverseRangeWithScores(key: String, start: Long, end: Long): MutableSet<ZSetOperations.TypedTuple<Any>>? {
        return redis.opsForZSet().reverseRangeWithScores(key, start, end)
    }
}