package tech.comfortheart.util;

import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class StringUtilityTest {
    @Test
    public void testCapitalize() {
        String hello = "hello";
        assertEquals("Hello", StringUtility.capitalize(hello));
        assertEquals("Hello", StringUtility.capitalize("Hello"));
        assertEquals(" hello", StringUtility.capitalize(" hello"));
    }

    @Test
    public void testNotEmpty() {
        assertTrue(StringUtility.notEmpty("x"));
        assertTrue(StringUtility.isEmpty(null));
        assertTrue(StringUtility.isEmpty(""));
        assertTrue(StringUtility.isEmpty(" "));
    }

    @Test
    public void testEqual() {
        assertTrue(StringUtility.equalRegardlessCaseOrRoundingSpaces("", ""));
        assertFalse(StringUtility.equalRegardlessCaseOrRoundingSpaces("", "x"));
        assertFalse(StringUtility.equalRegardlessCaseOrRoundingSpaces("x", null));
        assertTrue(StringUtility.equalRegardlessCaseOrRoundingSpaces("x", "X"));
        assertTrue(StringUtility.equalRegardlessCaseOrRoundingSpaces("x", "X "));
    }

    @Test
    public void testReplaceSuffix() {
        String s = "c:/hello/world.xlsx-mygod.xlsx";
        String s1 = StringUtility.replaceSuffix(s, "xlsx", "-copy.xlsx");
        assertEquals( "c:/hello/world.xlsx-mygod-copy.xlsx", s1);

        s1 = StringUtility.replaceSuffix(s, ".xlsx", "-copy.xlsx");
        assertEquals( "c:/hello/world.xlsx-mygod-copy.xlsx", s1);
    }

    @Test
    public void testReplaceVar() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("who", "you");
        vars.put("user", "I");
        String result = StringUtility.replaceVariable("are '$who' okay? $who is the best! $user is admiring $who! $today is a good day!", vars);
        assertEquals("are 'you' okay? you is the best! I is admiring you! $today is a good day!", result);

        LocalDate date = LocalDate.parse("2020-01-01");
        vars.put("today", date);
        result = StringUtility.replaceVariable(result, vars);
        assertEquals("are 'you' okay? you is the best! I is admiring you! 2020-01-01 is a good day!", result);


    }
}
