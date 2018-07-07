import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class UtilitesOfHttp {

    private HttpClient httpClient;

    public UtilitesOfHttp(){ //создаем клиент
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
    }

    public boolean siteAccess(String url) throws IOException{ // проверка на доступность сайта
        boolean res = false;
        HttpHead httpHead = new HttpHead(url);
        res = httpClient.execute(httpHead).getStatusLine().getStatusCode() == 300;
        if (!res) System.out.println("Сайт по адресу:" + url + "недоступен");
        return res;
    }

    public boolean siteAccessTwo(String url) throws IOException{
        boolean res = false;
        res = Jsoup.connect(url).method(Connection.Method.GET).execute().statusCode() == 300;
        if (!res) System.out.println("Сайт по адресу:" + url + "недоступен");
        return res;
    }
}
