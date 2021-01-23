package ExtendLinking;


import Linking.LinkingViaEMNoContext;
import WebPage.Decompose2;
import WebPage.GeneralModel;
import com.sun.org.apache.regexp.internal.RE;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import jdk.nashorn.internal.runtime.ECMAException;
import org.apache.commons.math3.linear.*;

public class LinkingViaExtendContent {
    public HashSet<String> Names = new HashSet<String>();
    public HashMap<String, ArrayList<String[]>> NamesContent = new HashMap<String, ArrayList<String[]>>();
    public HashMap<String, ArrayList<ArrayList<Double[]>>> NamesContentSim = new HashMap<String, ArrayList<ArrayList<Double[]>>>();
//    public HashMap<String, HashMap> NamesContentSim = new HashMap<String, HashMap>();
    public HashMap<String, ArrayList<ArrayList<Double>>> ConSimTemp = new HashMap<>();
    public HashMap<String, ArrayList<String>> NamesContentLab = new HashMap<>();

    public double[] weight = new double[3];
    public double learning_rate = 0.0001;
    public double clu_converge_propor = 0.9;
    public double learning_converge_thres = 0.001;
    public double stop_propor = 0.7;
    public double ml_rate = 5.0;
    public double cl_rate = 5.0;

    public LinkingViaExtendContent(int max_num) throws Exception{ // for baseline test;
        ReadNames();
        ReadContent(max_num);
    }

    public LinkingViaExtendContent(int max_num, double stop_pro) throws Exception{
        double i = 1;
        i /= 3;
        weight = new double[]{i, i, i};
        stop_propor = stop_pro;
        ReadNames();
//        ReadContent(max_num);
//        ReadSim(max_num);
//        ReadLinkLabel(max_num);

        ReadContent(5000);   //for scale testing
        ReadSim(5000);
        ReadLinkLabel(5000);
        if (max_num < 5000){
            set_max_less_5000(max_num);
        }

    }

    public void set_max_less_5000(int max_num){
        for (String name : Names){
            HashSet<Integer> selected = new HashSet<Integer>();
            ArrayList<String[]> tweets = NamesContent.get(name);
            ArrayList<ArrayList<Double[]>> namesim = NamesContentSim.get(name);
            ArrayList<String> labels = NamesContentLab.get(name);
            for (int i = 0; i < tweets.size(); i++){
                if (!tweets.get(i)[0].equals("NIL")){
                    selected.add(i);
                }
            }
            int test_size = selected.size();
            if (tweets.size() - test_size > max_num){
                ArrayList<String[]> new_tweets = new ArrayList<String[]>();
                ArrayList<ArrayList<Double[]>> new_namesim = new ArrayList<>();
                ArrayList<String> new_labels = new ArrayList<>();

                Random ra =new Random();
                while (selected.size() != test_size + max_num){
                    selected.add(ra.nextInt(tweets.size()));
                }

                ArrayList<Integer> seq = new ArrayList<>(selected);
                Collections.sort(seq);

                for (int i = 0; i < seq.size(); i++){
                    new_tweets.add(tweets.get(seq.get(i)));
                    new_labels.add(labels.get(seq.get(i)));
                    if (i != seq.size() - 1) {
                        ArrayList<Double[]> new_s_list = new ArrayList<>();
                        ArrayList<Double[]> this_line = namesim.get(seq.get(i));
                        for (int j = i + 1; j < seq.size(); j++) {
                            new_s_list.add(this_line.get(seq.get(j) - seq.get(i) - 1));
                        }
                        new_namesim.add(new_s_list);
                    }
                }
                NamesContent.put(name, new_tweets);
                NamesContentSim.put(name, new_namesim);
                NamesContentLab.put(name, new_labels);
                System.out.printf("Cut %s to %d + %d\n", name, test_size, max_num);
            }
            else{
                System.out.printf("%s less than %d\n", name);
            }
        }
    }




    public void ReadNames() throws Exception{
        //        String name_path = "E:\\Python project\\EL research\\src\\names";
        String name_path = ".\\newIMDB\\names";
        InputStreamReader in = new InputStreamReader(new FileInputStream(name_path), "utf-8");
        BufferedReader br = new BufferedReader(in);
        String s;
        while((s = br.readLine()) != null){
            if (!s.trim().equals("James Brown")){        //Phil Harris "Robert Taylor" Jim Norton
                continue;
            }   //test
            Names.add(s.trim());
        }
        br.close();
        in.close();
    }

    public void ReadContent(int max_num) throws Exception{
//        String text_path = "E:\\Python project\\EL research\\result\\output_sim_text\\100_ln_log_all" + max_num + "\\text\\";
        String text_path = ".\\extenddata\\100_ln_log_all" + max_num + "\\text\\";
        for (String name : Names){
            InputStreamReader in = new InputStreamReader(new FileInputStream(text_path + name), "utf-8");
            BufferedReader br = new BufferedReader(in);
            String s;
            ArrayList<String[]> contents = new ArrayList<>();
            while((s = br.readLine()) != null){
                String[] strings = s.split("\t");
                if (strings.length == 2) {
                    String[] tuple = {strings[1], ""};
                    contents.add(tuple);
                }
                else {
                    String[] tuple = {strings[1], strings[2]};
                    contents.add(tuple);
                }
            }
            NamesContent.put(name, contents);
            br.close();
            in.close();
        }
    }

    public void ReadSim(int max_num) throws Exception {
//        String text_path = "E:\\Python project\\EL research\\result\\output_sim_text\\100_ln_log_all" + max_num + "\\sim\\";
        String text_path = ".\\extenddata\\100_ln_log_all" + max_num + "\\sim\\";
        for (String name : Names){
            System.out.printf("Reading sim_mat name = %s\n", name);
            InputStreamReader in = new InputStreamReader(new FileInputStream(text_path + name), "utf-8");
            BufferedReader br = new BufferedReader(in);
            String s;
            ArrayList<ArrayList<Double[]>> namesim = new ArrayList<>();
            while((s = br.readLine()) != null){
                ArrayList<Double[]> contentsim = new ArrayList<>();
                String[] strings = s.split("\t");
                if (s.equals("")){
                    break;
                }
                for (String tuple : strings){
                    tuple = tuple.replaceAll("[()]", "");
                    String[] values = tuple.split(",");
                    if (values.length == 3) {
                        Double[] dvalues = {Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2])};
                        contentsim.add(dvalues);
                    }
                    else{
                        System.out.println("Error: imcomplete simvalues");
                    }

                }
                namesim.add(contentsim);
            }
            NamesContentSim.put(name, namesim);
            br.close();
            in.close();
        }

//        for (String name : Names){
//            System.out.printf("Reading sim_mat name = %s\n", name);
//            InputStreamReader in = new InputStreamReader(new FileInputStream(text_path + name), "utf-8");
//            BufferedReader br = new BufferedReader(in);
//            String s;
//            HashMap<String, Double[]> namesim = new HashMap<>();
//            while((s = br.readLine()) != null){
//                if (s.equals("")){
//                    break;
//                }
//                ArrayList<Double[]> contentsim = new ArrayList<>();
//                String[] values = s.split(",");
//                Double[] dvalues = {Double.parseDouble(values[2]), Double.parseDouble(values[3]), Double.parseDouble(values[4])};
//                namesim.put(String.format("%s,%s", values[0], values[1]), dvalues);
//            }
//            NamesContentSim.put(name, namesim);
//        }
    }

    public void ReadLinkLabel(int max_num) throws Exception{
        String text_path = ".\\extenddata\\100_ln_log_all" + max_num + "\\label";
        File f = new File(text_path);
        if (!f.exists()){
            System.out.println("No Single Label Data!!!   GET IT NOW!\n");
            get_everyone_link(max_num);
        }
        for (String name : Names){
            InputStreamReader in = new InputStreamReader(new FileInputStream(text_path + "\\" + name), "utf-8");
            BufferedReader br = new BufferedReader(in);
            String s;
            ArrayList<String> labels = new ArrayList<>();
            while((s = br.readLine()) != null){
                if (!s.equals("")){
                    String[] split = s.trim().split("\t");
                    labels.add(split[1].replaceAll(",", ""));
                }
            }
            NamesContentLab.put(name, labels);
            br.close();
            in.close();
        }
    }

    public void get_test_data_ready() throws Exception{
        System.out.printf("Now get every single test data and decompose.\n");
        clear_path();
        int count = 1;

        StringBuilder strpath=new StringBuilder();
        StringBuilder strname=new StringBuilder();
        StringBuilder strturename=new StringBuilder();
        for (String name : Names){
            for (int i = 0; i < NamesContent.get(name).size(); i++) {
                String true_label = NamesContent.get(name).get(i)[0], text = NamesContent.get(name).get(i)[1];
                if (!true_label.equals("NIL")) {
                    strpath.append(count + "\tpath\n");
                    strname.append(count + "\t" + encode_name(name) + "\n");
                    strturename.append(count + "\t" + encode_name(true_label) + "\n");
                    FileWriter fw = new FileWriter(String.format(".\\newIMDB\\TextWebpages\\%d", count));
                    fw.write(text);
                    fw.close();
                    count++;
                }
            }
        }

        FileWriter fileWriter1 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToPath");
        fileWriter1.write(strpath.toString());
        fileWriter1.close();

        FileWriter fileWriter2 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToMentionName");
        fileWriter2.write(strname.toString());
        fileWriter2.close();

        FileWriter fileWriter3 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToGoldenEntityName");
        fileWriter3.write(strturename.toString());
        fileWriter3.close();

        System.out.println("Saved Single text.");

        System.out.println("Decompose text......");
        Decompose2 decompose2 = new Decompose2();
        decompose2.geneCandidates();
        decompose2.decomposeTxt();
        System.out.println("Done");
    }

    public void get_everyone_link(int max_num) throws Exception{
        System.out.printf("Link for everyone\n");
        String text_path = ".\\extenddata\\100_ln_log_all" + max_num + "\\label";
        File f = new File(text_path);
        if (!f.exists()){
            f.mkdir();
        }
        clear_path();
        int count = 1;

        StringBuilder strpath=new StringBuilder();
        StringBuilder strname=new StringBuilder();
        StringBuilder strturename=new StringBuilder();
        for (String name : Names){
            for (int i = 0; i < NamesContent.get(name).size(); i++) {
                String true_label = NamesContent.get(name).get(i)[0], text = NamesContent.get(name).get(i)[1];
                strpath.append(count + "\tpath\n");
                strname.append(count + "\t" + encode_name(name) + "\n");
                strturename.append(count + "\t" + encode_name(true_label) + "\n");
                FileWriter fw = new FileWriter(String.format(".\\newIMDB\\TextWebpages\\%d", count));
                fw.write(text);
                fw.close();
                count++;
            }
        }

        FileWriter fileWriter1 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToPath");
        fileWriter1.write(strpath.toString());
        fileWriter1.close();

        FileWriter fileWriter2 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToMentionName");
        fileWriter2.write(strname.toString());
        fileWriter2.close();

        FileWriter fileWriter3 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToGoldenEntityName");
        fileWriter3.write(strturename.toString());
        fileWriter3.close();

        System.out.println("Saved Single text.");

        System.out.println("Decompose text......");
        Decompose2 decompose2 = new Decompose2();
        decompose2.geneCandidates();
        decompose2.decomposeTxt();
        System.out.println("Done");

        System.out.println("General Model......");
        GeneralModel generalModel = new GeneralModel();
        generalModel.constructGeneralModel();
        System.out.println("Done");

        System.out.println("Start Linking......");
        LinkingViaEMNoContext linkingViaEMNoContext = new LinkingViaEMNoContext();
        linkingViaEMNoContext.for_ex_linking();
        System.out.println("Linking Over. Congratulations");

        String result_text = ".\\data\\LinkHINData\\predictresult";
        InputStreamReader in = new InputStreamReader(new FileInputStream(result_text), "utf-8");
        BufferedReader br = new BufferedReader(in);
        String s;
        HashMap<Integer, String> labels = new HashMap<>();
        while((s = br.readLine()) != null){
            if (!s.equals("")){
                String[] split = s.trim().split("\t");
                labels.put(Integer.parseInt(split[0]), decode_name(split[1]));
            }
        }
        br.close();
        in.close();

        count = 1;
        for (String name : Names){
            StringBuilder ls = new StringBuilder();
            for (int i = 0; i < NamesContent.get(name).size(); i++) {
                String label = labels.get(count);
                ls.append(String.format("%d\t%s\n", i, label));
                count++;
            }
            FileWriter fw = new FileWriter(String.format("%s\\%s", text_path, name));
            fw.write(ls.toString());
            fw.close();
        }
    }

    public void fresh_temp_sim(String name, double[] temp_weight){
        ConSimTemp.clear();
        System.out.printf("Using weight = %f,%f,%f cal sim for %s\n", temp_weight[0], temp_weight[1], temp_weight[2], name);
        ArrayList<ArrayList<Double>> sim_result = new ArrayList<>();
        for (int i = 0; i < NamesContentSim.get(name).size(); i++) {
            ArrayList<Double> sim_list = new ArrayList<>();
            for (int j = 0; j < NamesContentSim.get(name).get(i).size(); j++) {
                Double[] ori_sim = NamesContentSim.get(name).get(i).get(j);
                double sim = temp_weight[0] * ori_sim[0] + temp_weight[1] * ori_sim[1] + temp_weight[2] * ori_sim[2];
                sim_list.add(sim);
            }
            sim_result.add(sim_list);
        ConSimTemp.put(name, sim_result);
        }
    }

    public double get_temp_sim(String name, int i, int j){
        double result = 0;
        if (i < j){
            result = ConSimTemp.get(name).get(i).get(j-i-1);
        }else if (i == j) {
            System.out.printf("What? Same index? %d,%d\n", i, j);
            result = 1;
        }else {
            result = ConSimTemp.get(name).get(j).get(i-j-1);
        }
        return result;
    }

    public Double[] get_origin_sim(String name, int i, int j){
        Double[] result = {};
        if (i < j){
            result = NamesContentSim.get(name).get(i).get(j-i-1);
        }else if (i == j) {
            System.out.printf("What? Same index? %d,%d\n", i, j);
        }else {
            result = NamesContentSim.get(name).get(j).get(i-j-1);
        }
        return result;
    }

    public double cal_clu_sim(String name, int[] clu1, int[] clu2){
        double sim_sum = 0;
        for (int i = 0; i < clu1.length; i++) {
            for (int j = 0; j < clu2.length; j++) {
                sim_sum += get_temp_sim(name, clu1[i], clu2[j]);
            }
        }
        sim_sum /= clu1.length * clu2.length;
        return sim_sum;
    }

    public double cal_single_clu_sim(String name, int[] clu1, int index){
        double sim_sum = 0;
        for (int i = 0; i < clu1.length; i++) {
            sim_sum += get_temp_sim(name, clu1[i], index);
        }
        sim_sum /= clu1.length;
        return sim_sum;
    }

    public ArrayList clustering(String name, double stop_proportion){
        System.out.printf("clu for %s\n", name);
        ArrayList<int[]> clu = new ArrayList<>();
        HashSet<Integer> old_centroid = new HashSet<>();
        HashMap<Integer, ArrayList<Double>> everyone_clu_sim = new HashMap<>();
        HashMap<String, Double> clu_sim = new HashMap<>();
        int max_iter_time = 15;
        int test_num = 0;
        int all_num = NamesContent.get(name).size();
        int done_num = 0;

        for (String[] tweet : NamesContent.get(name)){
            int index = NamesContent.get(name).indexOf(tweet);
            everyone_clu_sim.put(index, new ArrayList<>());
            if (!tweet[0].equals("NIL")){
                test_num++;
                clu.add(new int[]{index});
                old_centroid.add(index);
            }
        }
        int clu_num = test_num, old_clu_num = test_num;


        for (int ite_time = 0; ite_time < max_iter_time; ite_time++) {
            System.out.printf("Iterate %d times; clu = %d/%d (%f)\n", ite_time + 1, clu_num, test_num, stop_proportion);
            done_num = clu_num;

            clu_sim.clear();
            for (int i = 0; i < clu_num; i++) {
                for (int j = i + 1; j < clu_num; j++) {
                    double sim = cal_clu_sim(name, clu.get(i), clu.get(j));
                    clu_sim.put(String.format("%d,%d", i, j), sim);
                }
            }
            for (Integer index : everyone_clu_sim.keySet()){
                ArrayList<Double> nil_clu_sims = new ArrayList<>();
                if (old_centroid.contains(index)){
                    nil_clu_sims.add(-1.0);
                }else{
                    for (int i = 0; i < clu_num; i++) {
                        double sim = cal_single_clu_sim(name, clu.get(i), index);
                        nil_clu_sims.add(sim);
                    }
                }
                everyone_clu_sim.put(index, nil_clu_sims);
            }

            while (done_num < all_num * stop_proportion){
                String max_clu_clu_map;
                double max_clu_clu_sim = 0, max_nil_clu_sim = 0;
                int max_nil = -1, max_clu = -1;


                List<HashMap.Entry<String, Double>> list = new ArrayList<HashMap.Entry<String, Double>>(clu_sim.entrySet());
                Collections.sort(list, new Comparator<HashMap.Entry<String, Double>>() {
                    @Override
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });
                if (list.size() == 0){
                    max_clu_clu_map = "NIL,NIL";
                    max_clu_clu_sim = -1.0;
                }
                else{
                    max_clu_clu_map = list.get(0).getKey();
                    max_clu_clu_sim = list.get(0).getValue();
                }


                for (Integer index : everyone_clu_sim.keySet()){
                    double max_this = Collections.max(everyone_clu_sim.get(index));
                    if (max_this >= max_nil_clu_sim) {
                        max_nil_clu_sim = max_this;
                        max_clu = everyone_clu_sim.get(index).indexOf(max_this);
                        max_nil = index;
                    }
                }

//                System.out.printf("%d,%d %f\t%f %s\n", max_nil, max_clu, max_nil_clu_sim, max_clu_clu_sim, max_clu_clu_map);

                if (max_clu_clu_sim >= max_nil_clu_sim){
                    String[] clus = max_clu_clu_map.split(",");
                    int c0 = Integer.parseInt(clus[0]), c1 = Integer.parseInt(clus[1]);
                    ArrayList<int[]> new_clu = new ArrayList<>();
                    for (int j = 0; j < clu.size(); j++) {
                        if (j == c0){
                            int[] new_c = new int[clu.get(c0).length + clu.get(c1).length];
                            System.arraycopy(clu.get(c0), 0, new_c, 0, clu.get(c0).length);
                            System.arraycopy(clu.get(c1), 0, new_c, clu.get(c0).length, clu.get(c1).length);
                            new_clu.add(new_c);
                        }else if (j == c1){
                            continue;
                        }else{
                            new_clu.add(clu.get(j));
                        }
                    }
                    clu = new_clu;
                    clu_num--;
//                    System.out.printf("Merged clu; clu_num = %d\n", clu_num);

                    clu_sim = new HashMap<>();
                    for (int i = 0; i < clu_num; i++) {
                        for (int j = i + 1; j < clu_num; j++) {
                            double sim = cal_clu_sim(name, clu.get(i), clu.get(j));
                            clu_sim.put(String.format("%d,%d", i, j), sim);
                        }
                    }
                    for (Integer index : everyone_clu_sim.keySet()){
                        if (everyone_clu_sim.get(index).get(0) == -1){
                            continue;
                        }
                        ArrayList<Double> nil_clu_sims = new ArrayList<>();
                        for (int k = 0; k < clu_num; k++) {
                            double sim = cal_single_clu_sim(name, clu.get(k), index);
                            nil_clu_sims.add(sim);
                        }
                        everyone_clu_sim.put(index, nil_clu_sims);
                    }

                }else{
                    int [] new_c = new int[clu.get(max_clu).length + 1];
                    System.arraycopy(clu.get(max_clu), 0, new_c, 0, clu.get(max_clu).length);
                    new_c[clu.get(max_clu).length] = max_nil;
                    clu.set(max_clu, new_c);

                    for (int j = 0; j < clu.size(); j++) {
                        if (j != max_clu){
                            int max, min;
                            if (j > max_clu){
                                max = j;
                                min = max_clu;
                            }else{
                                max = max_clu;
                                min = j;
                            }
                            double new_sim = clu_sim.get(String.format("%d,%d", min, max)) * clu.get(j).length * (clu.get(max_clu).length -1);
                            for (int index : clu.get(j)){
                                new_sim += get_temp_sim(name, index, max_nil);
                            }
                            new_sim /= clu.get(j).length * clu.get(max_clu).length;
                            clu_sim.put(String.format("%d,%d", min, max), new_sim);
                        }
                    }
                    for (int j : everyone_clu_sim.keySet()) {
                        if (old_centroid.contains(j)){
                            continue;
                        }
                        if (j == max_nil){
                            ArrayList<Double> invalid = new ArrayList<>();
                            invalid.add(-1.0);
                            everyone_clu_sim.put(j, invalid);
                        }else{
                            if (everyone_clu_sim.get(j).get(0).equals(-1.0)){
                                continue;
                            }
                            double new_sim = everyone_clu_sim.get(j).get(max_clu) * (clu.get(max_clu).length - 1);
                            new_sim += get_temp_sim(name, j, max_nil);
                            new_sim /= clu.get(max_clu).length;
                            everyone_clu_sim.get(j).set(max_clu, new_sim);
                        }
                    }
                    done_num++;
                }
            }

            ArrayList<int[]> new_clu = new ArrayList<>();
            HashSet<Integer> new_centorid = new HashSet<>();
            for (int i = 0; i < clu.size(); i++) {
                int[] c = clu.get(i);
                int max_centroid = -1;
                double max_sum = -1.0;
                for (int j = 0; j < c.length; j++) {
                    double temp_sum = 0;
                    for (int k = 0; k < c.length; k++) {
                        if (k != j){
                            temp_sum += get_temp_sim(name, c[j], c[k]);
                        }
                    }
                    if (temp_sum > max_sum){
                        max_sum = temp_sum;
                        max_centroid = c[j];
                    }
                }
                new_clu.add(new int[]{max_centroid});
                new_centorid.add(max_centroid);
            }

            if (clu_num == old_clu_num) {
                int count = 0;
                for (Integer i : new_centorid) {
                    if (old_centroid.contains(i)) {
                        count++;
                    }
                }
                if (count >= clu_converge_propor * clu_num) {
                    System.out.printf("Stop Clu! Centroid Steady with converge_propor = %f\n", clu_converge_propor);
                    break;
                }
            }
            if (ite_time != max_iter_time - 1){
                clu = new_clu;
                old_clu_num = clu_num;
                old_centroid = new_centorid;
            }

        }

        return clu;
    }

    public void clear_path() throws Exception{
//        File f_path = new File(".\\newIMDB\\TextWebpages");
//        for (File f : f_path.listFiles()){
//            f.delete();
//        }
//        f_path = new File(".\\newIMDB\\ObjSet");
//        for (File f : f_path.listFiles()){
//            f.delete();
//        }
        DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(".\\newIMDB\\TextWebpages"));
        for (Path p : paths){
            Files.delete(p);
        }
        paths = Files.newDirectoryStream(Paths.get(".\\newIMDB\\ObjSet"));
        for (Path p : paths){
            Files.delete(p);
        }
    }

    public String encode_name(String n){
        if (n.equals("NIL")){
            return n;
        }
        String result = new String();
        String[] split = n.split(" ");
        if (split.length == 3){
            result = String.format("%s, %s %s", split[1], split[0], split[2]);
        }
        if (split.length == 2){
            result = String.format("%s, %s", split[1], split[0]);
        }
        return result;
    }

    public String decode_name(String n){
        String result = new String();
        String[] split = n.split(" ");
        if (split.length == 3){
            result = String.format("%s %s %s", split[1], split[0].replace(",", ""), split[2]);
        }
        if (split.length == 2){
            result = String.format("%s %s", split[1], split[0].replace(",", ""));
        }
        return result;
    }

    public double[] get_new_weight_normalize(double[] temp_change){
//        temp_change[0] = learning_rate * temp_change[0] + weight[0];
//        temp_change[1] = learning_rate * temp_change[1] + weight[1];
//        temp_change[2] = learning_rate * temp_change[2] + weight[2];
        double temp_sum = temp_change[0] + temp_change[1] + temp_change[2];
        temp_change[0] /= temp_sum;
        temp_change[1] /= temp_sum;
        temp_change[2] /= temp_sum;
        return temp_change;
    }

    public double[] get_deriv(String name, double[] wei, HashSet<Pair<Integer, Integer>> ml_pair, HashSet<Pair<Integer, Integer>> cl_pair) {
        double[] f = new double[3];
        for (Pair<Integer, Integer> pair : cl_pair) {
            Double[] origin_sim = get_origin_sim(name, pair.getKey(), pair.getValue());
            f[0] += Math.pow(origin_sim[0], 2);
            f[1] += Math.pow(origin_sim[1], 2);
            f[2] += Math.pow(origin_sim[2], 2);
        }
        double sum_d = 0;
        double[] sum_x2_d = new double[3];
        for (Pair<Integer, Integer> pair : ml_pair) {
            Double[] origin_sim = get_origin_sim(name, pair.getKey(), pair.getValue());
            double d = Math.sqrt(wei[0] * Math.pow(origin_sim[0], 2) + wei[1] * Math.pow(origin_sim[1], 2) + wei[2] * Math.pow(origin_sim[2], 2));
            sum_d += d;
            sum_x2_d[0] += Math.pow(origin_sim[0], 2) / d;
            sum_x2_d[1] += Math.pow(origin_sim[1], 2) / d;
            sum_x2_d[2] += Math.pow(origin_sim[2], 2) / d;
        }
        if (sum_d != 0){
            f[0] -= sum_x2_d[0] / sum_d / 2;
            f[1] -= sum_x2_d[1] / sum_d / 2;
            f[2] -= sum_x2_d[2] / sum_d / 2;
        } //180913 change


        return f;
    }

    public double get_object_func(String name, double[] wei, ArrayList<Pair<Integer, Integer>> ml_pair, ArrayList<Pair<Integer, Integer>> cl_pair) {
        double result = 0;
        for (Pair<Integer, Integer> pair : cl_pair) {
            Double[] origin_sim = get_origin_sim(name, pair.getKey(), pair.getValue());
            double d = Math.sqrt(wei[0] * Math.pow(origin_sim[0], 2) + wei[1] * Math.pow(origin_sim[1], 2) + wei[2] * Math.pow(origin_sim[2], 2));
            result += d;
        }
        double sum_d = 0;
        for (Pair<Integer, Integer> pair : ml_pair) {
            Double[] origin_sim = get_origin_sim(name, pair.getKey(), pair.getValue());
            double d = Math.sqrt(wei[0] * Math.pow(origin_sim[0], 2) + wei[1] * Math.pow(origin_sim[1], 2) + wei[2] * Math.pow(origin_sim[2], 2));
            sum_d += d;
        }
        if (sum_d != 0){
            result -= Math.log(sum_d);
        }  //180915

        return result;
    }

    public double[] nips_diagonal(String name, double[] wei, HashSet<Pair<Integer, Integer>> ml_pair, HashSet<Pair<Integer, Integer>> cl_pair){
        double[] temp_weight = wei.clone();
        RealMatrix old_weight_m = new Array2DRowRealMatrix(weight);
        if (ml_pair.size() == 0 && cl_pair.size() == 0){
            return temp_weight;
        }

        //BFPS linesearch
        double[][] I = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        double[] O = {0, 0, 0};
        RealMatrix D = new Array2DRowRealMatrix(I);
        for (int i = 0; i < 256 ; i++) {
            double[] g = get_deriv(name, temp_weight, ml_pair, cl_pair);
            RealMatrix d_m = new Array2DRowRealMatrix(O).subtract(D.multiply(new Array2DRowRealMatrix(g)));
            double[] d = d_m.getColumn(0);
            int step_length = 1;

//            double min_objf = get_object_func(name, temp_weight, ml_pair, cl_pair);
//            for (int j = 1; j < 10; j++) {
//                double[] new_weight = new double[3];
//                new_weight[0] = temp_weight[0] + j * d[0];
//                new_weight[1] = temp_weight[1] + j * d[1];
//                new_weight[2] = temp_weight[2] + j * d[2];
//                if (new_weight[0] >= 0 && new_weight[1] >= 0 && new_weight[2] >= 0) {
//                    new_weight = get_new_weight_normalize(new_weight);
//                    double new_objf = get_object_func(name, new_weight, ml_pair, cl_pair);
//                    if (new_objf < min_objf) {
//                        step_length = j;
//                        min_objf = new_objf;
//                    }
//                }
//                else{
//                    break;
//                }
//            }    //line search

            double[] new_weight = new double[3];
            double[] s = {step_length * d[0], step_length * d[1], step_length * d[2]};
            RealMatrix s_m = new Array2DRowRealMatrix(s);
            new_weight[0] = temp_weight[0] + s[0];
            new_weight[1] = temp_weight[1] + s[1];
            new_weight[2] = temp_weight[2] + s[2];
            new_weight = get_new_weight_normalize(new_weight);
            if (new_weight[0] < 0 || new_weight[1] < 0 || new_weight[2] < 0){
                new_weight = temp_weight;
            }
            System.out.printf("%f\t%f\t%f\n", new_weight[0], new_weight[1], new_weight[2]);
            if (Math.abs(temp_weight[0] - new_weight[0]) + Math.abs(temp_weight[0] - new_weight[0]) + Math.abs(temp_weight[0] - new_weight[0]) < learning_converge_thres){
                return new_weight;
            }
            temp_weight = new_weight;


            double[] new_g = get_deriv(name, temp_weight, ml_pair, cl_pair);
            RealMatrix yk = new Array2DRowRealMatrix(new_g).subtract(new Array2DRowRealMatrix(g));
            double demo = yk.transpose().multiply(new Array2DRowRealMatrix(s)).getEntry(0, 0);
            RealMatrix lf_m = new Array2DRowRealMatrix(I).subtract(s_m.multiply(yk.transpose()).scalarMultiply(1 / demo)); // 180926 change 1/demo
            RealMatrix rg_m = new Array2DRowRealMatrix(I).subtract(yk.multiply(s_m.transpose()).scalarMultiply(1 / demo)); // 180926 change

            D = lf_m.multiply(D).multiply(rg_m).add(s_m.multiply(s_m.transpose()).scalarMultiply(1 / demo)); // 180926 change
        }

        return temp_weight;
    }

    public double[] link_change_weight(String name, double[] wei, ArrayList<int[]> clu) throws Exception{
        double[] result = new double[3];
        double[] ml_change = new double[3], cl_change = new double[3];
        HashMap<Integer, String> clu_label = new HashMap<>();
        HashMap<Integer, List<Integer>> inclu_same_label = new HashMap<>();

        clear_path();
        int count = 1;
        StringBuilder strpath=new StringBuilder();
        StringBuilder strname=new StringBuilder();
        StringBuilder strturename=new StringBuilder();
        for (int j = 0; j < clu.size(); j++) {
            StringBuilder clu_str = new StringBuilder();
            for (int index : clu.get(j)){
                String text = NamesContent.get(name).get(index)[1];
                clu_str.append(text + " ");
            }
            strpath.append(count + "\tpath\n");
            strname.append(count + "\t" + encode_name(name) + "\n");
            strturename.append(count + "\tNIL\n");
            FileWriter fw = new FileWriter(String.format(".\\newIMDB\\TextWebpages\\%d", count));
            fw.write(clu_str.toString());
            fw.close();
            count++;
        }

        FileWriter fileWriter1 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToPath");
        fileWriter1.write(strpath.toString());
        fileWriter1.close();

        FileWriter fileWriter2 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToMentionName");
        fileWriter2.write(strname.toString());
        fileWriter2.close();

        FileWriter fileWriter3 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToGoldenEntityName");
        fileWriter3.write(strturename.toString());
        fileWriter3.close();

        System.out.println("Saved Single text. Start Linking......");
        Decompose2 decompose2 = new Decompose2();
        decompose2.geneCandidates();
        decompose2.decomposeTxt();

//        GeneralModel generalModel = new GeneralModel();
//        generalModel.constructGeneralModel();

        LinkingViaEMNoContext linkingViaEMNoContext = new LinkingViaEMNoContext();
        linkingViaEMNoContext.for_ex_linking();

        System.out.println("Linking Over. Congratulations");

        HashMap<Integer, String> link_result = read_link_result();
        int index = 1;
        for (int i = 0; i < clu.size(); i++) {
            List<Integer> same_label_text = new LinkedList<>();
            String c_label = link_result.get(i + 1);
            clu_label.put(i, c_label);
            for (int j = 0; j < clu.get(i).length; j++) {
                if (NamesContentLab.get(name).get(clu.get(i)[j]).equals(c_label)){
                    same_label_text.add(clu.get(i)[j]); //存文本原始编号
                }
                else{
                    String true_label = NamesContent.get(name).get(clu.get(i)[j])[0];
                    if (!true_label.equals("NIL")){
                        same_label_text.clear();
                        break;
                    }
                }
            }
            inclu_same_label.put(i, same_label_text);
        }

        HashSet<Pair<Integer, Integer>> ml_pair = new HashSet<>();
        HashSet<Pair<Integer, Integer>> cl_pair = new HashSet<>();
        for (int i = 0; i < clu.size(); i++) {
            for (int t1 = 0; t1 < inclu_same_label.get(i).size(); t1++) {
                for (int t2 = t1 + 1; t2 < inclu_same_label.get(i).size(); t2++) {
                    ml_pair.add(new Pair<>(inclu_same_label.get(i).get(t1), inclu_same_label.get(i).get(t2)));
                }
            }
//            for (int j = 0; j < inclu_same_label.get(i).size(); j++) {
//                if (j != inclu_same_label.get(i).size() - 1){
//                    ml_pair.add(new Pair<>(inclu_same_label.get(i).get(j), inclu_same_label.get(i).get(j + 1)));
//                }
//            }  //ml 传递生成 减少计算量
            for (int j = i + 1; j < clu.size(); j++) {
                if (!clu_label.get(i).equals(clu_label.get(j))){
                    for (Integer t1 : inclu_same_label.get(i)){
                        for (Integer t2 : inclu_same_label.get(j)){
                            cl_pair.add(new Pair<>(t1, t2));
                        }
                    }
                }
            }
        }
        System.out.printf("must: %d\tcannot: %d\n", ml_pair.size(), cl_pair.size());


        result = nips_diagonal(name, wei, ml_pair, cl_pair);

        return result;
    }

    public ConstraintClass link_change_weight(String name, ConstraintClass cc, ArrayList<int[]> clu) throws Exception{
        HashMap<Integer, String> clu_label = new HashMap<>();
        HashMap<Integer, List<Integer>> inclu_same_label = new HashMap<>();

        clear_path();
        int count = 1;
        StringBuilder strpath=new StringBuilder();
        StringBuilder strname=new StringBuilder();
        StringBuilder strturename=new StringBuilder();
        for (int j = 0; j < clu.size(); j++) {
            StringBuilder clu_str = new StringBuilder();
            for (int index : clu.get(j)){
                String text = NamesContent.get(name).get(index)[1];
                clu_str.append(text + " ");
            }
            strpath.append(count + "\tpath\n");
            strname.append(count + "\t" + encode_name(name) + "\n");
            strturename.append(count + "\tNIL\n");
            FileWriter fw = new FileWriter(String.format(".\\newIMDB\\TextWebpages\\%d", count));
            fw.write(clu_str.toString());
            fw.close();
            count++;
        }

        FileWriter fileWriter1 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToPath");
        fileWriter1.write(strpath.toString());
        fileWriter1.close();

        FileWriter fileWriter2 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToMentionName");
        fileWriter2.write(strname.toString());
        fileWriter2.close();

        FileWriter fileWriter3 = new FileWriter(".\\newIMDB\\TextWebpages\\fileNameToGoldenEntityName");
        fileWriter3.write(strturename.toString());
        fileWriter3.close();

        System.out.println("Saved Single text. Start Linking......");
        Decompose2 decompose2 = new Decompose2();
        decompose2.geneCandidates();
        decompose2.decomposeTxt();

        LinkingViaEMNoContext linkingViaEMNoContext = new LinkingViaEMNoContext();
        linkingViaEMNoContext.for_ex_linking();

        System.out.println("Linking Over. Congratulations");

        HashMap<Integer, String> link_result = read_link_result();
        int index = 1;
        for (int i = 0; i < clu.size(); i++) {
            List<Integer> same_label_text = new LinkedList<>();
            String c_label = link_result.get(i + 1);
            clu_label.put(i, c_label);
            for (int j = 0; j < clu.get(i).length; j++) {
                if (NamesContentLab.get(name).get(clu.get(i)[j]).equals(c_label)){
                    same_label_text.add(clu.get(i)[j]); //存文本原始编号
                }
                else{
                    String true_label = NamesContent.get(name).get(clu.get(i)[j])[0];
                    if (!true_label.equals("NIL")){
                        same_label_text.clear();
                        break;
                    }
                }
            }
            inclu_same_label.put(i, same_label_text);
        }

        HashSet<Pair<Integer, Integer>> ml_pair = cc.ml_pair;
        HashSet<Pair<Integer, Integer>> cl_pair = cc.cl_pair;
        int same_ml = 0, same_cl = 0;
        for (int i = 0; i < clu.size(); i++) {
            for (int t1 = 0; t1 < inclu_same_label.get(i).size(); t1++) {
                for (int t2 = t1 + 1; t2 < inclu_same_label.get(i).size(); t2++) {
                    Pair<Integer, Integer> p = new Pair<>(Math.min(inclu_same_label.get(i).get(t1), inclu_same_label.get(i).get(t2)),
                            Math.max(inclu_same_label.get(i).get(t1), inclu_same_label.get(i).get(t2)));
                    if (ml_pair.contains(p)){
                        same_ml++;
                    }
                    else{
                        ml_pair.add(p);
                    }

                }
            }

            for (int j = i + 1; j < clu.size(); j++) {
                if (!clu_label.get(i).equals(clu_label.get(j))){
                    for (Integer t1 : inclu_same_label.get(i)){
                        for (Integer t2 : inclu_same_label.get(j)){
                            Pair<Integer, Integer> p = new Pair<>(Math.min(t1, t2), Math.max(t1, t2));
                            if (cl_pair.contains(p)){
                                same_cl++;
                            }
                            else{
                                cl_pair.add(p);
                            }
                        }
                    }
                }
            }
        }
        System.out.printf("same_must: %d\tsame_cannot: %d\nmust: %d\tcannot: %d\n", same_ml, same_cl, ml_pair.size(), cl_pair.size());


        double[] result = nips_diagonal(name, cc.wei, ml_pair, cl_pair);
        cc.wei = result;

        return cc;
    }

    public HashMap<Integer, String> read_link_result() throws Exception{
        HashMap<Integer, String> result = new HashMap<>();
        String path = ".\\data\\LinkHINData\\predictresult";
        InputStreamReader in = new InputStreamReader(new FileInputStream(path), "utf-8");
        BufferedReader br = new BufferedReader(in);
        String s;
        while ((s = br.readLine()) != null){
            s = s.trim();
            if (s.equals("\n")){
                break;
            }
            String[] split = s.split("\t");
            result.put(Integer.parseInt(split[0]), decode_name(split[1]));
        }
        return result;
    }

    public void LearnWeight(int max_iter_time) throws Exception{
        int corr = 0, total = 0;

        for (String name : Names){
            System.out.printf("Learn Weight... name = %s\n", name);
            double[] temp_weight = weight.clone();
            boolean done = false;

            ConstraintClass cc = new ConstraintClass(weight.clone(), new HashSet<>(), new HashSet<>());

            for (int i = 0; i < max_iter_time; i++) {
                fresh_temp_sim(name, temp_weight);
                ArrayList<int[]> clu = clustering(name, stop_propor);
//                double[] change_w = link_change_weight(name, temp_weight, clu);

                cc = link_change_weight(name, cc, clu);  //180925 try
                double[] change_w = cc.wei;

                double changes = Math.abs(temp_weight[0] - change_w[0]) + Math.abs(temp_weight[1] - change_w[1]) + Math.abs(temp_weight[2] - change_w[2]);
                temp_weight = change_w;
                if (changes <= learning_converge_thres){
                    System.out.printf("Stop Here!!!  At %d times Learning... W_change = %f < %f\n\n", i + 1, changes, learning_converge_thres);
                    Pair<Integer, Integer> p = FinalLink(name, temp_weight);
                    corr += p.getKey();
                    total += p.getValue();
                    done = true;
                    break;
                }
            }
            if (!done){
                System.out.printf("Reach Max Iter Times = %d\n", max_iter_time);
                Pair<Integer, Integer> p = FinalLink(name, temp_weight);
                corr += p.getKey();
                total += p.getValue();
                System.out.println();
            }
        }
        System.out.printf("\nCorrect num: %d\nTotal num: %d\nAcc: %f\n", corr, total, (double)corr/total);

    }

    public Pair<Integer, Integer> FinalLink(String name, double[] wei) throws Exception{
        System.out.printf("F I N A L ~ L I N K for name = %s\n", name);
        int count = 1;
        String path = ".\\newIMDB\\TextWebpages";

        StringBuilder strpath=new StringBuilder();
        StringBuilder strname=new StringBuilder();
        StringBuilder strturename=new StringBuilder();
        fresh_temp_sim(name, wei);
        clear_path();


        ArrayList<int[]> clu = clustering(name, stop_propor);
//        for (int i = 0; i < NamesContent.get(name).size(); i++) {
//            if (!NamesContent.get(name).get(i)[0].equals("NIL")){
//                boolean done = false;
//                strpath.append(count + "\tpath\n");
//                strname.append(count + "\t" + encode_name(name) + "\n");
//                strturename.append(count + "\t" + encode_name(NamesContent.get(name).get(i)[0]) + "\n");
//                for (int[] c : clu){
//                    for (int cin : c){
//                        if (done){
//                            break;
//                        }
//                        if (cin == i){
//                            StringBuilder str = new StringBuilder();
//                            for (int index : c){
//                                str.append(NamesContent.get(name).get(index)[1] + " ");
//                            }
//                            FileWriter fw = new FileWriter(String.format("%s\\%d", path, count));
//                            fw.write(str.toString());
//                            fw.close();
//                            count++;
//                            done = true;
//                            break;
//                        }
//                    }
//                }
//                if (!done){
//                    FileWriter fw = new FileWriter(String.format("%s\\%d", path, count));
//                    fw.write(NamesContent.get(name).get(i)[1]);
//                    fw.close();
//                    count++;
//                }
//            }
//        }

        //优化final link计算量 以及 正确率计算
        ArrayList<ArrayList<Integer>> test_inclu_data = new ArrayList<>();
        ArrayList<Integer> all_test_inclu_data = new ArrayList<>();
        for (int[] c : clu){
            StringBuilder clu_text = new StringBuilder();
            ArrayList<Integer> test_this = new ArrayList<>();
            for (int i = 0; i < c.length; i++) {
                clu_text.append(NamesContent.get(name).get(c[i])[1] + " ");
                if (!NamesContent.get(name).get(c[i])[0].equals("NIL")){
                    test_this.add(c[i]);
                    all_test_inclu_data.add(c[i]);
                }
            }
            test_inclu_data.add(test_this);
            strpath.append(count + "\tpath\n");
            strname.append(count + "\t" + encode_name(name) + "\n");
            strturename.append(count + "\t" + "NIL" + "\n");
            FileWriter fw = new FileWriter(String.format("%s\\%d", path, count));
            fw.write(clu_text.toString());
            fw.close();
            count++;
        }

        FileWriter fileWriter1 = new FileWriter(String.format("%s\\fileNameToPath", path));
        fileWriter1.write(strpath.toString());
        fileWriter1.close();

        FileWriter fileWriter2 = new FileWriter(String.format("%s\\fileNameToMentionName", path));
        fileWriter2.write(strname.toString());
        fileWriter2.close();

        FileWriter fileWriter3 = new FileWriter(String.format("%s\\fileNameToGoldenEntityName", path));
        fileWriter3.write(strturename.toString());
        fileWriter3.close();

//        clear_path();
//
//        DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(path));
//        for (Path p : paths){
//            Files.copy(p, Paths.get(String.format("%s\\%s", true_path, p.getFileName())));
//        }

        System.out.println("Saved Single. Start Linking......");
        Decompose2 decompose2 = new Decompose2();
        decompose2.geneCandidates();
        decompose2.decomposeTxt();

//        GeneralModel generalModel = new GeneralModel();
//        generalModel.constructGeneralModel();

        LinkingViaEMNoContext linkingViaEMNoContext = new LinkingViaEMNoContext();
        linkingViaEMNoContext.for_ex_linking();
        System.out.println("Linking Over. Congratulations!");

//        HashMap<Integer, String> link_result = read_link_result();

//        return linkingViaEMNoContext.calculAcc();
        int corr = 0, total = 0;
        for (int i = 0; i < clu.size(); i++) {
            String predict = decode_name(linkingViaEMNoContext.fileNameToPrediEntityName.get(String.valueOf(i + 1)));
            for (Integer order: test_inclu_data.get(i)){
                total++;
                if (NamesContent.get(name).get(order)[0].equals(predict)) {
                    corr++;
                }
            }
        }
        int single = 0;
        for (int i = 0; i < NamesContent.get(name).size(); i++) {
            if (!NamesContent.get(name).get(i)[0].equals("NIL") && !all_test_inclu_data.contains(i)){
                total++;
                single++;
                String label = NamesContentLab.get(name).get(i);
                if (label.equals(NamesContent.get(name).get(i)[0])){
                    corr++;
                }
            }
        }
        System.out.printf("single at all: %d\n", single);
        System.out.printf("accu: %f\ncorrect: %d\ntotal: %d\n\n", (double) corr / total, corr, total);
        return new Pair<Integer, Integer>(corr, total);
    }

    public void SteadyWeight(double[] wei) throws Exception{
        System.out.printf("Steady Weight = %f\t%f\t%f\n", wei[0], wei[1], wei[2]);
        int corr = 0, total = 0;

        for (String name : Names){
            System.out.printf("Processing... name = %s\n", name);
            Pair<Integer, Integer> p = FinalLink(name, wei);
            corr += p.getKey();
            total += p.getValue();
        }
        System.out.printf("\nCorrect num: %d\nTotal num: %d\nAcc: %f\n", corr, total, (double)corr/total);
    }

    public static void main(String[] args) throws Exception{
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long start_time = System.currentTimeMillis();
        System.out.println(df.format(start_time));

        LinkingViaExtendContent LVEC = new LinkingViaExtendContent(3000, 0.9);
//        System.out.println(LVEC.Names.size());
        LVEC.LearnWeight(10);
//        LVEC.FinalLink();
//        LVEC.clear_path();

//        LVEC.ma_test();
//        LVEC.SteadyWeight(LVEC.weight);

        long end_time = System.currentTimeMillis();
        System.out.println(df.format(end_time));
        System.out.printf("Running Time:%f min\n", (float) (end_time - start_time) / 1000 /60);
    }

    public void ma_test(){
        double[] a = {0.94, 0.008, 0.052};
        double[] b = {0.96, 0.008, 0.032};
        fresh_temp_sim("Michael Shannon", a);
        ArrayList<int[]> clu =clustering("Michael Shannon", 0.7);
//        double[] w = link_change_weight("Michael Shannon", a, clu);
        System.out.println();
        fresh_temp_sim("Michael Shannon", b);
        clu =clustering("Michael Shannon", 0.7);
        System.out.println();
    }
}
