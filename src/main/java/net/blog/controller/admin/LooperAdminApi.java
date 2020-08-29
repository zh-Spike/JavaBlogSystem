package net.blog.controller.admin;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.pojo.Looper;
import net.blog.response.ResponseResult;
import net.blog.services.ILooperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/loop")
public class LooperAdminApi {

    @Autowired
    private ILooperService looperService;

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult addLoop(@RequestBody Looper looper) {
        return looperService.addLoop(looper);
    }

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{loopId}")
    public ResponseResult deleteLooper(@PathVariable("loopId") String loopId) {
        return looperService.deleteLooper(loopId);
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{loopId}")
    public ResponseResult updateLooper(@PathVariable("loopId") String loopId, @RequestBody Looper looper) {
        return looperService.updateLooper(loopId, looper);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/{loopId}")
    public ResponseResult getLoop(@PathVariable("loopId") String loopId) {
        return looperService.getLoop(loopId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listLoops() {
        return looperService.listLoops();
    }
}
