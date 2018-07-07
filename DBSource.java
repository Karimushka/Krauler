import org.apache.commons.dbcp2.BasicDataSource;

public class DBSource implements SourceOfDB {

    private static BasicDataSource dataSource;

    public static BasicDataSource getDataSource(){
        if (dataSource == null){
            dataSource = new BasicDataSource();
            dataSource.setUrl(dbURL);
            dataSource.setUsername(USER);
            dataSource.setPassword(PASSWORD);
        }
        return dataSource;
    }
}
