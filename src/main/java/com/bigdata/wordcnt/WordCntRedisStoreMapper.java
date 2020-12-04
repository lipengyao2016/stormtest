package com.bigdata.wordcnt;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;

public class WordCntRedisStoreMapper extends AbstractRedisBolt {
    private final RedisStoreMapper storeMapper;
    private final RedisDataTypeDescription.RedisDataType dataType;
    private final String additionalKey;

    public WordCntRedisStoreMapper(JedisPoolConfig config, RedisStoreMapper storeMapper) {
        super(config);
        this.storeMapper = storeMapper;
        this.dataType = storeMapper.getDataTypeDescription().getDataType();
        this.additionalKey = storeMapper.getDataTypeDescription().getAdditionalKey();
        System.out.println(" additionalKey: " + this.additionalKey);
    }

    @Override
    protected void process(Tuple tuple) {
      String key = this.storeMapper.getKeyFromTuple(tuple);
      String value = this.storeMapper.getValueFromTuple(tuple);
        System.out.println(" key: " + key+ " value:" +value);
        JedisCommands jedisCommands = null;
      try {
        jedisCommands = this.getInstance();
        jedisCommands.hincrBy(this.additionalKey,key,Long.valueOf(value));
        this.collector.ack(tuple);
    } catch (Exception var13) {
        this.collector.reportError(var13);
        this.collector.fail(tuple);
    } finally {
        this.returnInstance(jedisCommands);
    }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
