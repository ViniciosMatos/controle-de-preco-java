package domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Converter
public class LinksConverter implements AttributeConverter<List<Map<String, String>>, String> {

    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<List<Map<String, String>>>() {}.getType();

    @Override
    public String convertToDatabaseColumn(List<Map<String, String>> attribute) {
        if (attribute == null) {
            return "[]";
        }
        return gson.toJson(attribute);
    }

    @Override
    public List<Map<String, String>> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return gson.fromJson(dbData, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
