package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import ejercicio307.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonRoundTripTest {
    @Test
    public void studentSerializationRoundTrip() throws Exception {
        ObjectMapper m = new ObjectMapper();
        Student s = new Student("123","Juan","Perez",20);
        String json = m.writeValueAsString(s);
        Student s2 = m.readValue(json, Student.class);
        assertEquals(s.getId(), s2.getId());
        assertEquals(s.getName(), s2.getName());
        assertEquals(s.getSurname(), s2.getSurname());
        assertEquals(s.getAge(), s2.getAge());
    }
}
