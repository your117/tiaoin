package com.tiaoin.crawl.core.url;

import java.util.Collection;

import com.tiaoin.crawl.core.xml.Rule;

public class UrlRuleChecker {

    public final static String  regex_rule        = "regex";
    public final static String  start_rule        = "start";
    public final static String  end_rule          = "end";
    public final static String  contains_rule     = "contains";
    public final static String  equal_rule        = "equal";
    private final static String not               = "!";
    public final static String  not_regex_rule    = not + regex_rule;
    public final static String  not_start_rule    = not + start_rule;
    public final static String  not_end_rule      = not + end_rule;
    public final static String  not_contains_rule = not + contains_rule;
    public final static String  not_equal_rule    = not + equal_rule;

    public static boolean check(String url, Collection<Rule> rules) {
        for (Rule rule : rules) {
            String t = rule.getType();
            if (t == null)
                throw new RuntimeException("rule type is null of " + url);
            String r = rule.getValue();
            if (r == null)
                throw new RuntimeException("rule value is null of " + url);

            if (regex_rule.equals(t) && !url.matches(r))
                return false;
            if (start_rule.equals(t) && !url.startsWith(r))
                return false;
            if (end_rule.equals(t) && !url.endsWith(r))
                return false;
            if (contains_rule.equals(t) && !url.contains(r))
                return false;
            if (equal_rule.equals(t) && !url.equals(r))
                return false;

            if (not_regex_rule.equals(t) && url.matches(r))
                return false;
            if (not_start_rule.equals(t) && url.startsWith(r))
                return false;
            if (not_end_rule.equals(t) && url.endsWith(r))
                return false;
            if (not_contains_rule.equals(t) && url.contains(r))
                return false;
            if (not_equal_rule.equals(t) && url.equals(r))
                return false;
        }

        return true;
    }

    public static void main(String[] args) {
        String url = "http://news.163.com/12/1017/03/8E06TEVG00011229.html";
        String regex = "http://news.163.com/\\d+/\\d+/\\d+/\\w+\\.html";
        System.out.println(url.matches(regex));
    }
}
