package casestudy2;

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
 
public class MaxVariation {
	
	public static  class MaxVariationMapper extends
    	Mapper<Object, Text, Text, Text> {

		private Text ticker = new Text();
		private Text rest = new Text();

		public void map(Object key, Text value, Context context)
		    throws IOException, InterruptedException {
			String[] tsv = value.toString().split("\t");
			// Ticker, Date, Open, High, Low, Close, Volume, Adjusted Close
		    ticker.set(tsv[0]);
		    rest.set(tsv[1]+","+tsv[3]+","+tsv[4]);
		    
		    context.write(ticker, rest);
	    }
}
	
	public static class MaxVariationReducer extends
    	Reducer<Text, Text, Text, Text> {
           
           private Text val = new Text();
		public void reduce(Text text, Iterable<Text> values, Context context)
		        throws IOException, InterruptedException {
			float max = 0.0f,var;
		    for (Text value : values) {
		    	String []data = value.toString().split(",");
		    	var = Float.parseFloat(data[1]) - Float.parseFloat(data[2]);
		    	if(var >max){		    	
		    		max = var;
		    		val.set("\t"+data[0]+"\t"+max);
		    	}
		    }
		    
		    context.write(text, val);
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
        job.setJobName("MaxVariation");
        job.setJarByClass(MaxVariation.class);
 
        // Setup MapReduce class
        job.setMapperClass(MaxVariationMapper.class);
        job.setReducerClass(MaxVariationReducer.class);
                
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