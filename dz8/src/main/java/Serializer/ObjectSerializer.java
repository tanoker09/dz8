package Serializer;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjectSerializer {

    private final String CLASS_FLAG = "#class";

    private ArrayList<ObjectParams> objs = new ArrayList<>();
    private ArrayList<ObjectParams> deserializedObjs = new ArrayList<>();

    /**
     * Сохранения класса в формат json.
     * @param obj
     * @param filePath
     * @throws Exception
     */
    public void serialize(Object obj, String filePath) throws Exception {
        serializeObj(obj);
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("\"classes\":\n[\n");
        for(int i = 0; i < objs.size(); i++){
            json.append(objs.get(i).toString());
            if(i != objs.size() - 1)
                json.append(",\n");

        }

        json.append("\n]\n}");

        writeStringsToFile(filePath, json.toString());
    }

    /**
     * Сериализация объекта
     * @param obj
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private void serializeObj(Object obj) throws InstantiationException, IllegalAccessException {
        ObjectParams objectParams = new ObjectParams(obj.getClass().getName());
        objs.add(objectParams);
        Field[] fields = obj.getClass().getDeclaredFields();
        serializeFields(obj, objectParams, fields);
    }

    /**
     * Сохранение полей
     * @param obj
     * @param objectParams
     * @param fields
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void serializeFields(Object obj, ObjectParams objectParams, Field[] fields) throws IllegalAccessException, InstantiationException {
        for(int i = 0; i < fields.length; i++)
        {
            FieldJson fieldJson = new FieldJson();
            fieldJson.setName(fields[i].getName());
            Class<?> type = fields[i].getType();

            Class[] interfaces = type.getInterfaces();

            boolean isClass = false;

            if(interfaces.length != 0){
                for(int j = 0; j < interfaces.length; j++){
                    if(interfaces[j].equals(SerializeToJson.class)){
                        fields[i].setAccessible(true);
                        Object value = fields[i].get(obj);
                        serializeObj(value);
                        isClass = true;
                    }
                }

            }
            fieldJson.setType(fields[i].getType().getName());
            fieldJson.setModifier(fields[i].getModifiers());
            if(!isClass){
                fields[i].setAccessible(true);
                Object value = fields[i].get(obj);
                fieldJson.setValue(value);
            }
            else{
                fieldJson.setValue(fields[i].getType().getName() + CLASS_FLAG);
            }

            objectParams.addField(fieldJson);
        }
    }

    /**
     *Десериализация
     */

    public Object deserialize(String filePath) throws Exception {
        String fileData = readToString(filePath);
        Pattern CLASS_PATTERN = Pattern.compile("\"name\":.+,\n\"fields\":(.+\n)+\n]}");

        Matcher matcher = CLASS_PATTERN.matcher(fileData);
        while (matcher.find()) {
            String finStr = fileData.substring(matcher.start(), matcher.end());
            ObjectParams readObj = new ObjectParams();
            readObj.fromString(finStr);
            deserializedObjs.add(readObj);
        }

        //создаем объекты ссылочных типов
        for(int i = 0; i < deserializedObjs.size(); i++){
            ArrayList<String> references = deserializedObjs.get(i).getReferenceFields();
            for(String className : references){
                Class resultClass = Class.forName(className);
                for(int j = i + 1; j < deserializedObjs.size(); j++){
                    if(deserializedObjs.get(j).getMainObject().getClass().getName().equals(className)){
                        Field declaredField = deserializedObjs.get(i).getMainObject().getClass().getDeclaredField(className.toLowerCase());
                        declaredField.setAccessible(true);
                        declaredField.set(deserializedObjs.get(i).getMainObject(), deserializedObjs.get(j).getMainObject());
                    }

                }
            }
        }

        return deserializedObjs.get(0).getMainObject();
    }

    /**
     * Ятение запись
     */

    private static String readToString(String filePath) throws Exception {
        String content = "";

        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e) {
            Exception newException = new Exception("Не удалось зачитать файл");
            newException.addSuppressed(e);
            throw newException;
        }

        return content;
    }

    private static void writeStringsToFile(String outFilePath, String strs) throws Exception {
        try {
            Files.write(Paths.get(outFilePath), Collections.singleton(strs), Charset.defaultCharset());
        } catch (Exception e) {
            Exception newException = new Exception("Не удалось записать файл");
            newException.addSuppressed(e);
            throw newException;
        }

    }
}
