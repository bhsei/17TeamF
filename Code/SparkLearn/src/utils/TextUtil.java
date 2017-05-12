package utils;

import scala.Tuple2;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public class TextUtil {
    public static final int uncerntainLabel = 7878;
	
    private static final Pattern patternChinese = Pattern.compile("[\u4e00-\u9fa5]");
    
    private static final String regexChnStr = "[\u4e00-\u9fa5。，？”“《》：！——-、0123456789abcdefghijklmnopqr"
            + "stuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,.?:;<>']";
    private static final Pattern patternValidStr = Pattern.compile(regexChnStr);
    
    private static final String regexURLStr = "(http://[\\w\\.\\-/:?%!]+)|(ftp://[\\w\\.\\-/:?%!]+)|(https://[\\w\\.\\-/:?%!]+)|([\\w\\.\\-/:?%!]+\\.[a-z]+)";
    // )|(www[\\w\\.\\-/:?%!]+)
//    private static final String regexURLStr = "[http://|ftp://|https://|www.\\-/:?%!0123456789"
//            + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ]+";
    private static final Pattern patternURL = Pattern.compile(regexURLStr);

    private static final String regexFloat = "[0-9]+\\.[0-9]+";
    private static final Pattern patternFloat = Pattern.compile(regexFloat);
    
    public static String validString(String content) {
        StringBuilder filterStr = new StringBuilder();
        Matcher matcher = patternValidStr.matcher(content);
        while (matcher.find()) {
            filterStr.append(matcher.group());
        }
        String str = filterStr.toString().toLowerCase();
        return str.replace("nbsp", "").replace("　", "").trim();
    }
    public static void saveStrToFile(String text,String savePath,boolean append) throws IOException
    {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath),append))) {

            writer.write(text);
            writer.newLine();

        }
    }
    public static boolean hasChinese(String str) {
        Matcher matcher = patternChinese.matcher(str);
        if (matcher.find())
            return true;
        else
            return false;
    }

    public static boolean validHanziShuString(String str, int hNum) {
        int count = 0;
        Matcher matcher = patternChinese.matcher(str);
        while (matcher.find()) {
            count++;
            matcher.group();
        }
        if (count < hNum)
            return false;
        else
            return true;
    }
    
    public static List<String> GetURLInString(String str) {
        List<String> list = new ArrayList<>();
        Matcher matcher = patternURL.matcher(str);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    public static String RemoveURLInString(String str) {
        return str.replaceAll(regexURLStr, "").trim();
    }

    public static List<String> GetFloatInString(String str) {
        List<String> list = new ArrayList<>();
        Matcher matcher = patternFloat.matcher(str);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    public static float GetMaxFloatInString(String str) {
        List<String> list = GetFloatInString(str);
        float max = 0.0f;
        for (String s : list) {
            float f = Float.parseFloat(s);
            if (f > max)
                max = f;
        }
        return max;
    }
    
    public static void removeUndefChars(String srcPath, String desPath, int lowTh) 
            throws Exception {
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desPath)))) {
                String t;
                while ((t = reader.readLine()) != null) {
                    t = validString(t);
                    if (t.isEmpty() || t.length() < lowTh) {
                        continue;
                    }
                    writer.write(t);
                    writer.newLine();
                }
            }
        }      
    }
    
    public static String generalizeNumber(String srcstr) {
        StringBuilder builder = new StringBuilder();
        char[] array = srcstr.toCharArray();
        int i = 0;
        int start = 0;
        int end = 0;
        for (; i < array.length; i++) {
            if (Character.isDigit(array[i])) {
                if (i == 0) {
                    start = i;
                    end = start;
                }
                else {
                    if (!Character.isDigit(array[i-1])) {
                        start = i;
                        end = start;
                    }
                    else {
                        end++;
                    }
                }
            }
            else {
                if (end - start > 0) {
                    if (end - start >= 3) {
                        builder.append("number");
                    }
                    else {
                        for (int k = start; k <= end; k++) {
                            builder.append(array[k]);
                        }                        
                    }
                    start = i;
                    end = i;
                }
                builder.append(array[i]);
            }
        }
        if (end - start > 0) {
            if (end - start >= 3) {
                builder.append("number");
            }
            else {
                for (int k = start; k <= end; k++) {
                    builder.append(array[k]);
                }                        
            }
        }
        return builder.toString();
    }
    
    public static int editDistance(int[][] editDistArray, char[] c1, char[] c2, int i, int j) {
        if (i == 0 && j == 0)
            return 0;
        if (i > 0 && j == 0)
            return i;
        if (j > 0 && i == 0)
            return j;
        if (editDistArray[i-1][j-1] >=0)
            return editDistArray[i-1][j-1];
        int d = c1[i-1] == c2[j-1] ? 0 : 1;
        int d1 = editDistance(editDistArray, c1, c2, i-1, j)+1;
        int d2 = editDistance(editDistArray, c1, c2, i, j-1)+1;
        int d3 = editDistance(editDistArray, c1, c2, i-1, j-1)+d;
        d1 = d1 < d2 ? d1 : d2;
        d1 = d1 < d3 ? d1 : d3;
        editDistArray[i-1][j-1] = d1;
        return d1;
    }
    
    public static int editDistance(String str1, String str2) {
        char[] c1 = str1.toCharArray();
        char[] c2 = str2.toCharArray();
        int[][] editDistArray = new int[c1.length][c2.length];
        for (int i = 0; i < c1.length; i++) {
            for (int j = 0; j < c2.length; j++) {
                editDistArray[i][j] = -1;
            }
        }
        return editDistance(editDistArray, c1, c2, c1.length, c2.length);
    }

    public static void delDuplicateData(String srcPath) throws FileNotFoundException, IOException {
        delDuplicateData(srcPath, srcPath);
    }
    
    public static void delDuplicateData(String srcPath, String desPath) throws FileNotFoundException, IOException {
        
        HashSet<String> dataSet = new HashSet<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            String t;
            int line = 0;
            while ((t = reader.readLine()) != null) {
                t = t.trim();
                if (t.isEmpty()) {
                    continue;
                }
                dataSet.add(t);
                line++;
            }
            System.out.printf("原文件有%d行，去重后有%d行\n", line, dataSet.size());        
        }
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desPath)))) {
            for (String s : dataSet) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
    
    public static void delDuplicateDataInOrder(String srcPath) throws FileNotFoundException, IOException {
        delDuplicateDataInOrder(srcPath, srcPath);
    }
    
    public static void delDuplicateDataInOrder(String srcPath, String desPath) throws FileNotFoundException, IOException {
        
        TreeSet<String> dataSet = new TreeSet<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            String t;
            int line = 0;
            while ((t = reader.readLine()) != null) {
                t = t.trim();
                if (t.isEmpty()) {
                    continue;
                }
                dataSet.add(t);
                line++;
            }
            System.out.printf("原文件有%d行，去重后有%d行\n", line, dataSet.size());        
        }
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desPath)))) {
            for (String s : dataSet) {
                writer.write(s);
                writer.newLine();
            }
        }
    }

    public static void delDuplicateBigData(String srcData, String saveData, int filePiece) throws Exception {
        if (filePiece <= 0) {
            return;
        }
        int count = 0;
        try (final BufferedReader reader = new BufferedReader(new FileReader(srcData))) {
            String t;
            while ((t = reader.readLine()) != null) {
                t = t.trim();
                if (t.isEmpty()) {
                    continue;
                }
                count++;
            }
        }
        System.out.println("line count = " + count);
        int perFileSize = count / filePiece;
        int lastFileSize = perFileSize + (count % filePiece);
        List<File> fileList = new ArrayList<>();
        int line = 0;
        try (final BufferedReader reader = new BufferedReader(new FileReader(srcData))) {
            String t;
            for (int id = 0; id < filePiece; id++) {
                int start = id * perFileSize;
                int end = (id == filePiece - 1) ? id * perFileSize + lastFileSize : (id + 1) * perFileSize;
                HashSet<String> set = new HashSet<>();
                while ((t = reader.readLine()) != null) {
                    t = t.trim();
                    if (t.isEmpty()) {
                        continue;
                    }
                    if (line >= start && line < end) {
                        set.add(t);
                    }
                    line++;
                    if (line >= end) {
                        File f = new File(saveData + id);
                        fileList.add(f);
                        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                            for (String s : set) {
                                writer.write(s);
                                writer.newLine();
                            }
                        }
                        break;
                    }
                }
            }
        }
        mergeMultiFilesToOneFile(fileList, saveData);
        for (File f : fileList) {
            f.delete();
        }
    }
    
    public static void copyFile(String srcFile, String desFile) throws Exception {
        try {
            int byteread = 0;
            File fsrc = new File(srcFile);
            if (fsrc.exists() && fsrc.isFile()) {
                InputStream in = new FileInputStream(srcFile);
                FileOutputStream out = new FileOutputStream(desFile);
                byte[] buffer = new byte[1600];
                while ((byteread = in.read(buffer)) != -1) {
                    out.write(buffer, 0, byteread);
                }
                in.close();
            }
        }
        catch (Exception e) {
            System.out.println("发生异常！");
            e.printStackTrace();
        }
    }

    public static void mergeFolderFilesToOneFile(File filePath, BufferedWriter writer, 
            Boolean oneLinePerFile) throws Exception {
        if (!filePath.exists()) {
            return;
        }
        if (filePath.isFile()) {
            mergeSingleFileToOneFile(filePath, writer, oneLinePerFile);
            return;
        }
        File[] files = filePath.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                mergeFolderFilesToOneFile(file, writer, oneLinePerFile);
            } else if (file.isFile()) {
                mergeSingleFileToOneFile(file, writer, oneLinePerFile);
            } else {
            }
        }
    }
    
//    public static void mergeFolderFilesToOneFile(File filePath, BufferedWriter writer,
//            Boolean oneLinePerFile, int lineCountMin) throws Exception {
//        if (!filePath.exists()) {
//            return;
//        }
//        if (filePath.isFile()) {
//            mergeSingleFileToOneFile(filePath, writer, oneLinePerFile, lineCountMin);
//            return;
//        }
//        File[] files = filePath.listFiles();
//        for (File file : files) {
//            if (file.isDirectory()) {
//                mergeFolderFilesToOneFile(file, writer, oneLinePerFile, lineCountMin);
//            } else if (file.isFile()) {
//                mergeSingleFileToOneFile(file, writer, oneLinePerFile, lineCountMin);
//            } else {
//            }
//        }
//    }

    public static void mergeMultiFilesToOneFile(List<File> files, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (File file : files) {
                try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String t;
                    while ((t = reader.readLine()) != null) {
                        t = t.trim();
                        if (t.isEmpty()) {
                            continue;
                        }
                        writer.write(t);
                        writer.newLine();
                    }
                }
            }
        }
    }
    
    public static void mergeMultiFilesToOneFile(String[] files, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (String file : files) {
                try (final BufferedReader reader = new BufferedReader(new FileReader(new File(file)))) {
                    String t;
                    while ((t = reader.readLine()) != null) {
                        t = t.trim();
                        if (t.isEmpty()) {
                            continue;
                        }
                        writer.write(t);
                        writer.newLine();
                    }
                }
            }
        }
    }
    
    public static void mergeMultiFilesToOneFile(File[] files, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (File file : files) {
                try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String t;
                    while ((t = reader.readLine()) != null) {
                        t = t.trim();
                        if (t.isEmpty()) {
                            continue;
                        }
                        writer.write(t);
                        writer.newLine();
                    }
                }
            }
        }
    }

    public static void mergeSingleFileToOneFile(File filePath, BufferedWriter writer, 
            Boolean oneLinePerFile) throws Exception {
        if (!filePath.isFile()) {
            return;
        }
        try (final BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String t;
            if (oneLinePerFile) {
                while ((t = reader.readLine()) != null) {
                    t = t.trim();
                    if (t.isEmpty()) {
                        continue;
                    }
                    writer.write(t);
                    writer.write(" ");
                }
                writer.newLine();
            } else {
                while ((t = reader.readLine()) != null) {
                    t = t.trim();
                    if (t.isEmpty()) {
                        continue;
                    }
                    writer.write(t);
                    writer.newLine();
                }
            }
        }
    }
    
//    public static void mergeSingleFileToOneFile(File filePath, BufferedWriter writer,
//            Boolean oneLinePerFile, int lineCountMin) throws Exception {
//        if (!filePath.isFile()) {
//            return;
//        }
//        try (final BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String t;
//            if (oneLinePerFile) {
//                while ((t = reader.readLine()) != null) {
//                    t = ZhongWenFenCi.validString(t.trim());
//                    if (t.isEmpty()) {
//                        continue;
//                    }
//                    writer.write(t);
//                    writer.write(" ");
//                }
//                writer.newLine();
//            } else {
//                while ((t = reader.readLine()) != null) {
//                    t = ZhongWenFenCi.validString(t.trim());
//                    if (t.isEmpty()) {
//                        continue;
//                    }
//                    if (t.length() >= lineCountMin) {
//                        writer.write(t);
//                        writer.newLine();
//                    }
//                }
//            }
//        }
//    }
    
//    public static void filterAndValidFileByLineCount(String srcFile, String desFile, int lineCountMin)
//            throws Exception {
//        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desFile)))) {
//            mergeSingleFileToOneFile(new File(srcFile), writer, false, lineCountMin);
//        }
//    }

    public static void filterFileByLineCount(String srcFile, String desFile, int lineCountMin)
            throws Exception
    {
        List<String> listData = getAllSamplesList(srcFile);
        List<String> listSave = new ArrayList<>();

        for (String s : listData) {
            s = s.trim();
            if (s.length() >= lineCountMin)
                listSave.add(s);
        }

        TextUtil.saveAllSamples(listSave, desFile);
    }


    public static void shuffleData(String srcPath) throws Exception {
        shuffleData(srcPath, srcPath);
    }
    
    public static void shuffleData(String srcPath, String desPath) throws IOException {
        HashMap<Integer, String> dataSet = new HashMap<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            String t;
            int line = 0;
            while ((t = reader.readLine()) != null) {
                t = t.trim();
                if (t.isEmpty()) {
                    continue;
                }
                dataSet.put(line, t);
                line++;
            }
            Random r = new Random();
            for (int i = 0; i < line / 2; i++) {
                int n = r.nextInt(line);
                String s = dataSet.get(i);
                dataSet.put(i, dataSet.get(n));
                dataSet.put(n, s);
            }
        }
        
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desPath)))) {
            for (Map.Entry entry : dataSet.entrySet()) {
                String s = (String) entry.getValue();
                writer.write(s);
                writer.newLine();
            }
        }    
    }

    public static void removeSampleDataFromCorpus(String srcData, String removeData, String remainData) throws Exception {
        HashSet<String> delSet = new HashSet<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(removeData)))) {
            String t;
            while ((t = reader.readLine()) != null) {
                t = t.trim();
                if (t.isEmpty()) {
                    continue;
                }
                delSet.add(t);
            }
            System.out.println("remove size = " + delSet.size());
        }
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(remainData)))) {
            try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcData)))) {
                String t;
                int line = 0;
                while ((t = reader.readLine()) != null) {
                    t = t.trim();
                    if (t.isEmpty()) {
                        continue;
                    }
                    if (!delSet.contains(t)) {
                        writer.write(t);
                        writer.newLine();
                        line++;
                    }
                }
                System.out.println("remain size = " + line);
            }
        }
    }
    
    public static void readLinesFromBigFile(String srcPath, String desPath, int lineNum) 
            throws FileNotFoundException, IOException {
        
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desPath)))) {
                int n = 0;
                String text;
                while ((text = reader.readLine()) != null) {
                    text = text.trim();
                    writer.write(text);
                    writer.newLine();
                    n++;
                    if (n == lineNum) {
                        break;
                    }
                }
            }
        }
    }
    
    public static HashSet<String> getAllSamples(String srcPath) 
        throws FileNotFoundException, IOException{
        
        HashSet<String> set = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            String text;
            while ((text = reader.readLine()) != null) {
                text = text.trim();
                if (text.isEmpty()) continue;
                set.add(text);
            }
        }
        return set;
    }
    
    public static List<String> getAllSamplesList(String srcPath) 
        throws FileNotFoundException, IOException{
        
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            String text;
            while ((text = reader.readLine()) != null) {
                text = text.trim();
                if (text.isEmpty()) continue;
                list.add(text);
            }
        }
        return list;
    }
    
    public static void saveAllSamples(HashSet<String> set, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (String s : set) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
    
    public static void saveAllSamples(TreeSet<String> set, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (String s : set) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
    
    public static void saveAllSamples(List<String> list, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (String s : list) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
    
    public static void addToSaveAllSamples(List<String> list, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath),true))) {
            for (String s : list) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
    public static void appendToLog(String info, String savePath) throws Exception {
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        InetAddress addr=null;
        String address="unknown";
        try {
            addr=InetAddress.getLocalHost();
            address=addr.getHostName().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath),true))) {
            writer.write("[");
            writer.write(df.format(new Date()));
            writer.write("-");
            writer.write(address);
            writer.write("]");
            writer.write(info);
            writer.newLine();
        }
    }
    public static void saveErrorMsg(Exception e,String savePath)throws Exception
    {
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date=new Date();
        PrintWriter writer=new PrintWriter(new FileWriter(new File(savePath),true));
        writer.write(df.format(date));
        e.printStackTrace(writer);
        writer.close();

    }
    public static void saveAllSamples(Queue<String> list, String savePath) throws Exception {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (String s : list) {
                writer.write(s);
                writer.newLine();
            }
        }
    }
    
    public static void saveHeadSamples(String dataPath, String savePath, int count) throws Exception {
        List<String> list = getAllSamplesList(dataPath);
        if (list.size() <= count) {
            saveAllSamples(list, savePath);
        }
        else {
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
                for (int i = 0; i < count; i++) {
                    writer.write(list.get(i));
                    writer.newLine();                   
                }
            }
        }
    }
    
    public static void saveTailSamples(String dataPath, String savePath, int count) throws Exception {
        List<String> list = getAllSamplesList(dataPath);
        if (list.size() <= count) {
            saveAllSamples(list, savePath);
        }
        else {
            int start = list.size() - count;
            int end = list.size();
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
                for (int i = start; i < end; i++) {
                    writer.write(list.get(i));
                    writer.newLine();                   
                }
            }
        }
    }
    
    public static void saveRandomSamples(String dataPath, String savePath, int count) throws Exception {
        HashSet<String> set = getRandomSamples(dataPath, count);
        saveAllSamples(set, savePath);
    }
    
    public static void saveRandomSamplesInOrder(String dataPath, String savePath, int count) throws Exception {
        HashSet<String> set = getRandomSamples(dataPath, count);
        TreeSet<String> treeSet = new TreeSet<>();
        treeSet.addAll(set);
        saveAllSamples(treeSet, savePath);
    }

    public static void saveMapSamples(HashMap<String, List<String>> mapV, String savePath)
            throws Exception
    {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (Map.Entry<String, List<String>> entry : mapV.entrySet()) {
                String pat = entry.getKey();
                List<String> samples = entry.getValue();
                writer.write(pat);
                writer.newLine();
                for (String s : samples) {
                    writer.write("\t" + s);
                    writer.newLine();
                }
                writer.newLine();
            }
        }
    }

    public static List<Tuple2<String,String>> pickTuplesFromMapSamples(
            HashMap<String, List<String>> mapV)
    {
        List<Tuple2<String,String>> listTuple = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : mapV.entrySet()) {
            String pat = entry.getKey();
            String sample = getOneRandomSample(entry.getValue());
            listTuple.add(new Tuple2<>(pat, sample));
        }
        return listTuple;
    }

    public static void saveRandomMapValues(HashMap<String, List<String>> mapV,
                                           String savePath,
                                           int count) throws Exception
    {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
            for (Map.Entry<String, List<String>> entry : mapV.entrySet()) {
                HashSet<String> set = new HashSet<>(entry.getValue());
                if (set.size() > count) {
                    set = getRandomSamples(set, count);
                }
                for (String s : set) {
                    writer.write(s);
                    writer.newLine();
                }
            }
        }
    }
    
    public static HashSet<String> getRandomSamples(String srcPath, int sampleCount) 
        throws FileNotFoundException, IOException {
        
        HashSet<String> srcSet = getAllSamples(srcPath);
        return getRandomSamples(srcSet, sampleCount);
    }
    
    public static HashSet<String> getRandomSamples(HashSet<String> srcSet, int sampleCount) 
        throws IOException {

        HashSet<String> set = new HashSet<>();
        
        if (srcSet.size() <= sampleCount) {
            set.addAll(srcSet);
            return set;
        }
        
        String[] w = srcSet.toArray(new String[0]);
        Random r = new Random();
        r.setSeed(System.nanoTime());
        HashSet<Integer> index = new HashSet<>();
        while (index.size() != sampleCount) {
            index.add(r.nextInt(sampleCount));
        }        

        for (int i : index) 
            set.add(w[i]);
        return set;
    }

    public static String getOneRandomSample(HashSet<String> srcSet) {
        if (srcSet.isEmpty())
            return "";
        if (srcSet.size() == 1)
            return srcSet.iterator().next();

        String[] w = srcSet.toArray(new String[0]);

        Random r = new Random();
        r.setSeed(System.nanoTime());

        return w[r.nextInt(w.length)];
    }

    public static String getOneRandomSample(List<String> srcList) {
        if (srcList.isEmpty())
            return "";
        if (srcList.size() == 1)
            return srcList.iterator().next();

        String[] w = srcList.toArray(new String[0]);

        Random r = new Random();
        r.setSeed(System.nanoTime());

        return w[r.nextInt(w.length)];
    }

    public static List<String> getRandomSamples(List<String> srcList, int sampleCount)
            throws IOException {

        List<String> list = new ArrayList<>();

        if (srcList.size() <= sampleCount) {
            list.addAll(srcList);
            return list;
        }

        String[] w = srcList.toArray(new String[0]);
        Random r = new Random();
        r.setSeed(System.nanoTime());
        HashSet<Integer> index = new HashSet<>();
        while (index.size() != sampleCount) {
            index.add(r.nextInt(sampleCount));
        }

        for (int i : index)
            list.add(w[i]);
        return list;
    }
    
    public static void randomSplitData(String srcData, float ratio)
            throws Exception {
        if (ratio <=0 || ratio >=1)
            return;

        String splitData1 = srcData + ".split1";
        String splitData2 = srcData + ".split2";
        
        HashSet<String> set = getAllSamples(srcData);
        int count = (int)(set.size() * ratio);
        
        String[] w = set.toArray(new String[0]);
        Random r = new Random();
        r.setSeed(System.nanoTime());
        HashSet<Integer> index = new HashSet<>();
        while (index.size() != count) {
            index.add(r.nextInt(count));
        }  
        
        HashSet<String> set1 = new HashSet<>();
        HashSet<String> set2 = new HashSet<>();
        for (int i = 0; i < w.length; i++) {
            if (index.contains(i)) {
                set1.add(w[i]);
            }
            else {
                set2.add(w[i]);
            }
        }
        
        saveAllSamples(set1, splitData1);
        saveAllSamples(set2, splitData2);
    }
    
    public static Object[] splitSetToPieces(HashSet<String> spliSet, int pieceCount) {    
        if (pieceCount <= 0)
            return null;
        if (spliSet.size() < 10 || pieceCount == 1) {
            Object[] obj = new Object[1];
            obj[0] = spliSet;
            return obj;
        }
        Object[] obj = new Object[pieceCount];
        int n = 0;
        int i = 0;
        HashSet<String> set = new HashSet<>();
        int size = spliSet.size() / pieceCount;
        for (String s : spliSet) {
            set.add(s);
            n++;
            if (n == size) {
                obj[i++] = set;
                set = new HashSet<>();
                n = 0;
            }
        }
        if (n > 0) {
            HashSet<String> lastSet = (HashSet<String>)obj[pieceCount-1];
            lastSet.addAll(set);
        }
        return obj;
    }
    
    public static void intersectionData(String data1, String data2, String saveData) throws Exception {       
        HashSet<String> set1 = getAllSamples(data1);
        HashSet<String> set2 = getAllSamples(data2);
        set1.retainAll(set2);
        saveAllSamples(set1, saveData);
    }
    
    public static void removeData(String srcData, String delData) throws Exception {
        removeData(srcData, delData, srcData);
    }
    
    public static void removeData(String srcData, String delData, String remData) throws Exception {     
        HashSet<String> setSrc = getAllSamples(srcData);
        HashSet<String> setDel = getAllSamples(delData);
        setSrc.removeAll(setDel);
        saveAllSamples(setSrc, remData);
    }    
    
    public static void copyDataManyTimesWithBound(String srcData, int times, int bound) throws Exception {
        copyDataManyTimesWithBound(srcData, srcData, times, bound);
    }
    
    public static void copyDataManyTimesWithBound(String srcData, String desData, int times, int bound) 
            throws Exception {
        int count = 0;
        HashSet<String> set = getAllSamples(srcData);
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(desData)))) {
            for (int i = 0; i < times; i++) {
                for (String s : set) {
                    writer.write(s);
                    writer.newLine();
                    count++;
                    if (count == bound)
                        return;
                }
            }
        }
    }
            
    public static void validAndGeneralizeNumber(String sourceData, String saveData) throws Exception {
        HashSet<String> srcSet = getAllSamples(sourceData);
        HashSet<String> keepSet = new HashSet<>();
        for (String s : srcSet) {
            String valid = validString(s);
            if (valid.isEmpty()) {
                continue;
            }
            if (TextUtil.hasChinese(valid)) {
                String genStr = generalizeNumber(valid);
                if (genStr.length() > 2) {
                    keepSet.add(genStr);
                }                    
            }
        }
        saveAllSamples(keepSet, saveData);
    }
    
    public static void validAndGeneralizeNumber(String sourceData) throws Exception {
        validAndGeneralizeNumber(sourceData, sourceData);
    }
    
    public static void extractChineseLines(String srcPath, String savePath) throws Exception {
        try (final BufferedReader reader = new BufferedReader(new FileReader(new File(srcPath)))) {
            try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(savePath)))) {
                int n = 0;
                int m = 0;
                String text;
                while ((text = reader.readLine()) != null) {
                    text = text.trim();
                    if (text.isEmpty()) continue;
                    n++;
                    if (text.contains("zhwiki")) {
                        writer.write(text);
                        writer.newLine();
                        m++;
                    }
                }
                System.out.println(String.format("total line = %d, chn line = %d", n, m));
            }
        }
    }
    
    /**
     * 读取文件最后N行 
     * 
     * 根据换行符判断当前的行数，
     * 使用统计来判断当前读取第N行
     * 
     * PS:输出的List是倒叙，需要对List反转输出
     * 
     * @param file 待文件
     * @param numRead 读取的行数
     * @return List<String>
     */
    public static List<String> readLastNLineFromBigFile(File file, long numRead) {
        // 定义结果集
        List<String> result = new ArrayList<String>();
        //行数统计
        long count = 0;
        
        // 排除不可读状态
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            return null;
        }
        
        // 使用随机读取
        RandomAccessFile fileRead = null;
        try {
            //使用读模式
            fileRead = new RandomAccessFile(file, "r");
            //读取文件长度
            long length = fileRead.length();
            //如果是0，代表是空文件，直接返回空结果
            if (length == 0L) {
                return result;
            }
            else {
                //初始化游标
                long pos = length - 1;
                while (pos > 0) {
                    pos--;
                    //开始读取
                    fileRead.seek(pos);
                    //如果读取到\n代表是读取到一行
                    if (fileRead.readByte() == '\n') {
                        //使用readLine获取当前行
                        String line = fileRead.readLine();
                        //保存结果
                        result.add(line);
                        
                        //行数统计，如果到达了numRead指定的行数，就跳出循环
                        if (++count == numRead) {
                            break;
                        }
                    }
                }
                if (pos == 0) {
                    fileRead.seek(0);
                    result.add(fileRead.readLine());
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (fileRead != null) {
                try {
                    //关闭资源
                    fileRead.close();
                }
                catch (Exception e) {}
            }
        }
        
        return result;
    }
    
    public static void splitDataByTextLength(String dataPath, int len) 
            throws Exception {
        
        String xiaoPath = dataPath + ".lessthan" + len;
        String daPath = dataPath + ".morethan" + len;
        try (final BufferedReader reader = new BufferedReader(new FileReader(
                new File(dataPath)))) {
            try (final BufferedWriter xmatch = new BufferedWriter(new FileWriter(
                    new File(xiaoPath)))) {
                try (final BufferedWriter dmatch = new BufferedWriter(new FileWriter(
                    new File(daPath)))) {
                    String text;
                    while ((text = reader.readLine()) != null) {
                        text = text.trim();
                        if (text.isEmpty()) continue;
                        if (text.length() < len) {
                            xmatch.write(text);
                            xmatch.newLine();
                        }
                        else {
                            dmatch.write(text);
                            dmatch.newLine();
                        }
                    }                    
                }
            }
        }
    }
    
    public static void splitDataByRegex(String dataPath, String regex) 
            throws Exception {
        
        Pattern pattern = Pattern.compile(regex);
        try (final BufferedReader reader = new BufferedReader(new FileReader(
                new File(dataPath)))) {
            try (final BufferedWriter wrmatch = new BufferedWriter(new FileWriter(
                    new File(dataPath + ".match")))) {
                try (final BufferedWriter wrnomatch = new BufferedWriter(new FileWriter(
                    new File(dataPath + ".nomatch")))) {
                    String text;
                    while ((text = reader.readLine()) != null) {
                        text = text.trim();
                        if (text.isEmpty()) continue;
                        Matcher matcher = pattern.matcher(text);
                        if (matcher.find()) {
                            wrmatch.write(text);
                            wrmatch.newLine();
                        }
                        else {
                            wrnomatch.write(text);
                            wrnomatch.newLine();
                        }
                    }                    
                }
            }
        }
    }
    
    public static void splitDataBySet(String dataPath, String setPath, 
            int nth) throws Exception {
        
        HashSet<String> set = getAllSamples(setPath);
        try (final BufferedReader reader = new BufferedReader(new FileReader(
                new File(dataPath)))) {
            try (final BufferedWriter wrmatch = new BufferedWriter(new FileWriter(
                    new File(dataPath + ".match")))) {
                try (final BufferedWriter wrnomatch = new BufferedWriter(new FileWriter(
                    new File(dataPath + ".nomatch")))) {
                    String text;
                    while ((text = reader.readLine()) != null) {
                        text = text.trim();
                        if (text.isEmpty()) continue;
                        boolean match = false;
                        for (String s : set) {
                            if (editDistance(text, s) < nth) {
                                wrmatch.write(text);
                                wrmatch.newLine();
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            wrnomatch.write(text);
                            wrnomatch.newLine();
                        }
                    }                    
                }
            }
        }
    }
    public static boolean isSimiliar(final String s1, final String s2) {
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty())
            return false;
        int n1 = s1.length();
        int n2 = s2.length();
        int min = n1 < n2 ? n1 : n2;
        int thd = (int)(0.1f * min);
        return editDistance(s1, s2) < thd;
    }
    public static String getHdfsFileNameForThisHour()
    {
        Calendar c = Calendar.getInstance();
        long time=c.getTimeInMillis();
        //若当前七点二十，则获取七点的文件
        int year=c.get(Calendar.YEAR);
        int month=c.get(Calendar.MONTH)+1;
        int day=c.get(Calendar.DAY_OF_MONTH);
        int hour=c.get(Calendar.HOUR_OF_DAY);

        String fileName=String.format("ym=%d-%02d/dd=%d-%02d-%02d/hh=%02d",
                year,month,year,month,day,hour);
        return fileName;
    }
    public static String getNextHdfsFileName (String oldFileName) throws Exception
    {
        String [] w1=oldFileName.split("/dd=");
        String temp=w1[1];
        String [] w2=temp.split("/hh=");
        String formatTime=w2[0]+"-"+w2[1];
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd-HH");
        long oldDate = sdf.parse(formatTime).getTime();
        long newDate = oldDate+60*60*1000;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(newDate);
        int year=c.get(Calendar.YEAR);
        int month=c.get(Calendar.MONTH)+1;
        int day=c.get(Calendar.DAY_OF_MONTH);
        int hour=c.get(Calendar.HOUR_OF_DAY);
        String fileName=String.format("ym=%d-%02d/dd=%d-%02d-%02d/hh=%02d",
                year,month,year,month,day,hour);
        return fileName;
    }
    public static String getHdfsFileNameForLastHour()
    {
        Calendar c = Calendar.getInstance();
        long time=c.getTimeInMillis();
        time=time-60*60*1000;//若当前七点二十，则获取六点的文件
        c.setTimeInMillis(time);
        int year=c.get(Calendar.YEAR);
        int month=c.get(Calendar.MONTH)+1;
        int day=c.get(Calendar.DAY_OF_MONTH);
        int hour=c.get(Calendar.HOUR_OF_DAY);

        String fileName=String.format("ym=%d-%02d/dd=%d-%02d-%02d/hh=%02d",
                year,month,year,month,day,hour);
        return fileName;
    }
    public static boolean match(final Set<String> dataBase,final String text)
    {
        for(String str : dataBase)
        {
            if(isSimiliar(str, text))
                return true;
        }
        return false;
    }
    public static String matchAndCheckLabel(final Set<String> labeledDataBase,final String text)
    {
        for(String str : labeledDataBase)
        {
            String[] w=str.split("\1");
            String label=w[0];//  1/0
            String dxnr=w[1];// 短信正文
            if(isSimiliar(dxnr, text))
                return "1"+label;
        }
        return "0"//没有匹配
                +"0"//仅作为填充位
                ;
    }
}
