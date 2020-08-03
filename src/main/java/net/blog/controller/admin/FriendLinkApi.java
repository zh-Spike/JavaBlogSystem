package net.blog.controller.admin;

import net.blog.pojo.FriendLink;
import net.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/friend_link")
public class FriendLinkApi {

    @PostMapping
    public ResponseResult addFriendLink(@RequestBody FriendLink friendLink){
        return null;
    }

    @DeleteMapping("/{friendLinkId}")
    public ResponseResult deleteFriendLink(@PathVariable("friendLinkId") String friendLinkId){
        return null;
    }

    @PutMapping("/{friendLinkId}")
    public ResponseResult updateFriendLink(@PathVariable("friendLinkId") String friendLinkId){
        return null;
    }

    @GetMapping("/{friendLinkId}")
    public ResponseResult getFriendLink(@PathVariable("friendLinkId") String friendLinkId){
        return null;
    }

    @GetMapping("/list")
    public ResponseResult listFriendLinks(@RequestParam("page")int page,@RequestParam("size")int size){
        return null;
    }
}
