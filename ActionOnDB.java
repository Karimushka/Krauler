import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionOnDB implements FinaliseComandToDB {

    private TamperingWhisDB tamperingWhisDB;
    private DBSource dbSource;

    int count = 0;

    public void action (){
        int pagesCount = tamperingWhisDB.getCountPages();
        MongoClient mongoClient = new MongoClient("localhost");
        MongoDatabase db = mongoClient.getDatabase("urlwordsrate");
        MongoCollection <Document> documentMongoCollection = db.getCollection("ratingTest");
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch = new CountDownLatch(pagesCount/10000);
        MyParsing myParsing = new MyParsing();
        for (; count <= pagesCount; count += 10000){
            final int A = count;
            executorService.submit(() -> {
                System.out.println(Thread.currentThread().getName() + "\n" + Thread.activeCount());
                    try (Connection connection = dbSource.getDataSource().getConnection();
                         PreparedStatement updatePstmt = connection.prepareStatement(UPDATE_REQUEST);
                         ResultSet res = tamperingWhisDB.getId_URLfPage(connection, A, 10000)){
                        while (res.next()){
                            int IdOfPages = res.getInt("ID");
                            String url = res.getString("URL");
                            try {
                                Map <String, Integer> map = myParsing.countOfWords(url);
                                Document doc = new Document("Id", IdOfPages).append("words", new Document());
                                map.forEach((key, value) -> doc.get("words", new Document()).append(key, value));
                                try {
                                    documentMongoCollection.insertOne(doc);
                                    System.out.println("добавлены:" + IdOfPages + "\n" + url);
                                }catch (MongoException e){
                                    System.out.println("Произошла ошибка" + IdOfPages + "\n" + url);
                                }
                                tamperingWhisDB.updateALastScanDate(updatePstmt, IdOfPages);
                            }catch (IOException e){
                                System.out.println("Недоступный" + url + "\n" + IdOfPages);
                                e.printStackTrace();
                            }
                    }
                    updatePstmt.executeBatch();
                }catch (SQLException e){
                        e.printStackTrace();
                    }finally {
                        countDownLatch.countDown();
                    }
            });
        }
        try {
            countDownLatch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        executorService.shutdown();
    }
}
