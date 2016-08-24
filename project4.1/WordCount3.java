import java.io.IOException;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.hbase.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;

import org.apache.hadoop.hbase.*;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount3 {

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
                String phrase=words[0];
                for(int i=1;i<words.lengti-1;i++){
                    phrase=String.format("%s %s",phrase,words[i]);
                }
                String word=words[words.length-1];
                context.write(new Text(phrase),new Text(String.format("%s %s",word,keyPair[1])));
            }
    }

    public static class IntSumReducer
            extends TableReducer<Text,Text,ImmutableBytesWritable> {
            public void reduce(Text key, Iterable<Text> values,
                    Context context
                    ) throws IOException, InterruptedException {
                if(key.toString().isEmpty())
                    return;
                String pharse=key.toString();
                TreeMap<Integer,String> map=new TreeMap<Integer,String>();
                long total=0;
                for(Text value:values){
                    String[] words=value.toString().split(" ");
                    total+=Integer.parseInt(words[1]);
                    map.put(Integer.parseInt(words[1]),words[0]);
                }
                int count=0;
                for(Integer key2:map.descendingKeySet()){
                    double pro=key2/(double)total;
                    Put put = new Put(key.getBytes());
                    put.add(Bytes.toBytes("content"), Bytes.toBytes(map.get(key2))
                            , Bytes.toBytes(String.valueOf(pro)));
                    context.write(new ImmutableBytesWritable(key.getBytes()), put);
                    count++;
                    if(count>=5){
                        return;
                    }
                }
            }
    }

    public static void main(String[] args) throws Exception {
        String tablename = "wordcount3";
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "localhost");
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
        job.setJarByClass(WordCount2.class);
        job.setMapperClass(TokenizerMapper.class);
        TableMapReduceUtil.initTableReducerJob(tablename, IntSumReducer.class, job);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        System.exit(job.waitForCompletion(true) ? 0 : 1);  
    }
}

