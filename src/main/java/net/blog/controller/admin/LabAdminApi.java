package net.blog.controller.admin;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.pojo.Lab;
import net.blog.response.ResponseResult;
import net.blog.services.ILabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/lab")

public class LabAdminApi {
    @Autowired
    private ILabService labService;

    /*
    增加实验室
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult addLab(@RequestBody Lab lab) {
        return labService.addLab(lab);
    }

    /*
    删除实验室
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{labId}")
    public ResponseResult deleteLab(@PathVariable("labId") String labId) {
        return labService.deleteLab(labId);
    }

    /*
    更新实验室数据
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{labId}")
    public ResponseResult updateLab(@PathVariable("labId") String labId, @RequestBody Lab lab) {
        return labService.updateLab(labId, lab);
    }

    /*
    获取实验室数据
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{labId}")
    public ResponseResult getLab(@PathVariable("labId") String labId) {
        return labService.getLab(labId);
    }

    /*
    获取实验室列表
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listLab() {
        return labService.listLab();
    }
}
