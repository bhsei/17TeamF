package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 111111 on 2017/3/29.
 */
public class Test {
    public static void main(String[] args)throws Exception
    {
//        String s="\1";
//        System.out.println(s);
        List<String> list = TextUtil.getAllSamplesList("weibo.txt");
        List<String> result=new ArrayList<>();
        for(String s:list)
        {
            String[] w=s.split("\1");
            if(w.length!=3)
                System.out.println(s);

        }
//        TextUtil.saveAllSamples(result,"weibo.txt");
    }
}
