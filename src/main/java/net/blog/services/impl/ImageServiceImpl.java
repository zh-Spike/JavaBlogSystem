package net.blog.services.impl;

import lombok.extern.slf4j.Slf4j;
import net.blog.dao.ImageDao;
import net.blog.pojo.Image;
import net.blog.pojo.User;
import net.blog.response.ResponseResult;
import net.blog.services.IImageService;
import net.blog.services.IUserService;
import net.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class ImageServiceImpl extends BaseService implements IImageService {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");

    @Value("${blog.image.save-path}")
    public String imagePath;
    @Value("${blog.image.max-size}")
    public long maxSize;

    @Autowired
    private SnowflakeIdWorker idWorker;

    @Autowired
    private IUserService userService;

    @Autowired
    private ImageDao imageDao;

    /**
     * 上传路径：配置config
     * 上传内容，命名->ID,每天一个文件夹
     * 从配置文件来限制文件大小
     * 保存内容到数据库
     * ID/存储路径/url/原名称/用户ID/状态/创建日期/更新日期
     *
     * @param original
     * @param file
     * @return
     */
    @Override
    public ResponseResult uploadImage(String original, MultipartFile file) {
        // 判断有无文件
        if (file == null) {
            return ResponseResult.FAILED("图片不可以为空");
        }
        // 判断文件类型,只能图片
        String contentType = file.getContentType();
        log.info("contentType == >" + contentType);
        if (TextUtils.isEmpty(contentType)) {
            return ResponseResult.FAILED("文件格式错误");
        }
        String originalFilename = file.getOriginalFilename();
        log.info("originalFilename == > " + originalFilename);
        String type = getType(contentType, originalFilename);
        if (type == null) {
            return ResponseResult.FAILED("不支持该文件类型");
        }
        // 获取相关数据，图片类型、名称
        // 限制文件大小
        long size = file.getSize();
        log.info("maxSize == >" + maxSize + "    size == >" + size);
        if (size > maxSize) {
            return ResponseResult.FAILED("图片最大仅支持" + (maxSize / 1024 / 1024) + "MB");
        }
        // 创建图片保存目录
        // 规则:配置目录/日期/类型/ID.类型
        long currentMillions = System.currentTimeMillis();
        String currentDay = simpleDateFormat.format(currentMillions);
        log.info("currentDay == >" + currentDay);
        String dayPath = imagePath + File.separator + currentDay;
        File dayPathFile = new File(dayPath);
        // 判断日期文件夹是否存在
        if (!dayPathFile.exists()) {
            dayPathFile.mkdirs();
        }
        String targetName = String.valueOf(idWorker.nextId());
        String targetPath = imagePath + File.separator + currentDay + File.separator + type +
                File.separator + targetName + "." + type;
        File targetFile = new File(targetPath);
        // 判断类型文件夹是否存在
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try {
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            log.info("targetFile == >" + targetFile);
            // 保存文件
            file.transferTo(targetFile);
            // 返回结果：
            // 1. 访问路径--> 对应解析来
            Map<String, String> result = new HashMap<>();
            String resultPath = currentMillions + "_" + targetName + "." + type;
            result.put("id", resultPath);
            // 2. 图片名称--> alt="图片描述"，不写的话前端就有名称当描述
            result.put("name", originalFilename);
            Image image = new Image();
            image.setContentType(contentType);
            image.setId(targetName);
            image.setCreateTime(new Date());
            image.setUpdateTime(new Date());
            image.setPath(targetFile.getPath());
            image.setName(originalFilename);
            image.setUrl(resultPath);
            image.setOriginal(original);
            image.setState("1");
            User user = userService.checkUser();
            image.setUserId(user.getId());
            // 记录文件
            // 保存到数据库
            imageDao.save(image);
            // 返回结果
            return ResponseResult.SUCCESS("图片上传成功").setData(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILED("图片上传失败");
    }

    private String getType(String contentType, String name) {
        String type = null;
        if (Constants.ImageType.TYPE_PNG_WITH_PREFIX.equals(contentType)
                && name.toLowerCase().endsWith(Constants.ImageType.TYPE_PNG)) {
            type = Constants.ImageType.TYPE_PNG;
        } else if (Constants.ImageType.TYPE_GIF_WITH_PREFIX.equals(contentType)
                && name.toLowerCase().endsWith(Constants.ImageType.TYPE_GIF)) {
            type = Constants.ImageType.TYPE_GIF;
        } else if (Constants.ImageType.TYPE_JPG_WITH_PREFIX.equals(contentType)
                && name.toLowerCase().endsWith(Constants.ImageType.TYPE_JPG)) {
            type = Constants.ImageType.TYPE_JPG;
        }
        return type;
    }

    @Override
    public void viewImage(HttpServletResponse response, String imageId) throws IOException {
        // 已知配置目录
        // 根据尺寸来动态返回图片给前端 减少带宽 传输速度快 消耗后台CPU
        // 上传时将图片复制成三个尺寸 大中小
        // 需要日期
        String[] paths = imageId.split("_");
        String dayValue = paths[0];
        String format = simpleDateFormat.format(Long.parseLong(dayValue));
        log.info("viewImage format == >" + format);
        // Id
        String name = paths[1];
        // 类型
        String type = name.substring(name.length() - 3);
        // 日期的时间戳/日期/类型/ID.类型
        String targetPath = imagePath + File.separator + format + File.separator +
                type + File.separator + name;
        log.info("get image target path == >" + targetPath);

        File file = new File(targetPath);
        OutputStream writer = null;
        FileInputStream fos = null;
        try {
            response.setContentType("image/jpg");
            writer = response.getOutputStream();
            // 读取
            fos = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len;
            while ((len = fos.read(buff)) != -1) {
                writer.write(buff, 0, len);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public ResponseResult listImages(int page, int size, String original) {
        // 处理page和size
        page = checkPage(page);
        size = checkSize(size);
        User user = userService.checkUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        // 创建分页条件
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        // 查询
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        // 返回结果
        final String userId = user.getId();
        Page<Image> all = imageDao.findAll(new Specification<Image>() {
            @Override
            public Predicate toPredicate(Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                // 根据用户ID
                Predicate userIdPre = criteriaBuilder.equal(root.get("userId").as(String.class), userId);
                // 根据状态
                Predicate statePre = criteriaBuilder.equal(root.get("state").as(String.class), "1");
                Predicate and;
                if (!TextUtils.isEmpty(original)) {
                    Predicate originalPre = criteriaBuilder.equal(root.get("original").as(String.class), original);
                    and = criteriaBuilder.and(userIdPre, statePre, originalPre);
                } else {
                    and = criteriaBuilder.and(userIdPre, statePre);
                }
                return and;
            }
        }, pageable);
        return ResponseResult.SUCCESS("获取图片列表成功").setData(all);
    }

    /**
     * 删除图片
     * 改变状态
     *
     * @param imageId
     * @return
     */
    @Override
    public ResponseResult deleteById(String imageId) {
        int result = imageDao.deleteByIdUpdateState(imageId);
        if (result > 0) {
            return ResponseResult.FAILED("删除成功");
        }
        return ResponseResult.FAILED("图片不存在");
    }

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void createQrCode(String code, HttpServletResponse response, HttpServletRequest request) {
        // 检查是否过期
        String loginState = (String) redisUtils.get(Constants.User.KEY_PC_LOGIN_ID + code);
        if (TextUtils.isEmpty(loginState)) {
            // 返回一张图片显示二维码已经过期
            return;
        }
        String originalDomain = TextUtils.getDomain(request);
/*      log.info("requestURI == > " + requestURI);
        log.info("servletPath == > " + servletPath);
        log.info("URL == > " + requestURL.toString());*/
        // 生成二维码
        // 1. 可以简单是个code
        // 用自己的app来扫描可以识别和解释 请求对应端口
        // 用第三方端口可以识别 但无法访问 只能显示code
        // 2. 提供一个app下载地址+code,自己的app扫描就切割后面的code进行解析
        // 请求对应接口,如果是第三方app就是网址 访问下载地址
        // APP_DOWNLOAD_PATH/code
        String content = originalDomain + Constants.APP_DOWNLOAD_PATH + "===" + code;
        log.info("QR_code content == >" + content);
        byte[] result = QrCodeUtils.encodeQRCode(content);
        response.setContentType(QrCodeUtils.RESPONSE_CONTENT_TYPE);
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(result);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // http://localhost:8081/portal/app/===745950517162672128
        // 第三方就访问这个地址
    }
}
