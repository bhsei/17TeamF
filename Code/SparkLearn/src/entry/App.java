package entry;

import sparkUse.SparkUse;

/**
 * Created by 111111 on 2017/3/28.
 */
public class App {
    public static void main(String [] args)throws Exception
    {
        String cmd = args[0];
        if("readAndfilter".equals(cmd))
        {
            SparkUse.readAndfilter(args);
        }
        else if("saveHdfsTextFile2Local".equals(cmd))
        {
            SparkUse.saveHdfsTextFile2Local(args);
        }
        else if("kafkaSender".equals(cmd))
        {
            SparkUse.kafkaSender(args);
        }
        else if("readFromKafkaClassifySave2Kafka".equals(cmd))
        {
            SparkUse.readFromKafkaClassifySave2Kafka(args);
        }
        else if("readkafkaPrint".equals(cmd))
        {
            SparkUse.readkafkaPrint(args);
        }



    }
}
