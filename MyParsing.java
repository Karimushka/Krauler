import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class MyParsing { // Систему и класс парсинга я нагло спер у коллеги. Да Простит он меня за это.

    private List<String> excludeWords = new ArrayList<>(Arrays.asList("или", "но", "что", "чтобы", "от", "ну", "ни",
            "ко", "со", "во", "на", "из", "за", "по", "не", "для", "под", "около", "если", "да", "нет", "перед",
            "через", "сквозь", "при", "над", "до", "об", "обо", "после", "же", "так", "также", "либо", "ещё",
            "еще", "ый", "ой", "ая", "ые", "ую", "уже", "пока", "как")); //Можно использовать Set, разница несущественна в данном случае
    private static Pattern pattern = Pattern.compile("[^а-я^А-Я^ё^Ё^a-z^A-Z]+$");// вводим паттерн для сравнения строк, входящих в заданный промежуток

    private boolean isIncludedWord(String word){
        return !(excludeWords.contains(word) || word.length() == 1); // возвращаем значения, не входящие в лист excludeWords и с длинной более 1 символа
    }

    public Map <String,Integer> countOfWords(String url) throws IOException{
        final Map <String, Integer> mapOfWords = new HashMap<>();
        Document document = Jsoup.connect(url).get();
        String art;
        art = document.body().getElementsByTag("p").text();
        art = (document.title() + " " + art).toLowerCase();
        List<String> list = Arrays.asList(pattern.split(art));
        list.stream() //открываем поток
                .filter(this::isIncludedWord) // функциональная фильтрация c передачей ссылки на булеановский метод isIncludeWord, прописанный выше
                .forEach(word -> mapOfWords.merge(word, 1, (value, newValue) -> value + newValue));
        // в целом, это обычный перебор всех элементов коллекции с использованием лямбда выражения в качестве операции по заполнению мапы с генерацией ключа
        return mapOfWords;
    }
}
