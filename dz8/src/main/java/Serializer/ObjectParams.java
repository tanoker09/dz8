package Serializer;

import Serializer.FieldJson;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Класс хранящий информацию о сериализации
 */
public class ObjectParams {
    private String name;
    private ArrayList<FieldJson> fields = new ArrayList<>();
    private Object mainObject;
    private ArrayList<String> referenceFields = new ArrayList<>();

    public ObjectParams() {
    }

    public ObjectParams(String name) {
        this.name = name;
    }

    public Object getMainObject() {
        return mainObject;
    }

    public ArrayList<String> getReferenceFields() {
        return referenceFields;
    }

    public void addField(FieldJson field){
        fields.add(field);
    }

    /**
     * десериализация объекта из строки
     */
    public Object fromString(String json) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Pattern NAME_PATTERN = Pattern.compile("\"name\":\"\\w+\",\n");
        Pattern FIELDS_PATTERN = Pattern.compile("\"fields\":(.+\n)+");

        getNameFromJson(NAME_PATTERN, json);
        Class resultClass = Class.forName(this.name);
        Object resultObj = resultClass.newInstance();
        getFieldsFromJson(FIELDS_PATTERN, json, resultObj);

        mainObject = resultObj;
        return resultObj;
    }

    /**
     * получение имени класса
     * @param NAME_PATTERN
     * @param text
     */
    private void getNameFromJson(Pattern NAME_PATTERN, String text){
        Pattern NAME_NAME_PATTERN = Pattern.compile(":\".+\"");
        Matcher matcher = NAME_PATTERN.matcher(text);
        while (matcher.find()) {
           String name = text.substring(matcher.start(), matcher.end());
           Matcher insideNameMatcher = NAME_NAME_PATTERN.matcher(name);
           while(insideNameMatcher.find()){
               this.name = name.substring(insideNameMatcher.start() + 2, insideNameMatcher.end() - 1);
           }
        }
    }

    /**
     * Получение и инициализация полей
     * @param FIELDS_PATTERN
     * @param text
     * @param resultObj
     */
    private void getFieldsFromJson(Pattern FIELDS_PATTERN, String text, Object resultObj){
        Pattern FIELD_PATTERN = Pattern.compile("\\{.+\\}");
        Matcher matcher = FIELDS_PATTERN.matcher(text);
        while (matcher.find()) {
            String name = text.substring(matcher.start(), matcher.end());
            Matcher fieldMatcher = FIELD_PATTERN.matcher(name);
            while(fieldMatcher.find()){
                String params = name.substring(fieldMatcher.start() + 1, fieldMatcher.end() - 1);

                Map<String, String> result = Arrays.stream(params.split(","))
                        .map(i -> i.split(":"))
                        .collect(Collectors.toMap(a -> a[0],a->a[1]));

                Field declaredField = null;
                try {

                    declaredField = resultObj.getClass().getDeclaredField(result.get("\"name\"").replaceAll("\"", ""));
                    boolean accessible = declaredField.isAccessible();

                    declaredField.setAccessible(true);

                    String type = result.get("\"type\"").replaceAll("\"", "");
                    Class<?> cls = Class.forName(type);
                    String val = result.get("\"value\"").replaceAll("\"", "");

                    if(val.contains("#class")){
                        referenceFields.add(type);
                        continue;
                    }

                    Object value = convert(cls, val);
                    System.out.println(value.toString());

                    declaredField.set(resultObj, value);


                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | ClassNotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object convert(Class<?> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }

    @Override
    public String toString() {
        StringBuilder objJson = new StringBuilder();
        objJson.append("{\n\"name\":");
        objJson.append("\"" + name + "\",\n");

        objJson.append("\"fields\":[\n");
        for(int i = 0; i < fields.size(); i++){
            objJson.append(fields.get(i).toString());
            if(i != fields.size() - 1)
                objJson.append(",\n");
        }
        objJson.append("\n]");

//        objJson.append(",\n");
//        objJson.append("\"methods\":[\n");
//        for (int i = 0; i < methods.size(); i++) {
//            objJson.append(methods.get(i).toString());
//            if (i != methods.size() - 1)
//                objJson.append(",\n");
//        }
//        objJson.append("]\n");

        objJson.append("}");

        return objJson.toString();

    }
}

