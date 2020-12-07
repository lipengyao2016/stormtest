package com.bigdata.wordcnt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;

public class CountBold extends BaseRichBolt {
    protected   OutputCollector outputCollector;
    protected Map<String,Integer> countMap = new HashMap<String,Integer>();
    
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField("word");
        Integer countVal = countMap.get(word);
        if (countVal == null)
        {
            countVal = 0;
        }
        countVal++;
        countMap.put(word,countVal);
        System.out.println(" \n\n******execute begin****" );

        for (String key:countMap.keySet()) {
            System.out.println(" word:" + key + " count:" + countMap.get(key));
        }
        System.out.println(" ******execute end****\n\n" );

        outputCollector.emit(new Values(word,String.valueOf(countVal /*1*/)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
             outputFieldsDeclarer.declare(new Fields("word","count"));
    }
}
