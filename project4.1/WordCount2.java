import java.io.IOException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.*;


import org.apache.hadoop.hbase.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount2 {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, Text>{
            private Text word = new Text();
            public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
                String line=value.toString();
                String[] keyPair=line.split("\t");
                String[] words=keyPair[0].split(" ");
                if(words.length<=1){
                    return;
                }
                if(Integer.parseInt(keyPair[1])<=2){
                    return;
                }
                StringBuilder sb=new StringBuilder();
                for(int i=0;i<words.length-1;i++){
                    sb.append(words[i]).append(" ");
                }
                String word=words[words.length-1];
                context.write(new Text(sb.toString().trim()),new Text(String.format("%s %s",word,keyPair[1])));
                /*int end=keyPair[0].lastIndexOf(" ");
                context.write(new Text(keyPair[0].substring(0,end).trim())
                        ,new Text(keyPair[0].substring(end+1).trim()+" "+keyPair[1]));*/
            }
    }

    public static class IntSumReducer
            extends TableReducer<Text,Text,ImmutableBytesWritable> {
            public class WordCount implements Comparable<WordCount>{
                String word;
                int count;
                public WordCount(String word,int count){
                    this.word=word;
                    this.count=count;
                }
                @Override
                public int compareTo(WordCount o) {
                    if(this.count==o.count){
                        return this.word.compareTo(o.word);
                    }
                    return -(this.count-o.count);
                }
            }      
            public void reduce(Text key, Iterable<Text> values,
                    Context context
                    ) throws IOException, InterruptedException {
                if(key.toString().isEmpty())
                    return;
                String pharse=key.toString();
                List<WordCount> list=new ArrayList<>();
                int total=0;
                for(Text value:values){
                    String[] words=value.toString().split(" ");
                    list.add(new WordCount(words[0],Integer.parseInt(words[1])));
                }
                Collections.sort(list);
                for(int i=0;i<5&&i<list.size();i++){
                    total+=list.get(i).count;
                }
                Put put = new Put(key.getBytes());
                
                for(int i=0;i<5&&i<list.size();i++){
                    double pro=(double)(list.get(i).count)/total;
                    put.add(Bytes.toBytes("content"), Bytes.toBytes(list.get(i).word)
                            , Bytes.toBytes(String.valueOf(pro)));
                }
                /*context.write(new ImmutableBytesWritable(key.getBytes()), put);*/
                context.write(null, put);

            }
    }

    public static void main(String[] args) throws Exception {
        String tablename = "wordcount";
        Configuration conf = HBaseConfiguration.create();
        /*conf.set("hbase.zookeeper.quorum", "localhost");*/
        HBaseAdmin admin = new HBaseAdmin(conf);
        if(admin.tableExists(tablename)){
            System.out.println("table exists!recreating.......");
            admin.disableTable(tablename);
            admin.deleteTable(tablename);
        }
        HTableDescriptor htd = new HTableDescriptor(tablename);
        HColumnDescriptor tcd = new HColumnDescriptor("content");
        htd.addFamily(tcd);
        admin.createTable(htd);
        Job job = new Job(conf, "WordCountHbase");
        job.setInputFormatClass(TextInputFormat.class);
        job.setJarByClass(WordCount2.class);
        job.setMapperClass(TokenizerMapper.class);
        TableMapReduceUtil.initTableReducerJob(tablename, IntSumReducer.class, job);
        System.out.println(args[0]);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        /*job.setNumReduceTasks(0);*/
        System.exit(job.waitForCompletion(true) ? 0 : 1);  
    }
}

