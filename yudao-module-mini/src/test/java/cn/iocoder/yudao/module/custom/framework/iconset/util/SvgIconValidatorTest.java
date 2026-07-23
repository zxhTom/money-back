package cn.iocoder.yudao.module.custom.framework.iconset.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SvgIconValidatorTest {

    @Test
    public void testIsValid_null_returnsTrue() {
        assertTrue(SvgIconValidator.isValid(null));
    }

    @Test
    public void testIsValid_blank_returnsTrue() {
        assertTrue(SvgIconValidator.isValid("   "));
        assertTrue(SvgIconValidator.isValid(""));
    }

    @Test
    public void testIsValid_simpleSvg_returnsTrue() {
        assertTrue(SvgIconValidator.isValid(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\"><path d=\"M12 5v14\"/></svg>"));
    }

    @Test
    public void testIsValid_leadingTrailingWhitespace_returnsTrue() {
        assertTrue(SvgIconValidator.isValid("   <svg><path d=\"M1 1\"/></svg>   "));
    }

    @Test
    public void testIsValid_multilineSvg_returnsTrue() {
        assertTrue(SvgIconValidator.isValid("<svg>\n<path d=\"M1 1\"/>\n</svg>"));
    }

    @Test
    public void testIsValid_caseInsensitiveTag_returnsTrue() {
        assertTrue(SvgIconValidator.isValid("<SVG><path d=\"M1 1\"/></SVG>"));
    }

    @Test
    public void testIsValid_missingClosingTag_returnsFalse() {
        assertFalse(SvgIconValidator.isValid("<svg><path d=\"M1 1\"/>"));
    }

    @Test
    public void testIsValid_notSvgAtAll_returnsFalse() {
        assertFalse(SvgIconValidator.isValid("<div>not an icon</div>"));
    }

    @Test
    public void testIsValid_wrappedInOtherTag_returnsFalse() {
        assertFalse(SvgIconValidator.isValid("<div><svg><path d=\"M1 1\"/></svg></div>"));
    }

    @Test
    public void testIsValid_scriptTagInjection_returnsFalse() {
        assertFalse(SvgIconValidator.isValid("<svg><script>alert(1)</script></svg>"));
    }

    @Test
    public void testIsValid_onEventAttributeInjection_returnsFalse() {
        assertFalse(SvgIconValidator.isValid("<svg onload=\"alert(1)\"><path/></svg>"));
    }

}
