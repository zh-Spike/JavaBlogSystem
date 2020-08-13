package net.blog.services;

import net.blog.pojo.Looper;
import net.blog.response.ResponseResult;

public interface ILooperService {
    ResponseResult addLoop(Looper looper);

    ResponseResult getLoop(String loopId);

    ResponseResult listLoops();

    ResponseResult deleteLooper(String loopId);

    ResponseResult updateLooper(String loopId, Looper looper);
}
