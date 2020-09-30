package net.blog.controller.portal;

import net.blog.response.ResponseResult;
import net.blog.services.ILabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/lab")
public class LabPortalApi {
    @Autowired
    private ILabService labService;

    @GetMapping("/list")
    public ResponseResult listLab() {
        return labService.listLab();
    }

    @GetMapping("/{labId}")
    public ResponseResult getLab(@PathVariable("labId") String labId) {
        return labService.getLab(labId);
    }

    @GetMapping("/{labId}/available_count")
    public void updateAvailableNumber(@PathVariable("labId") String labId) {
        labService.updateAvailableNumber(labId);
    }

}
