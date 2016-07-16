package casestudy1;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 
public class SplitData {
	
	public static  class SplitDataMapper extends
    	Mapper<Object, Text, Text, Text> {

		private Text word = new Text();

		public void map(Object key, Text value, Context context)
		    throws IOException, InterruptedException {
			String[] csv = value.toString().split("\t");
			String val="";
			int x = csv.length;
			for(int i=2;i<x;i++){
				val+=(csv[i]+"\t");
			}
			value.set(val.trim());
		    word.set(csv[1]);
		    context.write(word, value);
	    }
}
	
	public static class SplitDataReducer extends
    	Reducer<Text, Text, Text, Text> {

		public void reduce(Text text, Iterable<Text> values, Context context)
		        throws IOException, InterruptedException {
		    for (Text value : values) {
			    context.write(text, value);
		    }
		}
}
 
    public static void main(String[] args) throws IOException,
            InterruptedException, ClassNotFoundException {

    	if(args.length<2){
    		System.out.println("USage: <Class name> <input file><Output folder>");
    		System.exit(0);
    	}
    	
    	//Set input/ Output path
    	Path inputPath = new Path(args[0]);
        Path outputDir = new Path(args[1]);
 
        // Create configuration
        Configuration conf = new Configuration(true);
 
        // Create job
		Job job = Job.getInstance(conf);
        job.setJobName("SplitData");
        job.setJarByClass(SplitData.class);
 
        // Setup MapReduce class
        job.setMapperClass(SplitDataMapper.class);
        job.setReducerClass(SplitDataReducer.class);
        
        //Set number of reducer tasks
        job.setNumReduceTasks(4);
        
        // Set Output key / value
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
 
        // Input
        FileInputFormat.addInputPath(job, inputPath);
        job.setInputFormatClass(TextInputFormat.class);
 
        // Output
        FileOutputFormat.setOutputPath(job, outputDir);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        // Execute job
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}