import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class UtilitesOfHttp {

    private HttpClient httpClient;

    public UtilitesOfHttp(){ //создаем клиент
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
    }

    public boolean siteAccess(String url) throws IOException{ // проверка на доступность сайта
        boolean res = false;
        HttpHead httpHead = new HttpHead(url);
        res = httpClient.execute(httpHead).getStatusLine().getStatusCode() == 200;
        if (!res) System.out.println("Сайт по адресу:" + url + "недоступен");
        return res;
    }

    /*public boolean siteAccessTwo(String url) throws IOException{ // более быстрый способ проверки на доступность через заголовок
        boolean res = false;
        res = Jsoup.connect(url).method(Connection.Method.HEAD).execute().statusCode() == 200;
        if (!res) System.out.println("Сайт по адресу:" + url + "недоступен");
        return res;
    }*/

    public StringBuilder getAPage(String url) throws IOException{ // вытаскиваем страницу
        StringBuilder page = new StringBuilder();
        if (siteAccess(url) && url.endsWith("xml.gz")){
            GZIPInputStream gzipInputStream = new GZIPInputStream(new URL(url).openStream());
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = gzipInputStream.read(buf,0,1024)) != -1){
                page.append(new String(buf),0, count);
            }
        }
        else if (siteAccess(url)){
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = bufferedInputStream.read(buf,0,1024)) != -1){
                page.append(new String(buf),0, count);
            }
        }
        return page;
    }

    public String getAText(String url) throws IOException { // вытаскиваем содержимое страницы
        String res = " ";
        if (siteAccess(url)){
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
            res = doc.body().text();
        }
        else System.out.println("Извините, сайт " + url + "недоступен");
        return res;
    }
}
