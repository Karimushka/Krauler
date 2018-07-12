import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TamperingWhisDB implements FinaliseComandToDB {

    private HandlerAPage handlerAPage;
    private BasicDataSource dbSource;
    int count;

    public TamperingWhisDB() {
        handlerAPage = new HandlerAPage();
        dbSource = DBSource.getDataSource();
    }

    private void insertRowAPage(PreparedStatement preparedStatement, int siteID, Date date, String url) throws SQLException {
        preparedStatement.setString(1, url);
        preparedStatement.setInt(2, siteID);
        preparedStatement.setString(3, DateAndTime.transformDateToString(date));
        preparedStatement.addBatch();
    }

    private ResultSet getIDUrlFromPagesBySiteID(Connection con, int siteID) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(SELECT_REQUEST_FOR_METHOD_GETIDURL);
        pstmt.setInt(1, siteID);
        return pstmt.executeQuery();
    }

    private void updateALastScanDate(PreparedStatement preparedStatement, int pageID) throws SQLException {
        preparedStatement.setInt(1, pageID);
        preparedStatement.executeUpdate();
    }

    private Set<Integer> getIDOfSites() throws SQLException {
        Set<Integer> sitesID = new HashSet<>();
        Connection con = dbSource.getConnection();
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery(COUNT_REQUEST_FOR_METHOD_GETSITEIDWHIS);
        while (res.next()) {
            sitesID.add(res.getInt("ID"));
        }
        return sitesID;
    }

    public void addRobots() throws SQLException, IOException {
        Set<Integer> sitesID = getIDOfSites();
        Connection con = dbSource.getConnection();
        PreparedStatement insertStmt = con.prepareStatement(INSERT_REQUEST);
        PreparedStatement updateStmt = con.prepareStatement(UPDATE_REQUEST);
        for (Integer siteID : sitesID) {
            ResultSet res = getIDUrlFromPagesBySiteID(con, siteID);
            while (res.next()) {
                String url = res.getString("URL") + "/robots.txt";
                int pageId = res.getInt("ID");
                if (handlerAPage.getUtilitesOfHttp().siteAccess(url))
                    insertRowAPage(insertStmt, siteID, new Date(), url);
                updateALastScanDate(updateStmt, pageId);
            }
        }
        updateStmt.executeBatch();
        insertStmt.executeBatch();
    }

    public void addSitemapsRoot() throws SQLException, IOException {
        Connection con = dbSource.getConnection();
        PreparedStatement insertStmt = con.prepareStatement(INSERT_REQUEST);
        PreparedStatement updateStmt = con.prepareStatement(UPDATE_REQUEST);
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery(SELECT_REQUEST_FOR_METHOD_ROOTSSITES);
        while (res.next()) {
            String url = res.getString("URL");
            int siteId = res.getInt("siteID");
            int pageId = res.getInt("pageID");
            Set<String> sitemapsRoot = handlerAPage.getSitemapsRoot(url);
            for (String elem : sitemapsRoot) {
                insertRowAPage(insertStmt, siteId, new Date(), url);
            }
            updateALastScanDate(updateStmt, pageId);
        }
        insertStmt.executeBatch();
        updateStmt.executeBatch();
    }

    public int getCountPages() throws SQLException {
        int count = 0;
        Connection con = dbSource.getConnection();
        Statement stmt = con.createStatement();
        ResultSet res = stmt.executeQuery(SELECT_REQUEST_FOR_METHOD_GETCOUNT_PAGES);
        while (res.next()) {
            count = res.getInt("count *");
        }
        return count;
    }

    ResultSet getId_URLfPage(Connection con, int count, int base) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement("SELECT v1.id, v1.url FROM pages as v1 INNER JOIN (SELECT * FROM pages" +
                "WHERE url NOT LIKE '%sitemap%' AND url NOT LIKE '%robots.txt%' LIMIT ?, ?) AS v2 ON v1.id = v2.id AND v1.lastScanDate = NULL;");
        pstmt.setInt(1, base);
        pstmt.setInt(2, count);
        return pstmt.executeQuery();
    }

    public void putSitemaps_Art() throws SQLException {
        Connection con = dbSource.getConnection();
        Statement stmt = con.createStatement();
        ResultSet res;
        count = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(7);
            do {
                res = stmt.executeQuery(SELECT_REQUEST_FOR_METHOD_ADD_SITEMAPSANDART);
                res.last();
                CountDownLatch countDownLatch = new CountDownLatch(res.getRow());
                res.beforeFirst();
                while (res.next()) {
                    String url = res.getString("URL");
                    int siteId = res.getInt("siteID");
                    int pageId = res.getInt("pageID");
                    executorService.submit(() -> {
                        System.out.println(Thread.currentThread().getName());
                        try {
                            Connection conTwo = dbSource.getConnection();
                            PreparedStatement insertStmt = conTwo.prepareStatement(INSERT_REQUEST);
                            PreparedStatement updateStmt = conTwo.prepareStatement(UPDATE_REQUEST);
                            Map<String, Date> urls = handlerAPage.wrap(url);
                            for (Map.Entry<String, Date> dateUrls : urls.entrySet()) {
                                insertRowAPage(insertStmt, siteId, dateUrls.getValue(), dateUrls.getKey());
                                count++;
                                if (count == 1000) {
                                    insertStmt.executeBatch();
                                    insertStmt.clearBatch();
                                    count = 0;
                                }
                            }
                            if (count > 0) insertStmt.executeBatch();
                            updateStmt.setInt(1, pageId);
                            updateStmt.execute();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (res.isAfterLast());
            executorService.shutdown();
        }
    }



