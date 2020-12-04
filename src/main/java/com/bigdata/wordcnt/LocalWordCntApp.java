package com.bigdata.wordcnt;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.redis.bolt.RedisStoreBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.topology.TopologyBuilder;

public class LocalWordCntApp {

    //在实际开发中这些参数可以将通过外部传入 使得程序更加灵活
    private static final String REDIS_HOST = "47.107.239.163";
    private static final int REDIS_PORT = 7006;
    private static final String REDIS_PWD = "xfz178";
    private static final String APP_NAME = "WordAppStorm";

    public static void main( String[] args )
    {
        String dataSourceSpout = "dataSourceSpout";
        String splidBold = "splidBold";
        String countBold = "countBold";


        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(dataSourceSpout,new DataSourceSpout());
        builder.setBolt(splidBold,new SplitBold()).shuffleGrouping(dataSourceSpout);
        builder.setBolt(countBold,new CountBold()).shuffleGrouping(splidBold);

        RedisStoreMapper redisStoreBold = new WordCntStoreMapper();
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig.Builder().setHost(REDIS_HOST)
                .setPort(REDIS_PORT).setPassword(REDIS_PWD).build();

//        RedisStoreBolt redisStoreBolt = new RedisStoreBolt(jedisPoolConfig,redisStoreBold);
//        builder.setBolt("redisStoreBold",redisStoreBolt).shuffleGrouping(countBold);

          WordCntRedisStoreMapper wordCntRedisStoreMapper = new WordCntRedisStoreMapper(jedisPoolConfig,redisStoreBold);
          builder.setBolt("wordCntStoreBold",wordCntRedisStoreMapper).shuffleGrouping(countBold);

          if (args.length > 0 && args[0].equals("cluster"))
          {
              System.out.println(" run cluster mode begin.");
                try {
                        StormSubmitter.submitTopology(APP_NAME,new Config(),builder.createTopology());
                        System.out.println(" commit ok.");
                    } catch (AlreadyAliveException e) {
                        e.printStackTrace();
                    } catch (InvalidTopologyException e) {
                        e.printStackTrace();
                    } catch (AuthorizationException e) {
                        e.printStackTrace();
                    }

          }
          else{
              System.out.println(" run local mode begin.");
              LocalCluster cluster = new LocalCluster();
              cluster.submitTopology(APP_NAME,new Config(),builder.createTopology());
          }

        System.out.println(" run ok.");




    }
}
