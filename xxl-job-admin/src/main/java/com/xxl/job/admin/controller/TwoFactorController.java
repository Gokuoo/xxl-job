package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.util.GoogleAuthenticatorUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/twoFactor")
public class TwoFactorController {

    @Resource
    private XxlJobUserDao xxlJobUserDao;

    @RequestMapping("/generate")
    @ResponseBody
    public ReturnT<Map<String, String>> generate(@RequestParam("userId") int userId) {
        XxlJobUser user = xxlJobUserDao.loadById(userId);
        if (user == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户不存在");
        }
        if (user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled() == 1) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "2FA已启用，请先禁用");
        }

        String secretKey = GoogleAuthenticatorUtil.generateSecretKey();
        String qrCodeUrl = GoogleAuthenticatorUtil.getQRCodeUrl(
                user.getUsername(), secretKey, "XXL-JOB");

        Map<String, String> result = new HashMap<>();
        result.put("secretKey", secretKey);
        result.put("qrCodeUrl", qrCodeUrl);
        return new ReturnT<>(result);
    }

    @RequestMapping("/enable")
    @ResponseBody
    public ReturnT<String> enable(@RequestParam("userId") int userId,
                                  @RequestParam("secretKey") String secretKey,
                                  @RequestParam("verifyCode") int verifyCode) {
        XxlJobUser user = xxlJobUserDao.loadById(userId);
        if (user == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户不存在");
        }

        if (!GoogleAuthenticatorUtil.verifyCode(secretKey, verifyCode)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "验证码错误");
        }

        user.setSecretKey(secretKey);
        user.setTwoFactorEnabled(1);
        xxlJobUserDao.update(user);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/disable")
    @ResponseBody
    public ReturnT<String> disable(@RequestParam("userId") int userId,
                                   @RequestParam("verifyCode") int verifyCode) {
        XxlJobUser user = xxlJobUserDao.loadById(userId);
        if (user == null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "用户不存在");
        }
        if (user.getTwoFactorEnabled() != 1) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "2FA未启用");
        }

        if (!GoogleAuthenticatorUtil.verifyCode(user.getSecretKey(), verifyCode)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "验证码错误");
        }

        user.setSecretKey(null);
        user.setTwoFactorEnabled(0);
        xxlJobUserDao.update(user);
        return ReturnT.SUCCESS;
    }
}