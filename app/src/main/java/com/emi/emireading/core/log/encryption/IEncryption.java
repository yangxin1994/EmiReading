package com.emi.emireading.core.log.encryption;


/**
 * @author :zhoujian
 * @description : 加密的接口
 * @company :翼迈科技
 * @date: 2017年7月06日下午 03:02
 * @Email: 971613168@qq.com
 */
public interface IEncryption {
    /**
     * 使用默认的密钥加密字符串
     *
     * @param content 需要加密的字符串
     * @return 返回已经加密完成的字符串
     * @throws Exception
     */
    String encrypt(String content) throws Exception;

    /**
     * 使用自定义密钥加密字符串
     *
     * @param key 加密的密钥
     * @param src 需要加密的字符串
     * @return 加密完成的字符串
     * @throws Exception
     */
    String encrypt(String key, String src) throws Exception;


    /**
     * 使用自定义密钥解密字符串
     *
     * @param key
     * @param content 需要加密的字符串
     * @return 解密后的文本
     * @throws Exception
     */
    String decrypt(String key, String content) throws Exception;

    /**
     * 使用默认的密钥解密字符串
     *
     * @param content 需要解密的字符串
     * @return 返回已经解密完成的字符串
     * @throws Exception
     */
    String decrypt(String content) throws Exception;


}
