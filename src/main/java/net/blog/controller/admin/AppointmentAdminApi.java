package net.blog.controller.admin;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.pojo.Appointment;
import net.blog.response.ResponseResult;
import net.blog.services.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/appointment")
public class AppointmentAdminApi {

    @Autowired
    private IAppointmentService appointmentService;

    /**
     * 添加
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PostMapping
    public ResponseResult addAppointment(@RequestBody Appointment appointment) {
        return appointmentService.addAppointment(appointment);
    }

    /**
     * 删除
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{appointmentId}")
    public ResponseResult deleteAppointmentId(@PathVariable("appointmentId") String appointmentId) {
        return appointmentService.deleteAppointment(appointmentId);
    }

    /**
     * 更新预约
     */
    @CheckTooFrequentCommit
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{appointmentId}")
    public ResponseResult updateAppointment(@PathVariable("appointmentId") String appointmentId, @RequestBody Appointment appointment) {
        return appointmentService.updateAppointment(appointmentId, appointment);
    }

    /**
     * 获取预约
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{appointmentId}")
    public ResponseResult getAppointment(@PathVariable("appointmentId") String appointmentId) {
        return appointmentService.getAppointment(appointmentId);
    }

    /**
     * 获取预约列表
     *
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listAppointment(@PathVariable("page") int page, @PathVariable("size") int size) {
        return appointmentService.listAppointment(page, size);
    }
}
