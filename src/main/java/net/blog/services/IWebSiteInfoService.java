package net.blog.services;

import net.blog.response.ResponseResult;

public interface IWebSiteInfoService {
    ResponseResult getWebsiteTitle();

    ResponseResult putWebSiteTitle(String title);

    ResponseResult getSeoInfo();

    ResponseResult putSeoInfo(String description, String keywords);

    ResponseResult getWebsiteViewCount();

    void updateViewCount();

}
