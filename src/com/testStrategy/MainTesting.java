package com.testStrategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by mishka on 22.06.15.
 */
public class MainTesting {
    public static void main(String[] args) throws IOException{
        BufferedReader inp =  new BufferedReader(new FileReader("Sun_Jun_21_14_36_05_MSK_2015/score21.txt"));
        ArrayList<String> scores = new ArrayList<String>();
        while(inp.ready())
        {
            scores.add(inp.readLine());
        }
        inp.close();
        String[] line;
        ArrayList<String> gamesScores = new ArrayList<String>();
        for (int i = 0; i < scores.size(); i++) {
            line = scores.get(i).split(" ");
            if(line.length == 5)
                gamesScores.add(line[1]);
            if(line.length == 7)
                gamesScores.add(line[3]);
            if (line.length == 8)
                gamesScores.add(line[4]);
        }
        int score400 = 0;
        int score4015 = 0;
        int score4030 = 0;
        int scoreA40 = 0;
        for (int i = 3; i < gamesScores.size(); i++) {
            if (gamesScores.get(i).equals("(0:0)")){
                if(gamesScores.get(i - 1).equals("(40:0)") || gamesScores.get(i - 1).equals("(0:40)"))
                    score400++;
                else if (gamesScores.get(i - 1).equals("(40:15)") || gamesScores.get(i - 1).equals("(15:40)"))
                    score4015++;
                else if (gamesScores.get(i - 1).equals("(40:30)") || gamesScores.get(i - 1).equals("(30:40)"))
                    score4030++;
                else if (gamesScores.get(i - 1).equals("(40:A)") || gamesScores.get(i - 1).equals("(A:40)"))
                    scoreA40++;
            }
        }
        System.out.println(score400 + "(40:0)");
        System.out.println(score4015 + "(40:15)");
        System.out.println(score4030 + "(40:30)");
        System.out.println(scoreA40 + "(A:40)");
//        for(String s: gamesScores){
//            System.out.println(s);
//        }

    }
}
