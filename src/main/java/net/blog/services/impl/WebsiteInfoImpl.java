package net.blog.services.impl;

import net.blog.dao.SettingDao;
import net.blog.pojo.Settings;
import net.blog.response.ResponseResult;
import net.blog.services.IWebSiteInfoService;
import net.blog.utils.Constants;
import net.blog.utils.RedisUtils;
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
        Map<String, String> result = new HashMap<>();
        result.put(description.getKey(), description.getValue());
        result.put(keyWords.getKey(), keyWords.getValue());
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
            descriptionFromDb.setId(idWorker.nextId() + "");
            descriptionFromDb.setCreateTime(new Date());
            descriptionFromDb.setUpdateTime(new Date());
            descriptionFromDb.setKey(Constants.Settings.WEBSITE_DESCRIPTION);
        }
        descriptionFromDb.setValue(description);
        settingDao.save(descriptionFromDb);
        Settings keyWordsFromDb = settingDao.findOneByKey(Constants.Settings.WEBSITE_KEYWORDS);
        if (keyWordsFromDb == null) {
            keyWordsFromDb = new Settings();
            keyWordsFromDb.setId(idWorker.nextId() + "");
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
     *
     * @return 浏览量
     */
    @Override
    public ResponseResult getWebsiteViewCount() {
        // 先从redis里拿
        String viewCountStr = (String) redisUtils.get(Constants.Settings.WEBSITE_VIEW_COUNT);
        Settings viewCount = settingDao.findOneByKey(Constants.Settings.WEBSITE_VIEW_COUNT);
        if (viewCount == null) {
            viewCount = this.initViewItem();
            settingDao.save(viewCount);
        }
        if (TextUtils.isEmpty(viewCountStr)) {
            viewCountStr = viewCount.getValue();
            redisUtils.set(Constants.Settings.WEBSITE_VIEW_COUNT, viewCountStr);
        } else {
            // 把redis里保存到数据库里
            viewCount.setValue(viewCountStr);
            settingDao.save(viewCount);
        }
        Map<String, Integer> result = new HashMap<>();
        result.put(viewCount.getKey(), Integer.valueOf(viewCount.getValue()));
        return ResponseResult.SUCCESS("获取网站浏览量成功").setData(result);
    }

    private Settings initViewItem() {
        Settings viewCount = new Settings();
        viewCount = new Settings();
        viewCount.setId(idWorker.nextId() + "");
        viewCount.setCreateTime(new Date());
        viewCount.setUpdateTime(new Date());
        viewCount.setKey(Constants.Settings.WEBSITE_VIEW_COUNT);
        viewCount.setValue("1");
        return viewCount;
    }

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 1. 并发量
     * 2. 过滤相同IP or ID
     * 3. 防止攻击
     */
    @Override
    public void updateViewCount() {
        // redis更新时机
        Object viewCount = redisUtils.get(Constants.Settings.WEBSITE_VIEW_COUNT);
        if (viewCount == null) {
            Settings setting = settingDao.findOneByKey(Constants.Settings.WEBSITE_VIEW_COUNT);
            if (setting == null) {
                setting = this.initViewItem();
                settingDao.save(setting);
            }
            redisUtils.set(Constants.Settings.WEBSITE_VIEW_COUNT, setting.getValue());
        } else {
            // 自增
            redisUtils.incr(Constants.Settings.WEBSITE_VIEW_COUNT, 1);
        }
    }
}
