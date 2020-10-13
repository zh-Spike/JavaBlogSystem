package spike;

import org.springframework.util.DigestUtils;

public class TestMd5Value {
    public static void main(String[] args) {
        String jwtKeyMd5Str = DigestUtils.md5DigestAsHex("123456".getBytes());
        System.out.println(jwtKeyMd5Str);
    }
}
