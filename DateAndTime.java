import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAndTime { // Представляет собой два статических, глобальных метода (static для удобства),
// преобразующийх строку в дату и наоборот

    static String transformDateToString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    static Date transformStringToDate(String stringADate) throws ParseException{
        Date date = new Date();
        if (stringADate.length() == 10) date = new SimpleDateFormat("yyyy-MM-dd").parse(stringADate);
        if (stringADate.length() >= 19){
            String [] date_taime = stringADate.split("[T+]");
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date_taime[0] + " " + date_taime[1]);
        }
        return date;
    }
}
