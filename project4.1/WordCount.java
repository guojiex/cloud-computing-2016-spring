import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable>{
            public static String parseALine(String line) {
                String result = line.toLowerCase().trim();
                result = result.replaceAll("</ref>", " ");
                result = result.replaceAll("<ref[^>]*>", " ");
                String[] urlPrefixs = { "http", "https", "ftp" };
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < urlPrefixs.length; i++) {
                    sb.append(urlPrefixs[i]);
                    if (i < urlPrefixs.length - 1)
                        sb.append('|');
                }
                result = result.replaceAll(String.format("(%s)+[^ ]*", sb.toString()), " ");

                result = result.replaceAll("[^a-z']+", " ");
                result=result.replaceAll("\\s{1,}", " ").trim();
                String[] words = result.split(" ");
                sb = new StringBuilder();
                for (String word : words) {
                    String temp = noApostropheDangling(word.toLowerCase());
                    if (!temp.isEmpty())
                        sb.append(temp).append(" ");
                }
                if (sb.toString().isEmpty())
                    return "";
                return sb.toString().substring(0, sb.length() - 1);
            }
            public static String noApostropheDangling(String word) {
                int left = 0;
                int right = word.length() - 1;
                while (left<word.length()&&word.charAt(left) == '\'') {
                    left++;
                }
                while(right>0&&word.charAt(right)=='\''){
                    right--;
                }
                if(left<=right){
                    return word.substring(left,right+1);
                }else{
                    return "";
                }
            }
            private final static IntWritable one = new IntWritable(1);
            private Text word = new Text();
            private static int ngram=5;
            public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
                String line=parseALine(value.toString());
                if(line.isEmpty())
                    return;
                String[] words=line.split(" ");
                for(int i=0;i<words.length;i++){
                    String temp=words[i];
                    if(temp.isEmpty()){
                        continue;
                    }
                    context.write(new Text(temp), one);
                    int count=0;
                    for(int j=1;count<(ngram-1)&&i+j<words.length;j++){
                        if(words[i+j].isEmpty()){
                            continue;
                        }
                        temp=String.format("%s %s", temp,words[i+j]);
                        context.write(new Text(temp), one);
                        count++;
                    }
                }  
                    }
    }
    public static class IntSumReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
            private IntWritable result = new IntWritable();

            public void reduce(Text key, Iterable<IntWritable> values,
                    Context context
                    ) throws IOException, InterruptedException {
                if(key.toString().isEmpty())
                    return;
                int sum = 0;
                for (IntWritable val : values) {
                    sum += val.get();
                }
                result.set(sum);
                context.write(key, result);
                    }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
