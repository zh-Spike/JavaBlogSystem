package net.blog.services.impl;

import net.blog.dao.SettingDao;
import net.blog.pojo.Settings;
import net.blog.response.ResponseResult;
import net.blog.services.IWebSiteInfoService;
import net.blog.utils.Constants;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class WebsiteInfoImpl extends BaseService implements IWebSiteInfoService {

    @Autowired
    private SettingDao settingDao;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Override
    public ResponseResult getWebsiteTitle() {
        Settings title = settingDao.findOneByKey(Constants.Settings.WEBSITE_TITLE);
        return ResponseResult.SUCCESS("获取网站title成功").setData(title);
    }

    @Override
    public ResponseResult putWebSiteTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("网站标题不能为空");
        }
        Settings titleFromDb = settingDao.findOneByKey(Constants.Settings.WEBSITE_TITLE);
        if (titleFromDb == null) {
            titleFromDb = new Settings();
            titleFromDb.setId(idWorker.nextId() + "");
            titleFromDb.setUpdateTime(new Date());
            titleFromDb.setCreateTime(new Date());
            titleFromDb.setKey(Constants.Settings.WEBSITE_TITLE);
        }
        titleFromDb.setValue(title);
        settingDao.save(titleFromDb);
        return ResponseResult.SUCCESS("网站title更新成功");
    }

    @Override
    public ResponseResult getSeoInfo() {
        Settings description = settingDao.findOneByKey(Constants.Settings.WEBSITE_DESCRIPTION);
        Settings keyWords = settingDao.findOneByKey(Constants.Settings.WEBSITE_KEYWORDS);
        Map<String,String> result = new HashMap<>();
        result.put(description.getKey(),description.getValue());
        result.put(keyWords.getKey(),keyWords.getValue());
        return ResponseResult.SUCCESS("获取SEO信息成功").setData(result);
    }

    @Override
    public ResponseResult putSeoInfo(String keywords, String description) {
        if (TextUtils.isEmpty(description)) {
            return ResponseResult.FAILED("描述不能为空");
        }
        if (TextUtils.isEmpty(keywords)) {
            return ResponseResult.FAILED("关键字不能为空");
        }
        Settings descriptionFromDb = settingDao.findOneByKey(Constants.Settings.WEBSITE_DESCRIPTION);
        if (descriptionFromDb == null) {
            descriptionFromDb = new Settings();
            descriptionFromDb.setId(idWorker.nextId()+"");
            descriptionFromDb.setCreateTime(new Date());
            descriptionFromDb.setUpdateTime(new Date());
            descriptionFromDb.setKey(Constants.Settings.WEBSITE_DESCRIPTION);
        }
        descriptionFromDb.setValue(description);
        settingDao.save(descriptionFromDb);
        Settings keyWordsFromDb = settingDao.findOneByKey(Constants.Settings.WEBSITE_KEYWORDS);
        if (keyWordsFromDb == null) {
            keyWordsFromDb = new Settings();
            keyWordsFromDb.setId(idWorker.nextId()+"");
            keyWordsFromDb.setCreateTime(new Date());
            keyWordsFromDb.setUpdateTime(new Date());
            keyWordsFromDb.setKey(Constants.Settings.WEBSITE_KEYWORDS);
        }
        keyWordsFromDb.setValue(description);
        settingDao.save(keyWordsFromDb);
        return ResponseResult.SUCCESS("更新SEO信息成功");
    }

    /**
     * 网站访问量，细的话还分来源
     * 只统计浏览量 提供一个接口 页面级
     * @return 浏览量
     */
    @Override
    public ResponseResult getWebsiteViewCount() {
        Settings viewCountFromDb = settingDao.findOneByKey(Constants.Settings.WEBSITE_VIEW_COUNT);
        if (viewCountFromDb == null) {
            viewCountFromDb = new Settings();
            viewCountFromDb.setId(idWorker.nextId()+"");
            viewCountFromDb.setCreateTime(new Date());
            viewCountFromDb.setUpdateTime(new Date());
            viewCountFromDb.setKey(Constants.Settings.WEBSITE_VIEW_COUNT);
            viewCountFromDb.setValue("1");
            settingDao.save(viewCountFromDb);
        }
        Map<String, Integer> result =new HashMap<>();
        result.put(viewCountFromDb.getKey(), Integer.valueOf(viewCountFromDb.getValue()));
        return ResponseResult.SUCCESS("获取网站浏览量成功").setData(result);
    }
}
