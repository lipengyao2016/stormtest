package com.bigdata.wordcnt;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Tuple;

public class HdfsWordCntApp {

    //在实际开发中这些参数可以将通过外部传入 使得程序更加灵活
    private static final String HDFS_BOLT = "hdfsBolt";
    private static final String APP_NAME = "HdfsWordAppStorm";

    public static void main( String[] args )
    {
        String dataSourceSpout = "dataSourceSpout";
        System.setProperty("HADOOP_USER_NAME","root");

        RecordFormat recordFormat = new DelimitedRecordFormat().withFieldDelimiter("|");
        SyncPolicy syncPolicy = new CountSyncPolicy(100);
        FileRotationPolicy fileRotationPolicy = new FileSizeRotationPolicy(1.0f, FileSizeRotationPolicy.Units.MB);
        FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath("/storm/wordCntResult");
        HdfsBolt hdfsBolt = new HdfsBolt().withFsUrl("hdfs://hadoop001:8020")
                .withRecordFormat(recordFormat)
                .withSyncPolicy(syncPolicy)
                .withRotationPolicy(fileRotationPolicy)
                .withFileNameFormat(fileNameFormat);




        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(dataSourceSpout,new DataSourceSpout());
        builder.setBolt(HDFS_BOLT,hdfsBolt,1).shuffleGrouping(dataSourceSpout);

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
