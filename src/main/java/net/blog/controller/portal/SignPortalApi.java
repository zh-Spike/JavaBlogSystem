package net.blog.controller.portal;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.pojo.Sign;
import net.blog.response.ResponseResult;
import net.blog.services.ISignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/sign")
public class SignPortalApi {

    @Autowired
    private ISignService signService;

    @CheckTooFrequentCommit
    @PostMapping()
    public ResponseResult signIn(@RequestBody Sign sign) {
        return signService.signIn(sign);
    }

    @CheckTooFrequentCommit
    @PutMapping("/{signId}")
    public ResponseResult signOut(@PathVariable("signId") String signId) {
        return signService.signOut(signId);
    }

    @GetMapping("/list")
    public ResponseResult listUserSign() {
        return signService.listUserSign();
    }
}
