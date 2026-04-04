package Test;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class codeTest {
    @Test
    public void test(){
        while (true) {
            System.out.println(new Random().nextInt(1,4));
        }
    }
}
