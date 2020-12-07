package com.bigdata.wordcnt;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;

public class HbaseWordCntApp {

    private static final String HBASE_BOLT = "hbaseBolt";
    private static final String APP_NAME = "HbaseWordAppStorm";

    public static void main( String[] args )
    {
        String dataSourceSpout = "dataSourceSpout";
        String splidBold = "splidBold";
        String countBold = "countBold";
        Config config = new Config();
        Map<String,Object> hbaseConfMap = new HashMap<>();
        hbaseConfMap.put("hbase.rootdir","hdfs://hadoop001:8020/hbase");
        hbaseConfMap.put("hbase.zookeeper.quorum","hadoop001:2181");
        config.put("hbase.conf",hbaseConfMap);

        SimpleHBaseMapper simpleHBaseMapper = new SimpleHBaseMapper()
                .withRowKeyField("word")
                .withColumnFields(new Fields("word","count"))
                .withColumnFamily("info");

        HBaseBolt hBaseBolt = new HBaseBolt("WordCnt",simpleHBaseMapper).withConfigKey("hbase.conf");

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(dataSourceSpout,new DataSourceSpout());
        builder.setBolt(splidBold,new SplitBold()).shuffleGrouping(dataSourceSpout);
        builder.setBolt(countBold,new CountBold()).shuffleGrouping(splidBold);
        builder.setBolt(HBASE_BOLT,hBaseBolt,1).shuffleGrouping(countBold);

        if (args.length > 0 && args[0].equals("cluster"))
        {
            System.out.println(" run cluster mode begin.");
            try {
                StormSubmitter.submitTopology(APP_NAME,config,builder.createTopology());
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
            cluster.submitTopology(APP_NAME,config,builder.createTopology());
        }

        System.out.println(" run ok.");



    }
}
