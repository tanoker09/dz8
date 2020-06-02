import Serializer.SerializeToJson;

public class Pet implements SerializeToJson {
    private String name;
    private int age;

    public Pet(){}

    public Pet(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
