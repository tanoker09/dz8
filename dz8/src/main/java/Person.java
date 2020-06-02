import Serializer.SerializeToJson;

public class Person implements SerializeToJson {
    private int age;
    private String name;
    private Integer height;
    private Pet pet;

    public Person(){}

    public Person(int age, String name, Integer height, Pet pet) {
        this.age = age;
        this.name = name;
        this.height = height;
        this.pet = pet;
    }
}
