/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80016
 Source Host           : localhost:3306
 Source Schema         : chryl

 Target Server Type    : MySQL
 Target Server Version : 80016
 File Encoding         : 65001

 Date: 15/04/2021 09:14:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chr_company
-- ----------------------------
DROP TABLE IF EXISTS `chr_company`;
CREATE TABLE `chr_company` (
  `company_id` bigint(20) NOT NULL,
  `company_name` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `company_code` int(4) DEFAULT NULL,
  `company_description` varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL,
  `company_chinese` varchar(6) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of chr_company
-- ----------------------------
BEGIN;
INSERT INTO `chr_company` VALUES (200199100, 'Google', 2091, '国外科技公司', '谷歌');
INSERT INTO `chr_company` VALUES (200199200, 'Apple', 2094, '国外科技公司gw', '苹果');
INSERT INTO `chr_company` VALUES (200199300, 'Tencent', 2098, '国内科技公司gn', '腾讯');
INSERT INTO `chr_company` VALUES (200199400, 'Alibaba', 2095, '国内科技公司', '阿里巴巴');
INSERT INTO `chr_company` VALUES (200199500, 'Facebook', 2099, '国外科技公司', '脸书');
INSERT INTO `chr_company` VALUES (200199600, 'Baidu', 2096, '国内科技公司', '百度');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
