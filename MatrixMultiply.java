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
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MatrixMultiply {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{

    private Text one = new Text();
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      int iter=0;
      String row = "";
      if(itr.hasMoreTokens()){
      	row = itr.nextToken().toString();
      }
      String val = "";
      while (itr.hasMoreTokens()) {
      	String s1 = itr.nextToken().toString();
      	val = val + s1 + " ";
        iter = iter+1;
      }
      one.set(val);
      String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
      for(int i=0;i<iter;i++){
      	String col = Integer.toString(i);
      	if(fileName.equals("left.mat")){
      		word.set(row+"\t"+col);
      		context.write(word, one);
      	}
        else if(fileName.equals("right.mat")){
        	word.set(col+"\t"+row);
    		context.write(word, one);
    	}
      }
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    private Text result = new Text();

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int i=0;
      String sL="",sR="",temp="";
      for (Text val : values){
      	if(i==0)	sL = val.toString();
      	else if(i==1)	sR = val.toString();
      	else break;
      	i++;
      }
      result.set(sL+sR);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "matrix multiply");
    job.setJarByClass(MatrixMultiply.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
