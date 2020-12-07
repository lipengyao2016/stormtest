package com.bigdata.wordcnt;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class WriteKafkaWordCntApp {

    private static final String KAFKA_BOLT = "kafkaWriteBolt";
    private static final String APP_NAME = "WriteKafkaWordAppStorm";
    private static final String KAFKA_TOPIC = "kafkaStorm";

    public static void main( String[] args )
    {
        String dataSourceSpout = "dataSourceSpout";
        Properties properties = new Properties();
        properties.put("bootstrap.servers","47.112.111.193:9092");
        properties.put("acks","1");
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");

        KafkaBolt kafkaBolt = new KafkaBolt().withProducerProperties(properties)
                .withTopicSelector(KAFKA_TOPIC)
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(dataSourceSpout,new DataSourceSpout());
        builder.setBolt(KAFKA_BOLT,kafkaBolt).shuffleGrouping(dataSourceSpout);

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
