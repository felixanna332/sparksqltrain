package com.data.hadoop.demo;


import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class ChainMapper2 extends Mapper<Text, IntWritable, CompKey, NullWritable> {

    @Override
    protected void map(Text key, IntWritable value, Context context) throws IOException, InterruptedException {

        int i = value.get();

        if(i != 9999){
            CompKey ck = new CompKey();
            ck.setYear(key.toString());
            ck.setTemp(i);
            context.write(ck, NullWritable.get());
        }
    }
}
