package rule;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NameRule 单元测试类。
 */
class NameRuleTest {

    @Test
    void testValidatePlayerNames_ValidNames() {
        List<String> names = Arrays.asList("张三", "李四", "王五");
        boolean result = NameRule.validatePlayerNames(names);
        assertTrue(result, "有效的三个名字应返回true");
    }

    @Test
    void testValidatePlayerNames_NullList() {
        boolean result = NameRule.validatePlayerNames(null);
        assertFalse(result, "null列表应返回false");
    }

    @Test
    void testValidatePlayerNames_EmptyList() {
        boolean result = NameRule.validatePlayerNames(Collections.emptyList());
        assertFalse(result, "空列表应返回false");
    }

    @Test
    void testValidatePlayerNames_OnlyOneName() {
        List<String> names = Arrays.asList("张三");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "只有一个名字应返回false");
    }

    @Test
    void testValidatePlayerNames_OnlyTwoNames() {
        List<String> names = Arrays.asList("张三", "李四");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "只有两个名字应返回false");
    }

    @Test
    void testValidatePlayerNames_FourNames() {
        List<String> names = Arrays.asList("张三", "李四", "王五", "赵六");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "四个名字应返回false");
    }

    @Test
    void testValidatePlayerNames_ContainsNull() {
        List<String> names = Arrays.asList("张三", null, "王五");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "包含null元素应返回false");
    }

    @Test
    void testValidatePlayerNames_ContainsBlankName() {
        List<String> names = Arrays.asList("张三", "  ", "王五");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "包含空白名字应返回false");
    }

    @Test
    void testValidatePlayerNames_EmptyString() {
        List<String> names = Arrays.asList("张三", "", "王五");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "包含空字符串应返回false");
    }

    @Test
    void testValidatePlayerNames_TabAndNewline() {
        List<String> names = Arrays.asList("张三", "\t", "王五");
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "包含制表符应返回false");
    }

    @Test
    void testValidatePlayerNames_FullWidthSpace() {
        List<String> names = Arrays.asList("张三", "　", "王五"); // 全角空格
        boolean result = NameRule.validatePlayerNames(names);
        assertFalse(result, "包含全角空格应返回false");
    }

    @Test
    void testValidatePlayerNames_EnglishNames() {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        boolean result = NameRule.validatePlayerNames(names);
        assertTrue(result, "英文名字也应验证通过");
    }

    @Test
    void testValidatePlayerNames_MixedNames() {
        List<String> names = Arrays.asList("张三", "Bob", "王五");
        boolean result = NameRule.validatePlayerNames(names);
        assertTrue(result, "中英文混合名字也应验证通过");
    }
}
