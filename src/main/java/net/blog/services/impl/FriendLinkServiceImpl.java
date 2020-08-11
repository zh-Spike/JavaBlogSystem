package net.blog.services.impl;

import net.blog.dao.FriendLinkDao;
import net.blog.pojo.FriendLink;
import net.blog.response.ResponseResult;
import net.blog.services.IFriendLinkService;
import net.blog.utils.SnowflakeIdWorker;
import net.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
@Transactional
public class FriendLinkServiceImpl  extends BaseService implements IFriendLinkService {

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private FriendLinkDao friendLinkDao;

    /**
     * 添加友链
     *
     * @param friendLink
     * @return
     */
    @Override
    public ResponseResult addFriendLink(FriendLink friendLink) {
        // 判断数据
        String url = friendLink.getUrl();
        if (TextUtils.isEmpty(url)) {
            return ResponseResult.FAILED("连接URL不能为空");
        }
        String logo = friendLink.getLogo();
        if (TextUtils.isEmpty(logo)) {
            return ResponseResult.FAILED("logo不能为空");
        }
        String name = friendLink.getName();
        if (TextUtils.isEmpty(name)) {
            return ResponseResult.FAILED("名称不能为空");
        }
        // 补全数据
        friendLink.setId(idWorker.nextId() + "");
        friendLink.setCreateTime(new Date());
        friendLink.setUpdateTime(new Date());
        // 保存数据
        friendLinkDao.save(friendLink);
        // 返回结果
        return ResponseResult.FAILED("添加成功");
    }

    @Override
    public ResponseResult getFriendLink(String friendLinkId) {
        FriendLink friendLink = friendLinkDao.findOneById(friendLinkId);
        if (friendLink == null) {
            return ResponseResult.FAILED("链接不存在");
        }
        return ResponseResult.SUCCESS("获取成功");
    }

    @Override
    public ResponseResult listFriendLinks(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        // 创建条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime", "order");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<FriendLink> all = friendLinkDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取成功").setData(all);
    }

    @Override
    public ResponseResult deleteFriendLink(String friendLinkId) {
        int result = friendLinkDao.deleteAllById(friendLinkId);
        if (result == 0) {
            return ResponseResult.FAILED("删除失败");
        }
        return ResponseResult.SUCCESS("删除成功");
    }

    /**
     * 更新内容
     * Logo
     * 友站名称
     * url
     * order
     *
     * @param friendLinkId
     * @param friendLink
     * @return
     */
    @Override
    public ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink) {
        FriendLink friendLinkFromDb = friendLinkDao.findOneById(friendLinkId);
        if (friendLinkFromDb == null) {
            return ResponseResult.FAILED("更新失败");
        }
        String logo = friendLink.getLogo();
        if (!TextUtils.isEmpty(logo)) {
            friendLinkFromDb.setLogo(logo);
        }
        String name = friendLink.getName();
        if (!TextUtils.isEmpty(name)) {
            friendLinkFromDb.setName(name);
        }
        String url = friendLink.getUrl();
        if (!TextUtils.isEmpty(url)) {
            friendLinkFromDb.setUrl(url);
        }
        friendLinkFromDb.setOrder(friendLink.getOrder());
        // 保存数据
        friendLinkDao.save(friendLinkFromDb);
        return ResponseResult.SUCCESS("更新成功");
    }
}
