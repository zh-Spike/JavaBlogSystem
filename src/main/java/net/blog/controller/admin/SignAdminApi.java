package net.blog.controller.admin;

import net.blog.pojo.Sign;
import net.blog.response.ResponseResult;
import net.blog.services.ISignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sign")
public class SignAdminApi {

    @Autowired
    private ISignService signService;

    @PreAuthorize("@permission.admin()")
    @PutMapping("/{signId}")
    public ResponseResult updateAppointment(@PathVariable("signId") String signId, @RequestBody Sign sign) {
        return signService.updateSign(signId, sign);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listSigns() {
        return signService.listSigns();
    }
}
