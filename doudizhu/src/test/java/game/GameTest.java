package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Stream;

public class GameTest {
    public static void main(String[] args) {
        String s = new Scanner(System.in).nextLine();
        if (!s.trim()
                .contains(" ")) {
            Stream.of(s)
                    .map(new Function<String, Collection<String>>() {
                        @Override
                        public Collection<String> apply(String s) {
                            //要求找出字符串中连续且相等的数字
                            Collection<String> list = new ArrayList<>();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 1; i < s.length(); i++) {
                                char c = s.charAt(i);
                                char c1 = s.charAt(i - 1);
                                list.add(String.valueOf(s.charAt(0)));
                                if (c == c1){
                                    sb.append(c1);
                                }else {
                                    list.add(sb.toString());
                                    sb = new StringBuilder();
                                    sb.append(c1);
                                }
                            }
                            list.add(sb.toString());
                            return list;
                        }
                    })
                    .forEach(System.out::println);
        } else {
            String[] split = s.trim()
                    .split(" ");
            for (String string : split) {
                System.out.println(string);
            }
        }
    }
}
