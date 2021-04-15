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

 Date: 15/04/2021 09:15:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chr_emp
-- ----------------------------
DROP TABLE IF EXISTS `chr_emp`;
CREATE TABLE `chr_emp` (
  `emp_id` bigint(20) NOT NULL,
  `emp_name` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  `emp_sal` decimal(10,2) DEFAULT NULL,
  `emp_date` date DEFAULT NULL,
  `company_id` bigint(20) DEFAULT NULL,
  `emp_idcard` bigint(19) DEFAULT NULL,
  `emp_code` int(4) DEFAULT NULL,
  `emp_str_date` varchar(10) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `emp_real_name` varchar(15) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`emp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- ----------------------------
-- Records of chr_emp
-- ----------------------------
BEGIN;
INSERT INTO `chr_emp` VALUES (10089284111, 'aizkal', 100.11, '2021-04-09', 200199100, 30202939002101203, 2001, '20201011', '张三');
INSERT INTO `chr_emp` VALUES (10089284112, 'cmcc', 20.19, '2021-04-08', 200199100, 2948390022124, 3099, '20211010', '贷款方');
INSERT INTO `chr_emp` VALUES (10089284113, 'salo2i9e', 2993.10, '2021-04-11', 200199100, 3045349204392, 9901, '20200304', '袄袄');
INSERT INTO `chr_emp` VALUES (10089284114, 'nancy', 300.99, '2021-03-09', 200199100, 349392492423, 7730, '20210401', '斯柯达');
INSERT INTO `chr_emp` VALUES (10089394922, 'jerry', 29.10, '2021-04-06', 200199400, 29933921933231340, 3830, '20201211', '嗷嗷哦');
INSERT INTO `chr_emp` VALUES (100393949932, 'klls', 201.11, '2021-04-14', 200199300, 93939202029391, 1002, '20111010', '张建安路');
INSERT INTO `chr_emp` VALUES (100849291242, 'jeccy', 921.11, '2021-04-06', 200199500, 3132193439943110, 7739, '20201212', '棕色');
INSERT INTO `chr_emp` VALUES (100983878821, 'pink', 882.22, '2021-04-09', 200199200, 10034839492, 2831, '20210619', '登记卡');
INSERT INTO `chr_emp` VALUES (100998492412, 'jack', 9901.99, '2021-03-25', 200199300, 259953002, 2399, '20201930', '搜の');
INSERT INTO `chr_emp` VALUES (10029422323259, 'wook', 292.01, '2021-04-12', 200199400, 23933838389291221, 4001, '20200101', '安东尼');
INSERT INTO `chr_emp` VALUES (100933483929134, 'jdkd', 2002.20, '2021-04-14', 200199200, 31193302022131, 2002, '20121012', '经济师');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
