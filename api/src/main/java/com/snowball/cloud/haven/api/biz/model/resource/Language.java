package com.snowball.cloud.haven.api.biz.model.resource;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>语言编码</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2024/12/20 20:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Language {

    public static final List<Language> LANGUAGE_LIST = new ArrayList<>();

    /**
     * 语言编码
     */
    private String code;

    /**
     * 语言描述
     */
    private String language;

    static {
        LANGUAGE_LIST.add(Language.of("英语", "English"));
        LANGUAGE_LIST.add(Language.of("孟加拉语", "বাংলা"));
        LANGUAGE_LIST.add(Language.of("印地语", "हिंदी"));
        LANGUAGE_LIST.add(Language.of("阿拉伯语", "عربي"));
        LANGUAGE_LIST.add(Language.of("菲律宾语", "Filipino"));
        LANGUAGE_LIST.add(Language.of("越南语", "Tiếng Việt"));
        LANGUAGE_LIST.add(Language.of("西班牙语", "Español"));
        LANGUAGE_LIST.add(Language.of("葡萄牙语", "Português"));
        LANGUAGE_LIST.add(Language.of("印度尼西亚语", "Indonesia"));
        LANGUAGE_LIST.add(Language.of("法语", "Français"));
        LANGUAGE_LIST.add(Language.of("德语", "Deutsch"));
        LANGUAGE_LIST.add(Language.of("日本语", "日本語"));
        LANGUAGE_LIST.add(Language.of("土耳其语", "Türkçe"));
        LANGUAGE_LIST.add(Language.of("韩语", "한국인"));
        LANGUAGE_LIST.add(Language.of("泰语", "แบบไทย"));
        LANGUAGE_LIST.add(Language.of("中文繁体", "中文繁體"));
    }

    /**
     * 语言列表
     *
     * @return
     */
    public static List<Language> getLanguageList() {
        return LANGUAGE_LIST;
    }

    /**
     * 根据code获取语言基本信息
     *
     * @param codeList
     * @return
     */
    public static List<Language> exchangeLanguage(List<String> codeList) {
        return LANGUAGE_LIST.stream().filter(lan -> codeList.contains(lan.getCode())).toList();
    }
}
