package net.blog.services;

import net.blog.pojo.Lab;
import net.blog.response.ResponseResult;

public interface ILabService {
    ResponseResult addLab(Lab lab);

    ResponseResult deleteLab(String labId);

    ResponseResult updateLab(String labId, Lab lab);

    ResponseResult getLab(String labId);

    ResponseResult listLab();

    void updateAvailableNumber(String labId);
}
