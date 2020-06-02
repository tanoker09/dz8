package Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * класс отвечающий за поле класса
 */
public class FieldJson{
    private String name;
    private String type;
    private int modifier;
    private Object value;

    private Map<String,String> builtInMap = new HashMap<String,String>();
    {
        builtInMap.put("int", "java.lang.Integer" );
        builtInMap.put("long", "java.lang.Long" );
        builtInMap.put("double", "java.lang.Double" );
        builtInMap.put("float", "java.lang.Float" );
        builtInMap.put("bool", "java.lang.Boolean" );
        builtInMap.put("char", "java.lang.Character" );
        builtInMap.put("byte", "java.lang.Byte" );
        builtInMap.put("void", "java.lang.Void" );
        builtInMap.put("short", "java.lang.Short" );
    }

    public FieldJson() {
    }

    public FieldJson(String name, String type, int modifier) {
        this.name = name;
        this.type = type;
        this.modifier = modifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(builtInMap.containsKey(type))
            this.type = builtInMap.get(type);
        else
            this.type = type;
    }

    public int getModifier() {
        return modifier;
    }

    public void setModifier(int modifier) {
        modifier = modifier;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder json = new StringBuilder();
        json.append("{" +
                "\"name\":\"" + name + "\"," +
                "\"type\":\"" + type + "\"," +
                "\"modifier\":" + modifier +","
        );

        if(value instanceof String){
            json.append("\"value\":\"" + value.toString()+"\"" +"}\n");
        }
        else{
            json.append("\"value\":" + value.toString() +"}\n");
        }

        return  json.toString();
    }
}

