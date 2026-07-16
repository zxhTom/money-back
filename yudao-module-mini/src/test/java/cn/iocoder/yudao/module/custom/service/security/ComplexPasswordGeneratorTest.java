package cn.iocoder.yudao.module.custom.service.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComplexPasswordGeneratorTest {

    @Test
    public void testDefaultLengthAndCharsetCoverage() {
        for (int i = 0; i < 50; i++) {
            String pwd = ComplexPasswordGenerator.generate();
            assertTrue(pwd.length() >= 24, "长度应≥24");
            assertTrue(pwd.chars().anyMatch(Character::isUpperCase), "含大写: " + pwd);
            assertTrue(pwd.chars().anyMatch(Character::isLowerCase), "含小写: " + pwd);
            assertTrue(pwd.chars().anyMatch(Character::isDigit), "含数字: " + pwd);
            assertTrue(pwd.chars().anyMatch(c -> !Character.isLetterOrDigit(c)), "含符号: " + pwd);
        }
    }

    @Test
    public void testRandomness() {
        String a = ComplexPasswordGenerator.generate();
        String b = ComplexPasswordGenerator.generate();
        assertNotEquals(a, b);
    }

    @Test
    public void testMinLengthFloor() {
        assertTrue(ComplexPasswordGenerator.generate(4).length() >= 12);
    }
}
