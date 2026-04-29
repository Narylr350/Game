/*
 Navicat Premium Data Transfer

 Source Server         : XM
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28)
 Source Host           : localhost:3306
 Source Schema         : doudizhu

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28)
 File Encoding         : 65001

 Date: 27/04/2026 10:41:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE doudizhu;
-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '用户状态',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间，用于7天内免密登录',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for game_session_log
-- ----------------------------
DROP TABLE IF EXISTS `game_session_log`;
CREATE TABLE `game_session_log`  (
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单局日志ID',
  `started_at` datetime NOT NULL COMMENT '本局开始时间',
  `ended_at` datetime NULL DEFAULT NULL COMMENT '本局结束时间',
  `player1_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '玩家1名称',
  `player2_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '玩家2名称',
  `player3_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '玩家3名称',
  `landlord_player_id` int NULL DEFAULT NULL COMMENT '地主玩家ID',
  `winner_player_id` int NULL DEFAULT NULL COMMENT '获胜玩家ID',
  `winner_side` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '获胜阵营，LANDLORD或FARMER',
  `end_reason` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结束原因',
  PRIMARY KEY (`session_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '单局对局日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for game_action_log
-- ----------------------------
DROP TABLE IF EXISTS `game_action_log`;
CREATE TABLE `game_action_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '动作日志自增ID',
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属单局日志ID',
  `step_no` int NOT NULL COMMENT '本局内的动作顺序',
  `phase` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作发生阶段',
  `player_id` int NOT NULL COMMENT '操作玩家ID',
  `player_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作玩家名称',
  `action_input` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '玩家原始输入',
  `action_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作结果说明',
  `remaining_cards_p1` int NOT NULL COMMENT '动作后玩家1剩余手牌数',
  `remaining_cards_p2` int NOT NULL COMMENT '动作后玩家2剩余手牌数',
  `remaining_cards_p3` int NOT NULL COMMENT '动作后玩家3剩余手牌数',
  `created_at` datetime NOT NULL COMMENT '动作记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_game_action_log_session_step`(`session_id` ASC, `step_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '单局动作日志表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
