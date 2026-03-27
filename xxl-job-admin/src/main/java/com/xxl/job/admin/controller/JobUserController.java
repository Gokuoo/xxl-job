package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.util.GoogleAuthenticatorUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2019-05-04 16:39:50
 */
@Controller
@RequestMapping("/user")
public class JobUserController {

    @Resource
    private XxlJobUserDao xxlJobUserDao;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    @RequestMapping
    @PermissionLimit(adminuser = true)
    public String index(Model model) {

        // 执行器列表
        List<XxlJobGroup> groupList = xxlJobGroupDao.findAll();
        model.addAttribute("groupList", groupList);

        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String username, int role) {

        // page list
        List<XxlJobUser> list = xxlJobUserDao.pageList(start, length, username, role);
        int list_count = xxlJobUserDao.pageListCount(start, length, username, role);

        // filter
        if (list!=null && list.size()>0) {
            for (XxlJobUser item: list) {
                item.setPassword(null);
            }
        }

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @RequestMapping("/add")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> add(XxlJobUser xxlJobUser) {

        // valid username
        if (!StringUtils.hasText(xxlJobUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input")+I18nUtil.getString("user_username") );
        }
        xxlJobUser.setUsername(xxlJobUser.getUsername().trim());
        if (!(xxlJobUser.getUsername().length()>=4 && xxlJobUser.getUsername().length()<=20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        }
        // valid password
        if (!StringUtils.hasText(xxlJobUser.getPassword())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input")+I18nUtil.getString("user_password") );
        }
        xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
        if (!(xxlJobUser.getPassword().length()>=4 && xxlJobUser.getPassword().length()<=20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        }
        // md5 password
        xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));

        // check repeat
        XxlJobUser existUser = xxlJobUserDao.loadByUserName(xxlJobUser.getUsername());
        if (existUser != null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("user_username_repeat") );
        }

        // write
        xxlJobUserDao.save(xxlJobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/update")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> update(HttpServletRequest request, XxlJobUser xxlJobUser) {

        // avoid opt login seft
        XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
        if (loginUser.getUsername().equals(xxlJobUser.getUsername())) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
        }

        // valid password
        if (StringUtils.hasText(xxlJobUser.getPassword())) {
            xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
            if (!(xxlJobUser.getPassword().length()>=4 && xxlJobUser.getPassword().length()<=20)) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
            }
            // md5 password
            xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));
        } else {
            xxlJobUser.setPassword(null);
        }

        // write
        xxlJobUserDao.update(xxlJobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> remove(HttpServletRequest request, int id) {

        // avoid opt login seft
        XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
        if (loginUser.getId() == id) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
        }

        xxlJobUserDao.delete(id);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public ReturnT<String> updatePwd(HttpServletRequest request, String password, String oldPassword){

        // valid
        if (oldPassword==null || oldPassword.trim().length()==0){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
        }
        if (password==null || password.trim().length()==0){
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
        }
        password = password.trim();
        if (!(password.length()>=4 && password.length()<=20)) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        }

        // md5 password
        String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // valid old pwd
        XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
        XxlJobUser existUser = xxlJobUserDao.loadByUserName(loginUser.getUsername());
        if (!md5OldPassword.equals(existUser.getPassword())) {
            return new ReturnT<String>(ReturnT.FAIL.getCode(), I18nUtil.getString("change_pwd_field_oldpwd") + I18nUtil.getString("system_unvalid"));
        }

        // write new
        existUser.setPassword(md5Password);
        xxlJobUserDao.update(existUser);

        return ReturnT.SUCCESS;
    }

    /**
     * 生成2FA密钥和二维码
     */
    @RequestMapping("/twoFactor/generate")
    @ResponseBody
    public ReturnT<Map<String, String>> generateTwoFactorSecret(@RequestParam("userId") int userId) {
        XxlJobUser loginUser = xxlJobUserDao.loadById(userId);
        if (loginUser == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户不存在");
        }

        // 生成密钥
        String secretKey = GoogleAuthenticatorUtil.generateSecretKey();
        String qrCodeUrl = GoogleAuthenticatorUtil.getQRCodeUrl(
                loginUser.getUsername(),
                secretKey,
                "XXL-JOB"
        );

        Map<String, String> result = new HashMap<>();
        result.put("secretKey", secretKey);
        result.put("qrCodeUrl", qrCodeUrl);

        return new ReturnT<>(result);
    }

    /**
     * 验证并启用2FA
     */
    @RequestMapping("/twoFactor/enable")
    @ResponseBody
    public ReturnT<String> enableTwoFactor(
            @RequestParam("userId") int userId,
            @RequestParam("secretKey") String secretKey,
            @RequestParam("verifyCode") int verifyCode) {

        XxlJobUser user = xxlJobUserDao.loadById(userId);
        if (user == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户不存在");
        }

        // 验证验证码
        boolean isValid = GoogleAuthenticatorUtil.verifyCode(secretKey, verifyCode);
        if (!isValid) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "验证码错误");
        }

        // 保存密钥并启用2FA
        user.setSecretKey(secretKey);
        user.setTwoFactorEnabled(1);
        xxlJobUserDao.update(user);

        return ReturnT.SUCCESS;
    }

    /**
     * 禁用2FA
     */
    @RequestMapping("/twoFactor/disable")
    @ResponseBody
    public ReturnT<String> disableTwoFactor(
            @RequestParam("userId") int userId,
            @RequestParam("verifyCode") int verifyCode) {

        XxlJobUser user = xxlJobUserDao.loadById(userId);
        if (user == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户不存在");
        }

        if (user.getTwoFactorEnabled() != 1) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "2FA未启用");
        }

        // 验证验证码
        boolean isValid = GoogleAuthenticatorUtil.verifyCode(user.getSecretKey(), verifyCode);
        if (!isValid) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "验证码错误");
        }

        user.setSecretKey(null);
        user.setTwoFactorEnabled(0);
        xxlJobUserDao.update(user);

        return ReturnT.SUCCESS;
    }
}
