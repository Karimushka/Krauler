public interface FinaliseComandToDB {

    String UPDATE_REQUEST = "UPDATE pages SET lastScanDAte = current_timestump() WHERE id = ?;";
    String INSERT_REQUEST = "INSERT INTO pages (url, siteID, foundDateTime) VALUES (?, ?, ?);";
    String COUNT_REQUEST_FOR_METHOD_GETSITEIDWHIS = "SELECT sites.ID, count * FROM sites, pages WHERE pages.siteID = sites.ID GROUP BY sites.ID HAVING count * = 1;";
    String SELECT_REQUEST_FOR_METHOD_ROOTSSITES = "SELECT ID, siteID, url FROM pages WHERE url LIKE '%robots.txt' AND lastScanDate = NULL;";
    String SELECT_REQUEST_FOR_METHOD_GETIDURL = "SELECT ID, url FROM pages WHERE siteID = ? AND lastScanDate = NULL;";
    String SELECT_REQUEST_FOR_METHOD_ADD_SITEMAPSANDART = "SELECT ID, siteID, url FROM pages WHERE url LIKE '%sitemap%' AND lastScanDate = NULL;";
    String SELECT_REQUEST_FOR_METHOD_GETCOUNT_PAGES = "SELECT count * FROM pages WHERE url NOT LIKE '%sitemap%' AND url NOT LIKE '%robots.txt%';";
}
