package tech.comfortheart.util;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;


public class TestUtil {
    private static Logger log = Logger.getLogger(TestUtil.class.getSimpleName());

    public static File getResourceFile(String relativePath) {
        return new File(ClassLoader.getSystemClassLoader().getResource(relativePath).getFile());
    }

    public static void testGetterAndSetters(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();

        for(Field field: fields) {
            String fieldTypeName = field.getType().getSimpleName();

            String fieldName = field.getName();
            String getter = "get" + StringUtility.capitalize(fieldName);
            if (fieldTypeName.equals("boolean") || fieldTypeName.equals("Boolean")) {
                try {
                    o.getClass().getDeclaredMethod(getter, null);
                } catch (NoSuchMethodException e) {
                    log.info("for boolean, should be 'is' instead of 'get': " + e.getMessage());
                    getter = "is" + StringUtility.capitalize(fieldName);
                }
            }

            String setter = "set" + StringUtility.capitalize(fieldName);
            Object o1;
            switch (fieldTypeName) {
                case "int":
                case "Integer":
                    o1 = 54;
                    break;
                case "double":
                case "Double":
                    o1 = 1d;
                    break;
                case "float":
                case "Float":
                    o1 = 1f;
                    break;
                case "boolean":
                case "Boolean":
                    o1 = true;
                    break;
                case "BigDecimal":
                    o1 = BigDecimal.ONE;
                    break;
                case "String":
                    o1 = "my god";
                    break;
                case "char":
                    o1 = 'B';
                    break;
                case "short":
                    o1 = (short) 1;
                    break;
                default:
                    log.info("Type is not primitive types: " + fieldTypeName);
                    o1 = null;
            }
            try {
                Method getterMethod = o.getClass().getDeclaredMethod(getter, null);
                o1 = o1 == null?field.getType().newInstance():o1;

                field.setAccessible(true);
                field.set(o, o1);
                assertEquals(o1, getterMethod.invoke(o, null));
                log.info("Successfully tested the getter: " + getterMethod.getName());
            } catch (NoSuchMethodException e) {
                log.info("No getter for " + field.getName());
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

            try {
                Method setterMethod = o.getClass().getDeclaredMethod(setter, field.getType());
                o1 = o1 == null?field.getType().newInstance():o1;
                setterMethod.invoke(o, o1);

                field.setAccessible(true);
                assertEquals(o1, field.get(o));
            } catch (NoSuchMethodException e) {
                log.info("No setter found for" + field.getName());
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
        }

    }
}
