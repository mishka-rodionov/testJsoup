package com.company;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Main {
    static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException{
        long currentTimeCGS;
        logger.info("Hello, world! log4j");
        System.out.println("!!!");
        while(true){
            try{
                currentTimeCGS = System.currentTimeMillis();
                commonGameScores();
                System.out.println("time commonGameScore " + (System.currentTimeMillis() - currentTimeCGS));
            }catch (IOException e){
                System.out.println("IOexception");
            }catch (NullPointerException ne){
                System.out.println("e catch");
                try{
                    printScoreToFile();
                }catch (IOException ee){
                    System.out.println("ee catch");
                }
            }catch (IndexOutOfBoundsException ie){
                System.out.println("IndexOutOfBounds");
            }
//            if (counter % 10 == 0)
//                System.out.println("counter = " + counter);
//            if (counter == 5){
//                try{
//                    printMapToFile();
//                    pushToMongoDB();
////                    out.close();
//                    System.out.println("Finish");
//                    System.exit(0);
//                }catch (IOException e){
//                    System.out.println("FilePrintException");
//                    e.printStackTrace();
//                }
//            }
            if (new Date().toString().contains("15:31")){
                try{
                    currentTime = System.currentTimeMillis();
                    printMapToFile();
                    System.out.println("time print to file = " + (System.currentTimeMillis() - currentTime));
                    currentTime = System.currentTimeMillis();
                    pushToMongoDB();
                    System.out.println("time push to DB = " + (System.currentTimeMillis() - currentTime));
//                    logger.addAppender();
                    System.out.println("Finish");
                    System.exit(0);
                }catch (IOException e){
                    System.out.println("FilePrintException");
                    e.printStackTrace();
                }
            }
            try{
                Thread.sleep(2000);
            }catch (InterruptedException ie){
                ie.printStackTrace();
            }
            counter++;
        }
        }

    public static String gameScore() throws IOException{//не используется, это начальная тестовая версия. (рабочая)
        Document document = Jsoup.connect("https://www.betmarathon.com/su/live/22723?openedMarkets=3264009").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36").get();// write your code here
        Element mainElement = document.getElementById("block3264009type111");
        Element elementParent = mainElement.child(mainElement.children().size() - 1).child(0).child(1);//Получение таблицы с результатом в гейме и ставками
        //Получение имен игроков
        if (firstPlayerRead){
            nameOfFirstPlayer = elementParent.child(0).child(0).child(1).text();
            firstPlayerRead = false;
        }
        if (secondPlayerRead){
            nameOfSecondPlayer = elementParent.child(0).child(0).child(2).text();
            secondPlayerRead = false;
        }
//        System.out.println(nameOfFirstPlayer + "\t" + nameOfSecondPlayer);

//        System.out.println(elementParent.toString());
        Element element = elementParent.child(0).child(1).child(0).child(2);//Получение сведений о счете. Первый вызов child(0) обращается к элементу таблицы <tbody>
//        System.out.println(element.text());
        return element.text();
    }

    public static void commonGameScores() throws IOException{
        //Установление соединения с сервером в разделе "live теннис" с нужным user agent и отправка HTTP запроса GET
        Document document = Jsoup.connect("https://www.betmarathon.com/su/live/22723").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36").get();
        Elements groupEvents = document.getElementsByClass("main-block-events");        //Выбираем массив тегов с матчами по турнирам
        Attributes attributes;                                                          //Атрибуты <div class = "main-block-events" >
        String playerNames;                                                             //Имена игроков
        String score;                                                                   //Счет
        LinkedList<String> listPlayerNames;
        for(Element e: groupEvents){                                                    //Выбираем конкретный турнир
            try{
                attributes = e.attributes();
                ArrayList<Element> pN = e.getElementsByClass("live-today-member-name"); //Выбираем имена всех игроков, если турнир начался сегодня
                ArrayList<Element> tmppN = e.getElementsByClass("live-member-name");    //Выбираем имена игроков, если турнир начался не сегодня
                if (tmppN.size() != 0) pN.addAll(tmppN);                                //Определяем какие имена использовать (возможно здесь ошибка, необходимо объединить матчи начавшиеся вчера и сегодня)
                ArrayList<Element> sc = e.getElementsByClass("cl-left");                //Выбираем текущий счет во вех встречах (счет в матче, счет по геймам, счет в гейме)
                String supply;
                int j = 0;
                for (int i = 0; i < pN.size() - 1; i+=2) {                              //Проходим по всем именам игроков выбирая по 2 для каждой встречи
                    playerNames = pN.get(i).text() + " " + pN.get(i + 1).text();        //Записываем имена игроков через пробел
                    if (sc.get(j).children().size() > 0)                                //
                        supply = sc.get(j).child(0).toString();                         //Проблемная строка. Проблема с child(0). Не везде существуют child-ы. При завершении игры отсутствует счет в гейме, поэтому в этом случае child-ов нет.
                    else supply = sc.get(j).text();                                     //Запись общего счета при завершении матча
                    if(supply.contains(">(<"))                                          //Вычисление подающего
                        score = sc.get(j).text() + " " + "<-";                          //Подает первый игрок
                    else
                        if(supply.contains(">)<"))
                            score = sc.get(j).text() + " " + "->";                      //Подает второй игрок
                    else score = supply;                                                //Если матч завершен
                    currentTime = System.currentTimeMillis();
                    writeToLiveStats(playerNames, score + " " + getBetGamePrice(e, i)); //Записываем имена игроков, текущий счет и текущие коэффициенты в специальный мап
                    System.out.println("time writeToLiveStats = " + (System.currentTimeMillis() - currentTime));
                    j++;                                                                //Переходим на следующий матч турнира
                }
            }catch(IndexOutOfBoundsException ie){
                System.out.println("IndexOutOfBound");
                ie.printStackTrace();
            }
        }
    }

    public static String getBetGamePrice(Element element, int index) throws IOException{                                                                                        //Метод для получения live коэффициентов по геймам
        String[] price;                                                                                                                                                         //live коэффициенты
        String openedMarkets = "";                                                                                                                                              //Параметр в адресной строке
        openedMarkets = element.getElementsByTag("tbody").get(index + 1).attributes().get("data-event-treeid").toString();                                                      //Получение данного параметра из атрибута какого-то тега
        String request = "https://www.betmarathon.com/su/live/22723?openedMarkets=" + openedMarkets;                                                                            //Конструирование правильного адрема запроса
        Document document = Jsoup.connect(request).userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36").get();   //HTTP GET запрос на сервер
        String id = "block" + openedMarkets + "type111";                                                                                                                        //Конструирование id для выборки конкретного спортивного матча
        if(document.getElementById(id) != null)                                                                                                                                 //Если матч действительно существует (еще не завершен), то (Здесь в группе матчей появляется нулл на матче с номером > 1)
            price = document.getElementById(id).getElementsByTag("table").get(0).getElementsByClass("coeff-price").text().split(" ");                                           //выбираем коэффициенты из нужных элементов большой таблицы (Проблемная строка. Скорее всего проблема с get(0))
        else{                                                                                                                                                                   //Иначе, если матч завершен, то
            price = new String[2];                                                                                                                                              //вместо коэффициентов ставим прочеки
            price[0] = "-";
            price[1] = "-";
        }
        return price[0] + " " + price[1];                                                                                                                                       //Возвращаем 2 строки с коэф. в гейме на первого и второго игрока соответственно
    }

    public static void writeToLiveStats(String playerNames, String score) {             //Запись имеющихся данных в специальную мапу для статистики
        LinkedList<String> listPlayerNames;                                             //Связный список для имен игроков
        if (liveStats.containsKey(playerNames)){                                        //Если мап уже содержит ключ эквивалентный именам игроков, то
            if (!liveStats.get(playerNames).getLast().equals(score))                    //счет под этим ключом не совпадает с записываемым счетом, то
                liveStats.get(playerNames).add(score);                                  //добавляем счет в конец значения, находящегося под данным ключом
        }else{                                                                          //Иначе если мап не содержит ключа с текущими именами игроков
            listPlayerNames = new LinkedList<String>();                                 //???
            listPlayerNames.add(score);                                                 //???
            liveStats.put(playerNames, listPlayerNames);                                //???
        }
    }

    public static void printMapToFile() throws IOException{
        Set<String> playerNames = liveStats.keySet();                                   //Получение имен игроков для всех матчей по отдельности
        String fileName;
        Date date = new Date();
        String directoryName = date.toString().replace(' ', '_').replace(':', '_');     //Составление валидного имени директории
        System.out.println(directoryName);                                              //Отображение на консоль валидного имени директории
        File dir = new File(directoryName);                                             //Создание директории с составленным именем
        if (dir.mkdir()) System.out.println("Directory create");                        //Подтверждение при корректном созздании директории
        else System.out.println("baaaaaaaaad");                                         //Оповещение при ошибке при создании директории
        LinkedList<String> scores;                                                      //Объявление связного списка для счета отдельного матча
        int fileCounter = 1;                                                            //Счетчик файлов
        for(String s : playerNames){                                                    //Цикл для прохода по всем составленным матчам
            fileName = directoryName + "/score" + fileCounter + ".txt";                 //Создание валидного имени файла
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));        //Перенаправление потока в файл с валидным именем
            out.println(s);                                                             //Запись имен игроков в файл первой строкой
            scores = liveStats.get(s);                                                  //Получение счета по геймам для конкретного матча
            for(String s1 : scores){                                                    //Цикл для прохода по всем записям
                out.println(s1);                                                        //Печать отдельной записи с счетом в файл
            }
            out.close();                                                                //Закрытие потока для того, чтобы следующие записи происходили в другой файл
            fileCounter++;
        }
    }

    public static void pushToMongoDB(){
        Set<String> playerNames = liveStats.keySet();                                   //Создание множества имен игроков
        Iterator<String> iter = playerNames.iterator();                                 //Получение итератора по данному множеству
        MongoClient mongoClient = new MongoClient();                                    //Создание клиента к БД
        MongoDatabase mdb = mongoClient.getDatabase("storage");                         //Создание/получение БД
        MongoCollection mdbCollection = mdb.getCollection("match");                     //Создание/получение коллекции БД
        org.bson.Document doc;                                                          //Объявление документа БД
        String key;
        while(iter.hasNext()){
            key = iter.next();                                                          //
            doc = new org.bson.Document(key, liveStats.get(key));                       //Создание нового документа в коллекции БД
            mdbCollection.insertOne(doc);                                               //Запись документа в коллекцию.
        }
    }

    public static void pushToPostgres(){
        System.out.println("-------- PostgreSQL " + "JDBC Connection Testing ------------");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
            e.printStackTrace();
            return;
        }
        System.out.println("PostgreSQL JDBC Driver Registered!");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/Statistic", "postgres", "987654");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }
        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
        try{
            Statement statement = connection.createStatement();
            Set<String> playerNames = liveStats.keySet();                                   //Создание множества имен игроков
            Iterator<String> iter = playerNames.iterator();                                 //Получение итератора по данному множеству
            String key;
            while(iter.hasNext()){
                key = iter.next().replace(' ', '_');
                statement.executeUpdate("CREATE TABLE "+ key + "(id INTEGER, SetNumber INTEGER , MatchScore VARCHAR(20), SetScore VARCHAR(20)," +
                        " GameScore VARCHAR(20), Serve VARCHAR(4), Coef1 VARCHAR(10), Coef2 VARCHAR(10))");
                LinkedList<String> scores = liveStats.get(key);
                Iterator<String> linkedIter = scores.iterator();
                int id = 1;
                while(linkedIter.hasNext()){
                    String[] score = linkedIter.next().split(" ");
                    if (score.length == 5){
                        statement.executeUpdate("INSERT INTO " + key + "id, SetNumber, MatchScore, SetScore, GameScore, Serve, Coef1, Coef2 VALUES "
                                + id + ",'1'" + "'(0:0)'" +  );
                    }
                }
            }

        }catch(SQLException se){
            se.printStackTrace();
        }
    }


    public static void printScoreToFile()throws IOException{
        out = new PrintWriter(new BufferedWriter(new FileWriter("score=3264009.txt")));
        for (int i = 0; i < scores.size(); i++) {
            out.println(scores.get(i));
        }
        out.close();
    }

    public static void createScoreList(String score){
//        System.out.println(scores.size());
//        if (scores.size() == 0)
//            scores.add("(0:0)");
        if (!scores.get(scores.size() - 1).equals(score))                               //Запись только уникального счета. Повторяющиеся значения игнорируются.
        {
            scores.add(score);
            System.out.println(score);
        }
    }


    private static PrintWriter out;
    private static ArrayList<String> scores = new ArrayList<String>();
    private static String nameOfFirstPlayer;
    private static String nameOfSecondPlayer;
    private static boolean firstPlayerRead = true;
    private static boolean secondPlayerRead = true;
    private static Map<String, LinkedList<String>> liveStats = new HashMap<String, LinkedList<String>>();
    private static int counter = 0;
    private static long currentTime;

}


