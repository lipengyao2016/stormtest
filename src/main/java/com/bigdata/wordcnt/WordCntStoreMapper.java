package com.bigdata.wordcnt;

import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.tuple.ITuple;

public class WordCntStoreMapper implements RedisStoreMapper {
    private  RedisDataTypeDescription redisDataTypeDescription;
    private  String wordHash = "wordCntMap5";

    @Override
    public RedisDataTypeDescription getDataTypeDescription() {
        redisDataTypeDescription = new RedisDataTypeDescription(RedisDataTypeDescription.RedisDataType.HASH,wordHash);
        return redisDataTypeDescription;
    }

    @Override
    public String getKeyFromTuple(ITuple iTuple) {
        return iTuple.getStringByField("word");
    }

    @Override
    public String getValueFromTuple(ITuple iTuple) {
        return iTuple.getStringByField("count");
    }
}
