package com.bigdata.wordcnt;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Properties;

public class ReadKafkaWordCntApp {

    private static final String KAFKA_SPOUT = "kafkaReadSpout";
    private static final String APP_NAME = "ReadKafkaWordAppStorm";
    private static final String KAFKA_TOPIC = "kafkaReadStorm";
    private static final String KAFKA_BROKER = "47.112.111.193:9092";

    protected static KafkaSpoutConfig getKafkaSpoutConfig(String brokerList,String topic)
    {
        return   KafkaSpoutConfig.builder(brokerList,topic)
                .setProp(ConsumerConfig.GROUP_ID_CONFIG,"stormKafkaConsumer")
                .setOffsetCommitPeriodMs(10000)
                .setRetry(getRetryService())
                .build();
    }

    protected static KafkaSpoutRetryService getRetryService()
    {
        return new KafkaSpoutRetryExponentialBackoff(KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(5),
                KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(2),Integer.MAX_VALUE, KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(10));
    }

    public static void main( String[] args )
    {
        String dataSourceSpout = "dataSourceSpout";
        TopologyBuilder builder = new TopologyBuilder();
        KafkaSpout kafkaSpout = new KafkaSpout(getKafkaSpoutConfig(KAFKA_BROKER,KAFKA_TOPIC));
        builder.setSpout(KAFKA_SPOUT,kafkaSpout,5);
        LogConsoleBold logConsoleBold = new LogConsoleBold();
        builder.setBolt("logConsole",logConsoleBold,5).shuffleGrouping(KAFKA_SPOUT);


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
