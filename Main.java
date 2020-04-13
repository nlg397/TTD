package ling.ttd;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    private static String mHomePath;
    private static StringBuilder mStrResults = new StringBuilder();

    public static void main(String[] args) {

        System.out.println("=== TextTonalityDefinition ===");
        System.out.println("Определение тональности текста");
        System.out.println("Чтобы прочитать описание и инструкцию, нажмите \"y\", " +
                "чтобы пропустить - любую другую клавишу...");
        if (System.console().readLine().equals("y")) {
            System.out.println("Для использования программы вам нужны два txt-файла: словарь и текст." +
                    "\nВ словаре должны быть записаны искомые слова (каждое следующее слово на новой строке). " +
                    "Если у слова есть несколько словоформ или близкие по значению синонимы, " +
                    "их нужно записать на той же строке через запятую (без пробелов)." +
                    "\nПример:\nслово,слова,слову,словом,слове,слов,словам,словами,словах" +
                    "\nплохой,нехороший\nрядом\nлучше");
            System.out.println("В другом файле должен быть записан исследуемый текст." +
                    "\nПосле анализа текста будет создан третий txt-файл с результатами, " +
                    "также их можно будет увидеть на экране.");
        }

        if (System.getenv().get("HOME") != null) {
            mHomePath = System.getenv().get("HOME") + "/" + Constants.DEFAULT_FOLDER + "/";
        } else {
            mHomePath = System.getenv().get("USERPROFILE") + "\\" + Constants.DEFAULT_FOLDER + "\\";
        }
        System.out.println("Каталог для текстов, словаря и результатов по умолчанию: " + mHomePath +
                "\nИзменить? (\"y\" - да, любая другая клавиша - нет)");
        if (System.console().readLine().equals("y")) {
            System.out.println("Текущий каталог - оставьте строку пустой, " +
                    "папка в текущем каталоге - \"some_folder/another_folder/\" для Linux, " +
                    "\"some_folder\\another_folder\\\" для Windows. " +
                    "Произвольный путь должен заканчиваться / (Linux) или \\ (Windows).");
            mHomePath = System.console().readLine();
            if (mHomePath.equals(""))
                System.out.println("Новый каталог для текстов, словаря и результатов: текущий");
            else
                System.out.println("Новый каталог для текстов, словаря и результатов: " + mHomePath);
        }

        String vocabularyPath = getPath("со словарем", Constants.VOCABULARY_FILE_NAME);
        String textPath = getPath("с текстом", Constants.TEXT_FILE_NAME);
        mStrResults.append("Текст " + textPath + ":");
        String resultPath = getPath("с результатами", Constants.RESULT_FILE_NAME);

        System.out.println("Нажмите любую клавишу, чтобы начать...");
        System.console().readLine();

        readAndCalc(textPath, vocabularyPath);

        System.out.println("Готово! Еще один текст? (\"y\" - да, любая другая клавиша - нет)");
        String answer = System.console().readLine();
        while (answer.equals("y")) {
            textPath = getPath("с текстом", Constants.TEXT_FILE_NAME);
            mStrResults.append("\n======\nТекст " + textPath + ":");
            System.out.println("Нажмите любую клавишу, чтобы начать...");
            System.console().readLine();
            readAndCalc(textPath, vocabularyPath);
            System.out.println("Готово! Еще один текст? (\"y\" - да, любая другая клавиша - нет)");
            answer = System.console().readLine();
        }

        write(resultPath);
        System.out.println("Результаты записаны в файл " + resultPath +
                ", также вы можете видеть их ниже:");
        System.out.println(mStrResults.toString());

        System.out.println("Нажмите любую клавишу, чтобы завершить программу...");
        System.console().readLine();
    }

    private static String getPath(String fileWithWhat, String defaultFileName) {
        System.out.print("Введите имя файла " + fileWithWhat + " или путь к нему, если он находится в ");
        if (mHomePath.equals(""))
            System.out.print("текущем каталоге");
        else
            System.out.print("каталоге " + mHomePath);
        System.out.println(" (например, " + defaultFileName + " или some_folder/" + defaultFileName + "). " +
                "Если файл находится в другом месте, нажмите \"p\".");
        String path = System.console().readLine();

        if (path.equals("p")) {
            System.out.println("Введите путь к файлу " + fileWithWhat +
                    " (например, some_folder/" + defaultFileName + " или C:\\some_folder\\" + defaultFileName + "):");
            path = System.console().readLine();
            if (path.equals("")) {
                path = mHomePath + defaultFileName;
                System.out.print("Вы не ввели путь к файлу " + fileWithWhat + ". ");
            }
        } else if (path.equals("")) {
            path = mHomePath + defaultFileName;
            System.out.print("Вы не ввели путь к файлу " + fileWithWhat + ". ");
        } else {
            path = mHomePath + path;
        }
        System.out.println("Будет использован следующий путь: " + path);

        return path;
    }

    private static void readAndCalc(String textPath, String vocabularyPath) {
        try {
            String text = (new String(Files.readAllBytes(Paths.get(textPath)))).toLowerCase();
            String[] textWords = textToWords(text);
            int commonCount = 0;

            FileReader vocabularyFR = new FileReader(vocabularyPath);
            BufferedReader vocabularyReader = new BufferedReader(vocabularyFR);
            String line = vocabularyReader.readLine();
            int count;
            while (line != null) {
                String[] words = line.toLowerCase().split(",");
                count = findMatchesCount(words, textWords);
                mStrResults.append(String.format("\nСлово %s встречается %d раз(а)", words[0], count));
                commonCount += count;
                line = vocabularyReader.readLine();
            }
            vocabularyFR.close();
            vocabularyReader.close();

            mStrResults.append("\nВсего слова из словаря встречаются в тесте " +
                    commonCount + " раз(а)\nВсего слов в тексте: " + textWords.length +
                    "\nПроцент слов из словаря в тексте: " + (commonCount * 100 / (float) textWords.length) + "%");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void write(String resultPath) {
        try {
            FileWriter resultFW = new FileWriter(resultPath);
            resultFW.write(mStrResults.toString());
            resultFW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int findMatchesCount(String[] words, String[] textWords) {
        int count = 0;
        for (String word : words) {
            for (String textWord : textWords) {
                if (textWord.equals(word)) count++;
            }
        }
        return count;
    }

    private static String[] textToWords(String text) {
        String[] arr = text.split
                ("(\\s+(\\-|\\—|\\−)\\s+)|(\\s*(\\s|,|!|\\.|:|;|«|»|\"|\\?|\\/|\\\\|\\(|\\)|\\{|\\}|\\[|\\])\\s*)");
        ArrayList<String> list = new ArrayList<String>();
        for (String item : arr) {
            if (!item.equals("")&&!item.equals("—")) list.add(item);
        }
        return list.toArray(new String[list.size()]);
    }
}
