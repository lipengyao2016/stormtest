package com.bigdata.wordcnt;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataSourceSpout extends BaseRichSpout{
    protected List<String> wordList = Arrays.asList("Spark","Hadoop","Water","Apple","People","Storm");

    protected SpoutOutputCollector spoutOutputCollector;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
         this.spoutOutputCollector = spoutOutputCollector;
    }

    @Override
    public void nextTuple() {

        String line = generateWordLine();
        spoutOutputCollector.emit(new Values(line));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
//         outputFieldsDeclarer.declare(new Fields("line"));
           outputFieldsDeclarer.declare(new Fields("message"));
    }

    protected String generateWordLine()
    {
        Collections.shuffle(this.wordList);

        int randNum = RandomUtils.nextInt(0,wordList.size()-1);
        return StringUtils.join(wordList.toArray()," ",0,randNum);


//        StringBuffer sb = new StringBuffer();
//        int wordCnt = 6;
//        for (int i = 0;i < wordCnt;i++)
//        {
//            int randNum = RandomUtils.nextInt(0,wordList.size()-1);
//            String word = wordList.get(randNum%wordList.size());
//            sb.append(word);
//            if (i != wordCnt-1)
//            {
//                sb.append(" ");
//            }
//        }
//        sb.append("\n");
//        return  sb.toString();
    }
}
