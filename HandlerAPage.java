import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class HandlerAPage {

   private UtilitesOfHttp utilitesOfHttp;
   public UtilitesOfHttp getUtilitesOfHttp(){
       return utilitesOfHttp;
   }

   public HandlerAPage(){
       HandlerAPage handlerAPage = new HandlerAPage();
   }

   public Set <String> getSitemapsRoot(String url) throws IOException{
       Set <String> sitemap = new HashSet<>();
       StringBuilder page = utilitesOfHttp.getAPage(url);
       String [] strings = page.toString().split("/n");
       for (String elem : strings){
           if (elem.toLowerCase().trim().startsWith("sitemap")){
               sitemap.add(elem.substring(elem.indexOf("http")).trim());
           }
       }
       return sitemap;
   }

   private Map <String, Date> getUrl(StringBuilder page) throws ParseException{
       Map <String, Date> urlsMap = new HashMap<>();
       Document document = Jsoup.parse(page.toString());
       Elements urlsElements;
       Elements setUrlElements = document.getElementsByTag("urlset");
       if (!setUrlElements.isEmpty()){
           for (Element urlsElement : setUrlElements){
               urlsElements = urlsElement.getElementsByTag("url");
               for (Element sitemap : urlsElements){
                   String date = "";
                   String loc = sitemap.getElementsByTag("loc").first().text();
                   Elements lastmod = sitemap.getElementsByTag("lastmod");
                   if (!lastmod.isEmpty()) date = sitemap.getElementsByTag("lastmod").first().text();
                   urlsMap.put(loc, DateAndTime.transformStringToDate(date));
               }
           }
       }
       return urlsMap;
   }

   private Map <String, Date> getSitemap(StringBuilder page) throws ParseException{
       Map <String, Date> sitemapsMap = new HashMap<>();
       Document document = Jsoup.parse(page.toString());
       Elements sitemapsElements;
       Elements sitemapsIndexElements = document.getElementsByTag("sitemapIndex");
       if (!sitemapsIndexElements.isEmpty()){
           for (Element indexOfSitemap : sitemapsIndexElements){
               sitemapsElements = indexOfSitemap.getElementsByTag("sitemap");
               for (Element sitemap : sitemapsElements){
                   String date = "";
                   String loc = sitemap.getElementsByTag("loc").first().text();
                   Elements lastmod = sitemap.getElementsByTag("lastmod");
                   if (!lastmod.isEmpty()) date = sitemap.getElementsByTag("lastmod").first().text();
                   sitemapsMap.put(loc, DateAndTime.transformStringToDate(date));
               }
           }
       }
       return sitemapsMap;
   }

   public Map <String, Date> wrap(String url) throws IOException, ParseException{
       Map <String, Date> mapOfUrls = new HashMap<>();
       StringBuilder page = utilitesOfHttp.getAPage(url);
       if (page.length() > 0){
           mapOfUrls = getSitemap(page);
           if (mapOfUrls.isEmpty()) mapOfUrls = getUrl(page);
       }
       return mapOfUrls;
   }
}
