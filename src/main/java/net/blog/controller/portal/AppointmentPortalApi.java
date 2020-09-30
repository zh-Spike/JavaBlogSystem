package net.blog.controller.portal;

import net.blog.interceptor.CheckTooFrequentCommit;
import net.blog.pojo.Appointment;
import net.blog.response.ResponseResult;
import net.blog.services.IAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/appointment")
public class AppointmentPortalApi {

    @Autowired
    private IAppointmentService appointmentService;

    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult addAppointment(@RequestBody Appointment appointment) {
        return appointmentService.addAppointment(appointment);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseResult deleteAppointmentById(@PathVariable("appointmentId") String appointmentId) {
        return appointmentService.deleteAppointmentById(appointmentId);
    }

    @GetMapping("/list/{page}/{size}")
    public ResponseResult listAppointment(@PathVariable("page") int page, @PathVariable("size") int size,
                                          @RequestParam(value = "userId", required = false) String userId,
                                          @RequestParam(value = "state", required = false) String state) {
        return appointmentService.listAppointment(page, size, userId, state);
    }
}
