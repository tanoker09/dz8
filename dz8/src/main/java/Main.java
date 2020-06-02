import Serializer.ObjectSerializer;

public class Main {
    final static String filePath = "src/main/resources/Task/result.json";

    public static void main(String[] args) throws Exception {
        Pet pet = new Pet("Barsik", 3);
        Person p = new Person(25, "Petr", 180, pet);
        ObjectSerializer objectSerializer = new ObjectSerializer();
        objectSerializer.serialize(p, filePath);

        Person p2 = (Person)objectSerializer.deserialize(filePath);
    }
}
