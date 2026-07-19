package cn.iocoder.yudao.module.custom.framework.skin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CssVarDeclarationValidatorTest {

    @Test
    public void testIsValid_null_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid(null));
    }

    @Test
    public void testIsValid_blank_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid("   "));
        assertTrue(CssVarDeclarationValidator.isValid(""));
    }

    @Test
    public void testIsValid_singleDeclaration_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid("--color-primary: #FF0000;"));
    }

    @Test
    public void testIsValid_multipleDeclarations_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid(
                "--color-primary: #FF0000;\n--radius-lg: 40rpx;\n--font-size-base: 28rpx;"));
    }

    @Test
    public void testIsValid_blankLinesIgnored_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid("--color-primary: #FF0000;\n\n   \n--radius-lg: 40rpx;"));
    }

    @Test
    public void testIsValid_leadingTrailingWhitespaceOnLine_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid("   --color-primary: #FF0000;   "));
    }

    @Test
    public void testIsValid_missingLeadingDashes_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid("color-primary: #FF0000;"));
    }

    @Test
    public void testIsValid_missingTrailingSemicolon_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid("--color-primary: #FF0000"));
    }

    @Test
    public void testIsValid_selectorInjection_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid(".evil { --color-primary: #FF0000; }"));
    }

    @Test
    public void testIsValid_curlyBraceInjection_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid("--color-primary: #FF0000; } body { display:none"));
    }

    @Test
    public void testIsValid_mediaQueryInjection_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid("@media (min-width: 100px) { --color-primary: red; }"));
    }

    @Test
    public void testIsValid_scriptTagInjection_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid("--color-primary: <script>alert(1)</script>;"));
    }

    @Test
    public void testIsValid_urlWithParenthesesStillValid_returnsTrue() {
        assertTrue(CssVarDeclarationValidator.isValid("--bg-image: url(https://example.com/a.png);"));
    }

    @Test
    public void testIsValid_oneValidOneInvalidLine_returnsFalse() {
        assertFalse(CssVarDeclarationValidator.isValid("--color-primary: #FF0000;\n@import url(evil.css);"));
    }

}
