package net.blog.services;

import net.blog.pojo.Appointment;
import net.blog.response.ResponseResult;

public interface IAppointmentService {
    ResponseResult addAppointment(Appointment appointment);

    ResponseResult deleteAppointment(String appointmentId);

    ResponseResult updateAppointment(String appointmentId, Appointment appointment);

    ResponseResult getAppointment(String appointmentId);

    ResponseResult listComments(int page, int size);
}
